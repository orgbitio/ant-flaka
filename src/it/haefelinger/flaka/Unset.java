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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

/**
 * @author merzedes
 * @since 1.0
 */
public class Unset extends Task
{
  protected String comment;
  protected String text = "";

  public void setComment(String comment)
  {
    this.comment = Static.trim2(comment, this.comment);
  }

  public void addText(String text)
  {
    this.text = Static.trim2(text, this.text);
  }

  public void execute() throws BuildException
  {
    Project project;
    TextReader tr;
    String line;

    project = getProject();
    tr = new TextReader(this.text).setComment(this.comment);
    tr.setSkipEmpty(true);

    while ((line = tr.readLine()) != null)
    {
      line = project.replaceProperties(line);
      line = Static.elresolve(project, line);
      line = line.trim();
      Static.unset(project, line);
    }

  }
}
