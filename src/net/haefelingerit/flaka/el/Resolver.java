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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.el.ArrayELResolver;
import javax.el.BeanELResolver;
import javax.el.CompositeELResolver;
import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ELResolver;
import javax.el.ListELResolver;
import javax.el.MapELResolver;
import javax.el.PropertyNotFoundException;
import javax.el.PropertyNotWritableException;
import javax.el.ResourceBundleELResolver;

import net.haefelingerit.flaka.util.Static;

import org.apache.tools.ant.AntTypeDefinition;
import org.apache.tools.ant.ComponentHelper;
import org.apache.tools.ant.Project;

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
public class Resolver extends ELResolver
{

  /* A map for top level objects */
  private final Map<String, Object> map;

  static public ELResolver makeResolver(Project project)
  {
    CompositeELResolver cr = new CompositeELResolver();

    cr.add(new StringELResolver(project));
    cr.add(new FileELResolver());
    cr.add(new AntELResolver());
    cr.add(new ArrayELResolver(false));
    cr.add(new ListELResolver(false));
    cr.add(new MapELResolver(false));
    cr.add(new ResourceBundleELResolver());
    cr.add(new BeanELResolver(false));
    return cr;
  }

  static public interface Wrapper
  {
    static final int HAVE = 0;
    static final int REFERENCE = 1;
    static final int PROPERTY = 2;
    static final int TASKDEF = 3;
    static final int MACRODEF = 4;
    static final int TASK = 5;
    static final int TYPE = 6;
    static final int FILTER = 7;
    static final int TARGET = 8;

    public Object lookup(ELContext context, String property);
  }

  static public class Have implements Wrapper
  {
    protected Project project;
    protected String what;

    public Have(Project project, String property)
    {
      this.project = project;
      this.what = property;
    }

    public Object lookup(ELContext context, String property)
    {
      if (property == null)
      {
        context.setPropertyResolved(true);
        return this;
      }
      if (this.what.matches("reference|var"))
      {
        context.setPropertyResolved(true);
        return this.project.getReference(property) == null ? Boolean.FALSE : Boolean.TRUE;
      }
      if (this.what.matches("property"))
      {
        context.setPropertyResolved(true);
        return this.project.getProperty(property) == null ? Boolean.FALSE : Boolean.TRUE;
      }
      if (this.what.matches("target"))
      {
        context.setPropertyResolved(true);
        return this.project.getTargets().contains(property) ? Boolean.FALSE : Boolean.TRUE;
      }
      if (this.what.matches("task"))
      {
        context.setPropertyResolved(true);
        return this.project.getTaskDefinitions().contains(property) ? Boolean.FALSE : Boolean.TRUE;
      }
      return Boolean.FALSE;
    }
  }

  static public class ProjectWrapper implements Wrapper
  {
    private final static Class CLASS_TASK = org.apache.tools.ant.Task.class;
    private final static Class CLASS_MACRO = org.apache.tools.ant.taskdefs.MacroInstance.class;

    protected Project project;
    protected int what;

    @SuppressWarnings("unused")
    private ProjectWrapper()
    {
      /* not allowed */
    }

    public ProjectWrapper(Project project, int what)
    {
      this.project = project;
      this.what = what;
    }

    public Object lookup(ELContext context, String property)
    {
      Object r = null;
      switch (this.what)
      {
        case Wrapper.HAVE:
        {
          context.setPropertyResolved(true);
          r = new Have(this.project, property);
          break;
        }
        case Wrapper.PROPERTY:
        {
          context.setPropertyResolved(true);
          r = this.project.getProperty(property);
          break;
        }
        case Wrapper.REFERENCE:
        {
          context.setPropertyResolved(true);
          r = this.project.getReference(property);
          break;
        }
        case Wrapper.TARGET:
        {
          context.setPropertyResolved(true);
          r = this.project.getTargets().get(property);
          break;
        }
        case Wrapper.TASKDEF:
        {
          context.setPropertyResolved(true);
          ComponentHelper ch = Static.getcomph(this.project);
          AntTypeDefinition atd = ch.getDefinition(property);
          if (atd != null)
          {
            Class C;
            C = atd.getExposedClass(this.project);
            r = Static.issubclass(C, CLASS_TASK) ? atd : null;
          }
          break;
        }
        case Wrapper.MACRODEF:
        {
          context.setPropertyResolved(true);
          ComponentHelper ch = Static.getcomph(this.project);
          AntTypeDefinition atd = ch.getDefinition(property);
          if (atd != null)
          {
            Class C;
            C = atd.getExposedClass(this.project);
            r = Static.issubclass(C, CLASS_MACRO) ? atd : null;
          }
          break;
        }
        case Wrapper.TASK:
        {
          context.setPropertyResolved(true);
          ComponentHelper ch = Static.getcomph(this.project);
          r = ch.getDefinition(property);
          break;
        }
        case Wrapper.TYPE:
        {
          context.setPropertyResolved(true);
          r = this.project.getDataTypeDefinitions().get(property);
          break;
        }
        case Wrapper.FILTER:
        {
          context.setPropertyResolved(true);
          r = this.project.getGlobalFilterSet().getFilterHash().get(property);
          break;
        }
      }
      return r;
    }

    public int size()
    {
      int size = 0;
      switch (this.what)
      {
        case Wrapper.PROPERTY:
        {
          size = this.project.getProperties().size();
          break;
        }
        case Wrapper.REFERENCE:
        {
          size = this.project.getReferences().size();
          break;
        }
        case Wrapper.TARGET:
        {
          size = this.project.getTargets().size();
          break;
        }
        case Wrapper.TASKDEF:
        case Wrapper.MACRODEF:
        case Wrapper.TASK:
        {
          ComponentHelper ch = Static.getcomph(this.project);
          size = ch.getTaskDefinitions().size();
          break;
        }
        case Wrapper.TYPE:
        {
          size = this.project.getDataTypeDefinitions().size();
          break;
        }
        case Wrapper.FILTER:
        {
          size = this.project.getGlobalFilterSet().getFilterHash().size();
          break;
        }
      }
      return size;
    }

    public String toString()
    {
      String s = null;
      switch (this.what)
      {
        case Wrapper.PROPERTY:
        {
          s = this.project.getProperties().toString();
          break;
        }
        case Wrapper.REFERENCE:
        {
          s = this.project.getReferences().toString();
          break;
        }
        case Wrapper.TARGET:
        {
          s = this.project.getTargets().toString();
          break;
        }
        case Wrapper.TASKDEF:
        case Wrapper.MACRODEF:
        case Wrapper.TASK:
        {
          ComponentHelper ch = Static.getcomph(this.project);
          s = ch.getTaskDefinitions().toString();
          break;
        }
        case Wrapper.TYPE:
        {
          s = this.project.getDataTypeDefinitions().toString();
          break;
        }
        case Wrapper.FILTER:
        {
          s = this.project.getGlobalFilterSet().getFilterHash().toString();
          break;
        }
      }
      return s;
    }
  }

  protected final ELResolver delegate;
  protected Project project = null;
  protected boolean debug = false;

  public Resolver(Project project)
  {
    this.delegate = makeResolver(project);
    this.project = project;
    this.map = makemap();
    this.map.put("project", project);
    this.map.put("has", new ProjectWrapper(project, Wrapper.HAVE));
    this.map.put("property", new ProjectWrapper(project, Wrapper.PROPERTY));
    this.map.put("reference", new ProjectWrapper(project, Wrapper.REFERENCE));
    this.map.put("var", new ProjectWrapper(project, Wrapper.REFERENCE));
    this.map.put("target", new ProjectWrapper(project, Wrapper.TARGET));
    this.map.put("taskdef", new ProjectWrapper(project, Wrapper.TASKDEF));
    this.map.put("macrodef", new ProjectWrapper(project, Wrapper.MACRODEF));
    this.map.put("task", new ProjectWrapper(project, Wrapper.TASK));
    this.map.put("type", new ProjectWrapper(project, Wrapper.TYPE));
    this.map.put("filter", new ProjectWrapper(project, Wrapper.FILTER));
  }

  public Resolver setDebug(boolean b)
  {
    this.debug = b;
    return this;
  }

  public boolean getDebug()
  {
    return this.debug;
  }

  protected Map<String, Object> makemap()
  {
    HashMap<String, Object> map;
    map = new HashMap<String, Object>();
    return Collections.synchronizedMap(map);
  }

  private boolean resolve(ELContext context, Object base)
  {
    context.setPropertyResolved(base == null);
    return context.isPropertyResolved();
  }

  private Object get(String property)
  {
    Object obj = null;
    if (this.map.containsKey(property))
      obj = this.map.get(property);
    if (obj == null)
      obj = this.project.getReference(property);
    return obj;
  }

  @Override
  public Class<?> getCommonPropertyType(ELContext context, Object base)
  {
    boolean b = resolve(context, base);
    return b ? Object.class : this.delegate.getCommonPropertyType(context, base);
  }

  @Override
  public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base)
  {
    boolean b = resolve(context, base);
    return b ? null : this.delegate.getFeatureDescriptors(context, base);
  }

  @Override
  public Class<?> getType(ELContext context, Object base, Object property)
      throws NullPointerException, PropertyNotFoundException, ELException
  {
    boolean b = resolve(context, base);
    return b ? Object.class : this.delegate.getType(context, base, property);
  }

  /**
   * Resolve property on base as 'rvalue'.
   * 
   * It is from extreme importance that no exception is thrown here. Rather null
   * shall be reported back (and a additional log message might be created).
   * 
   * Otherwise if a exception is thrown here, evaluation of a larger context
   * will be aborted. Take for example a context where three references like
   * 
   * <pre>
   * My name is #{..} is one of the #{..} recognizable opening #{..} in American
   * </pre>
   * 
   * Clearly, we want to have as much references as possible evaluated. If one
   * reference fails, we rather accept that there is something missing than
   * giving up on all.
   * 
   * Giving up on all references is exactly what will happen if a exception will
   * be thrown by this method. Therefore make sure that everything is wrapped by
   * a well defined exception handler.
   * 
   * */
  public Object getValue(ELContext context, Object base, Object property)
      throws NullPointerException, PropertyNotFoundException, ELException
  {
    Object obj = null;
    
    try 
    {
      /*
       * If base is null, let other resolvers handle the issue. This resolver
       * shall handle top level objects. Top level objects are: a. The implict
       * object 'project'. b. All references defined within project.
       */
    
      if (base == null)
      {
        String key;
        key = property.toString();
        obj = get(key);
      } 
      else
      {
        obj = this.delegate.getValue(context, base, property);
      }
      if (obj == null && this.debug)
      {
        String p = Static.q(property.toString());
        String b = base == null ? "{}" : Static.q(base.toString());
        error("unable to resolve property " + p + " on base "+b);
      }

    } 
    catch(PropertyNotFoundException pne) {
      /* we silently ignore this common exception */
    }
    catch (Exception e)
    {
      if (this.debug) {
        String p = Static.q(property.toString());
        String b = base == null ? "{}" : Static.q(base.toString());
        Static.debug(this.project,"error while evaluating "+p+" on base "+b,e);
      }
    }
    finally {
      context.setPropertyResolved(true);
    }
    return obj;
  }

  private void error(String s)
  {
    this.project.log("error: " + s, Project.MSG_ERR);
  }

  @Override
  public boolean isReadOnly(ELContext context, Object base, Object property)
      throws NullPointerException, PropertyNotFoundException, ELException
  {
    boolean b = resolve(context, base);
    return b ? false : this.delegate.isReadOnly(context, base, property);
  }

  @Override
  public void setValue(ELContext context, Object base, Object property, Object value)
      throws NullPointerException, PropertyNotFoundException, PropertyNotWritableException,
      ELException
  {
    if (resolve(context, base))
    {
      this.map.put(property.toString(), value);
    } else
    {
      this.delegate.setValue(context, base, property, value);
    }
  }

}
