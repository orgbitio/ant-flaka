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

/**
 * 
 */
package net.haefelingerit.flaka;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.haefelingerit.flaka.dep.Scanner;
import net.haefelingerit.flaka.util.MatchingTask;
import net.haefelingerit.flaka.util.Static;

import org.apache.tools.ant.DirectoryScanner;

/**
 * @author geronimo
 * 
 */
public class ScanDeps extends MatchingTask
{
  protected String var = "project.dependencies";
  protected File dir = null;
  protected Scanner scanner;

  public void setVar(String var)
  {
    this.var = Static.trim2(var, this.var);
  }

  public void setDir(File dir)
  {
    this.dir = dir;
  }

  protected void scan4deps(File file)
  {
    String fname;
    InputStream is = null;

    fname = file.getAbsolutePath();
    if (file.isFile() == false)
    {
      debug("ignored reading deps from " + fname);
      return;
    }
    debug("scanning dependencies from `" + fname + "' ..");
    try
    {
      is = new FileInputStream(file);
      /* parse a stream */
      this.scanner.reset();
      this.scanner.scan(is);
      this.scanner.annotate(file);
    } catch (Exception e)
    {
      debug("error reading dependencies from `" + fname + "' (ignored)", e);
    } finally
    {
      Static.close(is);
    }

  }

  protected DirectoryScanner getds()
  {
    DirectoryScanner ds = null;
    File dir = this.dir;
    if (dir == null)
      dir = this.toFile(null);
    ds = super.getDirectoryScanner(dir);
    return ds;
  }

  public void execute()
  {
    DirectoryScanner ds = getds();
    String[] filelist;

    this.scanner = new Scanner(this.getProject());
    filelist = ds.getIncludedFiles();
    for (int i = 0; i < filelist.length; ++i)
    {
      File file = this.toFile(filelist[i]);
      scan4deps(file);

      /* Assign or merge my dependencies with project.dependencies */
      Collection C;
      C = (Collection) this.getref(this.var);
      // TODO: make sure not to include twice?
      if (C != null)
        C.addAll(this.scanner.list);
      else
      {
        List list = new ArrayList();
        list.addAll(this.scanner.list);
        makeref(this.var, list);
      }
    }
  }
}
