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

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.FunctionMapper;
import javax.el.ValueExpression;
import javax.el.VariableMapper;

public class Context extends ELContext
{
  private Functions functions;
  private Variables variables;
  private ELResolver resolver;

  static class Functions extends FunctionMapper
  {
    Map<String, Method> map = Collections.emptyMap();

    public Method resolveFunction(String prefix, String localName)
    {
      return this.map.get(prefix + ":" + localName);
    }

    public void setFunction(String prefix, String localName, Method method)
    {
      if (this.map.isEmpty())
        this.map = new HashMap<String, Method>();
      this.map.put(prefix + ":" + localName, method);
    }
  }

  static class Variables extends VariableMapper
  {
    Map<String, ValueExpression> map = Collections.emptyMap();

    public ValueExpression resolveVariable(String variable)
    {
      return this.map.get(variable);
    }

    public ValueExpression setVariable(String variable, ValueExpression expression)
    {
      if (this.map.isEmpty())
        this.map = new HashMap<String, ValueExpression>();
      return this.map.put(variable, expression);
    }
  }

  protected Context()
  {
    this(null);
  }

  /**
   * Create a context, use the specified resolver.
   */
  public Context(ELResolver resolver)
  {
    if (resolver == null)
      throw new NullPointerException("context requires a resolver");
    this.resolver = resolver;

  }

  /**
   * Get our function mapper.
   */

  public FunctionMapper getFunctionMapper()
  {
    if (this.functions == null)
      this.functions = new Functions();
    return this.functions;
  }

  /**
   * Get our variable mapper.
   */

  public VariableMapper getVariableMapper()
  {
    if (this.variables == null)
      this.variables = new Variables();
    return this.variables;
  }

  /**
   * Define a function.
   */
  public void setFunction(String prefix, String localName, Method method)
  {
    if (this.functions == null)
      this.functions = new Functions();
    this.functions.setFunction(prefix, localName, method);
  }

  /**
   * Define a variable.
   */
  public ValueExpression setVariable(String name, ValueExpression expression)
  {
    if (this.variables == null)
      this.variables = new Variables();
    return this.variables.setVariable(name, expression);
  }

  public ELResolver getELResolver()
  {
    return this.resolver;
  }
}
