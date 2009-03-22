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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.haefelingerit.flaka.dep.Dependency;
import net.haefelingerit.flaka.dep.Retriever;
import net.haefelingerit.flaka.util.Printf;
import net.haefelingerit.flaka.util.Static;

import org.apache.tools.ant.BuildException;

/**
 * 
 * @author merzedes
 * @since 1.0
 */
public class GetDeps extends Task
{
  protected String var = "project.dependencies";
  protected File dstdir = null;
  protected URL[] depotlist;
  protected String ifs = ",";
  protected Retriever retriever;

  public GetDeps()
  {
    super();
    this.retriever = new Retriever(this);
  }

  public void setVar(String var)
  {
    this.var = Static.trim2(var, this.var);
  }

  public void setDst(File file)
  {
    this.dstdir = file;
  }

  /**
   * Sets the remote repository
   */
  public void setSrc(String s)
  {
    this.depotlist = toURLs(s);
  }

  protected String depotlist(String pfx)
  {
    String s = "";
    for (int i = 0; i < this.depotlist.length; ++i)
    {
      s += pfx;
      s += "[" + i + "] " + this.depotlist[i];
      s += "\n";
    }
    return s;
  }

  protected void displaystatus(Collection C)
  {
    int cntr = 0;
    Dependency d;
    String f;
    String s, pfx;
    long kB;
    Printf pf;
    Iterator i = C.iterator();
    pf = new Printf("     [%04d");

    log("     __DEPENDENCY_REPORT____________________");
    while (i.hasNext())
    {
      cntr += 1;
      pfx = pf.sprintf(cntr);
      d = (Dependency) i.next();
      f = d.getLocation();
      s = d.basename() + ((f != null) ? " (" + f + ")" : "");
      kB = 0L;
      if (d.getFile() != null)
      {
        kB = d.getFile().length() / 1000L;
      }
      switch (d.getStatus())
      {
        case Dependency.UNRESOLVED:
          log(pfx + "|?u] " + s + " (* unresolved *)");
          break;
        case Dependency.ISCURRENT:
          log(pfx + "|*=] " + s + " (" + kB + "kB)");
          break;
        case Dependency.RETRIEVED_REMOTE:
          log(pfx + "|*r] " + s + " (" + kB + "kB)");
          break;
        case Dependency.RETRIEVED_CACHE:
          log(pfx + "|*c] " + s + " (" + kB + "kB)");
          break;
        case Dependency.IOERROR_REMOTE:
          warn(pfx + "|?i] " + s + " (* IO retrieval error *)");
          break;
        case Dependency.NOTFOUND:
          warn(pfx + "|?^] " + s + " (* not found *)");
          break;
        case Dependency.ILLEGAL_STATE:
          log(pfx + "|??] " + s + " (* illegal dependency *)");
          break;
      }
    }
  }

  /**
   * Parses the string list of URLs given
   * 
   * @return a List of URLs in String form
   * @throws BuildException
   *           if ??
   */
  public URL[] toURLs(String repostr) throws BuildException
  {
    if (repostr == null || repostr.trim().length() == 0)
    {
      /* no or empty repository -> empty alt list */
      return null;
    }
    /* split string by seperator */
    String[] cand = repostr.split(this.ifs);
    ArrayList list = new ArrayList();

    for (int i = 0; i < cand.length; ++i)
    {
      String repo;

      repo = cand[i].trim();
      if (repo.equals(""))
      {
        continue;
      }

      /* check - must be a valid alt .., otherwise we skip */
      try
      {
        URL url = new URL(repo);
        list.add(url);
      } catch (MalformedURLException e)
      {
        /* we also try this whether we can make a URL */
        try
        {
          URL url = new URL("file://localhost/" + repo);
          list.add(url);
        } catch (MalformedURLException e2)
        {
          Static.throwbx("URL `" + repo + "' is malformed", e);
        }
      }
    }
    URL[] R = new URL[list.size()];
    for (int j = 0; j < R.length; ++j)
      R[j] = (URL) list.get(j);
    return R;
  }

  /**
   * defines execution of ant task
   */
  public void execute()
  {

    Collection C = null;
    Iterator i;
    List retrieved = new ArrayList();
    List failed = new ArrayList();
    List all = new ArrayList();

    /* use current working directory */
    if (this.dstdir == null)
      this.dstdir = toFile(null);

    /* set destination folder */
    this.retriever.setLocalDir(this.dstdir);

    if (this.depotlist == null)
    {
      this.depotlist = toURLs(getProperty("depot.csv"));
    }
    if (this.depotlist == null)
    {
      throwbx("error: no retrieval repository - use 'depotlist'");
    }
    this.retriever.setRepositories(this.depotlist);

    try
    {
      C = (Collection) this.getref(this.var);
      if (C == null || C.isEmpty())
      {
        debug("collection " + this.var + " is empty");
        return;
      }
    } catch (Exception e)
    {
      debug("reference " + this.var + " not a collection");
      return;
    }

    i = C.iterator();
    while (i.hasNext())
    {
      Dependency d = null;
      try
      {
        d = (Dependency) i.next();
      } catch (Exception e)
      {
        d = null;
      }
      if (d == null)
        continue;
      all.add(d);

      if (this.retriever.retrieve(d))
        retrieved.add(d);
      else
        failed.add(d);
    }

    displaystatus(all);

    int total = all.size();

    if (failed.isEmpty() == false)
    {
      String pfx = "     ";
      error("\n\n");
      error(pfx + "__DEPENDENCY_RETRIEVAL_ERROR___________\n");
      error(pfx + "Failed to retrieve " + (failed.size()) + "/" + total + " dependencies from:");
      error(depotlist(pfx));
      error("\n");
      Static.throwbx("error retrieving dependencies");
    }
  }
}
