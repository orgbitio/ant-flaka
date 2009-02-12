package net.haefelingerit.flaka;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.taskdefs.PreSetDef;

/**
 * A task allowing the dynamic creation of a target.
 * 
 * @author wh81752
 * @since 2.1.10
 */

public class CreateTarget extends Task
{
  protected String  name     = null; /* name of new target */
  protected String  task     = null; /* name of task (or macro) to exec */
  protected String  desc     = null; /* (optional) description */
  protected String  deps     = null; /* (optional) depends on */
  protected boolean override = false; /* override if target already exist */
  protected boolean fail     = true;

  public void setName(String s) {
    this.name = Static.trim(s, null);
  }

  public void setTask(String s) {
    this.task = Static.trim(s, null);
  }

  public void setDescription(String s) {
    this.desc = Static.trim(s, null);
  }

  public void setDepends(String s) {
    this.deps = Static.trim(s, null);
  }

  public void setOverride(boolean b) {
    this.override = b;
  }

  protected void onerror(String s) {
    if (this.fail)
      throwbx(s);
    else
      verbose("warning: " + s);
  }

  public void execute() throws BuildException {
    Target target;
    Object obj;
    org.apache.tools.ant.Task T = null;
    Project P = getProject();
    String pname = P.getName();

    if (this.name == null) {
      debug("attribute `name' missing.");
      return;
    }

    debug("creating target " + this.name + "[" + this.task + "] in project "
        + pname + "");

    if (istarget(this.name)) {
      if (this.override == false) {
        debug("target " + this.name + " exits in project [overriding=false]");
        return;
      }
      debug("target " + this.name + " exits in project [overriding=true]");
    } else {
      debug("creating new target " + this.name + " in project");
    }

    target = new Target();
    target.setProject(getProject());
    target.setName(this.name);
    if (this.desc != null)
      target.setDescription(this.desc);
    if (this.deps != null)
      target.setDepends(this.deps);

    if (this.task != null) {
      /* Allow for a sequence of tasks .. */
      String list[] = this.task.split("\\s+");
      String word;

      for (int i = 0; i < list.length; ++i) {
        word = list[i];
        obj = getcomp(word);

        if (obj == null) {
          debug("`" + word + "' does not exist in this project (ignored)");
          continue;
        }

        /*
         * Check whether it's a presetdef. If so then Project.createTask() fails
         * with ClassCastException (1.6.5). In such a way we need to create the
         * object like shown below ..
         */
        if (obj instanceof PreSetDef.PreSetDefinition) {
          PreSetDef.PreSetDefinition psd;
          psd = (PreSetDef.PreSetDefinition) obj;
          obj = psd.createObject(getProject());
        } else {
          /* try to create task */
          obj = getProject().createTask(this.task);
          if (obj == null) {
            /* this should not happen - anyhow, we check again */
            debug("failed to create task `" + this.task + "' in project");
            continue;
          }
        }

        if (!(obj instanceof org.apache.tools.ant.Task)) {
          debug("task `" + this.task + "' created, but not an Ant Task.");
          continue;
        }

        /* obj != null and instance of Task */
        T = (org.apache.tools.ant.Task) obj;

        debug("adding task " + T.getTaskName() + "[" + T.getTaskType()
            + "] to target " + target.getName() + "");

        target.addTask(T);
      }
    }

    /* eventually add target to project */
    getProject().addTarget(target);
    return;
  }
}
