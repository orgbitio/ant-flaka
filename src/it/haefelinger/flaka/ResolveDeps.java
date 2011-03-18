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

import it.haefelinger.flaka.dep.Dependency;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

/**
 * @author merzedes
 * @since 1.0
 */
public class ResolveDeps extends Task {
  protected String var = "project.dependencies";
  protected File baseline;

  public void setBaseline(File f) {
    this.baseline = f;
  }

  public void execute() {
    if (this.baseline == null) {
      /* compile default baseline */
      File d = toFile(this.getProperty("baseline.dir"));
      String b = this.getProperty("baseline");
      this.baseline = new File(d, b + ".txt");
    }
    if (this.baseline != null) {
      if (this.baseline.isFile() == false) {
        warn("Baseline " + this.baseline.getAbsolutePath()
            + " not a file or not existing.");
        warn("Giving up.");
        return;
      }
    }

    Properties Baseline = null;
    FileInputStream fin;

    try {
      fin = new FileInputStream(this.baseline);
      Baseline = new Properties();
      Baseline.load(fin);
      /* evaluate all properties */
      // Eval.eval(Baseline,this.getProject());
    } catch (Exception ex) {
      String s;
      s = this.baseline.getPath();
      throwbx("error while loading Baseline `" + s + "', got `" + ex + "'");
    }

    Collection C = null;
    try {
      C = (Collection) this.getref(this.var);
      if (C == null || C.isEmpty()) {
        debug("empty collection: " + this.var);
        return;
      }
    } catch (Exception e) {
      debug("not a collection: " + this.var);
      return;
    }

    /*
     * When building a snapshot, the version of an internal dependency shall be
     * SNAPSHOT regardless of the Baseline. This clashes with the default
     * implementation used so far. For a smooth transition, the new behaviour
     * will be enabled if property "updatemode" is set to snapshot.
     */

    Iterator i = C.iterator();

    while (i.hasNext()) {
      Dependency d = null;
      try {
        d = (Dependency) i.next();
      } catch (Exception e) {
        continue;
      }
      if (d == null)
        continue;
      String s;
      s = d.getAlias();
      if (s != null) {
        debug("resolving alias `" + s + "' ..");
        d.resolve(Baseline);
      }
    }
  }
}
