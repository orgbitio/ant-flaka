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
public class Unset extends Task {
  protected TextReader tr = new TextReader();

  public void setComment(String comment) {
    this.tr.setCL(comment);
  }

  public void setCs(String s) {
    // TODO: document me
    this.tr.setCL(s);
  }

  public void setIcs(String s) {
    // TODO: document me
    this.tr.setIC(s);
  }

  public void setCL(boolean b) {
    // TODO: document me
    this.tr.setResolveContLines(b);
  }

  public void addText(String text) {
    this.tr.setText(text);
  }

  public void execute() throws BuildException {
    Project project;
    String line;

    project = getProject();
    while ((line = this.tr.readLine()) != null) {
      line = line.trim();
      line = project.replaceProperties(line);

      /* resolve all EL references #{ ..} */
      line = Static.elresolve(project, line);

      Static.unset(project, line);
    }

  }
}
