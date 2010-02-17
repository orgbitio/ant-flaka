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
public class Echo extends org.apache.tools.ant.taskdefs.Echo
{
  protected boolean debug = true;
  protected String comment = ";";
  protected String shift = "";

  public Echo()
  {
    super();
  }

  public void setDebug(boolean b)
  {
    this.debug = b;
  }

  /**
   * Set the character  (sequence) identifying a comment line. A line
   * having only whitespace characters before that character sequence
   * will be ignored. In order to turn this feature off, provide the
   * empty string or a string consisting of whitespace characters
   * only.
   * 
   * The default comment sequence is <code>;</code>.
   * @param s not null
   */
  public void setComment(String s)
  {
    if (s.matches("\\s*"))
      this.comment = null;
    else
      this.comment = s;
  }

  /**
   * Set the shift attribute.
   * 
   * When set, then every line dumped will be prefixed by shome shift
   * characters.
   * 
   * By default shifting is disabled.
   * 
   * The general format of this attribute is <code>\d+(.*)</code>.
   * 
   * 
   * 
   * @param s
   */
  public void setShift(String s)
  {
    /* do not trim, leave as is */
    this.shift = s;
  }

 
  
  public void execute() throws BuildException
  {
    Project project;

    if (this.message != null && !this.message.matches("\\s*"))
    {
      project = getProject();

      TextReader tr = new TextReader();
      tr.setText(this.message);
      tr.setSkipEmpty(false);
      tr.setResolveContLines(true);
      tr.setComment(this.comment);
      tr.setSkipws(true);
      tr.setShift(this.shift);
      
      // Read all text in one go instead line by line.
      this.message = tr.read();
      
      /* resolve all EL references in message */
      this.message = Static.elresolve(project, this.message);

    }

    /* let my parent handle this */
    super.execute();
  }

}
