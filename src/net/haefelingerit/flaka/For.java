package net.haefelingerit.flaka;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.util.TaskLogger;

public class For extends Task
{
  /* Based on For implementation found in antcontrib */
  protected net.haefelingerit.flaka.imp.For delegate = new net.haefelingerit.flaka.imp.For();
  protected boolean             havebody = false;

  /**
   * 
   */
  public For() {
    super();
    this.delegate.setList("");
    this.delegate.setParam("x");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.tools.ant.ProjectComponent#setProject(org.apache.tools.ant.Project)
   */
  public void setProject(Project project) {
    super.setProject(project);
    this.delegate.setProject(project);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.tools.ant.Task#setOwningTarget(org.apache.tools.ant.Target)
   */
  public void setOwningTarget(Target target) {
    super.setOwningTarget(target);
    this.delegate.setOwningTarget(target);
  }

  /**
   * The argument list to be iterated over.
   * 
   * @param list
   */
  public void setIn(String list) {
    this.delegate.setList(list);
  }

  /**
   * Set the var attribute. This is the name of the macrodef attribute that gets
   * set for each iterator of the sequential element.
   * 
   * @param param
   *          the name of the macrodef attribute.
   */
  public void setVar(String param) {
    this.delegate.setParam(param);
  }

  /**
   * This is a path that can be used instread of the list attribute to interate
   * over. If this is set, each path element in the path is used for an
   * interator of the sequential element.
   * 
   * @param path
   *          the path to be set by the ant script.
   */
  public void addConfigured(Path path) {
    this.delegate.addConfigured(path);
  }

  /**
   * This is a path that can be used instread of the list attribute to interate
   * over. If this is set, each path element in the path is used for an
   * interator of the sequential element.
   * 
   * @param path
   *          the path to be set by the ant script.
   */
  public void addConfiguredPath(Path path) {
    this.delegate.addConfiguredPath(path);
  }

  /**
   * A nested Sequential.
   * 
   * @return
   */
  public Object createSequential() {
    this.havebody = true;
    return this.delegate.createSequential();
  }

  /**
   * Run the for task. This checks the attributes and nested elements, and if
   * there are ok, it calls doTheTasks() which constructes a macrodef task and a
   * for each interation a macrodef instance.
   */
  public void execute() throws BuildException {
    if (!this.havebody)
      this.createSequential();
    this.delegate.setLogger(new TaskLogger(this));
    this.delegate.execute();
  }

}
