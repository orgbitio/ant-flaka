/*
 * Copyright (c) 2009 Haefelinger IT 
 *
 * Licensed  under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required  by  applicable  law  or  agreed  to in writing, 
 * software distributed under the License is distributed on an "AS 
 * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied.
 
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */

package net.haefelingerit.flaka;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

import net.haefelingerit.flaka.dep.Dependency;
import net.haefelingerit.flaka.util.Static;

import org.apache.tools.ant.BuildException;

/**
 * A task which outputs a CLASSPATH ..
 * 
 * @author <a href="mailto:flaka (at) haefelingerit (dot) net">Wolfgang
 *         H&auml;felinger</a>
 */

public class WriteDeps extends Task
{
  /* shall point to a filelist set .. */
  protected String var = "project.dependencies";
  protected String out = "-";
  protected String fmt = "maven";
  protected String ifs = "\\s+";

  public void setIfs(String s)
  {
    this.ifs = Static.trim2(s, this.ifs);
  }

  /**
   * Change the resource variable to be used when reporting dependencies.
   * 
   * The default variable is <b>project.dependencies</b>. This variable comes
   * usually into existence when using task
   * {@link net.haefelingerit.flaka.GetDeps} to retrieve dependencies from a
   * depot.
   * 
   * @param var
   */
  public void setVar(String s)
  {
    this.var = Static.trim2(s, this.var);
  }

  /**
   * Write dependencies to loc <b>s</b>.
   * 
   * By default dependencies are writen to stdout (denoted by string "-").
   * 
   * @param s
   */
  public void setOut(String s)
  {
    this.out = Static.trim2(s, this.out);
  }

  /**
   * Change the format in which dependencies are written.
   * 
   * The default format is "maven". Alternative formats are:
   * <ul>
   * <li>flat, depotpath</li>
   * <li>var, alias</li>
   * </ul>
   * 
   * @param s
   */
  public void setFmt(String s)
  {
    this.fmt = Static.trim2(s, this.fmt);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.tools.ant.Task#execute()
   */
  public void execute() throws BuildException
  {
    String s = null;
    Dependency d = null;
    Collection D = null;

    try
    {
      this.info("ref: |" + this.var + "|");
      D = (Collection) this.getref(this.var);
      /* warning message if no dependency referenced */
      if (D == null && this.debug)
        this.info(this.var + " does not hold (dependency) references.");
      if (D != null && this.debug)
      {
        this.info("found #" + D.size());
        System.out.println("found #" + D.size() + " {" + D.hashCode() + "}");
      }
    } catch (Exception e)
    {
      if (this.debug)
      {
        this.info(this.var + ": collection expected");
      }
    }
    Iterator i;

    // Report dependencies in Maven style
    if (this.fmt.matches("(?i:maven)"))
    {
      boolean gotone = false;
      s = "";
      if (D != null && D.size() > 0)
      {
        i = D.iterator();
        while (i.hasNext())
        {
          Object o = i.next();
          String p;
          if (o instanceof Dependency)
          {
            d = (Dependency) o;
            p = d.toAliased();
            if (p != null)
            {
              gotone = true;
              s += p;
            }
          }
        }
      }
      if (gotone == false)
      {
        s = "<dependencies />";
      } else
        s = "<dependencies>\n" + s + "</dependencies>";
      this.write(s);
      return;
    }

    // This format shall report dependencies as "files". Together with
    // the depot's base URL it allows the download of a dependency
    // using a tool like wget.
    // Example:
    // <dependency name="log4j">
    // => external/log4j/jars/log4j-1.2.8.jar

    if (this.fmt.matches("(?i:m1|m1path)"))
    {
      /*
       * Make sure to writesomething out, otherwise some stupid Ant task
       * complain (like loadfile).
       */
      s = "# Maven 1 paths\n";
      if (D != null && D.size() > 0)
      {
        i = D.iterator();
        while (i.hasNext())
        {
          Object o = i.next();
          String p;
          if (o instanceof Dependency)
          {
            d = (Dependency) o;
            p = d.m1path();
            if (p != null)
            {
              s += p;
              s += '\n';
            }
          }
        }
      }
      this.write(s);
      return;
    }

    if (this.fmt.matches("(?i:m2|m2path)"))
    {
      s = "# Maven 2 paths\n";
      if (D != null && D.size() > 0)
      {
        i = D.iterator();
        while (i.hasNext())
        {
          Object o = i.next();
          String p = null;
          if (o instanceof Dependency)
          {
            d = (Dependency) o;
            p = d.m2path();
            if (p != null)
            {
              s += p;
              s += '\n';
            }
          }
        }
      }
      this.write(s);
      return;
    }

    // An "Flaka" dependency has an "alias" name. This is the name
    // used to lookup the dependency in the Baseline. When updating the
    // Baseline, a project will get asked to report dependencies which
    // are part of the Baseline.
    //
    // Example:
    // <dependency name="log4j" />
    // => LOG4J
    if (this.fmt.matches("(?i:var|alias)"))
    {
      s = "# Aliases (symbolic dependencies) used\n";
      if (D != null && D.size() > 0)
      {
        i = D.iterator();
        while (i.hasNext())
        {
          Object o = i.next();
          String p;
          if (o instanceof Dependency)
          {
            d = (Dependency) o;
            System.out.println(d);
            p = d.getAlias();
            if (p != null)
            {
              s += p;
              s += '\n';
            }
          }
        }
      }
      this.write(s);
      return;
    }

    /* unknown format */
    throwbx("format `" + this.fmt + "' not supported for listing dependencies.");
  }

  public String[] splitbyifs(String s)
  {
    String[] argv = null;
    try
    {
      argv = s.split(this.ifs);
    } catch (Exception e)
    {
      if (this.debug)
      {
        this.log(e.getCause().getMessage());
      }
    }
    return argv;
  }

  protected void write(String buf) throws BuildException
  {
    String[] argv = null;
    boolean stdout = true;
    boolean stderr = true;
    String s;

    argv = splitbyifs(this.out);

    for (int j = 0; j < argv.length; ++j)
    {
      s = argv[j].trim();
      if (s == null || s.equals("") || s.equals("-"))
      {
        if (stdout)
        {
          System.out.println(buf);
          stdout = false;
        }
        continue;
      }
      if (s.equals("--"))
      {
        if (stderr)
        {
          System.err.println(buf);
          stderr = false;
        }
        continue;
      }
      File file = toFile(s);
      try
      {
        Static.writex(file, buf, false);
        if (this.debug)
          log("wrote dependencies into loc `" + s + "' using format `" + this.fmt + "'.");
      } catch (Exception e)
      {
        String fname = file.getAbsolutePath();
        throwbx("failed to writing to `" + fname + "'", e);
      }
    }
  }
}
