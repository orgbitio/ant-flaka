package net.haefelingerit.flaka;

import java.io.File;

import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.AntClassLoader;

import java.net.URL;

public class WhichFlaka extends Task
{
  protected String resource = "net/haefelingerit/flaka/antlib.xml";
  protected String varname  = "flaka.jar";
  protected Path   classpath;

  /**
   * Set the classpath to be used for this compilation.
   * 
   * @param cp
   *          the classpath to be used.
   */
  public void setClasspath(Path cp) {
    if (this.classpath == null) {
      this.classpath = cp;
    } else {
      this.classpath.append(cp);
    }
  }

  /**
   * Adds a path to the classpath.
   * 
   * @return a classpath to be configured.
   */
  public Path createClasspath() {
    if (this.classpath == null) {
      this.classpath = new Path(getProject());
    }
    return this.classpath.createPath();
  }

  /**
   * execute it
   * 
   * @throws BuildException
   *           on error
   */
  public void execute() throws BuildException {
    AntClassLoader loader;

    if (this.classpath != null) {
      getProject().log("using user supplied classpath: " + this.classpath,
          Project.MSG_DEBUG);
      this.classpath = this.classpath.concatSystemClasspath("ignore");
    } else {
      this.classpath = new Path(getProject());
      this.classpath = this.classpath.concatSystemClasspath("only");
      getProject().log("using system classpath: " + this.classpath,
          Project.MSG_DEBUG);
    }

    loader = new AntClassLoader(getProject().getCoreLoader(), getProject(),
        this.classpath, false);

    if (this.resource.startsWith("/")) {
      this.resource = this.resource.substring(1);
    }

    log("Searching for " + this.resource, Project.MSG_VERBOSE);
    URL url;
    url = loader.getResource(this.resource);
    if (url != null) {
      log("url = " + url.toString());
      getProject().setNewProperty(this.varname + ".path", url.getPath());
      getProject().setNewProperty(this.varname + ".file", url.getFile());
      getProject().setNewProperty(this.varname + ".proto", url.getProtocol());
      getProject().setNewProperty(this.varname + ".host", url.getHost());
      getProject().setNewProperty(this.varname + ".port", "" + url.getPort());
      getProject().setNewProperty(this.varname + ".query", url.getQuery());
      getProject().setNewProperty(this.varname + ".ref", url.getRef());

      String path = url.getPath();
      if (path.startsWith("file:")) {
        path = path.substring("file:".length());
      }
      int idx = path.lastIndexOf("!");
      if (idx >= 0) {
        path = path.substring(0, idx);
      }
      getProject().setNewProperty(this.varname, path);
      File file = new File(path);
      getProject().setNewProperty(this.varname + ".exists",
          file.exists() ? "true" : "false");
      if (file.getParentFile() != null) {
        getProject().setNewProperty(this.varname + ".dirname",
            file.getParentFile().getAbsolutePath());
      } else {
        getProject().setNewProperty(this.varname + ".dirname", "");
      }
      getProject().setNewProperty(this.varname + ".basename", file.getName());

    }
  }

  /**
   * name the resource to look for
   * 
   * @param resource
   *          the name of the resource to look for.
   * @ant.attribute group="oneof"
   */
  public void setResource(String s) {
    this.resource = s;
  }

  /**
   * the property to fill with the URL of the resource or class
   * 
   * @param property
   *          the property to be set.
   * @ant.attribute group="required"
   */
  public void setVar(String s) {
    this.varname = s;
  }

}
