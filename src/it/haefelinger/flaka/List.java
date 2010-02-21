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
import org.apache.tools.ant.types.LogLevel;

/**
 * 
 * @author merzedes
 * @since 1.0
 */
public class List extends Task
{
  protected String var;
  protected java.util.List list;
  protected TextReader tr;
  
  public List()
  {
    this.tr = new TextReader();
    this.tr.setSkipEmpty(true);
  }
  
  public void setComment(String s)
  {
    this.tr.setComment(s);
  }

  public void setVar(String var)
  {
    this.var = Static.trim3(getProject(), var, this.var);
  }

  public void addText(String text)
  {
    String s = this.tr.getText();
    if (s == null)
      s = "";
    s +=  text;
    s = getProject().replaceProperties(s);
    this.tr.setText(s);
  }

  protected java.util.List makelist()
  {
    if (this.list == null)
      this.list = new ArrayList();
    return this.list;
  }

  protected java.util.List append(String s)
  {
    if (s != null)
      makelist().add(s);
    return this.list;
  }

  public java.util.List eval() throws BuildException
  {
    Project project;
    String line, v;

    if (this.tr.getText() != null)
    {
 
      /* start evaluation text */
      project = this.getProject();

 
      /* read line by line */
      while ((line = this.tr.readLine()) != null)
      {
        try
        {
          v = Static.elresolve(project, line);
          if (this.el)
            v = Static.el2str(project,v);
          else
            v = v.trim();
          append(v);
        } 
        catch (Exception e)
        {
          String s = "line : error evaluating EL expression (ignored) in "
            + Static.q(line);
          this.log(s);
        }
      }
    }
    /* ensure that we in any case return a list */
    return makelist();
  }

  public void execute() throws BuildException
  {
    Static.assign(getProject(), this.var, eval(), Static.VARREF);
  }
}
