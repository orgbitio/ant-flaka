package net.haefelingerit.flaka;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Sequential;

/**
 * A task container usable as replacment for <code>sequential</code>
 * 
 * This task has been fallen out of favour and is deprecated. There is no
 * alternative usage. This tasks remains for the reason of being backward
 * compatible.
 * 
 * @deprecated
 * @author wh81752
 * 
 */
public class Trace extends Sequential
{
  /**
   * @param b
   */
  public void setEnable(boolean b) { /* not used */

  }

  /**
   * @param b
   */
  public void setDisable(final boolean b) { /* not used */

  }

  /**
   * @param s
   */
  public void setName(String s) { /* not used */

  }

  public void execute() throws BuildException { /* not used */
    super.execute();
  }
}
