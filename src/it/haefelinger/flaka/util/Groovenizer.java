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

import java.io.File;

/**
 * Interface to hide how and who is translating Groovy code into a
 * Java class.
 * 
 * @author geronimo
 * @since 1.3.0
 */

public interface Groovenizer {
    /**
     * Parse Groovy code from file.
     * 
     * @param file - {@code null} allowed
     * @return {@code null} in case of errors or if {@code file} was {@code null}
     */
    public Class parse(File file);
    
    /**
     * Parse Groovy code from character sequence.
     * 
     * @param text - {@code null} allowed
     * @return {@code null} in case of errors or if {@code file} was {@code null}
     */
    public Class parse(String text);
}
