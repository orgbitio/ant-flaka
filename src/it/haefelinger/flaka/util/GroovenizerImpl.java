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

package it.haefelinger.flaka.util;

import groovy.lang.GroovyClassLoader;

import java.io.File;
import java.io.IOException;

import org.codehaus.groovy.control.CompilationFailedException;

/**
 * An implementation of interface Groovenizer.
 * 
 * @author geronimo
 *
 */
public class GroovenizerImpl implements Groovenizer {
  
  /**
   * Return a GroovyClassLoader instance.
   * @return ever null?
   */
  protected GroovyClassLoader getLoader() {
    ClassLoader parent;
    GroovyClassLoader loader;
    parent = getClass().getClassLoader();
    loader = new GroovyClassLoader(parent);
    return loader;
  }
  
  /* (non-Javadoc)
   * @see it.haefelinger.flaka.util.Groovenizer#parse(java.lang.String)
   */
  public Class parse(String text) {
    Class clazz = null;
    if (text != null) {
       clazz = getLoader().parseClass(text);
    }
    return clazz;
  }
  
  /* (non-Javadoc)
   * @see it.haefelinger.flaka.util.Groovenizer#parse(java.io.File)
   */
  public Class parse(File file) {
    Class clazz = null;
    if (file != null) {
       try {
        clazz = getLoader().parseClass(file);
      } catch (CompilationFailedException e) {
        // TODO Auto-generated catch block
        // e.printStackTrace();
        return null;
      } catch (IOException e) {
        // TODO Auto-generated catch block
        // e.printStackTrace();
        return null;
      }
    }
    return clazz;
  }

}
