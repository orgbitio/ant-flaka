package net.haefelingerit.flaka;

import org.apache.tools.ant.BuildException;

public class Logo extends Task
{
  protected String text  = "";
  protected String chr   = ":";
  protected int    width = 80;

  public void setText(String s) {
    this.text = Static.trim2(s, this.text);
  }

  public void setChr(String s) {
    this.chr = Static.trim2(s, this.chr);
  }

  public void setWidth(int s) {
    this.width = s;
  }

  public void execute() throws BuildException {
    System.out.print(Static.logo(this.text, this.width));
    System.out.flush();
  }
}
