package net.haefelingerit.flaka;

import org.apache.tools.ant.BuildException;

/**
 * A Task just doing nothing.
 * 
 * This clazz can be used to define a task that does nothing. This can be useful
 * in situations where scripts rely on the existince of a particular task. Ant
 * would fail if such a task does not exist. Rather than doing so, this clazz
 * can be used to let Ant continue, perhaps with a warning message.
 */

public class NoOp extends Task
{
  /** unused */
  /**
   * @param unused
   */
  public void setName(String unused) {
    /* not used */
  }

  /** unused */
  /**
   * @param unused
   */
  public void setVar(String unused) {
    /* not used */
  }

  /** unused */
  /**
   * @param unused
   */
  public void setProperty(String unused) {
    /* not used */
  }

  /** unused */
  /**
   * @param unused
   */
  public void setFlush(String unused) {
    /* not used */
  }

  /** unused */
  /**
   * @param unused
   */
  public void setSrcDir(String unused) {
    /* not used */
  }

  /** unused */
  /**
   * @param unused
   */
  public void setResDir(String unused) {
    /* not used */
  }

  /**
   * Standard task execution interface
   * 
   * This task does nothing
   */
  public void execute() throws BuildException {
    /* do nothing */
  }
}
