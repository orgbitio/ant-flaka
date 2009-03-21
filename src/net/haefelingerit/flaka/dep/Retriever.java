/*
 * Copyright (c) 2009 Haefelinger IT 
 *
 * Licensed  under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required  by  applicable  law  or  agreed  to in writing, 
 * software distributed under the License is distributed on an "AS 
 * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied.
 
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */

package net.haefelingerit.flaka.dep;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import net.haefelingerit.flaka.Task;
import net.haefelingerit.flaka.util.Static;


public class Retriever
{
  // set of dependencies
  //private File              _cachedir;
  private File              _localdir;
  // list of URLs
  private URL[]             _repositories;
  /* The task using this retriever */
  private Task           task; 

 
  
  public Retriever(Task task) {
    this.task = task;
  }

 
  /**
   * @param loc
   */
  public void setLocalDir(File file) {
    this._localdir = file;
  }

  /**
   * @param repositories
   *          The repositories to set.
   */
  public void setRepositories(URL[] repositories) {
    this._repositories = repositories;
  }

  public URL[] getRepositories() {
    return this._repositories;
  }

  /**
   * @param b
   */
  public void setVerbose(boolean b) {
    /* ignored */
  }

  

  /**
   * Sets whether to use a progress bar or not
   * 
   * @param b
   */
  public void setProgressBar(boolean b) {
    /* ignored */
  }

  public void log(String msg) {
    this.task.log(msg);
  }

  public void debug(String msg) {
    this.task.debug(msg);
  }

  public void warn(String msg) {
    this.task.warn(msg);
  }

 

  protected File localfile(String name) {
    File r = this._localdir;
    if (name != null && r != null)
      r = new File(r, name);
    return r;
  }

  
 
  protected File localfile(Dependency d) {
    return localfile(d.basename());
  }

  /**
   * Retrieve a single dependency
   * 
   * @param d
   * @return a code indicating if (and how) the dependency was retrieved
   */
  public boolean retrieve(Dependency d) {
    
    debug("retrieving dependency `" + d.basename() + "'..");

    /* Can't retrieve unresolved dependency */
    if (d.getStatus() == Dependency.UNRESOLVED) {
      debug("not retrieving unresolved dependency:" +d.getLocation());
      return false;
    }
    
    /* set default status */
    d.setStatus(Dependency.UNKOWN);
    d.setFile(localfile(d));
    d.setURL(null);
   
    /* try dest dir and cache */
    if (retrieve_local(d)) {
      return true;
    }
 
    /* try remote */
    if (retrieve_remote(d)) {
      return true;
    }

    d.setStatus(Dependency.NOTFOUND);
    /* That's it */
    return false;
  }

  protected boolean download(Dependency d) {
    boolean r = false;
    URLConnection socket = null;
    InputStream stream = null;
    OutputStream out = null;
    URL url = d.getURL();
    
    debug("trying: " + url);

    try {
      socket = url.openConnection();
    }
    catch (Exception e) {
      debug("open('" + url + "') => " + e.getMessage());
      d.setStatus(Dependency.IOERROR_REMOTE);
      return false;
    }

    try {
      socket.connect();
    }
    catch (Exception e) {
      debug("connect('" + url + "') => " + e.getMessage());
      d.setStatus(Dependency.IOERROR_REMOTE);
      return false;
    }

    try {
      stream = socket.getInputStream();
    }
    catch (IOException e) {
      debug("reading('" + url + "') => " + e.getMessage());
      d.setStatus(Dependency.IOERROR_REMOTE);
      return false;
    }

    try {
      File file = d.getFile();
      out = new FileOutputStream(file);
      debug("saving `" + url + "' as `" + file.getAbsolutePath()
          + "'");
      Static.copy(stream, out);
      /* Ladies and Gentlemen - we got'em */
      r = true;
      d.setStatus(Dependency.RETRIEVED_REMOTE);
    }
    catch (Exception e) {
      d.setStatus(Dependency.IOERROR_REMOTE);
      debug("unable to download `" + url
          + "', got exception -  will skip.");
    }
    finally {
      Static.close(out);
      Static.close(stream);
    }
    return r;
  }

  public URL makeurl(int i, String path) {
    URL r = null;
    try {
      r = new URL(this._repositories[i].toString() + path);
    }
    catch (Exception e) {
      /* ignored */
    }
    return r;
  }

  protected boolean retrieve_remote(Dependency d) {
    URL url = null;
    String m1path = null;
    String m2path = null;

    if (this._repositories == null) {
      debug("no remote repositories declared");
      return false;
    }

    if (d.m1path() == null || d.m2path() == null) {
      d.setStatus(Dependency.ILLEGAL_STATE);
      return false;
    }

    m1path = d.m1path();
    m2path = d.m2path();

    for (int i = 0; i < this._repositories.length; ++i) 
    {
      url = makeurl(i, m1path);
      if (url == null) {
        debug("malformed url skipped: m1path="+m1path);
        continue;
      }
      d.setURL(url);
      if (download(d)) {
        return true;
      }

      /* try Maven 2.0 style */
      url = makeurl(i, m2path);
      if (url == null) {
        debug("malformed url skipped: m2path=" + m2path);
        continue;
      }
      d.setURL(url);
      if (download(d)) {
        return true;
      }
    }
    return false;
  }



  protected boolean retrieve_local(Dependency d) 
  {
    File file;
    
    /* check if we have already a file associated */
    file = d.getFile();
    if (file != null && file.isFile()) {
      d.setStatus(Dependency.ISCURRENT);
      return true;
    }
 
    file = localfile(d);
    if (file !=null && file.isFile()) {
      d.setFile(file);
      d.setStatus(Dependency.ISCURRENT);
      return true;
    }
    return false;
  }

}
