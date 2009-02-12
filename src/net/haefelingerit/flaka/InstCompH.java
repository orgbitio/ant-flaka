package net.haefelingerit.flaka;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

/**
 * A simple task which installs or deinstalls a specialized * ComponentHelper.
 * This specialized version is supposed to get * rid of some anoying log
 * messages. Installation may fail if * your Ant does not support all required
 * methods. * *
 * 
 * @author flaka (at) haefelingerit (dot) net
 */

public class InstCompH extends Task
{
  protected boolean install = true;
  protected boolean fail    = false;

  public void setInstall(boolean b) {
    this.install = b;
  }

  public void setUnInstall(boolean b) {
    this.install = !b;
  }

  public void setFail(boolean b) {
    this.fail = b;
  }

  protected void warnordie(String msg) throws BuildException {
    if (this.fail)
      throwbx(msg);
    else
      verbose(msg);
  }

  public void execute() throws BuildException {
    Project P;

    P = getProject();
    if (this.install) {
      if (!CompH.install(P)) {
        warnordie("failed to install customized component helper.");
      }
    } else {
      if (!CompH.uninstall(P)) {
        warnordie("failed to uninstall customized component helper.");
      }
    }
  }
}
