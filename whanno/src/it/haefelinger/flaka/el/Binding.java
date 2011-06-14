package it.haefelinger.flaka.el;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Binding {
 
  String name() default "";
  /**
   * The type of this binding:
   * 1 : Add annotated static method into context.
   * 2 : Invoke static method and enter object returned into context.
   * 3 : Invoke static method and enter returned method into context.
   * @return 
   */
  int type() default 1;  
}
