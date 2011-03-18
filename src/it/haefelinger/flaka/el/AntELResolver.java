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

import javax.el.BeanELResolver;
import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ELResolver;
import javax.el.PropertyNotFoundException;
import javax.el.PropertyNotWritableException;

/**
 * The main resolver.
 * 
 * This resolver handles top-level entities (like the implicit object
 * <code>project</code>). If the <code>base</code> is not null however, then
 * resolution will be delegated to an underlying composite resolver.
 * 
 * A special resolver exists which handles the resolution on properties on a
 * object of Ant related entities like Project, Task, Target etc. See .
 * 
 * @author geronimo
 * 
 */
public class AntELResolver extends ELResolver {
  protected ELResolver resolver = new BeanELResolver(false);

  public AntELResolver() {
    super();
  }

  @Override
  public Class<?> getCommonPropertyType(ELContext context, Object base) {
    return Object.class;
  }

  @Override
  public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context,
      Object base) {
    return null;
  }

  @Override
  public Class<?> getType(ELContext context, Object base, Object property)
      throws NullPointerException, PropertyNotFoundException, ELException {
    return Object.class;
  }

  public Object getValue(ELContext context, Object base, Object property)
      throws NullPointerException, PropertyNotFoundException, ELException {
    Object r = null;
    String k = null;

    if (base == null || context == null || property == null)
      return null;

    try {
      k = (String) property;
    } catch (Exception e) {
      return null;
    }

    if (base instanceof Resolver.Wrapper) {
      return ((Resolver.Wrapper) base).lookup(context, k);
    }
    return r;
  }

  @Override
  public boolean isReadOnly(ELContext context, Object base, Object property)
      throws NullPointerException, PropertyNotFoundException, ELException {
    return true;
  }

  @Override
  public void setValue(ELContext context, Object base, Object property,
      Object value) throws NullPointerException, PropertyNotFoundException,
      PropertyNotWritableException, ELException {
    /* we do nothing here */
  }

}
