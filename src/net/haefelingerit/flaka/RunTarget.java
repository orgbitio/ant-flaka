package net.haefelingerit.flaka;

import org.apache.tools.ant.BuildException;

/**
 * A task allowing the dynamic execution of a target.
 * 
 * @author wh81752
 * @since 2.0
 */

public class RunTarget extends Task
{
  protected String  name = null;
  protected boolean fail = false;

  /**
   * The name of the target to execute
   * 
   * @param s
   */
  public void setName(String s) {
    this.name = Static.trim2(s, this.name);
  }

  public String getName() {
    return this.name;
  }

  /**
   * Whether to fail if target does not exist.
   * 
   * @param b
   */
  public void setFail(boolean b) {
    this.fail = b;
  }

  public boolean getFail() {
    return this.fail;
  }

  protected void onerror(String s) {
    if (this.fail)
      throwbx(s);
    else
      verbose("warning: " + s);
  }

  public void execute() throws BuildException {
    if (this.name == null) {
      onerror("attribute `name' missing.");
      return;
    }

    if (istarget(this.name)) {
      getProject().executeTarget(this.name);
    } else {
      onerror("`" + this.name + "' not a target.");
    }
    return;
  }
}
