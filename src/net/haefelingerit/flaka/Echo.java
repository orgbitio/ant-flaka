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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.haefelingerit.flaka.util.EchoReader;
import net.haefelingerit.flaka.util.Static;
import net.haefelingerit.flaka.util.TextReader;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

/**
 * 
 * @author merzedes
 * @since 1.0
 */
public class Echo extends org.apache.tools.ant.taskdefs.Echo
{
  protected boolean debug = true;
  protected String comment;
  protected String shift = "";
  protected char ic = '>';

  public Echo()
  {
    super();
  }

  public void setDebug(boolean b)
  {
    this.debug = b;
  }

  public void setComment(String s)
  {
    this.comment = Static.trim2(s, this.comment);
  }

  public void setShift(String s)
  {
    Pattern p;
    Matcher m;

    s = Static.trim3(getProject(), s, "");
    p = Pattern.compile("(\\d+)\\*?(.*)");
    m = p.matcher(s);

    if (m.matches())
    {
      int times = Integer.parseInt(m.group(1));
      String what = Static.trim2(m.group(2), " ");
      StringBuilder accu = new StringBuilder();
      for (int i = 0; i < times; ++i)
        accu.append(what);
      this.shift = accu.toString();
    } else
    {
      this.shift = s;
    }
  }

  public void setIc(String s)
  {
    s = Static.trim2(s, "" + this.ic);
    this.ic = s.charAt(0);
  }

  protected String accumulate(TextReader reader)
  {
    String line;
    StringBuilder accu;

    accu = new StringBuilder();
    line = reader.readLine();
    while (line != null)
    {
      accu.append(line);
      line = reader.readLine();
      if (line != null)
        accu.append('\n');
    }
    return accu.toString();
  }

  final private String prettyfy()
  {
    EchoReader er;

    er = new EchoReader(this.message);
    er.setComment(this.comment);
    er.setContinuation(true);
    er.setSkipEmpty(false);
    er.shift = this.shift;
    er.ic = this.ic;
    return accumulate(er);
  }

  final private String strip()
  {
    TextReader tr;

    tr = new TextReader(this.message);
    tr.setComment(this.comment);
    tr.setContinuation(false);
    tr.setSkipEmpty(false);
    return accumulate(tr);
  }

  public void execute() throws BuildException
  {
    Project project;

    if (this.message != null && !this.message.matches("\\s*"))
    {
      project = getProject();

      /* strip comments */
      this.message = strip();

      /* resolve all EL references in message */
      this.message = Static.el2str(project, this.message);

      /* format message */
      this.message = prettyfy();
    }

    /* let my parent handle this */
    super.execute();
  }

}
