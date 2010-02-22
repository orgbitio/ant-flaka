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

import it.haefelinger.flaka.dep.Dependency;
import it.haefelinger.flaka.dep.Scanner;
import it.haefelinger.flaka.util.MatchingTask;
import it.haefelinger.flaka.util.Static;
import it.haefelinger.flaka.util.TextReader;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;

/**
 * 
 * @author merzedes
 * @since 1.0
 */
public class ScanDeps extends MatchingTask
{
  static protected Pattern P = Pattern.compile("^\\s*(\\w+)\\s*=\\s*(.*)");
  protected String var;
  protected String dir = "''.tofile";
  protected Map map;
  protected TextReader dec;

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
  
 
  
  public TextReader createDecorate() 
  {
    this.dec = new TextReader();
    this.dec.setProject(getProject());
    return this.dec;
  }
  
  
  protected java.util.List each()
  {
    return (ArrayList)this.map.get("each");
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
    // All scanned dependencies are now in this.map.get('each').
    if (this.dec != null)
    {
      java.util.List list;
      String line,key,val;
      Matcher p;
      File dest;
      
      while ((line = this.dec.readLine()) != null) {
        p = P.matcher(line);
        if (p.matches())
        {
          // This should be handled in a dynamic way: checkout whether
          // Dependency has 'set<<key>>(<<Type>>)' method. If so, then
          // convert val like   el2obj(str,<<Type>>) and then assign
          // that result using method set..".
          key = p.group(1);
          if (key.equals("dest")) {
            val = p.group(2);
            val = Static.el2str(project, val);
            dest = Static.toFile(project,val);
            
            list = each();
            for(int i=0;i<list.size();++i)
            {
              Dependency d = (Dependency)list.get(i);
              d.setFile(new File(dest,d.basename()));
            }
            continue;
          }
        }
      }
    }
    Static.assign(project, this.var, this.map, Static.VARREF);
  }
}
