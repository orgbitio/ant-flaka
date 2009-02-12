package net.haefelingerit.flaka;

import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Sequential;

public class Choose extends Task
{
  protected List       whenlist;
  protected Sequential otherwise;

  public void addWhen(When task) {
    if (task == null) {
      return;
    }
    if (this.whenlist == null) {
      this.whenlist = new ArrayList();
    }
    this.whenlist.add(task);
    return;
  }

  public void addOtherwise(Sequential task) throws BuildException {
    if (task == null) {
      return;
    }
    if (this.otherwise != null) {
      throwbx("<otherwise/> clause already used.");
      return;
    }
    this.otherwise = task;
    return;
  }

  public void execute() throws BuildException {
    When when;
    /*
     * * If we do not have some 'when' conditions but we have an otherwise we *
     * execute the otherwise, otherwise we return silently.
     */
    if (this.whenlist == null || this.whenlist.size() <= 0) {
      if (this.otherwise != null) {
        this.otherwise.execute();
        return;
      }
    }

    /* execute the very fist 'when' that evaluates to 'true' */
    for (int i = 0; i < this.whenlist.size(); ++i) {
      when = (When) this.whenlist.get(i);
      if (when.eval()) {
        when.exec();
        return;
      }
    }

    /* otherwise execute the otherwise task */
    if (this.otherwise != null) {
      this.otherwise.execute();
    }
    return;
  }
}
