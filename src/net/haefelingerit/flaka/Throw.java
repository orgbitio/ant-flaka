/*
 * Copyright (c) 2001-2004 Ant-Contrib project. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package net.haefelingerit.flaka;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Exit;
import org.apache.tools.ant.types.Reference;

/**
 * Extension of <code>&lt;fail&gt;</code> that can throw an exception that is
 * a reference in the project.
 * 
 * <p>
 * This may be useful inside the <code>&lt;catch&gt;</code> block of a
 * <code>&lt;trycatch&gt;</code> task if you want to rethrow the exception
 * just caught.
 * </p>
 */

public class Throw extends Exit
{
  protected Reference reference = null;

  public void setVar(Reference var) {
    this.reference = var;
  }

  public void setReference(Reference var) {
    this.reference = var;
  }

  public void execute() throws BuildException {
    Object obj = null;

    if (this.reference != null) {
      obj = this.reference.getReferencedObject(getProject());
    } else {
      obj = getProject().getReference(TryCatch.REFERENCE);
    }

    if (obj != null) {
      if (obj instanceof BuildException) {
        throw (BuildException) obj;
      }
      if (obj instanceof Exception) {
        throw new BuildException((Exception) obj);
      }
      if (!(obj instanceof Throwable)) {
        throw new BuildException(obj.toString());
      }
    }

    super.execute();
  }
}
