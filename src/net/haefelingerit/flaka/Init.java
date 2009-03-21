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

package net.haefelingerit.flaka;

import org.apache.tools.ant.BuildException;

/**
 * @author geronimo
 * @deprecated
 * 
 */
public class Init extends Task
{

  protected Object comph = null;

  public void execute() throws BuildException
  {
    // Project P;
    // PropertyHelper ph;
    // org.apache.tools.ant.PropertyHelper current;
    // Enumeration e;
    // String ANT_HELPER_REFID = "ant.PropertyHelper";
    // String ns = null;
    //
    // /* get my project */
    // P = getProject();
    // /* create new property helper */
    // ph = new PropertyHelper();
    // ph.setProject(P);
    //
    // /* get current property helper */
    // current = org.apache.tools.ant.PropertyHelper.getPropertyHelper(P);
    //
    // /* install my property handler */
    // P.getReferences().put(ANT_HELPER_REFID, ph);
    //
    // /* copy all properties from current project in my property helper */
    // e = current.getProperties().keys();
    // while (e.hasMoreElements()) {
    // Object arg = e.nextElement();
    // if (!(arg instanceof String))
    // continue;
    // String k = (String) arg;
    // Object v = current.getProperty(ns, k);
    // ph.setProperty(ns, k, v, false);
    // }
    //
    // /* copy user properties */
    // current.copyUserProperties(P);
    //
    // /* copy inherited properties */
    // current.copyInheritedProperties(P);
  }

}
