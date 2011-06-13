package it.haefelinger.flaka.util;

import it.haefelinger.flaka.el.ELFunction;

public class ELFunctions {

  @ELFunction 
  public static String lowercase(Object obj) {
    return obj == null ? null : obj.toString().toLowerCase();
  }
}
