package net.haefelingerit.flaka;

import net.haefelingerit.flaka.dep.Dependency;

import org.apache.tools.ant.BuildException;


/**
 * A task which outputs a CLASSPATH ..
 * 
 * @author <a href="mailto:flaka (at) haefelingerit (dot) net">Wolfgang H&auml;felinger</a>
 */

public class WriteDeps extends Task
{
  /* shall point to a filelist set .. */
  protected String refid = "deps.object";
  protected String out   = "-";
  protected String fmt   = "maven";

  /**
   * Change the resource variable to be used when reporting dependencies.
   * 
   * The default variable is <b>deps.object</b>. This variable comes usually
   * into existence when using task {@link net.haefelingerit.flaka.GetDeps} to retrieve
   * dependencies from a depot.
   * 
   * @param refid
   */
  public void setRefid(String refid) {
    this.refid = Static.trim2(refid, this.refid);
  }

  /**
   * Write dependencies to file <b>s</b>.
   * 
   * By default dependencies are writen to stdout (denoted by string "-").
   * 
   * @param s
   */
  public void setOut(String s) {
    this.out = Static.trim2(s, this.out);
  }

  /**
   * Change the format in which dependencies are written.
   * 
   * The default format is "maven". Alternative formats are:
   * <ul>
   * <li>flat, depotpath</li>
   * <li>var, alias</li>
   * </ul>
   * 
   * @param s
   */
  public void setFmt(String s) {
    this.fmt = Static.trim2(s, this.fmt);
  }

  /**
   * Alias for method setFmt(String)
   * 
   * @param s
   */
  public void setFormat(String s) {
    setFmt(s);
  }

  /**
   * Reference variable id supposed to point to an array of dependencies.
   * 
   * @param id
   *          can be null
   * @return null if var does not exist or can't be converted to an array of
   *         dependencies either. Otherwise an array of dependencies is
   *         returned.
   */
  protected Dependency[] depsbyid(String id) {
    Dependency[] L = null;

    try {
      Object obj;
      obj = this.getref(id);

      L = (Dependency[]) obj;
      if (this.debug) {
        for (int i = 0; L != null && i < L.length; ++i) {
          if (L[i] == null) {
            String m;
            m = "** internal error - null dependency located in reference '";
            m = m + id + "' at position #" + i;
            System.err.println(m);
          }
        }
      }
    }
    catch (ClassCastException ce) {
      if (this.debug)
        System.err.println("reference " + id
            + " not convertable to Dependency[].");
      L = null;
    }
    catch (Exception e) {
      L = null;
    }
    return L;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.tools.ant.Task#execute()
   */
  public void execute() throws BuildException {
    int i;
    String s;
    StringBuffer B;
    Dependency d;
    Dependency[] D;

    B = null;
    D = depsbyid(this.refid);

    /* warning message if no dependency referenced */
    if (D == null && this.debug)
      System.err.println(this.refid + " does not hold dependency references.");

    // Report dependencies in Maven style
    if (this.fmt.matches("(?i:maven)")) {
      B = new StringBuffer();
      if (D == null || D.length <= 0)
        B.append("<dependencies />");
      else {
        B.append("<dependencies>\n");
        for (i = 0; i < D.length; ++i) {
          d = D[i];
          B.append(d.toAliased());
          B.append('\n');
        }
        B.append("</dependencies>\n");
      }
    }

    // This format shall report dependencies as "files". Together with
    // the depot's base URL it allows the download of a dependency
    // using a tool like wget.
    // Example:
    // <dependency name="log4j">
    // => external/log4j/jars/log4j-1.2.8.jar

    if (this.fmt.matches("(?i:flat|depotpath)")) {
      B = new StringBuffer();
      for (i = 0; D != null && i < D.length; ++i) {
        d = D[i];
        if (d != null) {
          s = d.depotpath();
          if (s != null) {
            B.append(d.depotpath());
            B.append('\n');
          }
        }
      }
      B.append('\n');
    }

    // An "Ant-Epoline" dependency has an "alias" name. This is the name
    // used to lookup the dependency in the Baseline. When updating the
    // Baseline, a project will get asked to report dependencies which
    // are part of the Baseline.
    //
    // Example:
    // <dependency name="log4j" />
    // => LOG4J
    if (this.fmt.matches("(?i:var|alias)")) {
      String v;
      B = new StringBuffer();
      for (i = 0; D != null && i < D.length; ++i) {
        d = D[i];
        if (d != null) {
          v = d.getAlias();
          if (v != null) {
            B.append(v);
            B.append('\n');
          }
        }
      }
      B.append('\n');
    }

    if (B == null) {
      throwbx("format `" + this.fmt
          + "' not supported for listing dependencies.");
      return;
    }

    write(B);
  }

  static public String[] splitbyws(String s) {
    String[] argv = null;
    try {
      argv = s != null ? s.split("\\s+") : null;
    }
    catch (Exception e) {
      argv = null;
    }
    return argv;
  }

  protected void write(StringBuffer b) throws BuildException {
    String[] argv = null;
    boolean seen = false;
    String s;

    argv = splitbyws(this.out);

    for (int j = 0; argv != null && j < argv.length; ++j) {
      s = argv[j].trim();
      if (seen == false && (s == null || s.equals("") || s.equals("-"))) {
        log(b.toString());
        seen = true;
      } else {
        try {
          Static.writex(s, b.toString(), false);
          if (this.debug)
            log("wrote dependencies into file `" + s + "' using format `"
              + this.fmt + "'.");
        }
        catch (Exception e) {
          throwbx("failed to write dependencies to file `" + this.out + "'", e);
        }
      }
    }
  }
}
