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

package net.haefelingerit.flaka.el;

import java.io.File;
import java.util.Arrays;

import de.odysseus.el.misc.TypeConverterImpl;

@SuppressWarnings("serial")
public class TypeConverter extends TypeConverterImpl
{
  protected String coerceToString(Object value) {
    if (value == null) {
      return "";
    }
    if (value instanceof String) {
      return (String)value;
    }
    if (value instanceof Enum) {
      return ((Enum<?>)value).name();
    }
    if (value.getClass().isArray())
      return Arrays.toString((Object[])value);
    return value.toString();
  }
  
  
  protected Boolean coerceToBoolean(Object value)
  {
    if (value == null)
    {
      return Boolean.FALSE;
    }
    if (value instanceof Boolean)
    {
      return (Boolean) value;
    }
    if (value instanceof String)
    {
      return ((String) value).equals("") ? Boolean.FALSE : Boolean.TRUE;
    }
    if (value instanceof File)
    {
      return ((File) value).exists() ? Boolean.TRUE : Boolean.FALSE;
    }
    return Boolean.TRUE;
  }
}
