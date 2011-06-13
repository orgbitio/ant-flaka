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

package it.haefelinger.flaka;

import it.haefelinger.flaka.util.Static;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.MatchingTask;

/**
 * A task echoing files in a fileset similar as the Unix command
 * <code>find</code> would do.
 * 
 * 
 * @author merzedes
 * @since 1.0
 */

public class Find extends MatchingTask {
  protected String srcdir = "''.tofile";
  protected String type = null;
  protected String var = null;

  public void setVar(String var) {
    this.var = Static.trim3(getProject(), var, this.var);
  }

  public void setDir(String dir) {
    this.srcdir = Static.trim3(getProject(), dir, this.srcdir);
  }

  public void setType(String type) {
    this.type = Static.trim3(getProject(), type, this.type);
  }

  protected DirectoryScanner getds(File dir) {
    DirectoryScanner ds = null;
    ds = super.getDirectoryScanner(dir);
    return ds;
  }

  protected void scan(File dir, List C) {
    Project project;
    DirectoryScanner ds;

    project = getProject();
    if (dir == null)
      return;

    if (!dir.exists()) {
      Static.debug(project, "ignoring non existent folder:" + dir);
      return;
    }
    if (!dir.isDirectory()) {
      Static.debug(project, "ignoring non folder:" + dir);
      return;
    }
    try {
      ds = getds(dir);
      String[] buf = null;
      if (this.type == null || this.type.equals("f"))
        buf = ds.getIncludedFiles();
      else if (this.type.equals("d"))
        buf = ds.getIncludedDirectories();
      if (buf != null)
        for (int j = 0; j < buf.length; ++j) {
          File file = new File(dir, buf[j]);
          C.add(file);
        }
    } catch (Exception e) {
      Static.debug(project, "error scanning " + dir, e);
    }
  }

  protected void set(String name, String value) {
    String p = getProject().getProperty(name);
    p = (p == null) ? value : (p + " " + value);
    getProject().setProperty(name, p);
  }

  static List makelist(Object... argv) {
    List L = new ArrayList();
    for (int i = 0, n = argv.length; i < n; ++i)
      if (argv[i] != null)
        L.add(argv[i]);
    return L;
  }

  static Iterator iteratorof(Object obj) {
    Iterator iter = null;
    if (obj instanceof Iterable) {
      iter = ((Iterable) obj).iterator();
    } else {
      iter = makelist(obj).iterator();
    }
    return iter;
  }

  /**
   * perfoms the <code>find</code> operation.
   * 
   * @exception BuildException
   *              if an error occurs
   */

  public void execute() throws BuildException {
    Project project;
    Object obj;
    File dir;
    Iterator di;
    ArrayList L;

    project = getProject();

    // eval dir attribute
    obj = Static.el2obj(project, this.srcdir);
    di = iteratorof(obj);

    L = new ArrayList();

    while (di.hasNext()) {
      obj = di.next();
      dir = null;
      if (obj instanceof File)
        dir = (File) obj;
      else {
        dir = Static.toFile(project, obj.toString());
      }
      scan(dir, L);
    }

    Static.assign(project, this.var, L, Static.VARREF);
  }
}
