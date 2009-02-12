package net.haefelingerit.flaka;

import java.util.Enumeration;
import java.util.Properties;

import java.io.ByteArrayInputStream;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

/**
 * This task is properly doc http://w/Flaka/API/set
 */

public class Set extends Task
{
  protected String  name     = null;
  protected String  value    = "";
  protected String  text     = null;
  protected boolean override = true;

  /**
   * * set the name of the property. * *
   * 
   * @param name
   *          not null
   */

  public void setName(String name) {
    this.name = Static.trim2(name, this.name);
  }

  public void setVar(String name) {
    this.name = Static.trim2(name, this.name);
  }

  public void setProperty(String name) {
    this.name = Static.trim2(name, this.name);
  }

  public void setValue(String value) {
    this.value = Static.trim2(value, this.value);
  }

  public void setOverride(boolean b) {
    this.override = b;
  }

  public void setPreserve(boolean b) {
    this.override = !b;
  }

  /**
   * Set a multiline message.
   * 
   * @param msg
   *          the CDATA text to append to the output text
   */
  public void addText(String msg) {
    this.text = getProject().replaceProperties(msg);
  }

  /**
   * Low level method to set a property. How the property is set depends on *
   * attributes. *
   * 
   * @param P
   *          not null *
   * @param k
   *          not null *
   * @param v
   *          not null
   */

  protected void set(Project P, String k, String v) {
    if (this.debug && this.override) {
      String was = P.getProperty(k);
      String msg = "overriding prop `" + k + "' with |" + v + "|, was |" + was
          + "|";
      System.err.println(msg);
    }
    if (this.override)
      P.setProperty(k, v);
    else
      P.setNewProperty(k, v);
  }

  /**
   * * Execute me. * *
   * 
   * @exception BuildException
   */

  public void execute() throws BuildException {
    Project P;

    if (this.name == null && this.text == null) {
      debug("nothing to do for this `set' task.");
      return;
    }

    /* get my project */
    P = getProject();

    /* set single property via attribute */
    if (!Static.empty(this.name))
      set(P, this.name, this.value);

    /* set a bunch of properties via implicit text element */
    if (!Static.empty(this.text)) {
      Enumeration i;
      ByteArrayInputStream S;
      Properties H;

      S = new ByteArrayInputStream(this.text.getBytes());
      H = new Properties();

      try {
        H.load(S);
      }
      catch (Exception e) {
        Static.throwbx("setting properties failed", e);
        return;
      }

      /* iterate over my properties, set one by one .. */
      i = H.keys();
      while (i.hasMoreElements()) {
        String k, v;
        k = (String) i.nextElement();
        v = H.getProperty(k);
        if (v != null) // can't be the case - play save anyway ..
          set(P, k, v);
      }
    }
  }
}
