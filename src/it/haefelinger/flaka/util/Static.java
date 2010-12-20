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

package it.haefelinger.flaka.util;

import it.haefelinger.flaka.el.EL;

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
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


import org.apache.tools.ant.AntTypeDefinition;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.ComponentHelper;
import org.apache.tools.ant.Project;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * From the basis of an XML source, creates a list of dependencies.
 * 
 */

final public class Static
{
  static final public String EL = "ant.el";

  static final public int VARREF = 0x1;
  static final public int PROPTY = 0x2;
  static final public int WRITEPROPTY = 0x3;

  static final Class CLASS_TASK = org.apache.tools.ant.Task.class;
  static final Class CLASS_MACRO = org.apache.tools.ant.taskdefs.MacroInstance.class;

  /**
   * Assign an object either as variable or as property to a given project.
   * 
   * @param project
   *          not null
   * @param key
   *          not null
   * @param obj
   *          null allowed
   * @param type
   *          must be either Static.VARREF, Static.PROPTY or Static.WRITEPROPTY
   * @return project
   */
  final public static Project assign(Project project, String key, Object obj, int type)
  {
    /* need a proper key */
    if (key == null || (key = key.trim()).equalsIgnoreCase(""))
      return project;

    switch (type)
    {
      case Static.VARREF:
      {
        Map refs = project.getReferences();
        if (obj != null)
          refs.put(key, obj);
        else
          refs.remove(key);
        break;
      }
      case Static.PROPTY:
      {
        if (obj != null)
        {
          String val = obj instanceof String ? (String) obj : obj.toString();
          project.setNewProperty(key, val);
        }
        break;
      }
      case Static.WRITEPROPTY:
      {
        if (obj != null)
        {
          String val = obj instanceof String ? (String) obj : obj.toString();
          project.setProperty(key, val);
        } else
        {
          Static.unset(project, key);
        }
      }
    }
    return project;
  }

  final public static void _log_(Project P, String msg, int type)
  {
    Project p = P;
    if (msg == null)
      return;

    /***************************************************************************
     * If we don't have a project use stderr - this case * should not happen but
     * is used for the 'just-in-case' case. *
     **************************************************************************/

    if (p == null)
    {
      System.err.println(">> " + msg);
      return;
    }

    /* use the project's logger */
    p.log(msg, type);
  }

  final public static void log(Project P, String msg, Exception e)
  {
    String m = msg;
    if (e != null)
      m += ": " + e.getMessage();
    _log_(P, m, Project.MSG_INFO);
  }

  final public static void log(Project P, String msg)
  {
    log(P, msg, null);
  }

  final public static void info(Project P, String msg, Exception e)
  {
    String m = msg;
    if (e != null)
      m += ": " + e.getMessage();
    _log_(P, m, Project.MSG_INFO);
  }

  final public static void info(Project P, String msg)
  {
    log(P, msg, null);
  }

  final public static void verbose(Project P, String msg, Exception e)
  {
    String m = msg;
    if (e != null)
      m += ": " + e.getMessage();
    _log_(P, m, Project.MSG_VERBOSE);
  }

  final public static void verbose(Project P, String msg)
  {
    verbose(P, msg, null);
  }

  final public static void debug(Project P, String msg, Exception e)
  {
    String m = msg;
    if (e != null)
      m += ": " + e.getMessage();
    _log_(P, m, Project.MSG_DEBUG);
  }

  final public static void debug(Project P, String msg)
  {
    debug(P, msg, null);
  }

  final public static void error(Project P, String msg, Exception e)
  {
    String m = msg;
    if (e != null)
      m += ": " + e.getMessage();
    _log_(P, m, Project.MSG_ERR);
  }

  final public static void error(Project P, String msg)
  {
    error(P, msg, null);
  }

  final public static void warning(Project P, String msg, Exception e)
  {
    String m = msg;
    if (e != null)
      m += ": " + e.getMessage();
    _log_(P, m, Project.MSG_WARN);
  }

  final public static void warning(Project P, String msg)
  {
    warning(P, msg, null);
  }

  final public static void writex(File f, String buf, boolean append) throws IOException
  {
    FileWriter out = null;
    try
    {
      out = new FileWriter(f, append);
      out.write(buf, 0, buf.length());
      out.flush();
    } catch (IOException ioe)
    {
      String path = f.getAbsolutePath();
      debug(null, path + ": " + ioe.getMessage());
      throw ioe;
    } finally
    {
      close(out);
    }
  }

  final public static void close(Writer w)
  {
    /* silently close a writer */
    if (w != null)
    {
      try
      {
        w.close();
      } catch (IOException ioex)
      {
        // ignore
      }
    }
  }

  final public static void close(OutputStream os)
  {
    if (os != null)
      try
      {
        os.close();
      } catch (IOException e)
      {
        /* ignored */
      }
  }

  final public static void close(InputStream is)
  {
    if (is != null)
      try
      {
        is.close();
      } catch (IOException e)
      {
        /* ignored */
      }
  }

  final public static void close(Reader r)
  {
    if (r != null)
      try
      {
        r.close();
      } catch (IOException e)
      {
        /* ignored */
      }
  }

  final public static int write(File name, String s, boolean mode)
  {
    int r = 1;
    try
    {
      writex(name, s, mode);
      r = 0;
    } catch (Exception e)
    { /* not used */
    }
    return r;
  }

  final public static void writex(InputStream cin, String fname, boolean mode) throws IOException
  {
    FileOutputStream out = null;
    byte[] buf = new byte[4098];
    int sz;
    try
    {
      File f = new File(fname);
      out = new FileOutputStream(f.getAbsolutePath(), mode);
      sz = cin.read(buf);
      while (sz >= 0)
      {
        out.write(buf, 0, sz);
        sz = cin.read(buf);
      }
    } catch (IOException ioe)
    {
      throw ioe;
    } finally
    {
      if (out != null)
      {
        try
        {
          out.close();
        } catch (IOException ioex)
        {
          // ignore
        }
      }
    }
  }

  public static String readlines(Reader reader)
  {
    String r;
    StringBuilder b;

    try
    {
      BufferedReader R;
      String L;

      b = new StringBuilder();
      R = new BufferedReader(reader);
      L = R.readLine();

      while (L != null)
      {
        b.append(L).append("\n");
        L = R.readLine();
      }

      R.close();
      r = b.toString();
    } catch (IOException e)
    {
      r = null;
    }
    return r;
  }

  final public static String readlines(InputStream src)
  {
    String r = null;
    if (src != null)
      r = readlines(new InputStreamReader(src));
    return r;
  }

  final public static String readlines(String src)
  {
    String r = null;
    if (src != null)
    {
      try
      {
        r = readlines(new FileReader(src));
      } catch (Exception e)
      { /* not used */
      }
    }
    return r;
  }

  final public static String readlines(File src)
  {
    String r = null;
    if (src != null)
      r = readlines(src.getPath());
    return r;
  }

  final public static String[] bufread(String buf)
  {
    if (buf != null)
      return split(buf, "\n");
    return null;
  }

  final public static String[] bufread(File fname)
  {
    return bufread(readlines(fname));
  }

 
  final static public String[] split(String v, String c)
  {
    if (v == null)
      return null;

    LinkedList L = new LinkedList();
    int j, i, e;

    i = 0;
    j = 0;
    e = v.length();

    while (i < e)
    {
      j = v.indexOf(c, i);
      if (j >= 0)
      {
        L.addLast(v.substring(i, j));
        i = j + 1;
      } else
      {
        L.addLast(v.substring(i, e));
        i = e;
      }
    }
    String[] str = new String[L.size()];
    for (i = 0; i < str.length; i++)
    {
      str[i] = (String) L.get(i);
    }
    return str;
  }

 

  final static public String trimNonDigit(String V)
  {
    int i, e;
    char c;
    String v = V;
    i = 0;
    e = v.length();
    while (i < e)
    {
      c = v.charAt(i);
      if (Character.isDigit(c))
        break;
      i += 1;
    }
    if (i < e)
    {
      v = v.substring(i, e);
    }
    return v;
  }

  final static public int contains(String v, char c)
  {
    int i, j, e, n;

    n = 0;
    i = 0;
    e = 0;
    if (v != null)
    {
      e = v.length();
      while (i < e)
      {
        j = v.indexOf(c, i);
        if (j < 0)
          i = e;
        else
        {
          i = j + 1;
          n = n + 1;
        }
      }
    }
    return n;
  }

  final static public String tag2ver(String V)
  {
    /* tag is given - derive `project.version' */
    // strip everything till first digit.
    String v = V;
    v = trimNonDigit(v);

    if (contains(v, '-') >= 2)
    {
      return v.replace('-', '.');
    }

    if (contains(v, '_') >= 2)
    {
      return v.replace('_', '.');
    }

    if (contains(v, '-') >= 1)
    {
      return v.replace('-', '.');
    }

    if (contains(v, '_') >= 1)
    {
      return v.replace('_', '.');
    }

    /* we give up */
    return null;
  }

  /**
   * Translates a given loc name in it's official dependency name.
   */
  final static public String jar2var(String V)
  {
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
    for (int i = 0; i < v.length(); ++i)
    {
      c = v.charAt(i);
      if (Character.isLetterOrDigit(c))
      {
        s += Character.toUpperCase(c);
      } else
      {
        /* everything else gets mapped to '_' */
        s += '_';
      }
    }
    return s;
  }

  final public static String mkchrseq(String c, int n)
  {
    String s = "";
    /* terrible inefficient */
    for (int i = 0; i < n; ++i)
      s += c;
    return s;
  }

  final public static String center(String S, int width, String chr)
  {
    int w;
    String s = S;
    w = s.length();

    if (w > width)
    {
      s = s.substring(0, width);
    } else
    {
      int w1, w2;
      w1 = (width - w) / 2;
      w2 = width - w1 - w;
      s = mkchrseq(chr, w1) + s + mkchrseq(chr, w2);
    }
    return s;
  }

  final public static String logo(String msg, int width)
  {
    String s = "";
    String h;

    h = (msg == null ? "Hello, Flaka" : msg );

    s += mkchrseq(":", width);
    s += '\n';
    s += "::";
    s += center(h, width - 4, " ");
    s += "::";
    s += '\n';
    s += mkchrseq(":", width);
    s += '\n';
    return s;
  }

  final public static String logo(String msg)
  {
    return logo(msg, 65);
  }

  final public static void throwbx(String S)
  {
    String s = S;
    if (s == null)
    {
      s = "(no reason given)";
    }
    throw new BuildException(s);
  }

  final public static void throwbx(String S, Exception e)
  {
    String s = S;
    if (s == null)
    {
      s = "(no reason given)";
    }
    throw new BuildException(s + "," + e.getMessage(), e);
  }

  /*
   * shall compare two versions similar as strcmp does: 0 => equal 1 => v2 is
   * newer -1 => v1 is newer throws exception if v1 or v2 are not a version, ie.
   * do not match ??? Examples: 1.0,1.0 => 0 1.0,1.0.0 => 0 1.1,1.0 => -1
   * 1.1,1.0.9 => -1 1.0,1.1 => 1 1.1,1.1.0.1 => 1
   */

  final public static int vercmp(String va, String vb)
  {
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
    if (na < nb)
    {
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
    if (nb < na)
    {
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

    for (i = 0; i < na; ++i)
    {
      a_s = a[i];
      b_s = b[i];

      try
      {
        a_i = Integer.parseInt(a_s);
        b_i = Integer.parseInt(b_s);

        if (a_i < b_i)
        {
          return -(i + 1);
        }
        if (a_i > b_i)
        {
          return (i + 1);
        }
      } catch (Exception e)
      {
        /***********************************************************************
         * at least one section is not a integral number, so let's compare
         * lexicographically .. *
         **********************************************************************/
        int c = a_s.compareTo(b_s);
        if (c < 0)
        {
          return -(i + 1);
        }
        if (c > 0)
        {
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

  final public static String getstem(String S) throws BuildException
  {
    String s = S;
    int i, j;
    char c;

    /* we need an argument at least to generate something */
    if (s == null)
    {
      return null;
    }

    j = s.lastIndexOf('/');
    if (j >= 0)
    {
      s = s.substring(j + 1);
    }

    /* trim any whitespace */
    s = s.trim();

    /* can't make a stem out of nothing either .. */
    if (s.equals(""))
    {
      return null;
    }

    /* get index of first '.' */
    j = s.indexOf('.');
    if (j < 0)
    {
      /* if there's no dot then we return argument as stem */
      /* example: 'abc_jar' => 'abc_jar' */
      return s;
    }

    if (j == 0)
    {
      /* i.e. ".jar" */
      return null;
    }

    /* just to make javac happy */
    c = 'c';

    for (i = j - 1; i >= 0; i--)
    {
      c = s.charAt(i);
      if (!Character.isDigit(c))
        break;
    }

    if (i < 0)
    {
      /* only digits left from "." */
      /* example: '123.jar' -> '123' */
      return s.substring(0, j);
    }

    /* have there been any digits at all ? */
    if (i == (j - 1))
    {
      /* no digits found */
      /* example: 'a-.jar' -> 'a-' */
      return s.substring(0, j);
    }

    /* ok, there a r e digits left of '.' */
    /* s[i] is not a digit and i>=0 */
    if (c != '-')
    {
      /* (i+1) exists because there's a dot in "s" */
      /* 'a1.2.jar' -> 'a1' */
      return s.substring(0, (i + 1));
    }

    /* s[i] is a '-' and i>=0 */
    if (i == 0)
    {
      /* '-1.jar' -> '-1' */
      return s.substring(0, j);
    }

    /* a-1.2-beta1.jar' -> 'a' */
    return s.substring(0, i);
  }

  final public static void fcopy(File src, File dst) throws Exception
  {
    byte[] buffer = new byte[512];
    InputStream in = new FileInputStream(src);
    OutputStream out = new FileOutputStream(dst);
    int bytesRead = 0;
    while ((bytesRead = in.read(buffer)) > 0)
    {
      out.write(buffer, 0, bytesRead);
    }
    out.flush();
    in.close();
    out.close();
  }

  final public static void copy(InputStream src, OutputStream dst) throws Exception
  {
    byte[] buffer;
    int n;

    if (src == null)
    {
      return;
    }
    if (dst == null)
    {
      return;
    }

    buffer = new byte[5124];
    while ((n = src.read(buffer)) > 0)
    {
      dst.write(buffer, 0, n);
    }
    dst.flush();
  }

  final public static String[] grep(Project project, String regexpr) throws Exception
  {
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

    while (E.hasMoreElements())
    {
      String k, v;

      k = (String) E.nextElement();
      M = P.matcher(k);

      if (M.matches() == false)
      {
        // verbose("key `" + k + "' does not match ..");
        continue;
      }

      v = project.getProperty(k);
      if (v == null)
      {
        debug(project, "undefined property `" + k + "' skipped.");
        continue;
      }
      v = v.trim();
      if (v.equals(""))
      {
        debug(project, "empty property `" + k + "' skipped.");
        continue;
      }
      /* check on unresolved references */
      if (v.contains("${"))
      {
        debug(project, "unresolved property `" + k + "' skipped.");
        continue;
      }
      // finally!
      verbose(project, "property `" + k + "' matches regexpr `" + regexpr + "', grepping `" + v
          + "'.");
      L.add(k + "=" + v);
    }

    /* sort list and return as array of strings */
    R = new String[L.size()];
    Collections.sort(L);
    for (int i = 0; i < L.size(); ++i)
      R[i] = (String) L.get(i);
    return R;
  }

  final static public Method methodbyname(Class C, String name, Class[] type)
      throws NoSuchMethodException, SecurityException
  {
    return C.getDeclaredMethod(name, type);
  }

  final static public Object invoke(Object obj, String name, Class type[], Object[] args)
      throws Exception
  {
    Method M;
    M = methodbyname(obj.getClass(), name, type);
    return M.invoke(obj, args);
  }

  final static public Field fieldbyname(Class clazz, String name)
  {
    Field F = null;
    Class C = clazz;
    while ((C != null) && (F == null))
    {
      try
      {
        F = C.getDeclaredField(name);
      } catch (NoSuchFieldException e)
      {
        C = C.getSuperclass();
      }
    }
    return F;
  }

  final static public Object getattr(Object obj, String name) throws IllegalAccessException
  {
    Field F;
    F = fieldbyname(obj.getClass(), name);
    return F.get(obj);
  }

  final static public void setattr(Object obj, String name, Object val) throws IllegalAccessException
  {
    Field F;
    F = fieldbyname(obj.getClass(), name);
    F.set(obj, val);
  }

  final static public Object valueof(Object obj, String name) throws IllegalAccessException
  {
    Field F;
    F = fieldbyname(obj.getClass(), name);
    F.setAccessible(true);
    return F.get(obj);
  }

  /**
   * Removes a Hashtable entry. The hashtable must be available as * attribute
   * <code>att</code> of object instance <code>obj</code>. * The entry to remove
   * is given by key <code>key</code>. Note that * this method does not care
   * about accessability, i.e. it allows * to remove a key from a private
   * Hashtable. * *
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
  final static public boolean htabremove(org.apache.tools.ant.PropertyHelper obj, String att, String key)
  {
    boolean b = false;
    if (obj != null)
    {
      Hashtable tab;
      try
      {
        tab = (Hashtable) Static.valueof(obj, att);
        if (tab != null)
        {
          tab.remove(key);
          b = true;
        }
      } catch (Exception e)
      {
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

  static final public boolean issubclass(Class clazz, Class base)
  {
    return clazz != null && base != null && base.isAssignableFrom(clazz);
  }

  static final public ComponentHelper comphelper(Project P)
  {
    return ComponentHelper.getComponentHelper(P);
  }

  static final public AntTypeDefinition compdef(Project project,String property) {
    return Static.comphelper(project).getDefinition(property);
  }
  
  /** shortcut to create a component */
  static final public Object makecomp(Project P, String s)
  {
    return comphelper(P).createComponent(s);
  }

  /** shortcut to get a component's class */
  static final public Class getclass(Project P, String s)
  {
    return comphelper(P).getComponentClass(s);
  }


  static final public boolean isproperty(Project P, String property)
  {
    return property(P,property) == null ? false : true;
  }
  
  
  static final public boolean isreference(Project P, String id)
  {
    return reference(P,id) == null ? false : true;
  }
  
  static final public boolean istarget(Project P, String s)
  {
    return P.getTargets().containsKey(s);
  }

  static final public boolean istask(Project P, String s)
  {
    Class clazz;
    boolean b = false;
    /** check whether we are a macro or a task */
    clazz = getclass(P, s);
    b = issubclass(clazz, org.apache.tools.ant.Task.class);
    return b;
  }

  final static public boolean istaskdef(Project P, String s)
  {
    boolean b = false;
    /** check whether we are a taskdef but not a macrodef  */
    Class C = getclass(P, s);
    boolean b1, b2;
    b1 = issubclass(C, org.apache.tools.ant.taskdefs.MacroInstance.class);
    b2 = issubclass(C, org.apache.tools.ant.Task.class);
    b = b2 && !b1;
    return b;
  }
  
  final static public boolean ismacrodef(Project P, String s)
  {
    boolean b = false;
    /** check whether we are a macrodef */
    Class C = getclass(P, s);
    b = issubclass(C, org.apache.tools.ant.taskdefs.MacroInstance.class);
    return b;
  }

  
  static final public String property(Project P, String property)
  {
    return P.getProperty(property);
  }
  
  static final public Object reference(Project P, String id)
  {
    return P.getReference(id);
  }

  static final public Object target(Project project,String property)
  {
    return project.getTargets().get(property);
  }
 
  static final public AntTypeDefinition taskdef(Project project,String property)
  {
    return istaskdef(project,property) ? compdef(project, property) : null;
  }
  
  static final public AntTypeDefinition macrodef(Project project,String property)
  {
    return ismacrodef(project,property) ? compdef(project, property) : null;
  }
  
  static final public AntTypeDefinition task(Project project,String property)
  {
    return istask(project,property) ? compdef(project, property) : null;
  }
  
  static final public Object type(Project project,String property)
  {
    Object obj = null;
    obj = project.getDataTypeDefinitions().get(property);
    return obj;
  }
  
  static final public Object filter(Project project,String property)
  {
    Object obj = null;
    obj = project.getGlobalFilterSet().getFilterHash().get(property);
    return obj;
  }
  
 
  final static public String patternAsRegex(String glob)
  {
    char c;
    int i, j, n;
    String r;

    i = 0;
    n = glob.length();
    r = "";

    while (i < n)
    {
      c = glob.charAt(i++);
      switch (c)
      {
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
          else
          {
            String s;
            s = substring(glob, i, j);
            s = replace(s, '\\', "\\\\");
            switch (s.charAt(0))
            {
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
  final static protected String escape(String s, char esc)
  {
    String r = "";
    char c;

    for (int i = 0; i < s.length(); ++i)
    {
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
   * @return <code>c</code> if <code>c</code> is alpanumeric character otherwise
   *         return <code>esc</code>.
   */
  final static protected String escape(char c, char esc)
  {
    return "" + (Character.isLetterOrDigit(c) ? c : esc);
  }

  /**
   * replace all <code>c</code> characters in <code>s</code> with string
   * <code>sub</code>.
   * 
   * @param s
   *          not null
   */
  final static protected String replace(String s, char c, String sub)
  {
    char x;
    String r = "";
    for (int i = 0; i < s.length(); ++i)
    {
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
   * <code>i</code>, the empty string is returned. Otherwise function behaves as
   * <code>s.substring(i,j)</code>.
   * 
   * @param s
   *          not null
   */
  final static protected String substring(String s, int i, int J)
  {
    String r = null;
    int L;
    int j = J;
    try
    {
      L = s.length();
      j = (j <= i) ? i : (j > L) ? L : j;
      r = s.substring(i, j);
    } catch (Exception e)
    {
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
  final public static boolean isprintable(char c)
  {
    int type;
    boolean retv;

    retv = false;
    type = Character.getType(c);

    switch (type)
    {
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

  final public static boolean istext(char c)
  {
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

  final public static boolean isbinary(char c)
  {
    return !istext(c);
  }

  final static public String trim3(Project project, String s, String otherwise)
  {
    if (s != null && s.indexOf('#') >= 0)
      s = Static.elresolve(project, s);
    return trim2(s, otherwise);
  }

  /**
   * Trim a given string.
   * 
   * This version of trim ensures that the result value is not an empty string,
   * i.e. a string consisting only of whitespace characters. If an empty string
   * would be returned after trimming down, the alternative
   */

  final static public String trim2(String s, String otherwise)
  {
    String r;
    /* assign default return value */
    r = otherwise;
    if (s != null && s.length() > 0)
    {
      char c1, c2;

      c1 = s.charAt(0);
      c2 = s.charAt(s.length() - 1);
      r = s;
      /* trim if there is something to trim down */
      if (Character.isWhitespace(c1) || Character.isWhitespace(c2))
      {
        r = s.trim();
        /* trimming down may have lead to an empty string */
        if (r.length() <= 0)
          r = otherwise;
      }
    }
    return r;
  }

  final static public boolean empty(String s)
  {
    return s == null ? true : s.length() <= 0;
  }

  final static public boolean isEmpty(String s)
  {
    return (s == null) || s.matches("\\s*");
  }

  final static public String cat(Project P, File file)
  {
    String buf = null;
    if (P != null && file != null)
    {
      buf = Static.readlines(file);
      if (buf != null)
        P.log(buf);
    }
    return buf;
  }

  final static public Document xmldoc(File file) throws Exception
  {
    return Static.getxmldoc(new FileInputStream(file));
  }

  final static public Element loadxml(Project P, File file)
  {
    Element r = null;
    try
    {
      Document d;
      d = xmldoc(file);
      r = d.getDocumentElement();
    } catch (IOException e)
    {
      String path = file.getAbsolutePath();
      if (file.exists() == false)
      {
        Static.debug(P, path + " does not exist.");
        return null;
      }
      if (file.isFile() == false)
      {
        Static.debug(P, path + " not a loc.");
        return null;
      }
      Static.debug(P, path + ": " + e.getMessage());
    } catch (Exception e)
    {
      String path = file.getAbsolutePath();
      String mesg = path + ": " + e.getMessage();
      Static.debug(P, mesg);
      throw new BuildException(mesg);
    }
    return r;
  }

  final static public String stripEmptyLines(String buf)
  {
    final Pattern p = Pattern.compile("\\s*$", Pattern.MULTILINE);
    Matcher m = p.matcher(buf);
    return m.replaceAll("");
  }

  /**
   * Cleans up the empty lines and tab after removing the nodes
   * 
   * @param source
   */
  final protected static void tidyxml(DOMSource source)
  {
    Node current = null;
    String cv = null;
    Node next = null;
    String nv = null;

    Node root = source.getNode();
    NodeList nlist = root.getChildNodes().item(0).getChildNodes();

    int i = 0;
    while (i < nlist.getLength() - 1)
    {
      current = nlist.item(i);
      cv = current.getNodeValue();
      next = nlist.item(i + 1);
      nv = next.getNodeValue();
      if (cv != null && cv.equals("\n\t") && nv != null && nv.equals("\n\t"))
      {
        root.getChildNodes().item(0).removeChild(next);
      } else
      {
        i++;
      }
    }
  }

  final static public String flushxml(Project P, Element root, StringBuilder buf)
  {
    String s = null;
    if (P != null && root != null)
    {
      try
      {
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
      } catch (Exception e)
      {
        s = null;
        Static.debug(P, "error transforming XML document: " + e.getMessage());
      }
    }
    return s;
  }

  final static public String flushxml(Project P, Element root, File file)
  {
    String s = null;
    if (P != null && root != null && file != null)
    {
      s = flushxml(P, root, (StringBuilder) null);

      /* create loc */
      try
      {
        Static.writex(file, s, false);
      } catch (IOException e)
      {
        throw new BuildException(e);
      }
    }
    return s;
  }

  final static public String nodeattribute(Node node, String name, String otherwise)
  {
    NamedNodeMap attributes;

    if (node == null || name == null)
      return otherwise;

    attributes = node.getAttributes();
    if (attributes == null)
      return otherwise;

    for (int i = 0; i < attributes.getLength(); ++i)
    {
      String s;
      Node attr;

      attr = attributes.item(i);
      if (attr == null)
      {
        continue;
      }
      s = attr.getNodeName();
      if (s == null)
      {
        continue;
      }
      if (s.equalsIgnoreCase(name))
      {
        s = attr.getNodeValue();
        if (s != null)
          s = s.trim();
        return s;
      }
    }
    return otherwise;
  }

  final static public Document getxmldoc(InputStream stream) throws Exception
  {
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

  final static public boolean isregexchar(char c)
  {
    return c == '/';
  }

  final static public boolean ispatternchar(char c)
  {
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
   * <li>When character <code>%</code> is used, a pattern expression is assumed</li>
   * <li>When both characters are not equal or, when equal, neither of both
   * characters above, then a pattern expression is assumed.</li>
   * </ul>
   */
  final static public Pattern patterncompile(String S, int f)
  {
    char c1, c2;
    Pattern P = null;
    String s = S;
    int sz = s.length();

    if (sz > 1)
    {
      c1 = s.charAt(0);
      c2 = s.charAt(sz - 1);

      if (c1 == c2)
        if (isregexchar(c1) || ispatternchar(c2))
          s = s.substring(1, sz - 1);
      if (c1 != c2 || ispatternchar(c1))
        s = Static.patternAsRegex(s);
    }
    try
    {
      P = Pattern.compile(s, f);
    } catch (Exception e)
    {
      try
      {
        P = Pattern.compile(Static.patternAsRegex(s), f);
      } catch (Exception ex)
      {
        System.err.println("** exception: " + ex);
      }
    }
    return P;
  }

//  /**
//   * Checks whether EL is enabled on project.
//   * 
//   * To disable EL, property (not reference) "ant.el" must be explicitly set to
//   * <code>false</code>.
//   */
//  final static public boolean shaveEL(Project project)
//  {
//    String p = project.getProperty(Static.EL);
//    return p == null ? true : (p.matches("false") ? false : true);
//  }

  /**
   * Resolve embedded EL references in <code>text</code>.
   * 
   * Text can be an arbitrary text containing one or more references
   * to EL expressions {@code #{..}}. Resolving a reference means 
   * that the embedded EL expression is evaluated into an object and
   * stringized in a second step. 
   */
  final static public String elresolve(Project project, String text)
  {
    EL ctxref = el(project);
    return ctxref == null ? text : ctxref.tostr(text);
  }
  
  /**
   * Evaluate EL expression in a string context.
   * 
   * The given expression is evaluated and in a second step stringized.
   */
  final static public String el2str(Project project, String expr)
  {
    EL ctxref = el(project);
    return ctxref == null ? expr : ctxref.tostr("#{"+expr+"}");
  }

  /**
  * Evaluate EL expression.
  * 
  * The expression given must be a native EL expression not containing any
  * embedded {@code #{}} references. The expression is simply evaluated and
  * not coerced.
   */
  final static public Object el2obj(Project project, String expr)
  {
    EL ctxref = el(project);
    return ctxref == null ? expr : ctxref.toobj("#{"+expr+"}");
  }

  
  final static public File el2file(Project project, String expr)
  {
    EL ctxref = el(project);
    expr = "#{" + (expr == null ? "''" : expr)+"}";
    return ctxref == null ? null : ctxref.tofile(expr);
  }
  
  /**
   * Evaluate a EL expression in a boolean context.
   * 
   * Important: The expr given is evaluated as true EL expression. Thus 
   * it may not contain EL references like {@code #{..}}.
   */
  final static public boolean el2bool(Project project, String expr)
  {
    EL ctxref = el(project);
    // TODO: match expr for 'true/false' if ctxref == null?
    expr = "#{" + (expr == null ? "false" : expr)+"}";
    return ctxref == null ? false : ctxref.tobool(expr);
  }

  /**
   * A function to return a EL context reference (consisting of a context and
   * expression factory) for a given project.
   * 
   * Important: This function is expected to be called only if and only if
   * function <code>haveEL(project)</code> returns true. Otherwise the behaviour
   * is undefined.
   */
  final static private EL el(Project project)
  {
    EL ctxref = null;

    try
    {
      ctxref = (EL) project.getReference(Static.EL);
      if (ctxref == null)
      {
        ctxref = new EL(project);
        project.addReference(Static.EL, ctxref);
        ctxref = (EL) project.getReference(Static.EL);
      }
    } catch (NullPointerException npe)
    {
      System.err.println("internal error, el(null) called.");
    } catch (Exception e)
    {
      error(project, q(Static.EL) + " references unexpected object");
    }
    return ctxref;
  }

  final static public String q(String s)
  {
    if (s != null)
    {
      s = "\"" + s + "\"";
    }
    return s;
  }

  static final public File toFile(Project project, String s)
  {
    File f;

    if (s != null)
      s = s.trim();
    if (s == null || s.matches("\\s*"))
      return project.getBaseDir();

    if (s.matches("\\.\\.?"))
    {
      return new File(s);
    }

    f = new File(s);
    if (f.isAbsolute() == false)
    {
      f = project.getBaseDir();
      f = new File(f, s);
    }
    return f;
  }

  
  @SuppressWarnings("deprecation")
  static final public Project unset(Project project, String... properties)
  {
    Object obj;
    org.apache.tools.ant.PropertyHelper ph;
   
    obj = project.getReference("ant.PropertyHelper");
    ph = (org.apache.tools.ant.PropertyHelper)obj;
    
    /* ID #1, loop over all property handlers when removing
     * a property. 
     */
    while (ph != null)
    {
      for (String name : properties)
      {
        Static.htabremove(ph, "properties", name);
        Static.htabremove(ph, "userProperties", name);
      }
      ph = ph.getNext();
    }
    return project;
  }
  
  static final public int bitset(int bitset,int bit,boolean on)
  {
    if (on) {
      bitset |= bit;
    }
    else {
      bitset &= (~bit);
    }
    return bitset;
  }
  
}
