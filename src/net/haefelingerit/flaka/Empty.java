package net.haefelingerit.flaka;

import org.apache.tools.ant.BuildException;

public class Empty extends NotEmpty
{
  public boolean eval() throws BuildException {
    return !(super.eval());
  }
}
