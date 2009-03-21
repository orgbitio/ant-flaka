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
import java.util.Iterator;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ELResolver;
import javax.el.PropertyNotFoundException;
import javax.el.PropertyNotWritableException;

import net.haefelingerit.flaka.util.Static;

import org.apache.tools.ant.Project;


public class StringELResolver extends ELResolver
{
  protected Project project = null;
  
  public StringELResolver(Project project)
  {
    super();
    this.project = project;
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
 
    
    if (base == null || context == null || property == null || !(base instanceof String))
      return null;

    
    try {
      String k;
      k = (String)property;
      if (k.equals("length") || k.equals("size")) {
        r = new Integer(((String)base).length());
        context.setPropertyResolved(true);
        return r;
      }
      if (k.equals("tolower")) {
        r = ((String)base).toLowerCase();
        context.setPropertyResolved(true);
        return r;
      }
      if (k.equals("toupper")) {
        r = ((String)base).toUpperCase();
        context.setPropertyResolved(true);
        return r;
      }
      if (k.equals("trim")) {
        r = ((String)base).trim();
        context.setPropertyResolved(true);
        return r;
      }
      if (k.equals("tofile")) {
        r = Static.toFile(this.project,(String)base);
        context.setPropertyResolved(true);
        return r;
      }
    }
    catch(Exception e) {
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
