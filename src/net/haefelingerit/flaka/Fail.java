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

import net.haefelingerit.flaka.util.Static;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Exit;
import org.apache.tools.ant.taskdefs.condition.ConditionBase;

/**
 * Same as Ant's fail task, additionally resolve EL references in message and
 * property.
 * 
 * @author geronimo
 * 
 */
public class Fail extends Exit
{
  protected String test;
  protected boolean havemsg = false;
  protected boolean dosuper = false;

  public void setMessage(String msg)
  {
    Project project = getProject();
    msg = project.replaceProperties(msg);
    msg = Static.el2str(project, msg);
    super.setMessage(msg);
    this.havemsg = true;
  }

  public void addText(String msg)
  {
    Project project = getProject();
    msg = project.replaceProperties(msg);
    msg = Static.el2str(project, msg);
    super.addText(msg);
    this.havemsg = true;
  }

  public void setTest(String expr)
  {
    this.test = Static.trim2(expr, this.test);
  }

  public void setIf(String s)
  {
    this.dosuper = true;
    super.setIf(s);
  }

  public void setUnless(String s)
  {
    this.dosuper = true;
    super.setUnless(s);
  }

  public ConditionBase createCondition()
  {
    this.dosuper = true;
    return super.createCondition();
  }

  public void execute() throws BuildException
  {
    String s;
    Project project;

    /* standard behaviour */
    if (this.test == null)
    {
      super.execute();
      return;
    }

    project = getProject();
    s = Static.el2str(project, this.test);
    s = "#{" + s + "}";
    if (Static.el2bool(project, s))
    {
      /* Set a nice message if not set */
      if (this.havemsg == false)
      {
        this.setMessage("test(\"" + this.test + "\") => true");
      }
      /*
       * Set impossible property - have to do this in order to make fail going
       * off. This property can't be set by regular means.
       */
      this.setUnless("${}");
      /*
       * We want to use super here to handle all the other logistics (exist
       * status, message handling etc).
       */
      super.execute();
      return;
    }

    /*
     * If other attributes/conditions have been used, let super handle them.
     */
    if (this.dosuper)
      super.execute();
  }
}
