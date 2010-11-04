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

package it.haefelinger.flaka.el;

import it.haefelinger.flaka.util.Static;

import java.beans.FeatureDescriptor;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ELResolver;
import javax.el.PropertyNotFoundException;
import javax.el.PropertyNotWritableException;

import org.apache.tools.ant.Project;

public class ELProjectResolver extends ELResolver
{
  public ELProjectResolver()
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
    Project b = null;

    if (base == null || context == null || property == null || !(base instanceof Project))
      return null;

    try
    {
      k = (String) property;
      b = (Project) base;
      if (k.equals("targets"))
      {
        List L = new ArrayList();
        Enumeration e = b.getTargets().keys();
        while(e.hasMoreElements()) {
          String s = e.nextElement().toString();
          if (s.equals("") == false)
            L.add(s);
        }
        context.setPropertyResolved(true);
        return L;
      }
      if (k.equals("tasks"))
      {
        List L = new ArrayList();
        Enumeration e = b.getTaskDefinitions().keys();
        while(e.hasMoreElements()) {
          Object o = e.nextElement();
          String s = o.toString();
          if (s.equals("") == false)
            L.add(s);
        }
        context.setPropertyResolved(true);
        return L;
      }
      if (k.equals("taskdefs"))
      {
        List L = new ArrayList();
        Enumeration e = b.getTaskDefinitions().keys();
        while(e.hasMoreElements()) {
          Object K = e.nextElement();
          String s = K.toString();
          if (Static.istaskdef(b,s))
            L.add(s);
        }
        context.setPropertyResolved(true);
        return L;
      }
      if (k.equals("macrodefs"))
      {
        List L = new ArrayList();
        Enumeration e = b.getTaskDefinitions().keys();
        while(e.hasMoreElements()) {
          Object K = e.nextElement();
          String s = K.toString();
          if (Static.ismacrodef(b, s))
            L.add(s);
        }
        context.setPropertyResolved(true);
        return L;
      }
      if (k.equals("basedir"))
      {
        context.setPropertyResolved(true);
        return b.getBaseDir();
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
