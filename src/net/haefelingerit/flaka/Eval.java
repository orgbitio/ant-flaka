package net.haefelingerit.flaka;

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

public class Eval
{
  protected Hashtable keys;
  protected Project   P;

  public Eval(Hashtable keys, Project P) throws BuildException {
    this.keys = keys;
    this.P = P;

    /* assert keys not null and P not null */
    if (P == null)
      throw new BuildException("project shall not be null here.");

    if (keys == null)
      throw new BuildException("keys shall not be null here.");
  }

  protected void debug(String msg) {
    this.P.log(msg, Project.MSG_DEBUG);
  }

  protected void verbose(String msg) {
    // P.log(msg,Project.MSG_VERBOSE);
    this.P.log(msg, Project.MSG_DEBUG);
  }

  protected void log(String msg) {
    this.P.log(msg);
  }

  /*
   * evaluate expr ${..} returns fully evaluated string, no further evaluation
   * can be done regarding the set of properties given.
   */
  protected String eval0(String V, Hashtable seen) throws BuildException {
    char c;
    String s;
    int j, e;
    String v = V;

    /* make sure that v has the format `${.*}' */
    e = v.length();
    if (e <= 2) {
      return v;
    }
    c = v.charAt(e - 1);
    if (c != '}') {
      return v;
    }
    c = v.charAt(0);
    if (c != '$') {
      return v;
    }
    c = v.charAt(1);
    if (c != '{') {
      return v;
    }

    // debug("eval0? "+ v);

    /* we take again a substring .. :-( */
    v = v.substring(2, e - 1).trim();

    /* evaluate */
    if (needseval(v)) {
      s = eval(v, seen);
    } else
      s = v;

    j = s.indexOf(' ');
    if (j > 0) {
      int i;
      String func, argv;
      String[] args;

      func = s.substring(0, j);
      argv = s.substring(j + 1, s.length()).trim();
      debug("eval `" + func + " " + argv + "' ..");
      args = Static.split(argv, ",");
      e = args.length;
      for (i = 0; i < e; ++i)
        args[i] = args[i].trim();
      s = "";

      if (func.equals("get") && (e > 0)) {
        String k;
        k = args[0];
        if (this.keys.containsKey(k)) {
          s = lookup(k, seen);
        } else {
          if (e > 1) {
            s = args[1];
          }
        }
      }
      if (func.equals("getset") && (e > 0)) {
        /* split up argv .. */
        String k;
        k = args[0];
        if (this.keys.containsKey(k)) {
          s = lookup(k, seen);
        } else {
          if (e > 1) {
            s = eval(args[1], seen);
          }
        }
        /* as side effect we also create this key */
        verbose("creating key `" + k + "' with value `" + s + "'");
        this.keys.put(k, s);
      }
      if (func.equals("ifdef") && (e > 0)) {
        String k;
        k = args[0];
        if (this.keys.containsKey(k)) {
          if (e > 1) {
            s = args[1];
          }
        } else {
          if (e > 2) {
            s = args[2];
          }
        }
      }
      if (func.equals("ifndef") && (e > 0)) {
        String k;
        k = args[0];
        if (!this.keys.containsKey(k)) {
          if (e > 1) {
            s = args[1];
          }
        } else {
          if (e > 2) {
            s = args[2];
          }
        }
      }
      if (func.equals("uppercase")) {
        s = argv.toUpperCase();
      }
      if (func.equals("lowercase")) {
        s = argv.toLowerCase();
      }
      if (func.equals("length")) {
        s = Integer.toString(s.length());
      }
      if (func.equals("basename")) {
        File F = new File(argv);
        s = F.getName();
      }
      if (func.equals("dirname")) {
        File F = new File(argv);
        s = F.getParent();
      }
      if (func.equals("exists")) {
        File F = new File(argv);
        s = F.exists() ? "true" : "false";
      }
      if (func.equals("isfile")) {
        File F = new File(argv);
        s = (F.exists() && F.isFile()) ? "true" : "false";
      }
      if (func.equals("isdir")) {
        File F = new File(argv);
        s = (F.exists() && F.isDirectory()) ? "true" : "false";
      }
      if (func.equals("mtime")) {
        File F = new File(argv);
        s = F.exists() ? Long.toString(F.lastModified()) : "";
      }
      if (func.equals("ls")) {
        try {
          File F = new File(argv);
          if (F.exists() && F.isDirectory()) {
            String[] list = F.list();
            s = Static.sjoin(" ", list);
          }
        }
        catch (Exception ex) {
          s = "";
        }
      }
      if (func.equals("subst")) {
        if (e < 3) {
          debug("subst: e=`" + e + "' => evaluate to empty string ..");
          s = "";
        } else {
          String regex = args[0];
          String replm = args[1];
          try {
            s = Static.concat(args, 2, args.length, ",");
            s = s.replaceAll(regex, replm);
            debug("subst: => evaluates to `" + s + "'");
          }
          catch (Exception ex) {
            debug("error while executing `" + argv + "', got `" + ex + "'");
            s = "";
          }
        }
      }

      /* reevaluate again */
      if (needseval(s))
        s = eval(s, seen);
    } else {
      s = lookup(s, seen);
    }

    // debug("eval0! " + v + "=>`"+s+"'");
    return s;
  }

  protected String lookup(String S, Hashtable seen) {
    String y;
    String s = S;
    y = (String) this.keys.get(s);
    if (y == null) {
      debug("lookup: no such key defined: `" + s + "'");
      s = "${" + s + "}";
      return s;
    }
    /* have we seen this variable before ?? */
    if (seen.containsKey(s)) {
      throw new BuildException("circular variable: \"" + s + "\"");
    }
    /* now we have seen it .. */
    seen.put(s, "");

    if (needseval(y)) {
      String y1;
      y1 = eval(y, seen);
      if (!y1.equals(y))
        this.keys.put(s, y1);
      y = y1;
    }
    return y;
  }

  /* evaluate a string one time .. */
  protected int eval1(String v, Hashtable seen, StringBuffer buf)
      throws BuildException {
    int q, p, j, e, r;
    char c;

    r = 0;
    q = 0;
    e = v.length();

    // search for the next instance of $, start from `q`
    while ((p = v.indexOf("$", q)) >= 0) {
      if (p > 0) {
        buf.append(v.substring(q, p));
      }
      // if we are at the end of the string, we tack on a $
      // then move past it
      if (p == (e - 1)) {
        buf.append('$');
        q = p + 1;
        continue;
      }

      // peek ahead to see whether next char starts a property. If
      // next char isn't a '{' then this is not a property.
      c = v.charAt(p + 1);
      if (c != '{') {
        buf.append('$');
        buf.append(c);
        q = p + 2;
        continue;
      }

      // we are at the start of a property, where's the end? Find a
      // matching '}'.
      j = Static.endOf(v, p, e);
      if (j >= e)
        throw new BuildException("variable syntax error: " + v);

      /* grab ${.*} and eval */
      Hashtable localseen = new Hashtable(seen);
      buf.append(eval0(v.substring(p, j + 1), localseen));
      localseen = null;

      /* we have now evaluated up to 'q' chars */
      q = j + 1;
    }

    // no more $ signs found
    // if there is any tail to the file, append it
    if (q < e) {
      buf.append(v.substring(q));
    }

    return r;
  }

  public int eval(String V, Hashtable Seen, StringBuffer out)
      throws BuildException {
    int i;
    StringBuffer buf;
    Hashtable seen = Seen;
    String v = V;
    if (out == null)
      throw new BuildException("string buffer shall not be null here");

    if (v == null) {
      return 0;
    }

    buf = new StringBuffer();
    if (seen == null) {
      seen = new Hashtable();
    } else {
      seen = new Hashtable(seen);
    }
    i = 0;

    while (eval1(v, seen, buf) > 0) {
      debug("eval: something has changed, need to eval again");
      v = buf.toString();
      buf = new StringBuffer();
      i += 1;
    }
    v = buf.toString();
    out.append(v);
    return i;
  }

  public String eval(String v) throws BuildException {
    StringBuffer B = new StringBuffer();
    eval(v, null, B);
    return B.toString();
  }

  public String eval(String V, Hashtable seen) throws BuildException {
    StringBuffer B;
    String v = V;
    B = new StringBuffer();
    while (eval(v, seen, B) > 0) {
      v = B.toString();
      B = new StringBuffer();
    }

    return B.toString();
  }

  static private String _eval(String v, Hashtable keys, Project P) {
    /* all checks on parameters assumed to be done and ok */
    Eval E;
    StringBuffer B;

    E = new Eval(keys, P);
    B = new StringBuffer();

    E.eval(v, null, B);
    return B.toString();
  }

  static public void keval(String k, Hashtable keys, Project P)
      throws BuildException {
    String v;

    if (P == null) {
      throw new BuildException("keval called with `null' project.");
    }
    if (k == null) {
      P.log("keval: skipping k=null.", Project.MSG_DEBUG);
      return;
    }
    if (keys == null) {
      P.log("keval: can't do anything, no properties.", Project.MSG_DEBUG);
      return;
    }
    v = (String) keys.get(k);
    if (v == null) {
      P.log("keval: skipping v=(null).", Project.MSG_DEBUG);
      return;
    }
    if (v.indexOf('$') < 0) {
      /* we don't log this */
      return;
    }
    P.log("keval: " + k + "=`" + v + "'.", Project.MSG_DEBUG);
    v = _eval(v, keys, P);
    if (v == null) {
      P.log("keval: unexpected `null' value returned by veval.",
          Project.MSG_DEBUG);
    }
    keys.put(k, v);
    P.log("keval! " + k + "=`" + v + "'", Project.MSG_DEBUG);
  }

  static public String veval(String v, Hashtable keys, Project P) {
    String s;

    if (P == null) {
      throw new BuildException("veval called with `null' project.");
    }
    if (v == null) {
      P.log("veval: skipping null.", Project.MSG_DEBUG);
      return "";
    }
    if (v.indexOf('$') < 0) {
      /* we don't log this */
      return v;
    }
    // Static.debug(P,"veval: `"+v+"'");
    s = _eval(v, keys, P);
    // Static.verbose(P,"veval: `" + v + "' => '" + s + "'");
    return s;
  }

  static public void eval(Hashtable keys, Project P) {
    String name;
    Enumeration e;

    if (P == null) {
      throw new BuildException("eval called with `null' project.");
    }

    e = keys.keys();
    /* keys() is assumed to return always a non-null object */
    while (e.hasMoreElements()) {
      name = (String) e.nextElement();
      keval(name, keys, P);
    }
  }

  static public void eval(Project P) throws BuildException {
    if (P != null) {
      Hashtable H;
      Static.verbose(P, "evaluating project properties ..");
      H = P.getProperties();
      eval(H, P);
      Static.setProperty(P, H);
    }
  }

  static public boolean needseval(String v) {
    if (v == null)
      return false;
    return v.indexOf("${") >= 0;
  }

  static public boolean unresolved(String k, Project P) {
    /* unresolved properties if we can find something like ${ */
    if (P == null || k == null)
      return false;
    return needseval(P.getProperty(k));
  }

  static public void print(Hashtable keys, Project P) {
    String name;
    String value;
    Enumeration e;

    if (P == null) {
      throw new BuildException("print called with `null' project.");
    }

    e = keys.keys();
    /* keys() is assumed to return always a non-null object */
    while (e.hasMoreElements()) {
      name = (String) e.nextElement();
      value = (String) keys.get(name);
      P.log(name + "=`" + value + "'");
    }
  }

  static public void print(Project P) {
    if (P != null)
      print(P.getProperties(), P);
  }
}
