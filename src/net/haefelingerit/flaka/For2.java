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

package net.haefelingerit.flaka;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.haefelingerit.flaka.util.Static;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.TaskContainer;
import org.apache.tools.ant.taskdefs.MacroDef;
import org.apache.tools.ant.taskdefs.MacroInstance;

public class For2 extends Task implements TaskContainer
{
  // Macro and sequential belong together. We have hold both here due
  // some half back ANT API.
  protected MacroDef macro;
  protected MacroDef.NestedSequential sequential;
  protected String expr;
  protected String var;

  public For2()
  {
    super();
  }

  /**
   * The argument list to be iterated over.
   * 
   * @param list
   */
  public void setIn(String expr)
  {
    this.expr = Static.trim2(expr, this.expr);
  }

  /**
   * Set the var attribute. This is the name of the macrodef attribute that gets
   * set for each iterator of the sequential element.
   * 
   * @param param
   *          the name of the macrodef attribute.
   */
  public void setVar(String var)
  {
    // TODO: document that this value will not be subject to EL evaluation.
    this.var = Static.trim2(var, this.var);
  }

  public void addTask(org.apache.tools.ant.Task task)
  {
    if (this.macro == null)
    {
      this.macro = new MacroDef();
      this.sequential = this.macro.createSequential();
    }
    this.sequential.addTask(task);
  }

  public void execute() throws BuildException
  {
    MacroInstance instance;
    Project project;
    Object obj;
    String key, val;

    if (this.macro == null)
    {
      debug("nothing to be done");
      return;
    }
    if (this.var == null)
    {
      debug("empty variable given in for element");
      return;
    }
    project = getProject();

    this.macro.setProject(project);
    this.macro.setName("??");
    this.macro.init();
    this.macro.execute();
    /* now we should have a new anonymous macro */
    instance = new MacroInstance();
    instance.setProject(project);
    instance.setOwningTarget(this.getOwningTarget());
    instance.setMacroDef(this.macro);

    key = Static.el2str(project, this.var).trim();
    val = Static.el2str(project, this.expr);
    obj = Static.el2obj(project, "#{" + val + "}");

    /* we need to have something to iterate over */
    if (obj == null)
    {
      return;
    }
    if (!(obj instanceof Iterable))
    {
      List L = new ArrayList();
      L.add(obj);
      obj = L;
    }

    Iterator i = ((Iterable) obj).iterator();
    boolean hasname = project.getReferences().containsKey(key);
    Object saved = project.getReference(key);

    try
    {

      while (i.hasNext())
      {
        Object tmp;
        tmp = i.next();
        /* make sure not to iterate over null */
        if (tmp == null)
          continue;
        try
        {
          project.addReference(key, tmp);
          instance.execute();
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
      if (hasname)
      {
        project.addReference(key, saved);
      } else
      {
        project.getReferences().remove(key);
      }
    }
  }
}
