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
 * 
 * @author merzedes
 * @since 1.0
 */
public class Let extends Task
{
  protected TextReader tr = new TextReader();

 
  public void setComment(String s)
  {
    this.tr.setComment(s);
  }

  public void addText(String text)
  {
    this.tr.setProject(getProject());
    this.tr.addText(text);
  }

  public String toString()
  {
    StringBuilder buf = new StringBuilder();
    if (this.tr.getText() != null)
    {
      buf.append("<mset>\n");
      buf.append(this.tr.getText());
      buf.append("\n</mset>\n");
    } else
    {
      buf.append("<mset/>\n");
    }
    return buf.toString();
  }

  protected Pattern makeregex(String s)
  {
    Pattern re = null;
    try
    {
      re = Pattern.compile(s);
    } catch (Exception e)
    {
      /* TODO: error */
      this.debug("error compiling regex '" + s + "'", e);
    }
    return re;
  }

  protected Pattern getPropRegex()
  {
    return makeregex("([^=:]+)(=|:=|::=)(.*)");
  }

  protected int howto(String s)
  {
    int r = Static.VARREF;
    switch (s.charAt(0))
    {
      case '=':
        r = Static.VARREF;
        break;
      default:
        switch (s.length())
        {
          case 3: /* ::= */
            r = Static.WRITEPROPTY;
            break;
          default:
            r = Static.PROPTY;
        }
        break;
    }
    return r;
  }

  public void execute() throws BuildException
  {
    Project project;
    Pattern regex;
    Matcher M;
    String line;
    String k, v;
    Object o;
    int type;

    project = this.getProject();
    type = Static.VARREF;

 
    regex = getPropRegex();

    while ((line = this.tr.readLine()) != null)
    {
      /* eval text */
      if ((M = regex.matcher(line)).matches() == false)
      {
        debug("line : syntax error '" + line + "'");
        continue;
      }
      try
      {
        type = howto(M.group(2));
        k = M.group(1);
        if (type == Static.PROPTY && project.getProperty(k) != null)
          continue;
        v = M.group(3);
        o = Static.el2obj(project, v);
        Static.assign(project, k, o, type);

      } catch (Exception e)
      {
        if (this.debug)
          debug("line : error evaluating EL expression (ignored) in "
              + Static.q(line));
      }
    }
  }
}
