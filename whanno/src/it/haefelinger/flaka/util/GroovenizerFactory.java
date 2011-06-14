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

/**
 * A singleton factory for a Groovenizer instance.
 * 
 * This factory is thread-safe.
 * 
 * @author geronimo
 *
 */
public class GroovenizerFactory {
  /**
   * The one and only instance.
   */
  static Groovenizer instance;
  
  static {
    /* create instance on loading this class */
    instance = new GroovenizerImpl();
  }
  
  /**
   * Returns Groovenizer instance.
   * 
   * This function will always return the very same instance.
   * @return
   */
  static public Groovenizer newInstance() {
    return instance;
  }
}
