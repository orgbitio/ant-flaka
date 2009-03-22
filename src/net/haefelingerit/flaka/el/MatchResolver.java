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
import java.util.regex.Matcher;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ELResolver;
import javax.el.PropertyNotFoundException;
import javax.el.PropertyNotWritableException;

import net.haefelingerit.flaka.Switch.MatcherBean;

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
    
    if (base == null || context == null || property == null || !(base instanceof MatcherBean))
      return null;

    obj = null;
    try 
    {
      idx = toIndex(property);
      Matcher bean = ((MatcherBean)base).getBean();
      if (idx >= 0 && idx <= bean.groupCount())
        obj = new Group((MatcherBean)base,idx);
      context.setPropertyResolved(true);
    } catch (Exception e)
    {
      /* ignore me */
    }
    return obj;
  }

  final static public  class Group {
    final int index;
    final Matcher matcher;
    
    public Group(MatcherBean matcher,int index) {
      this.index = index;
      this.matcher = matcher.getBean();
    }
    
    public String getGroup() {
      return this.matcher.group(this.index);
    }
    public String getG() {
      return this.matcher.group(this.index);
    }
    public int getS() {
      return this.matcher.start(this.index);
    }
    public int getE() {
      return this.matcher.end(this.index);
    }
    public String toString() {
      return this.matcher.group(this.index);
    }
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
