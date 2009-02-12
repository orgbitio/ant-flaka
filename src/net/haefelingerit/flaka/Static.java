package net.haefelingerit.flaka;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.haefelingerit.flaka.tel.TestEL;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.ComponentHelper;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * From the basis of an XML source, creates a list of dependencies.
 * 
 * @author Wolfgang Haefelinger (flaka (at) haefelingerit (dot) net)
 * @version
 * 
 */

//class MySaxErrHndlr implements ErrorHandler
//{
//  public void error(SAXParseException e) {
//    Static.debug("XML parsing error", e);
//  }
//
//  public void fatalError(SAXParseException e) {
//    Static.debug("fatal XML parsing error at line " + e.getLineNumber() + " ",
//        e);
//  }
//
//  public void warning(SAXParseException e) {
//    Static.debug("XML parsing warning", e);
//  }
//}

final public class Static
{
  /*
   * we keep a pointer to a project for the purpose of logging * and for no
   * other reasons.
   */
  static private Project __logproject__;

  static public void setProject(Project p) {
    __logproject__ = p;
  }

  public static void _log_(Project P, String msg, int type) {
    Project p = P;
    if (msg == null)
      return;

    /* If there's no project then get the configured one */
    if (p == null)
      p = __logproject__;

    /***************************************************************************
     * If we don't have a project use stderr - this case * should not happen but
     * is used for the 'just-in-case' case. *
     **************************************************************************/

    if (p == null) {
      System.err.println(">> " + msg);
      return;
    }

    /* use the project's logger */
    p.log(msg, type);
  }

  public static void log(Project P, String msg, Exception e) {
    String m = msg;
    if (e != null)
      m += ": " + e.getMessage();
    _log_(P, m, Project.MSG_INFO);
  }

  public static void log(Project P, String msg) {
    log(P, msg, null);
  }

  public static void log(String msg) {
    log(__logproject__, msg);
  }

  public static void log(String msg, Exception e) {
    log(__logproject__, msg, e);
  }

  public static void info(Project P, String msg, Exception e) {
    String m = msg;
    if (e != null)
      m += ": " + e.getMessage();
    _log_(P, m, Project.MSG_INFO);
  }

  public static void info(Project P, String msg) {
    log(P, msg, null);
  }

  public static void info(String msg) {
    log(__logproject__, msg);
  }

  public static void info(String msg, Exception e) {
    log(__logproject__, msg, e);
  }

  public static void verbose(Project P, String msg, Exception e) {
    String m = msg;
    if (e != null)
      m += ": " + e.getMessage();
    _log_(P, m, Project.MSG_VERBOSE);
  }

  public static void verbose(Project P, String msg) {
    verbose(P, msg, null);
  }

  public static void verbose(String msg) {
    verbose(__logproject__, msg);
  }

  public static void verbose(String msg, Exception e) {
    verbose(__logproject__, msg, e);
  }

  public static void debug(Project P, String msg, Exception e) {
    String m = msg;
    if (e != null)
      m += ": " + e.getMessage();
    _log_(P, m, Project.MSG_DEBUG);
  }

  public static void debug(Project P, String msg) {
    debug(P, msg, null);
  }

  public static void debug(String msg) {
    debug(__logproject__, msg);
  }

  public static void debug(String msg, Exception e) {
    debug(__logproject__, msg, e);
  }

  public static void error(Project P, String msg, Exception e) {
    String m = msg;
    if (e != null)
      m += ": " + e.getMessage();
    _log_(P, m, Project.MSG_ERR);
  }

  public static void error(Project P, String msg) {
    error(P, msg, null);
  }

  public static void error(String msg) {
    error(__logproject__, msg);
  }

  public static void error(String msg, Exception e) {
    error(__logproject__, msg, e);
  }

  public static void warning(Project P, String msg, Exception e) {
    String m = msg;
    if (e != null)
      m += ": " + e.getMessage();
    _log_(P, m, Project.MSG_WARN);
  }

  public static void warning(Project P, String msg) {
    warning(P, msg, null);
  }

  public static void warning(String msg) {
    warning(__logproject__, msg);
  }

  public static void warning(String msg, Exception e) {
    warning(__logproject__, msg, e);
  }

  public static String toDOS(Project P, FileList FL) {
    String r = "";
    String[] buf = FL.getFiles(P);
    for (int i = 0; i < buf.length; ++i) {
      r += "SET CLASSPATH=%CLASSPATH%;" + buf[i] + "\n";
    }
    return r;
  }

  public static void writex(File f, String buf, boolean append)
      throws IOException {
    FileWriter out = null;
    try {
      out = new FileWriter(f, append);
      out.write(buf, 0, buf.length());
      out.flush();
    }
    catch (IOException ioe) {
      String path = f.getAbsolutePath();
      debug(path + ": " + ioe.getMessage());
      throw ioe;
    }
    finally {
      close(out);
    }
  }

  public static void close(Writer w) {
    /* silently close a writer */
    if (w != null) {
      try {
        w.close();
      }
      catch (IOException ioex) {
        // ignore
      }
    }
  }

  public static void close(OutputStream os) {
    if (os != null) 
      try {
        os.close();
      }
      catch(IOException e) {
        /* ignored */
      }
  }
  
  public static void close(InputStream is) {
    if (is != null) 
      try {
        is.close();
      }
      catch(IOException e) {
        /* ignored */
      }
  }
  
  public static void close(Reader r) {
    if (r!= null) 
      try {
        r.close();
      }
      catch(IOException e) {
        /* ignored */
      }
  }
  
  public static void writex(String name, String s, boolean mode)
      throws IOException {
    writex(new File(name), s, mode);
  }

  public static int write(String name, String s, boolean mode) {
    int r = 1;
    try {
      writex(name, s, mode);
      r = 0;
    }
    catch (Exception e) { /* not used */
    }
    return r;
  }

  public static void writex(InputStream cin, String fname, boolean mode)
      throws IOException {
    FileOutputStream out = null;
    byte[] buf = new byte[4098];
    int sz;
    try {
      File f = new File(fname);
      out = new FileOutputStream(f.getAbsolutePath(), mode);
      sz = cin.read(buf);
      while (sz >= 0) {
        out.write(buf, 0, sz);
        sz = cin.read(buf);
      }
    }
    catch (IOException ioe) {
      throw ioe;
    }
    finally {
      if (out != null) {
        try {
          out.close();
        }
        catch (IOException ioex) {
          // ignore
        }
      }
    }
  }

  public static String readlines(Reader reader) {
    String r;
    StringBuffer b;

    try {
      BufferedReader R;
      String L;

      b = new StringBuffer();
      R = new BufferedReader(reader);
      L = R.readLine();

      while (L != null) {
        b.append(L).append("\n");
        L = R.readLine();
      }

      R.close();
      r = b.toString();
    }
    catch (IOException e) {
      r = null;
    }
    return r;
  }

  public static String readlines(InputStream src) {
    String r = null;
    if (src != null)
      r = readlines(new InputStreamReader(src));
    return r;
  }

  public static String readlines(String src) {
    String r = null;
    if (src != null) {
      try {
        r = readlines(new FileReader(src));
      }
      catch (Exception e) { /* not used */
      }
    }
    return r;
  }

  public static String readlines(File src) {
    String r = null;
    if (src != null)
      r = readlines(src.getPath());
    return r;
  }

  public static String[] bufread(String buf) {
    if (buf != null)
      return split(buf, "\n");
    return null;
  }

  public static String[] bufread(File fname) {
    return bufread(readlines(fname));
  }

  /*
   * add a property to the project: => property is not added in case property
   * already exists => property is not RESOLVED.
   */
  static public void addProperty(Project P, String k, String v) {
    // exactly the same as Project.setNewProperty() but avoids to
    // log any comment as Ant is doing by default.
    if (P.getProperty(k) == null)
      P.setProperty(k, v);
  }

  static public void setProperty(Project P, Hashtable H) {
    String k, v;
    Enumeration e;

    if (P == null || H == null)
      return;

    e = H.keys();
    while (e.hasMoreElements()) {
      k = (String) e.nextElement();
      v = (String) H.get(k);
      if (v != null)
        P.setProperty(k, v);
    }
  }

  static public String concat(String[] argv, int I, int E, String Sep) {
    StringBuffer buf;
    int i = I;
    int e = E;
    String sep = Sep;
    if (argv == null)
      return "";
    if (e > argv.length)
      e = argv.length;
    if (i >= e)
      return "";
    if (sep == null)
      sep = " ";

    buf = new StringBuffer();
    while (i + 1 < e) {
      buf.append(argv[i]);
      buf.append(sep);
      i += 1;
    }
    buf.append(argv[e - 1]);
    return buf.toString();
  }

  static public String concat(String[] argv, String sep) {
    return concat(argv, 0, argv.length, sep);
  }

  static public int endOf(String v, int Pos, int end) {
    int open = 0;
    int close = 0;
    char c;
    int pos = Pos;
    while (pos < end) {
      c = v.charAt(pos);
      if (c == '}') {
        close += 1;
        if (open == close)
          return pos;
      }
      if (c == '$') {
        if (pos + 1 < end) {
          pos += 1;
          c = v.charAt(pos);
          if (c == '{') {
            open += 1;
          }
        }
      }
      pos += 1;
    }

    return pos;
  }

  static public String[] split(String v, String c) {
    if (v == null)
      return null;

    LinkedList L = new LinkedList();
    int j, i, e;

    i = 0;
    j = 0;
    e = v.length();

    while (i < e) {
      j = v.indexOf(c, i);
      if (j >= 0) {
        L.addLast(v.substring(i, j));
        i = j + 1;
      } else {
        L.addLast(v.substring(i, e));
        i = e;
      }
    }
    String[] str = new String[L.size()];
    for (i = 0; i < str.length; i++) {
      str[i] = (String) L.get(i);
    }
    return str;
  }

  static public String dirname(String name) {
    File F = new File(name);
    return F.getParent();
  }

  static public String basename(String name) {
    File F = new File(name);
    return F.getName();
  }

  static public String trimNonDigit(String V) {
    int i, e;
    char c;
    String v = V;
    i = 0;
    e = v.length();
    while (i < e) {
      c = v.charAt(i);
      if (Character.isDigit(c))
        break;
      i += 1;
    }
    if (i < e) {
      v = v.substring(i, e);
    }
    return v;
  }

  static public int contains(String v, char c) {
    int i, j, e, n;

    n = 0;
    i = 0;
    e = 0;
    if (v != null) {
      e = v.length();
      while (i < e) {
        j = v.indexOf(c, i);
        if (j < 0)
          i = e;
        else {
          i = j + 1;
          n = n + 1;
        }
      }
    }
    return n;
  }

  static public String tag2ver(String V) {
    /* tag is given - derive `project.version' */
    // strip everything till first digit.
    String v = V;
    v = trimNonDigit(v);

    if (contains(v, '-') >= 2) {
      return v.replace('-', '.');
    }

    if (contains(v, '_') >= 2) {
      return v.replace('_', '.');
    }

    if (contains(v, '-') >= 1) {
      return v.replace('-', '.');
    }

    if (contains(v, '_') >= 1) {
      return v.replace('_', '.');
    }

    /* we give up */
    return null;
  }

  /**
   * Translates a given file name in it's official dependency name.
   * 
   * @param v
   *          library name as string
   * @return A string representing the variable name used in a baseline for
   *         given library (artifact) name <code>v</code>. The method never
   *         returns null.
   */
  static public String jar2var(String V) {
    char c;
    String s;
    String v = V;
    if (v == null)
      return "";

    v = v.trim();
    if (v.equals(""))
      return "";

    /* strip directory and extension .. */
    v = getstem(v);
    if (v == null)
      return "";

    s = "";
    for (int i = 0; i < v.length(); ++i) {
      c = v.charAt(i);
      if (Character.isLetterOrDigit(c)) {
        s += Character.toUpperCase(c);
      } else {
        /* everything else gets mapped to '_' */
        s += '_';
      }
    }
    return s;
  }

  static public String sjoin(String Sep, String[] list) {
    /* inspired by Python's build 'join' string operator */
    String s = "";
    String sep = Sep;
    int i, e;

    if (list == null)
      return s;

    e = list.length;
    if (e == 0) {
      return s;
    }
    s = list[0];
    if (e == 1) {
      return s;
    }
    if (sep == null)
      sep = "";
    i = 1;
    while (i < e) {
      s += sep + list[i];
      i += 1;
    }
    return s;
  }

  public static String mkchrseq(char c, int n) {
    String s = "";
    /* terrible inefficient */
    for (int i = 0; i < n; ++i)
      s += c;
    return s;
  }

  private static String center(String S, int width, char chr) {
    int w;
    String s = S;
    w = s.length();

    if (w > width) {
      s = s.substring(0, width);
    } else {
      int w1, w2;
      w1 = (width - w) / 2;
      w2 = width - w1 - w;
      s = mkchrseq(chr, w1) + s + mkchrseq(chr, w2);
    }
    return s;
  }

  public static String logo(String msg, int width) {
    String s = "";
    String h;

    h = "use `ant -p' to get a list of useful targets";

    s += mkchrseq(':', width);
    s += '\n';
    s += "::";
    s += center(msg, width - 4, ' ');
    s += "::";
    s += '\n';
    s += "::";
    s += center(h, width - 4, ' ');
    s += "::";
    s += "\n";
    s += mkchrseq(':', width);
    s += '\n';
    return s;
  }

  public static String logo(String msg) {
    return logo(msg, 65);
  }

  public static void throwbx(String S) {
    String s = S;
    if (s == null) {
      s = "(no reason given)";
    }
    throw new BuildException(s);
  }

  public static void throwbx(String S, Exception e) {
    String s = S;
    if (s == null) {
      s = "(no reason given)";
    }
    throw new BuildException(s + "," + e.getMessage(), e);
  }

  /*
   * shall compare two versions similar as strcmp does: * 0 => equal * 1 => v2
   * is newer * -1 => v1 is newer * throws exception if v1 or v2 are not a
   * version, ie. do not * match ??? * Examples: * 1.0,1.0 => 0 1.0,1.0.0 => 0 *
   * 1.1,1.0 => -1 1.1,1.0.9 => -1 * 1.0,1.1 => 1 1.1,1.1.0.1 => 1 *
   */

  public static int vercmp(String va, String vb) {
    int i, na, nb;
    int a_i, b_i;
    String a_s, b_s;
    String[] a, b;

    // if not va and not vb:
    // return 0
    if (va == null && vb == null)
      return 0;

    if (va == null)
      return -1;
    if (vb == null)
      return 1;

    // split va and vb into arrays using sep '.'
    a = va.split("\\.");
    b = vb.split("\\.");

    na = a.length;
    nb = b.length;

    // get the shortest length
    if (na < nb) {
      // fill up a ..
      String[] y = new String[nb];
      String zero = "0";

      i = 0;
      for (; i < na; ++i)
        y[i] = a[i];
      for (; i < nb; ++i)
        y[i] = zero;
      a = y;
    }
    if (nb < na) {
      // fill up a ..
      String[] y = new String[na];
      String zero = "0";
      i = 0;
      for (; i < nb; ++i)
        y[i] = b[i];
      for (; i < na; ++i)
        y[i] = zero;
      b = y;
    }

    na = a.length;
    nb = b.length;

    for (i = 0; i < na; ++i) {
      a_s = a[i];
      b_s = b[i];

      try {
        a_i = Integer.parseInt(a_s);
        b_i = Integer.parseInt(b_s);

        if (a_i < b_i) {
          return -(i + 1);
        }
        if (a_i > b_i) {
          return (i + 1);
        }
      }
      catch (Exception e) {
        /***********************************************************************
         * at least one section is not a integral number, so let's compare
         * lexicographically .. *
         **********************************************************************/
        int c = a_s.compareTo(b_s);
        if (c < 0) {
          return -(i + 1);
        }
        if (c > 0) {
          return (i + 1);
        }
      }
      // still here? Then both versions are equal till section 'i+1'.
      // Take the next round ..
    }
    // very well, versions are identical ..
    return 0;
  }

  /*
   * Redesign of rule to generate the stem. The ultimate goal is to get a name
   * suitable as shell variable. The stem should shall not include any version
   * information and shall unique stems. Here's how this shall be done now:
   * 
   * a. find index of first dot (.) character scanning from left to right. b. if
   * there is no dot, then the argument will be the stem, otherwise we have an
   * index j >= 0 with s[j] = '.'. c. scan from j backwards to 1: while s[j-1]
   * is a digit, do j--. d. if j==1 then there's just digits left from "." and
   * in this case we take [0,j) as stem. Otherwise we have s[j-1] is not a
   * digit. f. If s[j-1] is not a hyphen we take s[0,j) as stem. g. if j>1 then
   * we take s[0,j-1) as stem. h. s[0] is a '-', then take s[0,(first dot)) as
   * stem.
   */

  final public static String getstem(String S) throws BuildException {
    String s = S;
    int i, j;
    char c;

    /* we need an argument at least to generate something */
    if (s == null) {
      return null;
    }

    j = s.lastIndexOf('/');
    if (j >= 0) {
      s = s.substring(j + 1);
    }

    /* trim any whitespace */
    s = s.trim();

    /* can't make a stem out of nothing either .. */
    if (s.equals("")) {
      return null;
    }

    /* get index of first '.' */
    j = s.indexOf('.');
    if (j < 0) {
      /* if there's no dot then we return argument as stem */
      /* example: 'abc_jar' => 'abc_jar' */
      return s;
    }

    if (j == 0) {
      /* i.e. ".jar" */
      return null;
    }

    /* just to make javac happy */
    c = 'c';

    for (i = j - 1; i >= 0; i--) {
      c = s.charAt(i);
      if (!Character.isDigit(c))
        break;
    }

    if (i < 0) {
      /* only digits left from "." */
      /* example: '123.jar' -> '123' */
      return s.substring(0, j);
    }

    /* have there been any digits at all ? */
    if (i == (j - 1)) {
      /* no digits found */
      /* example: 'a-.jar' -> 'a-' */
      return s.substring(0, j);
    }

    /* ok, there a r e digits left of '.' */
    /* s[i] is not a digit and i>=0 */
    if (c != '-') {
      /* (i+1) exists because there's a dot in "s" */
      /* 'a1.2.jar' -> 'a1' */
      return s.substring(0, (i + 1));
    }

    /* s[i] is a '-' and i>=0 */
    if (i == 0) {
      /* '-1.jar' -> '-1' */
      return s.substring(0, j);
    }

    /* a-1.2-beta1.jar' -> 'a' */
    return s.substring(0, i);
  }

  public static void fcopy(File src, File dst) throws Exception {
    byte[] buffer = new byte[512];
    InputStream in = new FileInputStream(src);
    OutputStream out = new FileOutputStream(dst);
    int bytesRead = 0;
    while ((bytesRead = in.read(buffer)) > 0) {
      out.write(buffer, 0, bytesRead);
    }
    out.flush();
    in.close();
    out.close();
  }

  public static void copy(InputStream src, OutputStream dst) throws Exception {
    byte[] buffer;
    int n;

    if (src == null) {
      return;
    }
    if (dst == null) {
      return;
    }

    buffer = new byte[5124];
    while ((n = src.read(buffer)) > 0) {
      dst.write(buffer, 0, n);
    }
    dst.flush();
  }

  public static String[] grep(Project project, String regexpr) throws Exception {
    String[] R;
    Pattern P;
    Matcher M;
    Enumeration E;
    LinkedList L;

    R = null;
    P = Pattern.compile(regexpr);
    L = new LinkedList();
    E = project.getProperties().keys();

    verbose(project, "grepping properties matching `" + regexpr + "' ..");

    while (E.hasMoreElements()) {
      String k, v;

      k = (String) E.nextElement();
      M = P.matcher(k);

      if (M.matches() == false) {
        // verbose("key `" + k + "' does not match ..");
        continue;
      }

      v = project.getProperty(k);
      if (v == null) {
        debug(project, "undefined property `" + k + "' skipped.");
        continue;
      }
      v = v.trim();
      if (v.equals("")) {
        debug(project, "empty property `" + k + "' skipped.");
        continue;
      }
      /* check on unresolved references */
      if (Eval.needseval(v)) {
        debug(project, "unresolved property `" + k + "' skipped.");
        continue;
      }
      // finally!
      verbose(project, "property `" + k + "' matches regexpr `" + regexpr
          + "', grepping `" + v + "'.");
      L.add(k + "=" + v);
    }

    /* sort list and return as array of strings */
    R = new String[L.size()];
    Collections.sort(L);
    for (int i = 0; i < L.size(); ++i)
      R[i] = (String) L.get(i);
    return R;
  }

  static public Method methodbyname(Class C, String name, Class[] type)
      throws NoSuchMethodException, SecurityException {
    return C.getDeclaredMethod(name, type);
  }

  static public Object invoke(Object obj, String name, Class type[],
      Object[] args) throws Exception {
    Method M;
    M = methodbyname(obj.getClass(), name, type);
    return M.invoke(obj, args);
  }

  static public Field fieldbyname(Class clazz, String name) {
    Field F = null;
    Class C = clazz;
    while ((C != null) && (F == null)) {
      try {
        F = C.getDeclaredField(name);
      }
      catch (NoSuchFieldException e) {
        C = C.getSuperclass();
      }
    }
    return F;
  }

  static public Object getattr(Object obj, String name)
      throws IllegalAccessException {
    Field F;
    F = fieldbyname(obj.getClass(), name);
    return F.get(obj);
  }

  static public void setattr(Object obj, String name, Object val)
      throws IllegalAccessException {
    Field F;
    F = fieldbyname(obj.getClass(), name);
    F.set(obj, val);
  }

  static public Object valueof(Object obj, String name)
      throws IllegalAccessException {
    Field F;
    F = fieldbyname(obj.getClass(), name);
    F.setAccessible(true);
    return F.get(obj);
  }

  /**
   * Removes a Hashtable entry. The hashtable must be available as * attribute
   * <code>att</code> of object instance <code>obj</code>. * The entry to
   * remove is given by key <code>key</code>. Note that * this method does
   * not care about accessability, i.e. it allows * to remove a key from a
   * private Hashtable. * *
   * 
   * @param obj
   *          might be null *
   * @param att
   *          might be null *
   * @param key
   *          might be null * *
   * @return true in case key could be removed from Hashtable denoted * by
   *         attribute <code>att</code>.
   */
  static protected boolean htabremove(Object obj, String att, String key) {
    boolean b = false;
    if (obj != null) {
      Hashtable tab;
      try {
        tab = (Hashtable) Static.valueof(obj, att);
        if (tab != null) {
          tab.remove(key);
          b = true;
        }
      }
      catch (Exception e) {
        /* don't care */
      }
    }
    return b;
  }

  /**
   * tests whether class <code>obj</code> is a subclass of <code>
   ** base</code>. * *
   * 
   * @return true if clazz is a subclass of base (or equals base).
   */

  static final public boolean issubclass(Class clazz, Class base) {
    return clazz != null && base != null && base.isAssignableFrom(clazz);
  }

  static final public ComponentHelper getcomph(Project P) {
    return ComponentHelper.getComponentHelper(P);
  }

  /** shortcut to create a component */
  static final public Object getcomp(Project P, String s) {
    return getcomph(P).createComponent(s);
  }

  /** shortcut to get a component's class */
  static final public Class getclass(Project P, String s) {
    return getcomph(P).getComponentClass(s);
  }

  static final public boolean isdefined(Project P, String property) {
    boolean b = false;
    if (property != null && P != null) {
      b = P.getProperty(property) != null;
    }
    return b;
  }

  static final public boolean isproperty(Project P, String property) {
    boolean b = false;
    if (property != null && P != null) {
      b = P.getProperty(property) != null;
    }
    return b;
  }

  static final public boolean isreference(Project P, String id) {
    boolean b = false;
    if (id != null && P != null) {
      b = P.getReference(id) != null;
    }
    return b;
  }

  static final public Object getref(Project P, String id) {
    Object obj = null;
    if (id != null && P != null) {
      obj = P.getReference(id);
    }
    return obj;
  }

  static final public boolean istarget(Project P, String s) {
    boolean b = false;
    if (s != null && P != null) {
      b = P.getTargets().containsKey(s);
    }
    return b;
  }

  /**
   * Test whether <code>s</code> is a Macro or Task in project *
   * <code>P</code>. * *
   * 
   * @return true if <code>s<code> is a subclass of class Task.
   */

  static final public boolean ismacroOrtask(Project P, String s) {
    boolean b = false;
    if (s != null && P != null) {
      /** check whether we are a macro or a task */
      Class C = getclass(P, s);
      b = issubclass(C, org.apache.tools.ant.Task.class);
      verbose("ismacroOrtask(..," + s + ") = " + b);
    }
    return b;
  }

  /**
   * Test whether <code>s</code> is a Macro project <code>P</code>. * *
   * 
   * @return true if <code>s<code> is a subclass of class <code>
   ** MacroInstance</code>.
   */

  static public boolean ismacro(Project P, String s) {
    boolean b = false;
    if (s != null && P != null) {
      /** check whether we are a macro or a task */
      Class C = getclass(P, s);
      b = issubclass(C, org.apache.tools.ant.taskdefs.MacroInstance.class);
      verbose("ismacro(..," + s + ") = " + b);
    }
    return b;
  }

  /**
   * Test whether <code>s</code> is a Taskdef in project <code>P</code>. * *
   * 
   * @return true if <code>s<code> is Taskdef.
   */

  static public boolean istask(Project P, String s) {
    boolean b = false;
    if (s != null && P != null) {
      /** check whether we are a macro or a task */
      Class C = getclass(P, s);
      boolean b1, b2;
      b1 = issubclass(C, org.apache.tools.ant.taskdefs.MacroInstance.class);
      b2 = issubclass(C, org.apache.tools.ant.Task.class);
      b = b2 && !b1;
      verbose("istask(..," + s + ") = " + b);
    }
    return b;
  }

  /**
   * Translates a <i>Unix Shell</i> file matching pattern into a regular
   * expression. A Unix Shell pattern may consist of: * matches everything ?
   * matches any single character [seq] matches any character in seq [!seq]
   * matches any char not in seq
   * 
   * Note that there's no way to quote meta-characters.
   * 
   * @param glob
   *          not null
   */

  static public String glob2regex(String pat) {
    return patternAsRegex(pat) + '$';
  }

  static public String patternAsRegex(String glob) {
    char c;
    int i, j, n;
    String r;

    i = 0;
    n = glob.length();
    r = "";

    while (i < n) {
      c = glob.charAt(i++);
      switch (c) {
        case '*':
          r += ".*";
          break;
        case '?':
          r += '.';
          break;
        case '[':
          j = i;
          if (j < n && glob.charAt(j) == '!')
            j = j + 1;
          if (j < n && glob.charAt(j) == ']')
            j = j + 1;
          while (j < n && glob.charAt(j) != ']')
            j = j + 1;

          if (j >= n)
            r = r + "\\[";
          else {
            String s;
            s = substring(glob, i, j);
            s = replace(s, '\\', "\\\\");
            switch (s.charAt(0)) {
              case '!':
                s = "^" + substring(s, i, j);
                break;
              case '^':
                s = '\\' + s;
                break;
            }
            r = r + "[" + s + "]";
            i = j + 1;
          }
          break;
        default:
          r = r + escape("" + c, '\\');
          break;
      }
    }
    return r;
  }

  /**
   * escape all non-alphanumeric characters in <code>s</code> by escape
   * character <code>esc</code>.
   * 
   * @param s
   *          not null
   */
  static protected String escape(String s, char esc) {
    String r = "";
    char c;

    for (int i = 0; i < s.length(); ++i) {
      c = s.charAt(i);
      if (Character.isLetterOrDigit(c) == false)
        r += esc;
      r += c;
    }
    return r;
  }

  /**
   * escape non-alphanumeric character <code>c</code> by escape character
   * <code>esc</code>.
   * 
   * @param c
   *          character in question
   * @param esc
   *          the escape character
   * 
   * @return <code>c</code> if <code>c</code> is alpanumeric character
   *         otherwise return <code>esc</code>.
   */
  static protected String escape(char c, char esc) {
    return "" + (Character.isLetterOrDigit(c) ? c : esc);
  }

  /**
   * replace all <code>c</code> characters in <code>s</code> with string
   * <code>sub</code>.
   * 
   * @param s
   *          not null
   */
  static protected String replace(String s, char c, String sub) {
    char x;
    String r = "";
    for (int i = 0; i < s.length(); ++i) {
      x = s.charAt(i);
      if (x == c)
        r += sub;
      else
        r += x;
    }
    return r;
  }

  /**
   * similar as <code>s.substring(i,j)</code> except that j is allowed to be
   * larger than <code>s.length()</code>. If <code>j</code> is less then
   * <code>i</code>, the empty string is returned. Otherwise function behaves
   * as <code>s.substring(i,j)</code>.
   * 
   * @param s
   *          not null
   */
  static protected String substring(String s, int i, int J) {
    String r = null;
    int L;
    int j = J;
    try {
      L = s.length();
      j = (j <= i) ? i : (j > L) ? L : j;
      r = s.substring(i, j);
    }
    catch (Exception e) {
      System.err.println("this should never happen ..");
    }
    return r;
  }

  /**
   * * test whether a character is printable. * *
   * 
   * @param c
   *          character to test *
   * @return true if printable
   */
  final public static boolean isprintable(char c) {
    int type;
    boolean retv;

    retv = false;
    type = Character.getType(c);

    switch (type) {
      case Character.COMBINING_SPACING_MARK:
      case Character.CONNECTOR_PUNCTUATION:
      case Character.CURRENCY_SYMBOL:
      case Character.DASH_PUNCTUATION:
      case Character.DECIMAL_DIGIT_NUMBER:
      case Character.ENCLOSING_MARK:
      case Character.END_PUNCTUATION:
      case Character.LOWERCASE_LETTER:
      case Character.MATH_SYMBOL:
      case Character.MODIFIER_SYMBOL:
      case Character.NON_SPACING_MARK:
      case Character.OTHER_LETTER:
      case Character.OTHER_PUNCTUATION:
      case Character.OTHER_SYMBOL:
      case Character.SPACE_SEPARATOR:
      case Character.START_PUNCTUATION:
      case Character.TITLECASE_LETTER:
      case Character.UPPERCASE_LETTER:
        retv = true;
    }
    return retv;
  }

  /**
   * * test whether a character is a 'textual' character, i.e. * not a binary
   * character. * *
   * 
   * @param c
   *          character to test *
   * @return true if non-binary character.
   */

  final public static boolean istext(char c) {
    return isprintable(c) || Character.isWhitespace(c);
  }

  /**
   * * test whether a character is a 'textual' character, i.e. * not a binary
   * character. * *
   * 
   * @param c
   *          character to test *
   * @return true if non-binary character.
   */

  final public static boolean isbinary(char c) {
    return !istext(c);
  }

  final public static String[] lex(String s) {
    String[] r = null;
    /* split argument */
    try {
      r = Static.split0x1(s, '\'');
    }
    catch (Exception e) {
      /* ignored */
    }
    return r;
  }

  final static protected String trim(String s, String def) {
    return s == null ? def : s.trim();
  }

  /**
   * Trim a given string.
   * 
   * This version of trim ensures that the result value is not an empty string,
   * i.e. a string consisting only of whitespace characters. If an empty string
   * would be returned after trimming down, the alternative
   * 
   * @otherwise is returned instead.
   */

  final static public String trim2(String s, String otherwise) {
    String r;
    /* assign default return value */
    r = otherwise;
    if (s != null && s.length() > 0) {
      char c1, c2;
      c1 = s.charAt(0);
      c2 = s.charAt(s.length() - 1);
      r = s;
      /* trim if there is something to trim down */
      if (Character.isWhitespace(c1) || Character.isWhitespace(c2)) {
        r = s.trim();
        /* trimming down may have lead to an empty string */
        if (r.length() <= 0)
          r = otherwise;
      }
    }
    return r;
  }

  final static public boolean empty(String s) {
    return s == null ? true : s.length() <= 0;
  }

  final static public boolean isEmpty(String s) {
    return (s == null) || s.matches("\\s*");
  }

  static public String cat(Project P, File file) {
    String buf = null;
    if (P != null && file != null) {
      buf = Static.readlines(file);
      if (buf != null)
        P.log(buf);
    }
    return buf;
  }

  static protected Element loadxml(Project P, File file) {
    Element r = null;
    try {
      Document d;
      d = net.haefelingerit.flaka.dep.Reader.getxmldoc(file);
      r = d.getDocumentElement();
    }
    catch (IOException e) {
      String path = file.getAbsolutePath();
      if (file.exists() == false) {
        Static.debug(P, path + " does not exist.");
        return null;
      }
      if (file.isFile() == false) {
        Static.debug(P, path + " not a file.");
        return null;
      }
      Static.debug(P, path + ": " + e.getMessage());
    }
    catch (Exception e) {
      String path = file.getAbsolutePath();
      String mesg = path + ": " + e.getMessage();
      Static.debug(P, mesg);
      throw new BuildException(mesg);
    }
    return r;
  }

  static public String stripEmptyLines(String buf) {
    final Pattern p = Pattern.compile("\\s*$", Pattern.MULTILINE);
    Matcher m = p.matcher(buf);
    return m.replaceAll("");
  }

  /**
   * Cleans up the empty lines and tab after removing the nodes
   * 
   * @param source
   */
  protected static void tidyxml(DOMSource source) {
    Node current = null;
    String cv = null;
    Node next = null;
    String nv = null;

    Node root = source.getNode();
    NodeList nlist = root.getChildNodes().item(0).getChildNodes();

    int i = 0;
    while (i < nlist.getLength() - 1) {
      current = nlist.item(i);
      cv = current.getNodeValue();
      next = nlist.item(i + 1);
      nv = next.getNodeValue();
      if (cv != null && cv.equals("\n\t") && nv != null && nv.equals("\n\t")) {
        root.getChildNodes().item(0).removeChild(next);
      } else {
        i++;
      }
    }
  }

  static public String flushxml(Project P, Element root, StringBuffer buf) {
    String s = null;
    if (P != null && root != null) {
      try {
        DOMSource source = new DOMSource(root.getOwnerDocument());
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer tr = tf.newTransformer();
        tr.setOutputProperty("omit-xml-declaration", "yes");
        StringWriter sw = new StringWriter();
        StreamResult result = new StreamResult(sw);
        tidyxml(source);
        tr.transform(source, result);
        s = stripEmptyLines(sw.toString());
        if (buf != null)
          buf.append(s);
      }
      catch (Exception e) {
        s = null;
        Static.debug(P, "error transforming XML document: " + e.getMessage());
      }
    }
    return s;
  }

  static public String flushxml(Project P, Element root, File file) {
    String s = null;
    if (P != null && root != null && file != null) {
      s = flushxml(P, root, (StringBuffer) null);

      /* create file */
      try {
        Static.writex(file, s, false);
      }
      catch (IOException e) {
        throw new BuildException(e);
      }
    }
    return s;
  }

  static public String nodeattribute(Node node, String name) {
    NamedNodeMap attributes;

    if (node == null || name == null)
      return null;

    attributes = node.getAttributes();
    if (attributes == null)
      return null;

    for (int i = 0; i < attributes.getLength(); ++i) {
      String s;
      Node attr;

      attr = attributes.item(i);
      if (attr == null) {
        continue;
      }
      s = attr.getNodeName();
      if (s == null) {
        continue;
      }
      if (s.equalsIgnoreCase(name)) {
        s = attr.getNodeValue();
        if (s != null)
          s = s.trim();
        return s;
      }
    }
    return null;
  }

  static public Document getxmldoc(InputStream stream) throws Exception {
    Document doc = null;
    DocumentBuilderFactory dbf = null;
    DocumentBuilder db = null;
    System.out.println("* using JAXP **");
    dbf = DocumentBuilderFactory.newInstance();
    /* rather important !! */
    dbf.setNamespaceAware(true);
    db = dbf.newDocumentBuilder();
    doc = db.parse(stream);
    return doc;
  }
  
  
//  static public Document getxmldoc2(InputStream stream) throws Exception {
//    Document doc;
//    DOMParser parser;
//    ErrorHandler orig;
//
//    /* make a parser */
//    parser = new DOMParser();
//
//    /* get installed error handler */
//    orig = parser.getErrorHandler();
//
//    /* returns null document by default */
//    doc = null;
//    try {
//      InputSource is;
//
//      /* reset error handler */
//      parser.setErrorHandler(new MySaxErrHndlr());
//
//      is = new InputSource(stream);
//
//      /* parser my stream */
//      parser.parse(is);
//
//      /* get parser's document .. */
//      doc = parser.getDocument();
//    }
//    catch (Exception e) {
//      /* throw again */
//      throw e;
//    }
//    finally {
//      /* reset error handler */
//      parser.setErrorHandler(orig);
//    }
//    return doc;
//  }

  static public boolean isregexchar(char c) {
    return c == '/';
  }

  static public boolean ispatternchar(char c) {
    return c == '%';
  }

  /**
   * A standard way to compile a given pattern into a RE Pattern.
   * 
   * The interpretation of the given string depends on the first and last
   * character:
   * <ul>
   * <li>When character <code>/</code> is used, then the enclosed string is a
   * treated regular expression. If that is not the case, then a pattern
   * expression is assumed.</li>
   * <li>When character <code>%</code> is used, a pattern expression is
   * assumed</li>
   * <li>When both characters are not equal or, when equal, neither of both
   * characters above, then a pattern expression is assumed.</li>
   * </ul>
   * 
   * @param s
   *          the pattern to compile (not null)
   * @param f
   *          flags to use when compiling
   * @return Regular Expression (not null)
   */
  static public Pattern patterncompile(String S, int f) {
    char c1, c2;
    Pattern P = null;
    String s = S;
    int sz = s.length();

    if (sz > 1) {
      c1 = s.charAt(0);
      c2 = s.charAt(sz - 1);

      if (c1 == c2)
        if (isregexchar(c1) || ispatternchar(c2))
          s = s.substring(1, sz - 1);
      if (c1 != c2 || ispatternchar(c1))
        s = Static.patternAsRegex(s);
    }
    try {
      P = Pattern.compile(s, f);
    }
    catch (Exception e) {
      try {
        P = Pattern.compile(Static.patternAsRegex(s), f);
      }
      catch (Exception ex) {
        System.err.println("** exception: " + ex);
      }
    }
    return P;
  }

  static public void split(String s, Collection C, char quote)
      throws Exception {
    int i, j;
    char c;
    String buf;
  
    i = 0;
    buf = null;
  
    while (i < s.length()) {
      c = s.charAt(i);
  
      if (TestEL.ISBLANK(c)) {
        if (buf != null) {
          C.add(buf);
          buf = null;
        }
        i = TestEL.skipws(s, i + 1);
        continue;
      }
  
      if (c == '\\') {
        if ((i + 1) < s.length())
          buf = TestEL.a2buf(buf, s.charAt(i + 1));
        i += 2;
        continue;
      }
  
      if (c == quote) {
        j = TestEL.nextc(s, i + 1, quote);
        if (j < 0) {
          throw new Exception("string `" + s + "' not properly quoted.");
        }
        buf = TestEL.a2buf(buf, TestEL.unescape(s, i + 1, j));
        i = j + 1;
      } else {
        buf = TestEL.a2buf(buf, c);
        i += 1;
      }
    }
  
    if (buf != null) {
      C.add(buf);
    }
  }

  static public String[] split0x1(String s, char quote) throws Exception {
    String[] r;
    LinkedList L;
    L = new LinkedList();
    split(s, L, quote);
    r = new String[L.size()];
    for (int i = 0; i < L.size(); ++i)
      r[i] = (String) L.get(i);
    return r;
  }

}
