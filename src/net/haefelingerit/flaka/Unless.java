package net.haefelingerit.flaka;

import org.apache.tools.ant.BuildException;

public class Unless extends When
{
  public boolean eval() throws BuildException {
    return !(super.eval());
  }
}
