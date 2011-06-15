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

import it.haefelinger.flaka.el.Binding;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tools.ant.Project;

/**
 * A class containing functions and variables to be imported into an EL context.
 * 
 * @author geronimo
 * 
 */
public class ELBinding {

  @Binding(type = 2)
  static public Object e() {
    return new Double(Math.E);
  }

  @Binding(type = 2)
  static public Object pi() {
    return new Double(Math.PI);
  }

  @Binding(type = 3)
  static public Method rand() throws SecurityException, NoSuchMethodException {
    return Math.class.getMethod("random");
  }

  @Binding(type = 3)
  static public Method sin() throws SecurityException, NoSuchMethodException {
    return Math.class.getMethod("sin", double.class);
  }

  @Binding(type = 3)
  static public Method cos() throws SecurityException, NoSuchMethodException {
    return Math.class.getMethod("cos", double.class);
  }

  @Binding(type = 3)
  static public Method tan() throws SecurityException, NoSuchMethodException {
    return Math.class.getMethod("tan", double.class);
  }

  @Binding(type = 3)
  static public Method log() throws SecurityException, NoSuchMethodException {
    return Math.class.getMethod("log", double.class);
  }

  @Binding(type = 3)
  static public Method exp() throws SecurityException, NoSuchMethodException {
    return Math.class.getMethod("exp", double.class);
  }

  @Binding(type = 3)
  static public Method abs() throws SecurityException, NoSuchMethodException {
    return Math.class.getMethod("abs", double.class);
  }

  @Binding(type = 3)
  static public Method sqrt() throws SecurityException, NoSuchMethodException {
    return Math.class.getMethod("sqrt", double.class);
  }

  @Binding(type = 3)
  static public Method min() throws SecurityException, NoSuchMethodException {
    return Math.class.getMethod("min", double.class, double.class);
  }

  @Binding(type = 3)
  static public Method max() throws SecurityException, NoSuchMethodException {
    return Math.class.getMethod("max", double.class, double.class);
  }

  @Binding(type = 3)
  static public Method pow() throws SecurityException, NoSuchMethodException {
    return Math.class.getMethod("pow", double.class, double.class);
  }

  /**
   * Quote a EL expression.
   * 
   * A EL string is not allowed to have escaped characters other than
   * <code>\'</code> and <code>\\</code>.
   */
  @Binding(type = 1)
  static public String quote(String s) {
    char c0, c1;
    int i, n, l;
    StringBuilder buf = new StringBuilder();
    for (i = 0, l = s.length(), n = l - 1; i < n; ++i) {
      c0 = s.charAt(i);
      c1 = s.charAt(i + 1);
      switch (c0) {
      case '\\':
        switch (c1) {
        case '\\':
          buf.append(c0);
          i = i + 1;
          break;
        case '\'':
        case '"':
          break;
        default:
          buf.append(c0);
        }
        break;
      }
      buf.append(c0);
    }
    if (i < l) {
      buf.append(s.charAt(n));
    }
    return buf.toString();
  }

  static private File obj2file(Object obj) {
    String s;

    if (obj == null)
      return new File(".");

    if (obj instanceof Project) {
      Project p = (Project) obj;
      File basedir = p.getBaseDir();
      return basedir;
    }

    if (obj instanceof File)
      return (File) obj;

    if (obj instanceof Iterable) {
      Iterator iter = ((Iterable) obj).iterator();
      File f;
      if (iter.hasNext() == false)
        f = new File(".");
      else
        f = obj2file(iter.next());
      while (iter.hasNext()) {
        f = new File(f, obj2file(iter.next()).toString());
      }
      return f;
    }

    if (obj instanceof String)
      s = (String) obj;
    else
      s = obj.toString();

    if (s.matches("\\s*"))
      s = ".";

    return new File(s);
  }

  @Binding(type = 1)
  static public Object file(Object... varg) {
    File f = null;
    switch (varg.length) {
    case 0:
      // When called without argument return the
      f = new File(".");
      break;
    case 1: {
      f = obj2file(varg[0]);
      break;
    }
    default: {
      f = obj2file(varg[0]);
      for (int i = 1; i < varg.length; ++i) {
        f = new File(f, obj2file(varg[i]).toString());
      }
    }
    }
    return f;
  }

  static private Object invoke(Object obj, String method) {
    Object r = null;
    Method m;
    try {
      m = obj.getClass().getMethod(method);
      if (m != null) {
        r = m.invoke(obj, (Object[]) null);
      }
    } catch (Exception e) {
      /* pass */
    }
    return r;
  }

  static private Object getfield(Object obj, String name) {
    Object r = null;
    Field f;
    try {
      f = obj.getClass().getField(name);
      if (f != null) {
        r = f.get(obj);
      }
    } catch (Exception e) {
      /* pass */
    }
    return r;
  }

  @Binding(type = 1)
  static public Object size(Object obj) {
    Object r = null;
    Class C;

    if (obj != null) {
      C = obj.getClass();

      // The size of a primitve object is 0
      if (C.isPrimitive())
        return new Long(0);

      if (C.isArray()) {
        return new Long(Array.getLength(obj));
      }
      if (obj instanceof File) {
        File f = (File) obj;
        if (f.isDirectory()) {
          String[] entries = f.list();
          r = new Long(entries != null ? entries.length : 0);
        } else {
          r = new Long(f.length());
        }
        return r;
      }

      r = invoke(obj, "size");
      if (r != null)
        return r;
      r = invoke(obj, "length");
      if (r != null)
        return r;
      r = getfield(obj, "size");
      if (r != null)
        return r;
      r = getfield(obj, "length");
      if (r != null)
        return r;
    }
    return new Long(0);
  }
  
  @Binding(type = 1)
  static public Boolean nullp(Object obj) {
    return new Boolean(obj == null);
  }

  @Binding(type = 1)
  static public Object concat(Object... argv) {
    Object obj;
    String str;
    StringBuilder buf = new StringBuilder();
    for (int i = 0, n = argv.length; i < n; ++i) {
      obj = argv[i];
      if (obj == null)
        str = null;
      else if (obj instanceof String)
        str = (String) obj;
      else
        str = obj.toString();
      if (str == null)
        str = "";
      buf.append(str);
    }
    return buf.toString();
  }

  /**
   * This function returns a list containing all the elements.
   * 
   */
  @Binding(type = 1)
  static public Object append(Object... argv) {
    int i, n;
    List L;

    L = new ArrayList();
    n = argv.length;

    for (i = 0, n = argv.length; i < n; ++i) {
      Object obj = argv[i];
      if (obj instanceof Iterable) {
        Iterator I = ((Iterable) obj).iterator();
        while (I.hasNext()) {
          L.add(I.next());
        }
      } else {
        /* null is like the empty list */
        if (obj != null)
          L.add(obj);
      }
    }
    return L;
  }

  @Binding(type = 1)
  static public List list(Object... args) {
    List list = new ArrayList();
    for (int i = 0, n = args.length; i < n; ++i)
      list.add(args[i]);
    return list;
  }

  @Binding(type = 1)
  static public List split_ws(Object s) {
    return split(s, "\\s+");
  }

  @Binding(type = 1)
  static public List split(Object... args) {
    String regex;
    List list = new ArrayList();

    if (args.length < 1)
      return list;

    if (args.length > 1)
      regex = args[1].toString();
    else
      regex = "\\s*,\\s*";

    String[] arr;
    String s = args[0].toString();
    arr = s.split(regex);
    list = Arrays.asList(arr);
    return list;
  }

  @Binding(type = 1)
  static public String replace(Object... args) {
    String r, src, regex, subst;

    if (args.length < 1)
      return null;

    if (args.length < 2)
      return args[0].toString();

    src = args[0].toString();
    subst = args[1].toString();
    regex = "\\s*,\\s*";

    if (args.length > 2)
      regex = args[2].toString();

    if (src == null || regex == null || subst == null)
      return null;
    try {
      r = src.replaceAll(regex, subst);
    } catch (Exception e) {
      r = null;
    }
    return r;
  }

  @Binding(type = 1)
  static public String trim(Object s) {
    return replace(s, "", "^\\s*|\\s*$");
  }

  @Binding(type = 1)
  static public String ltrim(Object s) {
    return replace(s, "", "^\\s*");
  }

  @Binding(type = 1)
  static public String rtrim(Object s) {
    return replace(s, "", "\\s*$");
  }

  @Binding(type = 1)
  static public String format(String f, Object... args) {
    String r;
    r = String.format(f, args);
    return r;
  }

  @Binding(type = 1)
  static public String join(String f, Object... args) {
    String r;
    switch (args.length) {
    case 0:
      r = "";
      break;
    case 1: {
      Object arg0 = args[0];
      if (arg0 instanceof Iterable) {
        Iterator i = ((Iterable) arg0).iterator();
        r = "";
        if (i.hasNext()) {
          r = i.next().toString();
        }
        while (i.hasNext()) {
          r = r + f + i.next();
        }
      } else {
        r = arg0.toString();
      }
      break;
    }
    default:
      // if there are more than one vararg, treat them as list.
      r = args[0].toString();
      for (int i = 1; i < args.length; ++i) {
        r = r + f + args[i].toString();
      }
    }
    return r;
  }

  @Binding(type = 1)
  static public String nativetype(Object object) {
    if (object == null) {
      return "";
    }
    Class clazz = object.getClass();
    return clazz.getName();
  }

  @Binding(type = 1)
  static public String typeof(Object object) {
    if (object == null)
      return "null";

    if (object instanceof String)
      return "string";

    if (object instanceof File)
      return "file";

    if (object.getClass().isArray())
      return "list";

    if (object instanceof Iterable)
      return "list";

    if (Integer.class.isAssignableFrom(object.getClass()))
      return "integer";

    if (Long.class.isAssignableFrom(object.getClass()))
      return "integer";

    if (Double.class.isAssignableFrom(object.getClass()))
      return "float";

    if (Boolean.class.isAssignableFrom(object.getClass()))
      return "boolean";

    if (object instanceof Map)
      return "map";

    if (object instanceof Project)
      return "project";

    return "object";
  }

  public static String stringize(Object obj) {
    return obj == null ? "" : obj.toString();
  }

  /**
   * A simple text matching function based on regular expressions.
   * 
   * Both arguments are stringized into textual arguments. The first argument is
   * the text to be matched against the second, assumed to be a valid regular
   * expression.
   * 
   * This function returns true if the regular expression matches either the
   * full input text or only a fraction. This is contrary to Java's behaviour
   * where {@code matches} works on the entire input sequence only.
   * 
   * Notice that this function will not set a matcher object as side effect.
   * 
   * @param text
   *          can be null
   * @param regex
   *          can be null
   */
  @Binding(type = 1)
  public static boolean matches(Object text, Object regex) {
    boolean ret = false;
    try {
      Pattern pat;
      Matcher mat;
      pat = Pattern.compile(stringize(regex));
      mat = pat.matcher(stringize(text));
      ret = mat.find();
    } catch (Exception e) {
      // TODO: log warning
      ret = false;
    }
    return ret;
  }

  @Binding(type = 1)
  public static boolean glob(Object text, Object glob) {
    boolean ret = false;
    try {
      Pattern pat;
      Matcher mat;
      String rex;
      rex = Static.patternAsRegex(stringize(glob));
      pat = Pattern.compile(rex);
      mat = pat.matcher(stringize(text));
      ret = mat.matches();
    } catch (Exception e) {
      // TODO: log warning although this can't happen.
      ret = false;
    }
    return ret;
  }
  
  public static Object otherwise(Object... arg) {
    return "";
  }
}