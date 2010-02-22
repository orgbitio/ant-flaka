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
package it.haefelinger.flaka;

import it.haefelinger.flaka.dep.Scanner;
import it.haefelinger.flaka.util.MatchingTask;
import it.haefelinger.flaka.util.Static;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;

/**
 * 
 * @author merzedes
 * @since 1.0
 */
public class ScanDeps extends MatchingTask
{
  protected String var;
  protected String dir = "''.tofile";
  protected Map map;

  // protected Scanner scanner;

  public void setVar(String var)
  {
    this.var = Static.trim3(getProject(), var, this.var);
  }

  public void setDir(String dir)
  {
    this.dir = dir;
  }

  protected void scan(String fname)
  {
    File file;
    Scanner scanner;
    Project project;
    
    project = this.getProject();
    file = Static.toFile(project, fname);
    this.map = new HashMap();
    scanner = new Scanner(project,this.map);
    scanner.scan(file);
  }

  protected DirectoryScanner getds(File dir)
  {
    DirectoryScanner ds = null;
    if (dir != null)
      ds = super.getDirectoryScanner(dir);
    return ds;
  }

  public void execute()
  {
    Project project;
    DirectoryScanner ds;
    String[] args;

    project = getProject();
    ds = getds(Static.el2file(project, this.dir));
    if (ds != null)
    {
      args = ds.getIncludedFiles();
      for (int i = 0; i < args.length; ++i)
      {
        scan(args[i]);
      }
    }
    Static.assign(project, this.var, this.map, Static.VARREF);
  }
}
