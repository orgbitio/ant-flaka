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
 * @author wh81752
 * 
 */
public class Exec extends ExecTask
{
  protected boolean failIfExecFails = true;

  public void setFailIfExecutionFails(boolean flag) {
    super.setFailIfExecutionFails(flag);
    this.failIfExecFails = flag;
  }

  protected void maybeSetResultPropertyValue(int result) {
    super.maybeSetResultPropertyValue(result);
  }

  protected void runExec(Execute exe) throws BuildException {
    log(this.cmdl.describeCommand(), Project.MSG_VERBOSE);
    exe.setCommandline(this.cmdl.getCommandline());
    try {
      runExecute(exe);
    }
    catch (IOException e) {
      if (this.failIfExecFails) {
        throw new BuildException("Execute failed: " + e.toString(), e,
            getLocation());
      }
    }
    finally {
      logFlush();
    }
  }
}
