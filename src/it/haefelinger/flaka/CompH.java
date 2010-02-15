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

package it.haefelinger.flaka;

import it.haefelinger.flaka.util.Static;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Hashtable;


import org.apache.tools.ant.AntTypeDefinition;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.ComponentHelper;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.TaskAdapter;
import org.apache.tools.ant.UnknownElement;

/**
 * This whole class just exists to get rid of some anoying warnings * issued by
 * Ant. Here's an example situation: * * <code>
 **  <presetdef name="javac"> <javac debug="on" /> </presetdef>
 ** </code> Executing this command would
 * issue a warning like * * <code>
 ** Overriding previous definition of type javac
 ** </code> Unfortunatly there is no easy way to
 * remove such warnings. Looking * how this is implemented, it appears that
 * method * * <code>
 ** ComponentHelper.updateDataTypeDefinition()
 ** </code> is the guilty part. It is rather easy to exchange this
 * faulty * ComponentHelper instance by a customized on. All that needs to be *
 * done is to save a reference to a customized instance via property * name
 * "ant.ComponentHelper". Unfortunatly our faulty method is made * private. So
 * how can we change the behaviour? This is how it's done: * * 1. Make a new
 * class using ComponentHelper as base class. * 2. The new class contains a
 * pointer to the original instance (dlgt) * 3. Delegate all public methods to
 * dlgt except those public methods * (indirectly) call our faulty method. * 4.
 * Implement our faulty method again without issuing a warning. * 5. Implement
 * non-delegation public methods in such a way that they * are using my improved
 * method. * * Steps 4. and 5. are problematic of course and are using Java's *
 * reflection API to access private methods and fields. While this * works with
 * Ant 1.6.3 - 1.6.5 it may not work with newer versions * of Ant. * * There are
 * other approaches to solve the problem: * a.) override the setProject() method
 * and wrap the project's instance * with a wrapper overriding Project.log().
 * Since there's no * factory methods for Projects a delegation technique
 * similar to * the one used in this class needs to be applied. * b.) what was
 * the other approach again?? * * *
 * 
 * 
 * @author merzedes
 * @since 1.0
 */

public class CompH extends ComponentHelper
{
  protected static Field project = null;
  protected static Field rebuildTaskClassDefinitions = null;
  protected static Field rebuildTypeClassDefinitions = null;
  protected static Method invalidateCreatedTasks = null;
  protected static Method sameDefinition = null;
  protected static boolean applicable = false;

  static
  {
    try
    {
      Class C = ComponentHelper.class;
      Class rtype;
      /* check whether field "project" exists .. */
      project = Static.fieldbyname(C, "project");
      rebuildTaskClassDefinitions = Static.fieldbyname(C, "rebuildTaskClassDefinitions");
      rebuildTypeClassDefinitions = Static.fieldbyname(C, "rebuildTypeClassDefinitions");
      invalidateCreatedTasks = Static.methodbyname(
        C, "invalidateCreatedTasks", new Class[] { String.class });
      sameDefinition = Static.methodbyname(C, "sameDefinition", new Class[] {
          AntTypeDefinition.class, AntTypeDefinition.class });

      /* tweak permissions */
      project.setAccessible(true);
      rebuildTaskClassDefinitions.setAccessible(true);
      rebuildTypeClassDefinitions.setAccessible(true);
      invalidateCreatedTasks.setAccessible(true);
      sameDefinition.setAccessible(true);

      /* check return type of `sameDefinition' .. */
      rtype = sameDefinition.getReturnType();
      if (!rtype.getName().equals("boolean"))
      {
        throw new Exception("sameDefinition() does not return `boolean' ..");
      }

      /* all right then .. */
      applicable = true;
    } catch (Exception e)
    { /* ignored */
    }
  }

  protected ComponentHelper dlgt;

  /**
   * Set the next chained component helper.
   * 
   * @param next
   *          the next chained component helper.
   */
  public void setNext(ComponentHelper next)
  {
    super.setNext(next);
    this.dlgt = super.getNext();
  }

  /**
   * Get the next chained component helper.
   * 
   * @return the next chained component helper.
   */
  public ComponentHelper getNext()
  {
    return this.dlgt;
  }

  /**
   * Sets the project for this component helper.
   * 
   * @param project
   *          the project for this helper.
   */
  public void setProject(Project project)
  {
    this.dlgt.setProject(project);
  }

  /**
   * Used with creating child projects. Each child project inherits the
   * component definitions from its parent.
   * 
   * @param helper
   *          the component helper of the parent project.
   */
  public void initSubProject(ComponentHelper helper)
  {
    this.dlgt.initSubProject(helper);
  }

  /**
   * Factory method to create the components.
   * 
   * This should be called by UnknownElement.
   * 
   * @param ue
   *          The Unknown Element creating this component.
   * @param ns
   *          Namespace URI. Also available as ue.getNamespace().
   * @param componentType
   *          The component type, Also available as ue.getComponentName().
   * @return the created component.
   * @throws BuildException
   *           if an error occurs.
   */
  public Object createComponent(UnknownElement ue, String ns, String componentType)
      throws BuildException
  {
    return this.dlgt.createComponent(ue, ns, componentType);
  }

  /**
   * Create an object for a component.
   * 
   * @param componentName
   *          the name of the component, if the component is in a namespace, the
   *          name is prefixed with the namespace uri and ":".
   * @return the class if found or null if not.
   */
  public Object createComponent(String componentName)
  {
    return this.dlgt.createComponent(componentName);
  }

  /**
   * Return the class of the component name.
   * 
   * @param componentName
   *          the name of the component, if the component is in a namespace, the
   *          name is prefixed with the namespace uri and ":".
   * @return the class if found or null if not.
   */
  public Class getComponentClass(String componentName)
  {
    return this.dlgt.getComponentClass(componentName);
  }

  /**
   * Return the antTypeDefinition for a componentName.
   * 
   * @param componentName
   *          the name of the component.
   * @return the ant definition or null if not present.
   */
  public AntTypeDefinition getDefinition(String componentName)
  {
    return this.dlgt.getDefinition(componentName);
  }

  /**
   * This method is initialization code implementing the original ant component
   * loading from /org/apache/tools/ant/taskdefs/default.properties and
   * /org/apache/tools/ant/types/default.properties.
   */
  public void initDefaultDefinitions()
  {
    this.dlgt.initDefaultDefinitions();
  }

  /**
   * Checks whether or not a class is suitable for serving as Ant task. Ant task
   * implementation classes must be public, concrete, and have a no-arg
   * constructor.
   * 
   * @param taskClass
   *          The class to be checked. Must not be <code>null</code>.
   * 
   * @exception BuildException
   *              if the class is unsuitable for being an Ant task. An error
   *              level message is logged before this exception is thrown.
   */
  public void checkTaskClass(final Class taskClass) throws BuildException
  {
    this.dlgt.checkTaskClass(taskClass);
  }

  /**
   * Returns the current task definition hashtable. The returned hashtable is
   * "live" and so should not be modified.
   * 
   * @return a map of from task name to implementing class (String to Class).
   */
  public Hashtable getTaskDefinitions()
  {
    return this.dlgt.getTaskDefinitions();
  }

  /**
   * Returns the current type definition hashtable. The returned hashtable is
   * "live" and so should not be modified.
   * 
   * @return a map of from type name to implementing class (String to Class).
   */
  public Hashtable getDataTypeDefinitions()
  {
    return this.dlgt.getDataTypeDefinitions();
  }

  /**
   * Returns the current datatype definition hashtable. The returned hashtable
   * is "live" and so should not be modified.
   * 
   * @return a map of from datatype name to implementing class (String to
   *         Class).
   */
  public Hashtable getAntTypeTable()
  {
    return this.dlgt.getAntTypeTable();
  }

  /**
   * Creates a new instance of a task, adding it to a list of created tasks for
   * later invalidation. This causes all tasks to be remembered until the
   * containing project is removed
   * 
   * Called from Project.createTask(), which can be called by tasks. The method
   * should be deprecated, as it doesn't support ns and libs.
   * 
   * @param taskType
   *          The name of the task to create an instance of. Must not be
   *          <code>null</code>.
   * 
   * @return an instance of the specified task, or <code>null</code> if the task
   *         name is not recognised.
   * 
   * @exception BuildException
   *              if the task name is recognised but task creation fails.
   */
  public Task createTask(String taskType) throws BuildException
  {
    return this.dlgt.createTask(taskType);
  }

  /**
   * Creates a new instance of a data type.
   * 
   * @param typeName
   *          The name of the data type to create an instance of. Must not be
   *          <code>null</code>.
   * 
   * @return an instance of the specified data type, or <code>null</code> if the
   *         data type name is not recognised.
   * 
   * @exception BuildException
   *              if the data type name is recognised but instance creation
   *              fails.
   */
  public Object createDataType(String typeName) throws BuildException
  {
    return this.dlgt.createDataType(typeName);
  }

  /**
   * Returns a description of the type of the given element.
   * <p>
   * This is useful for logging purposes.
   * 
   * @param element
   *          The element to describe. Must not be <code>null</code>.
   * 
   * @return a description of the element type.
   * 
   * @since Ant 1.6
   */
  public String getElementName(Object element)
  {
    return this.dlgt.getElementName(element);
  }

  /**
   * Called at the start of processing an antlib.
   * 
   * @param uri
   *          the uri that is associated with this antlib.
   */
  public void enterAntLib(String uri)
  {
    this.dlgt.enterAntLib(uri);
  }

  /**
   * @return the current antlib uri.
   */
  public String getCurrentAntlibUri()
  {
    return this.dlgt.getCurrentAntlibUri();
  }

  /**
   * Called at the end of processing an antlib.
   */
  public void exitAntLib()
  {
    this.dlgt.exitAntLib();
  }

  /**
   * Adds a new datatype definition. Attempting to override an existing
   * definition with an equivalent one (i.e. with the same classname) results in
   * a verbose log message. Attempting to override an existing definition with a
   * different one results in a warning log message, but the definition is
   * changed.
   * 
   * @param typeName
   *          The name of the datatype. Must not be <code>null</code>.
   * @param typeClass
   *          The full name of the class implementing the datatype. Must not be
   *          <code>null</code>.
   */
  public void addDataTypeDefinition(String typeName, Class typeClass)
  {
    // this.dlgt.addDataTypeDefinition(typeName,typeClass);
    AntTypeDefinition def = new AntTypeDefinition();
    def.setName(typeName);
    def.setClass(typeClass);
    updateDataTypeDefinition(def);
    getProject().log(
      " +User datatype: " + typeName + "     " + typeClass.getName(), Project.MSG_DEBUG);
  }

  /**
   * Describe <code>addDataTypeDefinition</code> method here.
   * 
   * @param def
   *          an <code>AntTypeDefinition</code> value.
   */
  public void addDataTypeDefinition(AntTypeDefinition def)
  {
    updateDataTypeDefinition(def);
  }

  /**
   * Adds a new task definition to the project. Attempting to override an
   * existing definition with an equivalent one (i.e. with the same classname)
   * results in a verbose log message. Attempting to override an existing
   * definition with a different one results in a warning log message and
   * invalidates any tasks which have already been created with the old
   * definition.
   * 
   * @param taskName
   *          The name of the task to add. Must not be <code>null</code>.
   * @param taskClass
   *          The full name of the class implementing the task. Must not be
   *          <code>null</code>.
   * 
   * @exception BuildException
   *              if the class is unsuitable for being an Ant task. An error
   *              level message is logged before this exception is thrown.
   * 
   * @see #checkTaskClass(Class)
   */
  public void addTaskDefinition(String taskName, Class taskClass)
  {
    this.dlgt.checkTaskClass(taskClass);
    AntTypeDefinition def = new AntTypeDefinition();
    def.setName(taskName);
    def.setClassLoader(taskClass.getClassLoader());
    def.setClass(taskClass);
    def.setAdapterClass(TaskAdapter.class);
    def.setClassName(taskClass.getName());
    def.setAdaptToClass(Task.class);
    updateDataTypeDefinition(def);
  }

  /**
   * Update the component definition table with a new or modified definition.
   * 
   * @param def
   *          the definition to update or insert.
   */
  private void updateDataTypeDefinition(AntTypeDefinition def)
  {
    String name = def.getName();
    Hashtable antTypeTable = getAntTypeTable(); // (changed)
    synchronized (antTypeTable)
    {
      setattr(rebuildTaskClassDefinitions, new Boolean(true)); // changed
      setattr(rebuildTypeClassDefinitions, new Boolean(true)); // changed
      AntTypeDefinition old = getDefinition(name); // changed
      if (old != null)
      {
        if (sameDefinition(def, old))
        {
          return;
        }
        Class oldClass = getComponentClass(name); // changed
        boolean isTask = (oldClass != null && Task.class.isAssignableFrom(oldClass));
        // project.log("Trying to override old definition of "
        // + (isTask ? "task " : "datatype ") + name,
        // (def.similarDefinition(old, project))
        // ? Project.MSG_VERBOSE : Project.MSG_WARN);
        if (isTask)
        {
          invalidateCreatedTasks(name);
        }
      }
      getProject().log(" +Datatype " + name + " " + def.getClassName(), Project.MSG_DEBUG);
      antTypeTable.put(name, def);
    }
  }

  Project getProject()
  {
    return (Project) getattr(project);
  }

  void invalidateCreatedTasks(String name)
  {
    Object[] args = { name };
    invoke(invalidateCreatedTasks, args);
  }

  protected boolean sameDefinition(AntTypeDefinition def, AntTypeDefinition old)
  {
    Object[] args = { def, old };
    Object r = invoke(sameDefinition, args);
    return ((Boolean) r).booleanValue();
  }

  protected Object invoke(Method method, Object[] args)
  {
    try
    {
      return method.invoke(this.dlgt, args);
    } catch (Exception e)
    {
      System.err.println("error: `" + e + "'.");
    }
    return null;
  }

  protected Object getattr(Field field)
  {
    try
    {
      return field.get(this.dlgt);
    } catch (Exception e)
    {
      System.err.println("error: `" + e + "'.");
    }
    return null;
  }

  protected void setattr(Field field, Object val)
  {
    try
    {
      field.set(this.dlgt, val);
    } catch (Exception e)
    {
      System.err.println("error: `" + e + "'.");
    }
  }

  /**
   * Checks whether it is possible to use this Class as replacement * for class
   * ComponentHelper. Using this class may not be possible * if at least one of
   * the private methods and attributes are not * any longer available. * *
   * 
   * @return true if it's safe to use this class as replacement for * class
   *         ComponentHelper.
   */

  static public boolean isapplicable()
  {
    return applicable;
  }

  /**
   * Installs this ComponentHandler in project given. * *
   * 
   * @param P
   *          not null * *
   * @return true if this ComponentHandler could be installed in * given
   *         project.
   */

  static public boolean install(Project P)
  {
    ComponentHelper prv;
    Static.verbose(P, "installing customized ComponentHelper ..");
    prv = ComponentHelper.getComponentHelper(P);
    if (prv instanceof CompH)
    {
      /* already installed */
      return true;
    }
    if (isapplicable())
    {
      CompH cur;
      cur = new CompH();
      cur.setNext(prv);
      /* install it .. */
      P.addReference("ant.ComponentHelper", cur);
      return true;
    }

    /* unable to install */
    return false;
  }

  /**
   * Checks whether current component handler is instance of this * particular
   * class (or a subclass). If so, the current component * handler is replaced
   * by it's successor (retrieved via method * getNext()). If current component
   * handler is not a instance of * this class then method returns false and
   * true otherwise. * *
   * 
   * @param P
   *          not null * *
   * @return true if this ComponentHandler could be uninstalled in * given
   *         project.
   */

  static public boolean uninstall(Project P)
  {
    ComponentHelper cur;
    Static.verbose(P, "uninstalling customized ComponentHelper ..");
    cur = ComponentHelper.getComponentHelper(P);
    if (cur instanceof CompH)
    {
      /* uninstall */
      ComponentHelper nxt = cur.getNext();
      P.addReference("ant.ComponentHelper", nxt);
      return true;
    }
    /* unable to uninstall */
    return false;
  }
}
