package net.haefelingerit.flaka;

import java.util.Hashtable;

import org.apache.tools.ant.BuildException;

public class SetDefault extends Task
{
  protected String  target   = null;
  protected boolean fail     = false;
  protected boolean override = false;

  /**
   * The name of the default target.
   * 
   * @param s
   */
  public void setName(String s) {
    this.target = Static.trim2(s, this.target);
  }

  public String getName() {
    return this.target;
  }

  /**
   * Whether to fail if the target does not exist.
   * 
   * @param b
   */
  public void setFail(boolean b) {
    this.fail = b;
  }

  public boolean getFail() {
    return this.fail;
  }

  /**
   * Whether to override an existing default target
   * 
   * @param b
   */
  public void setOverride(boolean b) {
    this.override = b;
  }

  protected boolean hastarget(String s) {
    Hashtable H = getProject().getTargets();
    return H.containsKey(s);
  }

  public void execute() throws BuildException {
    String s;

    if (this.target == null) {
      debug("no default target given to set");
      return;
    }

    s = getProject().getDefaultTarget();
    if (!Static.isEmpty(s) && this.override == false) {
      debug("default target already set to `" + s + "' (ignored)");
      return;
    }

    if (hastarget(this.target)) {
      getProject().setDefault(this.target);
    } else {
      String m;
      m = "target `" + this.target + "' does not exist.";
      if (this.fail) {
        throwbx(m);
      } else {
        verbose(m + " (ignored)");
      }
    }
  }
}
