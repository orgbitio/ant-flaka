package net.haefelingerit.flaka;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.MatchingTask;

/**
 * Implements well known <code>wc</code> - or at least part of * it's
 * functionality. The basic idea is to make it easy to * count number of
 * directories, files et cetera in a fileset. * Those numbers are saved as
 * properties, the name of the * property needs to be given as task attribute.
 * Note that * given properties are overridden. * * Example: * * <wc dir="."
 * total="total.int" dirs="dir.int" files="file.int" /> * * <echo> *
 * ${total.int} = ${file.int} + ${dir.int} * </echo> * * This task is an
 * implicit <code>fileset</code> and understands * therefore the usual nested
 * elements like selectors, mappers * et cetera.
 */

public class WC extends MatchingTask
{
  protected String srcdir  = null;
  protected String type    = null;
  protected String var     = null;

  int              counter = 0;

  public void setDir(String dir) {
    this.srcdir = Static.trim(dir, this.srcdir);
  }

  public void setType(String type) {
    this.type = Static.trim(type, this.type);
  }

  public void setVar(String s) {
    this.var = Static.trim(s, this.var);
  }

  protected DirectoryScanner getds(File dir) {
    DirectoryScanner ds = null;
    if (dir != null)
      ds = super.getDirectoryScanner(dir);
    return ds;
  }

  protected boolean contains(char c) {
    return this.type == null || this.type.indexOf(c) >= 0;
  }

  protected void scan(DirectoryScanner ds) {
    if (ds != null) {
      if (contains('f')) {
        this.counter += ds.getIncludedFilesCount();
      }
      if (contains('d')) {
        this.counter += ds.getIncludedDirsCount();
      }
    }
  }

  protected void set(int Value) {
    /*
     * add value to property `name'. If not possible * then append value to
     * name's value.
     */
    int value = Value;
    if (this.var != null) {
      Project P = getProject();
      String v = P.getProperty(this.var);
      if (v == null)
        P.setProperty(this.var, Integer.toString(value));
      else {
        try {
          value += Integer.parseInt(v);
          v = Integer.toString(value);
        }
        catch (Exception e) {
          v += " " + Integer.toString(value);
        }
        P.setProperty(this.var, v.trim());
      }
    }
  }

  /**
   * perfoms the <code>find</code> operation.
   * 
   * @exception BuildException
   *              if an error occurs
   */

  public void execute() throws BuildException {
    String[] argv;

    /* no need to count */
    if (Static.empty(this.var))
      this.var = "_";

    /* scan current directory if no argument is given */
    if (Static.empty(this.srcdir))
      this.srcdir = ".";

    /* split argument */
    argv = Static.lex(this.srcdir);

    for (int i = 0; i < argv.length; ++i) {
      File dir;
      dir = new File(argv[i]);
      if (!dir.exists()) {
        continue;
      }
      if (!dir.isDirectory()) {
        this.counter += 1;
        continue;
      }
      scan(getds(dir));
    }

    /* set property */
    set(this.counter);
  }
}
