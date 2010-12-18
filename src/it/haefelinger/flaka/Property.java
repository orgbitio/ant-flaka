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

package it.haefelinger.flaka;

import it.haefelinger.flaka.util.Static;
import it.haefelinger.flaka.util.TextReader;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

/**
 * A task to set multiple properties at once.
 * 
 * 
 * @author merzedes
 * @since 1.0
 */
public class Property extends Task
{
  protected TextReader tr = new TextReader();

  public void setComment(String s)
  {
    this.tr.setCL(s);
  }

  public void setCL(String s)
  {
    this.tr.setCL(s);
  }

  public void setIC(String s)
  {
    this.tr.setIC(s);
  }

  public void addText(String text)
  {
    this.tr.setText(text);
  }

  protected Pattern makeregex(String s)
  {
    Pattern re = null;
    try
    {
      re = Pattern.compile(s);
    }
    catch (Exception e)
    {
      /* TODO: error */
      Static.debug(getProject(), "error compiling regex '" + s + "'", e);
    }
    return re;
  }

  protected Pattern getPropRegex()
  {
    return makeregex("([^=:]+)[:=](.*)");
  }

  public void execute() throws BuildException
  {
    Project project;
    Pattern regex;
    Matcher M;
    String line, k, v;

    project = this.getProject();
    regex = this.getPropRegex();

    while ((line = this.tr.readLine()) != null)
    {
      line = project.replaceProperties(line);

      /* resolve all EL references #{ ..} */
      line = Static.elresolve(project, line);

      // Unescape escaped characters
      // TODO: I believe this should be done after (key,val) separation.
      line = TextReader.unescape(line);

      if (!(M = regex.matcher(line)).matches())
      {
        Static.debug(getProject(), "line : bad property line '" + line + "'");
        continue;
      }
      // otherwise:
      k = M.group(1);
      v = M.group(2).trim();
      try
      {
        // k = project.replaceProperties(k);
        // k = Static.elresolve(project, k);
        // v = project.replaceProperties(v);
        // v = Static.elresolve(project, v);
      }
      catch (Exception e)
      {
        Static.debug(project, "line : error evaluating EL expression (ignored) in " + Static.q(v));
      }
      Static.assign(project, k, v, Static.PROPTY);
    }
  }

}
