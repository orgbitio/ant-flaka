package it.haefelinger.flaka.el;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ELFunction {
  String name() default "";
}
