package net.haefelingerit.flaka;

import org.apache.tools.ant.BuildException;

public class Unset extends Task
{
  protected String name = "";
  protected String ifs  = "\\s+";

  public void setName(String name) {
    this.name = Static.trim2(name, this.name);
  }

  public void setVar(String name) {
    this.name = Static.trim2(name, this.name);
  }

  public void setProperty(String name) {
    this.name = Static.trim2(name, this.name);
  }

  public void setIfs(String ifs) {
    this.ifs = Static.trim2(ifs, this.ifs);
  }

  public void execute() throws BuildException {
    String[] name;
    /* break string apart based on ifs */
    name = this.name.split(this.ifs);
    unset(name);
  }
}
