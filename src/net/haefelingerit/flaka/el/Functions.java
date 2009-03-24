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

/**
 * 
 */
package net.haefelingerit.flaka.el;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Functions
{
  /**
   * Quote a EL expression.
   * 
   * A EL string is not allowed to have escaped characters other than <code>\'</code>
   * and <code>\\</code>.
   */
  static public String quote(String s)
  {
    char c0, c1;
    int i, n, l;
    StringBuilder buf = new StringBuilder();
    for (i = 0, l = s.length(), n = l - 1; i < n; ++i)
    {
      c0 = s.charAt(i);
      c1 = s.charAt(i + 1);
      switch (c0)
      {
        case '\\':
          switch (c1)
          {
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
    if (i < l)
    {
      buf.append(s.charAt(n));
    }
    return buf.toString();
  }

  static public File file(Object obj)
  {
    String s;

    if (obj == null)
      return null;

    if (obj instanceof File)
      return (File) obj;

    s = (obj instanceof String ? (String) obj : obj.toString());

    /* TODO: must be relative to project */
    if (s.matches("\\s*"))
      s = ".";
    return new File(s);
  }

  static public Object size(Object obj)
  {
    Object r = null;
    Class C = null;
    /* check if obj has method size */
    if (obj == null)
      return null;

    C = obj.getClass();
    if (C.isArray())
    {
      return new Integer(Array.getLength(obj));
    }
    if (C.isPrimitive())
      return null;

    try
    {
      Method m = null;
      m = obj.getClass().getMethod("size");
      if (m != null)
      {
        r = m.invoke(obj, (Object[]) null);
        if (r != null)
          return r;
      }
    } catch (Exception e)
    {
      r = null;
    }
    try
    {
      Field f = null;
      f = obj.getClass().getField("length");
      if (f != null)
      {
        r = f.get(obj);
        if (r != null)
          return r;
      }
    } catch (Exception e)
    {
      r = null;
    }

    try
    {
      Field f = null;
      f = obj.getClass().getField("size");
      if (f != null)
      {
        r = f.get(obj);
        if (r != null)
          return r;
      }
    } catch (Exception e)
    {
      r = null;
    }

    return r;
  }

  static public Boolean isnil(Object obj)
  {
    return new Boolean(obj == null);
  }

  static public Object concat(Object... argv)
  {
    StringBuilder buf = new StringBuilder();
    for (int i = 0, n = argv.length; i < n; ++i)
    {
      Object obj = argv[i];
      buf.append(obj instanceof String ? (String) obj : obj.toString());
    }
    return buf.toString();
  }

  /**
   * This function returns a list containing all the elements.
   * 
   */
  static public Object append(Object... argv)
  {
    int i, n;
    List L;

    L = new ArrayList();
    n = argv.length;

    for (i = 0, n = argv.length; i < n; ++i)
    {
      Object obj = argv[i];
      if (obj instanceof Iterable)
      {
        Iterator I = ((Iterable) obj).iterator();
        while (I.hasNext())
        {
          L.add(I.next());
        }
      } else
      {
        /* null is like the empty list */
        if (obj != null)
          L.add(obj);
      }
    }
    return L;
  }

  static public List list(Object... args)
  {
    List list = new ArrayList();
    for (int i = 0, n = args.length; i < n; ++i)
      list.add(args[i]);
    return list;
  }

  static public String typeof(Object object)
  {
    if (object == null)
      return "null";

    if (object instanceof String)
      return "string";

    if (object instanceof File)
      return "file";

    if (object.getClass().isArray())
      return "array";

    if (object instanceof Iterable)
      return "list";

    if (Integer.class.isAssignableFrom(object.getClass()))
      return "integer";

    if (Double.class.isAssignableFrom(object.getClass()))
      return "float";

    if (Boolean.class.isAssignableFrom(object.getClass()))
      return "boolean";

    return "object";
  }

}