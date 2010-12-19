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
import it.haefelinger.flaka.util.TextReader;

import java.util.ArrayList;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

/**
 * 
 * @author merzedes
 * @since 1.0
 */
public class List extends Task
{
  protected String var;
  protected java.util.List list = new ArrayList();
  protected TextReader tr = new TextReader();

  public void setComment(String s)
  {
    this.tr.setCL(s);
  }

  public void setCL(String s)
  {
    this.tr.setCL(s);
  }

  public void setIC(String s)
  {
    this.tr.setIC(s);
  }

  public void setVar(String var)
  {
    this.var = Static.trim3(getProject(), var, this.var);
  }

  public void addText(String text)
  {
    this.tr.setText(text);
  }

  protected void append(Object obj)
  {
    this.list.add(obj);
  }

  public java.util.List eval() throws BuildException
  {
    Project project;
    String line;
    Object obj;

    if (this.tr.getText() != null)
    {
      /* start evaluation text */
      project = this.getProject();

      /* read line by line */
      while ((line = this.tr.readLine()) != null)
      {
        line = project.replaceProperties(line);

        /* resolve all EL references #{ ..} */
        line = Static.elresolve(project, line);

        try
        {
          if (this.el)
            obj = Static.el2obj(project, line);
          else
            obj = line.trim();
          append(obj);
        }
        catch (Exception e)
        {
          String s = "line : error evaluating EL expression (ignored) in " + Static.q(line);
          this.log(s);
        }
      }
    }
    /* ensure that we in any case return a list */
    return this.list;
  }

  public void execute() throws BuildException
  {
    Static.assign(getProject(), this.var, eval(), Static.VARREF);
  }
}
