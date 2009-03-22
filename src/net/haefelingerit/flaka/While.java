package net.haefelingerit.flaka;

import java.util.Iterator;
import java.util.Vector;

import net.haefelingerit.flaka.util.Static;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.TaskContainer;

public class While extends net.haefelingerit.flaka.Task implements TaskContainer
{
  protected String test;
  protected Vector tasks = new Vector();

  public void setTest(String test)
  {
    this.test = Static.trim3(getProject(), test, this.test);
  }

  public void addTask(Task nestedTask)
  {
    this.tasks.add(nestedTask);
  }

  protected void exectasks() throws BuildException
  {
    Iterator iter;
    Task task;

    iter = this.tasks.iterator();
    while (iter.hasNext())
    {
      task = (Task) iter.next();
      task.perform();
    }
  }

  protected boolean eval()
  {
    // TODO: parse EL into value expression once, then only eval that Ast.
    return Static.el2bool(getProject(),"#{"+this.test+"}");
  }

  public void execute() throws BuildException
  {
    if (this.test == null)
    {
      // TODO: debug message
      return;
    }

    while (eval())
    {
      try
      {
        /* exec all tasks using on current list item */
        exectasks();
      } catch (BuildException bx)
      {
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
