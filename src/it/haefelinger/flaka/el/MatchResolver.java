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


import java.beans.FeatureDescriptor;
import java.util.Iterator;
import java.util.regex.Matcher;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ELResolver;
import javax.el.PropertyNotFoundException;
import javax.el.PropertyNotWritableException;


import org.apache.tools.ant.Project;

public class MatchResolver extends ELResolver
{
  protected Project project = null;

  public MatchResolver(Project project)
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

  final private int toIndex(Object property) {
    int index = 0;
    if (property instanceof Number) {
      index = ((Number) property).intValue();
    } else if (property instanceof String) {
      try {
        index = Integer.valueOf((String) property).intValue();
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("Cannot parse array index: " + property);
      }
    } else if (property instanceof Character) {
      index = ((Character) property).charValue();
    } else if (property instanceof Boolean) {
      index = ((Boolean) property).booleanValue() ? 1 : 0;
    } else {
      throw new IllegalArgumentException("Cannot coerce property to array index: " + property);
    }
    return index;
  }
  
  
  public Object getValue(ELContext context, Object base, Object property)
      throws NullPointerException, PropertyNotFoundException, ELException
  {
    int idx;
    Object obj;
    Matcher bean;
    
    if (base == null || context == null || property == null || !(base instanceof MatcherBean))
      return null;

    try 
    {
      idx = toIndex(property);
    }
    catch (Exception e)
    {
      /* Unable to  convert to numeric value, give up on numerics. However,
       * do not set property as resolved to give further resolvers a chance
       * to handle.
       */
      return null;
    }
    
    obj = null;
    bean = ((MatcherBean)base).getMatcher();
    
    if (idx >= 0 && idx <= bean.groupCount())
    {
      obj = new MatcherBean(bean,idx);
      context.setPropertyResolved(true);
    } 
    return obj;
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
