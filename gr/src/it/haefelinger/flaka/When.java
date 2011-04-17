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
import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.TaskContainer;

/**
 * A task to simulate a else-less if statement.
 * 
 * @author merzedes
 * @since 1.0
 */
public class When extends Task implements TaskContainer {
  protected String test = null;
  protected List tasklist = null;

  /**
   * The test that must evaluate to true in order to execute the body.
   */
  public void setTest(String s) {
    this.test = Static.elresolve(this.getProject(), s);
  }

  public void addTask(org.apache.tools.ant.Task task) {
    if (this.tasklist == null)
      this.tasklist = new ArrayList();
    this.tasklist.add(task);
  }

  /**
   * Evalutes the internal test condition.
   * 
   * @return true if the condition evalutes to true of if no condition is given.
   * @throws BuildException
   */
  protected boolean eval() throws BuildException {
    Project p;
    p = this.getProject();
    return Static.el2bool(p, this.test);
  }

  /**
   * Executes the when body.
   * 
   * @throws BuildException
   */
  public void exec() throws BuildException {
    if (this.tasklist != null) {
      Iterator i;
      org.apache.tools.ant.Task task;
      i = this.tasklist.iterator();

      while (i.hasNext()) {
        task = (org.apache.tools.ant.Task) i.next();
        task.perform();
      }
    }
  }

  /**
   * Evalutes the test condition and if true, executes the body.
   * 
   * @see org.apache.tools.ant.taskdefs.Sequential#execute()
   */
  public void execute() throws BuildException {
    if (eval())
      exec();
  }

}
