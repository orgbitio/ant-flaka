package it.haefelinger.flaka.el;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Binding {
  public enum Type {
    FUNCTION,             /* use annotated method as EL function */
    VARIABLE,             /* call annotated method, use value returnd as EL variable */
    FUNCTION_INDIRECT     /* call annotated method, use value returned as EL function */
  }

  String name() default "";

  /**
   * The type of this binding: 1 : Add annotated static method into context. 2 :
   * Invoke static method and enter object returned into context. 3 : Invoke
   * static method and enter returned method into context.
   * 
   * @return
   */
  Type type() default Type.FUNCTION;

}
