package it.haefelinger.flaka;

import it.haefelinger.flaka.util.Static;

import java.util.Iterator;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.TaskContainer;

/**
 * @author merzedes
 * @since 1.0
 */
public class While extends it.haefelinger.flaka.Task implements TaskContainer {
  protected String test;
  protected Vector tasks = new Vector();

  public void setTest(String test) {
    this.test = Static.elresolve(this.getProject(), test);
  }

  public void addTask(Task nestedTask) {
    this.tasks.add(nestedTask);
  }

  protected void exectasks() throws BuildException {
    Iterator iter;
    Task task;

    iter = this.tasks.iterator();
    while (iter.hasNext()) {
      task = (Task) iter.next();
      task.perform();
    }
  }

  protected boolean eval() {
    Project project;
    // TODO: parse EL into value expression once, then only eval that Ast.
    project = getProject();
    return Static.el2bool(project, this.test);
  }

  public void execute() throws BuildException {
    if (this.test == null) {
      // TODO: debug message
      return;
    }

    while (eval()) {
      try {
        /* exec all tasks using on current list item */
        exectasks();
      } catch (BuildException bx) {
        String s;
        s = bx.getMessage();
        /* we are looking for a special designed message */
        if (s == null)
          throw bx;
        /* handle special break statement */
        if (s.endsWith(Break.TOKEN))
          break;
        /* handle continue statement */
        if (s.endsWith(Continue.TOKEN))
          continue;
        /* regular exception */
        throw bx;
      }
    }
  }
}
