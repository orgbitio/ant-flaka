package net.haefelingerit.flaka;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Sequential;

/**
 * A task to simulate a else-less if statement.
 * 
 * @author wh81752
 * 
 */
public class When extends Sequential
{
  protected String test = null;

  /**
   * The test that must evaluate to true in order to execute the body.
   * 
   * @param s
   */
  public void setTest(String s) {
    this.test = Static.trim2(s, this.test);
  }

  /**
   * Evalutes the internal test condition.
   * 
   * @return true if the condition evalutes to true of if no condition is given.
   * @throws BuildException
   */
  protected boolean eval() throws BuildException {
    Project p;
    p = getProject();
    return (this.test == null ? true : Test.eval(p, this.test));
  }

  /**
   * Executes the when body.
   * 
   * @throws BuildException
   */
  public void exec() throws BuildException {
    super.execute();
  }

  /**
   * Evalutes the test condition and if true, executes the body.
   * 
   * @see org.apache.tools.ant.taskdefs.Sequential#execute()
   */
  public void execute() throws BuildException {
    if (eval()) {
      super.execute();
    }
  }
}
