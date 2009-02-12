package net.haefelingerit.flaka;

import org.apache.tools.ant.BuildException;

public class IfNDef extends IfDef
{
  public boolean eval() throws BuildException {
    return !(super.eval());
  }
}
