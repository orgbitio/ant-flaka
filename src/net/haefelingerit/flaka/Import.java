package net.haefelingerit.flaka;

import org.apache.tools.ant.BuildException;

/**
 * A task to load a Ant build script via the classpath rather than from a local
 * file.
 * 
 * This task is no longer needed by Flaka cause targets are now generated
 * on the fly rather than via importing a build script containing targets. Note
 * that this task does noting when being used. It remains for legacy reasons
 * only.
 * 
 * @deprecated
 * @author wh81752
 */

public class Import extends Task
{

  /**
   * @param file
   */
  public void setFile(String file) {
    /* do nothing */
  }

  /** how we import */
  public void execute() throws BuildException { /* do nothing */
  }
}
