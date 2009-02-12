package net.haefelingerit.flaka;

import org.apache.tools.ant.BuildException;

/**
 * A task to throw a special build exception: to terminate a for-loop.
 * 
 * See {@link http://w/AntFlaka/API/Task#for} for details on task for.
 */

public class Break extends Task
{
  final static public String            TOKEN     = "%%bR3Ak%%";
  final static protected BuildException EXCEPTION = new BuildException(TOKEN);

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.tools.ant.Task#execute()
   */
  
  public void execute() throws BuildException {

    // Implementation Note:
    // Throwing a derived build execption (i.e. a subclass of BuildException)
    // did *not* work out: Ant will wrap any exception not having a message or
    // location assigned. It is technically easy to create such an exception -
    // however, the implementation depends then on Ant internals.
    // The current implementation inserts a special character sequence (TOKEN)
    // in the exception message. A "for" implementation needs then to scan the
    // message for this token.
    // While this implementation is also not bullet proof, it appears to be more
    // robust than relying on Ant internals.
    /* Throw the specialized build exception */

    throw EXCEPTION;
  }
}
