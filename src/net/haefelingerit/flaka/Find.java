package net.haefelingerit.flaka;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.taskdefs.MatchingTask;

/**
 * A task echoing files in a fileset similar as the Unix command
 * <code>find</code> would do.
 */

public class Find extends MatchingTask
{
  protected String srcdir = ".";
  protected String type   = null;
  protected String var    = null;

  public void setDir(String dir) {
    this.srcdir = Static.trim2(dir, this.srcdir);
  }

  public void setType(String type) {
    this.type = Static.trim2(type, this.type);
  }

  public void setVar(String var) {
    this.var = Static.trim2(var, this.var);
  }

  protected DirectoryScanner getds(File dir) {
    DirectoryScanner ds = null;
    if (dir != null)
      ds = super.getDirectoryScanner(dir);
    return ds;
  }

  protected void scan(DirectoryScanner ds, List C) {
    if (ds != null && C != null) {
      String[] buf = null;
      if (this.type == null || this.type.equals("f")) {
        buf = ds.getIncludedFiles();
        for (int j = 0; j < buf.length; ++j)
          C.add(buf[j]);
      }
      if (this.type == null || this.type.equals("d")) {
        buf = ds.getIncludedDirectories();
        for (int j = 0; j < buf.length; ++j)
          C.add(buf[j]);
      }
    }
  }

  protected void print(String dir, String fname) {
    if (dir != null) {
      System.out.print(dir);
    }
    if (fname != null && dir != null)
      System.out.print('/');
    if (fname != null)
      System.out.print(fname);
    System.out.println("");
  }

  protected void set(String name, String value) {
    String p = getProject().getProperty(name);
    p = (p == null) ? value : (p + " " + value);
    getProject().setProperty(name, p);
  }

  /**
   * perfoms the <code>find</code> operation.
   * 
   * @exception BuildException
   *              if an error occurs
   */

  public void execute() throws BuildException {
    File dir;
    Iterator I;
    ArrayList L;
    DirectoryScanner D;
    String[] dirs;

    I = null;
    L = new ArrayList();

    /* split argument */
    // BUG: does not work with DOS paths - every "\\" gets swallowed.
    dirs = Static.lex(this.srcdir);

    for (int i = 0; i < dirs.length; ++i) {
      dir = new File(dirs[i]);
      if (!dir.exists()) {
        continue;
      }
      if (!dir.isDirectory()) {
        L.add(dirs[i]);
        continue;
      }
      D = getds(dir);
      if (D != null) {
        scan(D, L);
      }

      /* print */
      Collections.sort(L);
      I = L.iterator();

      while (I.hasNext()) {
        String fname = (String) I.next();
        if (this.var == null)
          print(dirs[i], fname);
        else {
          String v = "";
          if (dirs[i] != null)
            v += dirs[i];
          if (fname != null)
            v += "/" + fname;
          set(this.var, v);
        }
      }

      L.clear();
      I = null;
    }
  }
}
