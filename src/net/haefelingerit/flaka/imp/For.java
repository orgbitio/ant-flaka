/*
 * Copyright (c) 2003-2005 Ant-Contrib project. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package net.haefelingerit.flaka.imp;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.haefelingerit.flaka.Break;
import net.haefelingerit.flaka.Continue;
import net.haefelingerit.flaka.Static;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.taskdefs.MacroDef;
import org.apache.tools.ant.taskdefs.MacroInstance;
import org.apache.tools.ant.taskdefs.Parallel;
import org.apache.tools.ant.types.DirSet;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.util.TaskLogger;


/*******************************************************************************
 * Task definition for the for task. This is based on the foreach task but takes
 * a sequential element instead of a target and only works for ant >= 1.6Beta3
 * 
 * @author Peter Reilly
 */

public class For
{

  private String     list;
  private String     param;
  private Path       currPath;
  private boolean    keepgoing    = false;
  private MacroDef   macroDef;
  private List       hasIterators = new ArrayList();
  private boolean    parallel     = false;
  private Integer    threadCount;
  private Parallel   parallelTasks;
  private TaskLogger logger;

  private Project    project;
  private Target     owningTarget;

  public void setOwningTarget(Target owningTarget) {
    this.owningTarget = owningTarget;
  }

  public void setProject(Project project) {
    this.project = project;
  }

  /**
   * Attribute whether to execute the loop in parallel or in sequence.
   * 
   * @param parallel
   *          if true execute the tasks in parallel. Default is false.
   */
  public void setParallel(boolean parallel) {
    this.parallel = parallel;
  }

  /*****************************************************************************
   * Set the maximum amount of threads we're going to allow to execute in
   * parallel
   * 
   * @param threadCount
   *          the number of threads to use
   */
  public void setThreadCount(int threadCount) {
    if (threadCount < 1) {
      throw new BuildException("Illegal value for threadCount " + threadCount
          + " it should be > 0");
    }
    this.threadCount = new Integer(threadCount);
  }

  /**
   * Set the keepgoing attribute, indicating whether we should stop on errors or
   * continue heedlessly onward.
   * 
   * @param keepgoing
   *          a boolean, if <code>true</code> then we act in the keepgoing
   *          manner described.
   */
  public void setKeepgoing(boolean keepgoing) {
    this.keepgoing = keepgoing;
  }

  /**
   * Set the list attribute.
   * 
   * @param list
   *          a list of delimiter separated tokens.
   */
  public void setList(String list) {
    this.list = list;
  }

  /**
   * Set the param attribute. This is the name of the macrodef attribute that
   * gets set for each iterator of the sequential element.
   * 
   * @param param
   *          the name of the macrodef attribute.
   */
  public void setParam(String param) {
    this.param = param;
  }

  private Path getOrCreatePath() {
    if (this.currPath == null) {
      this.currPath = new Path(this.project);
    }
    return this.currPath;
  }

  /**
   * This is a path that can be used instread of the list attribute to interate
   * over. If this is set, each path element in the path is used for an
   * interator of the sequential element.
   * 
   * @param path
   *          the path to be set by the ant script.
   */
  public void addConfigured(Path path) {
    getOrCreatePath().append(path);
  }

  /**
   * This is a path that can be used instread of the list attribute to interate
   * over. If this is set, each path element in the path is used for an
   * interator of the sequential element.
   * 
   * @param path
   *          the path to be set by the ant script.
   */
  public void addConfiguredPath(Path path) {
    addConfigured(path);
  }

  /**
   * @return a MacroDef#NestedSequential object to be configured
   */
  public Object createSequential() {
    this.macroDef = new MacroDef();
    this.macroDef.setProject(this.project);
    return this.macroDef.createSequential();
  }

  /**
   * Run the for task. This checks the attributes and nested elements, and if
   * there are ok, it calls doTheTasks() which constructes a macrodef task and a
   * for each interation a macrodef instance.
   */
  public void execute() throws BuildException {
    if (this.parallel) {
      this.parallelTasks = (Parallel) this.project.createTask("parallel");
      if (this.threadCount != null) {
        this.parallelTasks.setThreadCount(this.threadCount.intValue());
      }
    }
    if (this.list == null && this.currPath == null
        && this.hasIterators.size() == 0) {
      throw new BuildException(
          "You must have a list or path to iterate through");
    }
    if (this.param == null) {
      throw new BuildException("You must supply a property name to set on"
          + " each iteration in param");
    }
    if (this.macroDef == null) {
      throw new BuildException("You must supply an embedded sequential "
          + "to perform");
    }
    try {
      doTheTasks();
    }
    catch (Exception e) {
      throw new BuildException(e);
    }
    if (this.parallel) {
      this.parallelTasks.perform();
    }
  }

  private void doSequentialIteration(String val) {
    MacroInstance instance = new MacroInstance();
    instance.setProject(this.project);
    instance.setOwningTarget(this.owningTarget);
    instance.setMacroDef(this.macroDef);
    instance.setDynamicAttribute(this.param.toLowerCase(), val);
    if (!this.parallel) {
      instance.execute();
    } else {
      this.parallelTasks.addTask(instance);
    }
  }

  private void doTheTasks() throws Exception {
    int errorCount = 0;
    int taskCount = 0;

    // Create a macro attribute
    MacroDef.Attribute attribute = new MacroDef.Attribute();
    attribute.setName(this.param);
    this.macroDef.addConfiguredAttribute(attribute);
    // Take Care of the list attribute
    if (this.list != null) {
      String[] args = Static.split0x1(this.list, '\'');
      for (int i = 0; i < args.length; ++i) {
        String arg = args[i];
        taskCount++;
        try {
          doSequentialIteration(arg);
        }
        catch (BuildException bx) {
          String s = bx.getMessage();
          if (s != null && s.endsWith(Break.TOKEN))
            break;
          if (s != null && s.endsWith(Continue.TOKEN))
            continue;
          if (this.keepgoing) {
            if (this.logger != null) {
              this.logger.error(arg + ": " + bx.getMessage());
            }
            errorCount++;
          } else {
            throw bx;
          }
        }
      }
    }
    if (this.keepgoing && (errorCount != 0)) {
      throw new BuildException("Keepgoing execution: " + errorCount + " of "
          + taskCount + " iterations failed.");
    }

    // Take Care of the path element
    String[] pathElements = new String[0];
    if (this.currPath != null) {
      pathElements = this.currPath.list();
    }
    for (int i = 0; i < pathElements.length; i++) {
      File nextFile = new File(pathElements[i]);
      try {
        taskCount++;
        doSequentialIteration(nextFile.getAbsolutePath());
      }
      catch (BuildException bx) {
        if (this.keepgoing) {
          if (this.logger != null) {
            this.logger.error(nextFile + ": " + bx.getMessage());
          }
          errorCount++;
        } else {
          throw bx;
        }
      }
    }
    if (this.keepgoing && (errorCount != 0)) {
      throw new BuildException("Keepgoing execution: " + errorCount + " of "
          + taskCount + " iterations failed.");
    }

    // Take care of iterators
    for (Iterator i = this.hasIterators.iterator(); i.hasNext();) {
      Iterator it = ((HasIterator) i.next()).iterator();
      while (it.hasNext()) {
        try {
          taskCount++;
          doSequentialIteration(it.next().toString());
        }
        catch (BuildException bx) {
          if (this.keepgoing) {
            if (this.logger != null) {
              this.logger.error(it.next().toString() + ": " + bx.getMessage());
            }
            errorCount++;
          } else {
            throw bx;
          }
        }
      }
    }
    if (this.keepgoing && (errorCount != 0)) {
      throw new BuildException("Keepgoing execution: " + errorCount + " of "
          + taskCount + " iterations failed.");
    }
  }

  /**
   * Specify a logger to be used for passing along messages.
   */
  public void setLogger(TaskLogger logger) {
    this.logger = logger;
  }

  /**
   * Add a Map, iterate over the values
   * 
   * @param map
   *          a Map object - iterate over the values.
   */
  public void add(Map map) {
    this.hasIterators.add(new MapIterator(map));
  }

  /**
   * Add a fileset to be iterated over.
   * 
   * @param fileset
   *          a <code>FileSet</code> value
   */
  public void add(FileSet fileset) {
    getOrCreatePath().addFileset(fileset);
  }

  /**
   * Add a fileset to be iterated over.
   * 
   * @param fileset
   *          a <code>FileSet</code> value
   */
  public void addFileSet(FileSet fileset) {
    add(fileset);
  }

  /**
   * Add a dirset to be iterated over.
   * 
   * @param dirset
   *          a <code>DirSet</code> value
   */
  public void add(DirSet dirset) {
    getOrCreatePath().addDirset(dirset);
  }

  /**
   * Add a dirset to be iterated over.
   * 
   * @param dirset
   *          a <code>DirSet</code> value
   */
  public void addDirSet(DirSet dirset) {
    add(dirset);
  }

  /**
   * Add a collection that can be iterated over.
   * 
   * @param collection
   *          a <code>Collection</code> value.
   */
  public void add(Collection collection) {
    this.hasIterators.add(new ReflectIterator(collection));
  }

  /**
   * Add an iterator to be iterated over.
   * 
   * @param iterator
   *          an <code>Iterator</code> value
   */
  public void add(Iterator iterator) {
    this.hasIterators.add(new IteratorIterator(iterator));
  }

  /**
   * Add an object that has an Iterator iterator() method that can be iterated
   * over.
   * 
   * @param obj
   *          An object that can be iterated over.
   */
  public void add(Object obj) {
    this.hasIterators.add(new ReflectIterator(obj));
  }

  /**
   * Interface for the objects in the iterator collection.
   */
  private interface HasIterator
  {
    Iterator iterator();
  }

  private static class IteratorIterator implements HasIterator
  {
    private Iterator iterator;

    public IteratorIterator(Iterator iterator) {
      this.iterator = iterator;
    }

    public Iterator iterator() {
      return this.iterator;
    }
  }

  private static class MapIterator implements HasIterator
  {
    private Map map;

    public MapIterator(Map map) {
      this.map = map;
    }

    public Iterator iterator() {
      return this.map.values().iterator();
    }
  }

  private static class ReflectIterator implements HasIterator
  {
    private Object obj;
    private Method method;

    public ReflectIterator(Object obj) {
      this.obj = obj;
      try {
        this.method = obj.getClass().getMethod("iterator", new Class[] {});
      }
      catch (Throwable t) {
        throw new BuildException("Invalid type " + obj.getClass()
            + " used in For task, it does"
            + " not have a public iterator method");
      }
    }

    public Iterator iterator() {
      try {
        return (Iterator) this.method.invoke(this.obj, new Object[] {});
      }
      catch (Throwable t) {
        throw new BuildException(t);
      }
    }
  }
}
