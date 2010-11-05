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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.TaskContainer;

/**
 * 
 * @author merzedes
 * @since 1.0
 */
public class For extends it.haefelinger.flaka.Task implements TaskContainer
{
  protected String expr;
  protected String var;
  /** Optional Vector holding the nested tasks */
  protected Vector tasks = new Vector();
  protected Object saved = null;

  public For()
  {
    super();
  }

  /**
   * The argument list to be iterated over.
   */
  public void setIn(String expr)
  {
    this.expr = Static.trim3(getProject(), expr, this.expr);
  }

  /**
   * Set the var attribute. This is the name of the macrodef attribute that gets
   * set for each iterator of the sequential element.
   */
  public void setVar(String var)
  {
    this.var = Static.trim3(getProject(), var, this.var);
  }

  public void addTask(Task nestedTask)
  {
    this.tasks.add(nestedTask);
  }

  protected void rescue()
  {
    /* save variable (if existing) */
    // TODO: variables are not references
    this.saved = getProject().getReference(this.var);
  }

  protected void restore()
  {
    Static.assign(getProject(), this.var, this.saved, Static.VARREF);
  }

  protected void exectasks(Object val) throws BuildException
  {
    Iterator iter;
    Task task;

    Static.assign(getProject(), this.var, val, Static.VARREF);
    iter = this.tasks.iterator();
    while (iter.hasNext())
    {
      task = (Task) iter.next();
      task.perform();
    }
  }

  protected Iterator iterator()
  {
    Iterator iter = null;
    Project project;
    Object obj;

    project = getProject();
    obj = Static.el2obj(project,this.expr);

    if (obj == null)
      return null;

    if (obj instanceof Iterable) {
      iter = ((Iterable) obj).iterator();
      return iter;
    }
    // If we are a map, then we iterate over the keys.
    if (obj instanceof Map) {
      Set keys = ((Map)obj).keySet();
      // Do not use `keys.iterator()` here otherwise we end up in
      // an concurrent modification exception.
      iter = Arrays.asList(keys.toArray()).iterator();
      return iter;
    }
    // Otherwise, we create an array and iterate over
    // it's one and only argument.
    iter = Arrays.asList(obj).iterator();
    return iter;
  }

  public void execute() throws BuildException
  {
    Iterator iter;

    if (this.expr == null || this.var == null)
    {
      // TODO: debug message
      return;
    }

    try
    {
      /* rescue variable `var` */
      rescue();

      /* iterate over each list item */
      iter = iterator();
      while (iter != null && iter.hasNext())
      {
        try
        {
          /* exec all tasks using on current list item */
          exectasks(iter.next());
        } catch (BuildException bx)
        {
          String s;
          s = bx.getMessage();
          /* we are looking for a special designed message */
          if (s == null)
            throw bx;
          /* handle special break statement */
          if (s.endsWith(Break.TOKEN))
            break;
          /* handle continue statement */
          if (s.endsWith(Continue.TOKEN))
            continue;
          /* regular exception */
          throw bx;
        }
      }
    } finally
    {
      /* restore variable */
      restore();
    }
  }
}
