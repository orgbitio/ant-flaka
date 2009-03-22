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

import java.io.IOException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.ExecTask;
import org.apache.tools.ant.taskdefs.Execute;

/**
 * A task to check whether a given binary is executable or not.
 * 
 * Usage of this task is deprecated.
 * 
 * @deprecated
 * @author merzedes
 * @since 1.0
 */
public class Exec extends ExecTask
{
  protected boolean failIfExecFails = true;

  public void setFailIfExecutionFails(boolean flag)
  {
    super.setFailIfExecutionFails(flag);
    this.failIfExecFails = flag;
  }

  protected void maybeSetResultPropertyValue(int result)
  {
    super.maybeSetResultPropertyValue(result);
  }

  protected void runExec(Execute exe) throws BuildException
  {
    log(this.cmdl.describeCommand(), Project.MSG_VERBOSE);
    exe.setCommandline(this.cmdl.getCommandline());
    try
    {
      runExecute(exe);
    } catch (IOException e)
    {
      if (this.failIfExecFails)
      {
        throw new BuildException("Execute failed: " + e.toString(), e, getLocation());
      }
    } finally
    {
      logFlush();
    }
  }
}
