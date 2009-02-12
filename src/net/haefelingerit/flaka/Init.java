package net.haefelingerit.flaka;

import java.util.Enumeration;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

public class Init extends Task
{

  protected Object comph = null;

  public void execute() throws BuildException {
    Project P;
    PropertyHelper ph;
    org.apache.tools.ant.PropertyHelper current;
    Enumeration e;
    String ANT_HELPER_REFID = "ant.PropertyHelper";
    String ns = null;

    /* get my project */
    P = getProject();
    /* create new property helper */
    ph = new PropertyHelper();
    ph.setProject(P);

    /* get current property helper */
    current = org.apache.tools.ant.PropertyHelper.getPropertyHelper(P);

    /* install my property handler */
    P.getReferences().put(ANT_HELPER_REFID, ph);

    /* copy all properties from current project in my property helper */
    e = current.getProperties().keys();
    while (e.hasMoreElements()) {
      Object arg = e.nextElement();
      if (!(arg instanceof String))
        continue;
      String k = (String) arg;
      Object v = current.getProperty(ns, k);
      ph.setProperty(ns, k, v, false);
    }

    /* copy user properties */
    current.copyUserProperties(P);

    /* copy inherited properties */
    current.copyInheritedProperties(P);
  }

}
