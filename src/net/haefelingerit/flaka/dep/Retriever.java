package net.haefelingerit.flaka.dep;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import net.haefelingerit.flaka.Printf;
import net.haefelingerit.flaka.Static;

import org.apache.tools.ant.Project;


public class Retriever
{
  // set of dependencies
  private File              _cachedir;
  private File              _localdir;
  // list of URLs
  private URL[]             _repositories;
  private Project           project;                       // Ant project

  // error codes
  public static final short NOT_RETRIEVED              = 0;
  public static final short RETRIEVED_REMOTE           = 1;
  // the file was found in the local cache
  public static final short RETRIEVED_LOCAL_CACHE      = 2;
  // the file exists locally and is up-to-date
  public static final short EXISTS_CURRENT             = 3;
  // the file could not be found either on the network or
  // in the local cache or local directory
  public static final short NOT_RETRIEVED_NOT_FOUND    = 4;
  // the file exists in the local directory, but cannot be
  // downloaded from the given URL
  public static final short NOT_RETRIEVED_EXISTS_STALE = 5;
  // some kind of local IO error
  public static final short NOT_RETRIEVED_LOCAL_IO_ERR = 6;
  // illegal state (added by wh).
  public static final short ILLEGAL_STATE              = 7;

  public Retriever(Project project) {
    this.project = project;
  }

  public int retrieve(Dependency[] D) {
    Dependency d;
    File f;
    String s, pfx;
    int found, digits;
    long kB;
    Printf pf;

    digits = ("" + D.length).length();
    pf = new Printf("     [%0" + digits + "d");

    log("     Locating " + D.length + " dependencies ..");
    found = 0;
    for (int i = 0; i < D.length; ++i) {
      pfx = pf.sprintf(i + 1);
      d = D[i];
      // retrieval takes place here, all the other junk around is for
      // pretty printing ..
      this.retrieve(d);
      f = d.getFile();
      s = d.basename() + ((f != null) ? " (" + f.getName() + ")" : "");
      kB = 0L;
      if (d.getLocalFile() != null) {
        kB = d.getLocalFile().length() / 1000L;
      }
      switch (d.getStatus()) {
        case Retriever.EXISTS_CURRENT:
          log(pfx + "|.] " + s + " (" + kB + "kB)");
          found++;
          break;
        case Retriever.RETRIEVED_REMOTE:
          log(pfx + "|>] " + s + " (" + kB + "kB)");
          found++;
          break;
        case Retriever.RETRIEVED_LOCAL_CACHE:
          log(pfx + "|c] " + s + " (" + kB + "kB)");
          found++;
          break;
        case Retriever.NOT_RETRIEVED:
          warn(pfx + "|?] " + s + " (retrieval error)");
          break;
        case Retriever.NOT_RETRIEVED_NOT_FOUND:
          warn(pfx + "|?] " + s + " (* not found *)");
          break;
        case Retriever.NOT_RETRIEVED_EXISTS_STALE:
          warn(pfx + "|*] " + s + " (stale)");
          found++;
          break;
        case Retriever.ILLEGAL_STATE:
          log(pfx + "|?] " + s + " (illegal)");
          break;
      }
    }
    return found;
  }

  /**
   * @param file
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
   * Sets the local cache, first checking that this is a valid directory
   * 
   * @param file
   *          a File object representing the Directory
   */
  public void setLocalCache(File file) {
    this._cachedir = file;
  }

  /**
   * Sets whether to use a progress bar or not
   * 
   * @param b
   */
  public void setProgressBar(boolean b) {
    /* ignored */
  }

  private void log(String msg) {
    Static.log(this.project, msg);
  }

  private void debug(String msg) {
    Static.debug(this.project, msg);
  }

  private void warn(String msg) {
    Static.warning(this.project, msg);
  }

  private File cache(String name) {
    File r = this._cachedir;
    if (name != null && r != null)
      r = new File(r, name);
    return r;
  }

  private File local(String name) {
    File r = this._localdir;
    if (name != null && r != null)
      r = new File(r, name);
    return r;
  }

  private boolean exists(File file) {
    return (file == null) ? false : file.exists();
  }

  final private boolean caching() {
    return this._cachedir != null;
  }

  final private File localfile(Dependency dep) {
    File r;
    String b;
    b = dep.basename();
    r = caching() ? cache(b) : local(b);
    return r;
  }

  /**
   * Retrieve a single dependency
   * 
   * @param dep
   * @return a code indicating if (and how) the dependency was retrieved
   */
  protected void retrieve(Dependency dep) {
    debug("retrieving dependency `" + dep.basename() + "'..");

    /* set default status */
    dep.setStatus(NOT_RETRIEVED_NOT_FOUND);

    /* try dest dir and cache */
    retrieve_local(dep);
    switch (dep.getStatus()) {
      case RETRIEVED_LOCAL_CACHE:
      case EXISTS_CURRENT: {
        // was found locally
        return;
      }
    }

    /* try remote */
    retrieve_remote(dep);

    /*
     * when chaching make sure that dependency gets retrieved into final
     * destination
     */
    if (dep.getStatus() == RETRIEVED_REMOTE && caching())
      retrieve_local(dep);

    /* That's it */
    return;
  }

  static public boolean download(URL url, File localFile) {
    boolean r = false;
    URLConnection socket = null;
    InputStream stream = null;
    OutputStream out = null;

    Static.debug("trying `" + url + "'");

    try {
      socket = url.openConnection();
    }
    catch (Exception e) {
      Static.debug("open('" + url + "') => " + e.getMessage());
      return false;
    }

    try {
      socket.connect();
    }
    catch (Exception e) {
      Static.debug("connect('" + url + "') => " + e.getMessage());
      return false;
    }

    try {
      stream = socket.getInputStream();
    }
    catch (IOException e) {
      Static.debug("reading('" + url + "') => " + e.getMessage());
      return false;
    }

    try {
      out = new FileOutputStream(localFile);
      Static.verbose("saving `" + url + "' as `" + localFile.getAbsolutePath()
          + "'");
      Static.copy(stream, out);
      /* Ladies and Gentlemen - we got'em */
      r = true;
    }
    catch (Exception e) {
      Static.debug("unable to download `" + url
          + "', got exception -  will skip.");
    }
    finally {
      if (out != null) {
        try {
          out.flush();
          out.close();
        }
        catch (Exception nex) {
          /* do nothing */
        }
      }
      if (stream != null) {
        try {
          stream.close();
        }
        catch (Exception nex) {
          /* do nothing */
        }
      }
    }
    return r;
  }

  public URL getURL(int i, String path) {
    URL r = null;
    try {
      r = new URL(this._repositories[i].toString() + path);
    }
    catch (Exception e) {
      /* ignored */
    }
    return r;
  }

  protected void retrieve_remote(Dependency dep) {
    URL url = null;
    File localFile = null;
    String depotpath = null;
    String depotpath2dotnull = null;

    if (this._repositories == null) {
      debug("no remote repositories declared");
      dep.setStatus(NOT_RETRIEVED);
      return;
    }

    if (dep.depotpath() == null || dep.depotpath2dotnull() == null) {
      dep.setStatus(ILLEGAL_STATE);
      return;
    }

    localFile = localfile(dep);
    depotpath = dep.depotpath();
    depotpath2dotnull = dep.depotpath2dotnull();

    for (int i = 0; i < this._repositories.length; ++i) {
      url = getURL(i, depotpath);
      if (url == null) {
        debug("malformed url - skipped.");
        continue;
      }

      if (download(url, localFile)) {
        /* successful download, can exit the loop */
        dep.setStatus(RETRIEVED_REMOTE);
        dep.setDownloadSource(url);
        dep.setLocalFile(localFile);
        break;
      }

      /* try Maven 2.0 style */
      url = getURL(i, depotpath2dotnull);
      if (url == null) {
        debug("malformed url skipped, path='" + depotpath2dotnull + "'.");
        continue;
      }
      if (download(url, localFile)) {
        /* successful download, can exit the loop */
        dep.setStatus(RETRIEVED_REMOTE);
        dep.setDownloadSource(url);
        dep.setLocalFile(localFile);
        break;
      }
    }
  }

  /**
   * Tries to retrieve the file locally, using the following logic if (file
   * exists in cache) if (the file exists locally and is as current or more
   * current than the cached copy) use local copy else copy from the cache else
   * if the file exists locally, use it
   * 
   * @param localFile
   */

  private void retrieve_local(Dependency dep) {
    String name;
    File localfile;

    name = dep.basename();

    localfile = local(name);

    if (exists(localfile)) {
      debug("dependency `" + name + "' is up-to-date.");
      dep.setLocalFile(localfile);
      dep.setStatus(EXISTS_CURRENT);
      return;
    }

    debug("dependency `" + name + "' not found and not in cache.");
    dep.setStatus(NOT_RETRIEVED_NOT_FOUND);
  }

}
