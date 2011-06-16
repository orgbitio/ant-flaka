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

import it.haefelinger.flaka.el.EL;
import it.haefelinger.flaka.util.Groovenizer;
import it.haefelinger.flaka.util.GroovenizerFactory;
import it.haefelinger.flaka.util.Static;
import it.haefelinger.flaka.util.TextReader;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

/**
 * Load EL functions into EL instance.
 * 
 * @author merzedes
 * @since 1.3
 * 
 */
public class ELLoad extends Task {
  protected String ns;
  protected String text;
  protected String type = "groovy";

  /**
   * Define how to interpret the (inlined) text.
   * 
   * type | meaning class | text is a list of classes, one per line. groovy |
   * text is inlined groovy source code
   * 
   * @param type
   */
  public void setType(String type) {
    this.type = type;
  }

  public void setNS(String ns) {
    this.ns = ns;
  }

  /**
   * Experimental feature for adding inline functions.
   * 
   * @param text
   */
  public void addText(String text) {
    this.text = text;
  }

  public void execute() throws BuildException {
    Project project;

    project = this.getProject();
    try {
      if (this.type.matches("(?i)\\s*file/class\\s*")) {
        EL el = Static.el(project);
        String line;
        TextReader tr = new TextReader();
        
        tr.setText(this.text);
        while ((line = tr.readLine()) != null) {
          /* resolve properties */
          line = project.replaceProperties(line);
          /* resolve all EL references #{ ..} */
          line = Static.elresolve(project, line);
          /* interpret this line as name of a class */
          el.sourceFunctions(this.ns,Static.trim2(line,null));
        }
      }
      if (this.type.matches("(?i)\\s*file/groovy\\s*")) {
        EL el = Static.el(project);
        Class cz;
        String line;
        TextReader tr = new TextReader();
        
        tr.setText(this.text);
        while ((line = tr.readLine()) != null) {
          /* resolve properties */
          line = project.replaceProperties(line);
          /* resolve all EL references #{ ..} */
          line = Static.elresolve(project, line);
          /* interpret this line as name of a class */
          Groovenizer grvnzr = GroovenizerFactory.newInstance();
          cz = grvnzr.parse(new File(Static.trim2(line,"")));
          el.sourceFunctions(this.ns,cz);
        }
      }
      if (this.text != null && this.type.matches("(?i)\\s*text/groovy\\s*")) {
        Class cz;
        EL el;
        String text;

        /* resolve all Ant properties ${ } */
        text = project.replaceProperties(this.text);

        /* resolve all EL references #{ ..} */
        text = Static.elresolve(project, text);

        el = Static.el(project);
        if (el != null) {
          Groovenizer grvnzr = GroovenizerFactory.newInstance();
          cz = grvnzr.parse(text);
          el.sourceFunctions(this.ns, cz);
        }
      }

    } catch (SecurityException e) {
      throw new BuildException(e);
    } catch (ClassNotFoundException e) {
      throw new BuildException(e);
    } catch (Exception e) {
      throw new BuildException(e);
    }

  }
}
