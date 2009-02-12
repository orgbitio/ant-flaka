package net.haefelingerit.flaka;

import java.util.Hashtable;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

public class PropertyHelper extends org.apache.tools.ant.PropertyHelper
{

  protected Project P = null;

  protected PropertyHelper() {
    super();
  }

  public void setProject(Project P) {
    this.P = P;
    super.setProject(P);
  }

  protected void debug(String msg) {
    if (this.P == null)
      System.out.println(msg);
    else
      this.P.log(msg, Project.MSG_DEBUG);
  }

  public String replaceProperties(String ns, String value, Hashtable keys)
      throws BuildException {
    String s;

    if (value == null)
      return null;

    if (!Eval.needseval(value))
      return value;

    if (this.P == null)
      throw new BuildException("missing project..");

    s = value;
    if (keys != null)
      s = Eval.veval(s, keys, this.P);

    if (!Eval.needseval(s))
      return s;

    s = Eval.veval(s, this.P.getProperties(), this.P);
    if (!Eval.needseval(s))
      return s;

    s = super.replaceProperties(ns, s, keys);
    return s;
  }
}
