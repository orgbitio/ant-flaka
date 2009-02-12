package net.haefelingerit.flaka;

import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.ComponentHelper;
import org.apache.tools.ant.Project;

/**
 * A task to list all tasks known to this project
 * 
 * @author wh81752
 * @since 2.1.10
 */

public class ListTasks extends Task
{
  static public void details(Project P) {
    ComponentHelper ch;
    Hashtable tab;
    Enumeration e;

    if (P != null) {
      ch = Static.getcomph(P);

      tab = ch.getTaskDefinitions();
      e = tab.keys();
      while (e.hasMoreElements()) {
        String k;
        k = (String) e.nextElement();
        System.out.println(k);
      }
    }
  }

  public void execute() throws BuildException {
    details(getProject());
    return;
  }
}
