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

package net.haefelingerit.flaka.el;

import java.beans.FeatureDescriptor;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ELResolver;
import javax.el.PropertyNotFoundException;
import javax.el.PropertyNotWritableException;

public class FileELResolver extends ELResolver
{
  public FileELResolver()
  {
    super();
  }

  @Override
  public Class<?> getCommonPropertyType(ELContext context, Object base)
  {
    return Object.class;
  }

  @Override
  public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base)
  {
    return null;
  }

  @Override
  public Class<?> getType(ELContext context, Object base, Object property)
      throws NullPointerException, PropertyNotFoundException, ELException
  {
    return Object.class;
  }

  public Object getValue(ELContext context, Object base, Object property)
      throws NullPointerException, PropertyNotFoundException, ELException
  {
    Object r = null;
    String k = null;
    File b = null;

    if (base == null || context == null || property == null || !(base instanceof File))
      return null;

    try
    {
      k = (String) property;
      b = (File) base;
      if (k.equals("toabs"))
      {
        r = b.getAbsoluteFile();
        context.setPropertyResolved(true);
        return r;
      }
      if (k.equals("parent"))
      {
        r = b.getParentFile();
        context.setPropertyResolved(true);
        return r;
      }
      if (k.equals("exists"))
      {
        r = new Boolean(b.exists());
        context.setPropertyResolved(true);
        return r;
      }
      if (k.equals("isfile"))
      {
        r = new Boolean(b.isFile());
        context.setPropertyResolved(true);
        return r;
      }
      if (k.equals("isdir"))
      {
        r = new Boolean(b.isDirectory());
        context.setPropertyResolved(true);
        return r;
      }
      if (k.equals("ishidden"))
      {
        r = new Boolean(b.isHidden());
        context.setPropertyResolved(true);
        return r;
      }
      if (k.equals("isread"))
      {
        r = new Boolean(b.canRead());
        context.setPropertyResolved(true);
        return r;
      }
      if (k.equals("iswrite"))
      {
        r = new Boolean(b.canWrite());
        context.setPropertyResolved(true);
        return r;
      }
      if (k.equals("delete"))
      {
        r = new Boolean(b.delete());
        context.setPropertyResolved(true);
        return r;
      }
      if (k.equals("mkdir"))
      {
        r = new Boolean(b.mkdirs());
        context.setPropertyResolved(true);
        return r;
      }
      if (k.equals("length") || k.equals("size"))
      {
        r = new Long(b.length());
        context.setPropertyResolved(true);
        return r;
      }
      if (k.equals("list"))
      {
        File[] A = b.listFiles();
        ArrayList L = new ArrayList();
        for (int i = 0; i < A.length; ++i)
          L.add(A[i]);
        context.setPropertyResolved(true);
        return L;
      }
      if (k.equals("mtime"))
      {
        r = new Date(b.lastModified());
        context.setPropertyResolved(true);
        return r;
      }
      if (k.equals("touri"))
      {
        r = b.toURI();
        context.setPropertyResolved(true);
        return r;
      }
      if (k.equals("tourl"))
      {
        r = b.toURI().toURL();
        context.setPropertyResolved(true);
        return r;
      }
      if (k.equals("tostr"))
      {
        r = b.toString();
        context.setPropertyResolved(true);
        return r;
      }
    } catch (Exception e)
    {
      return null;
    }

    return r;
  }

  @Override
  public boolean isReadOnly(ELContext context, Object base, Object property)
      throws NullPointerException, PropertyNotFoundException, ELException
  {
    return true;
  }

  @Override
  public void setValue(ELContext context, Object base, Object property, Object value)
      throws NullPointerException, PropertyNotFoundException, PropertyNotWritableException,
      ELException
  {
    /* we do nothing here */
  }

}
