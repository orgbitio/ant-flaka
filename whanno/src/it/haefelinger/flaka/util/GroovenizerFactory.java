package it.haefelinger.flaka.util;

public class GroovenizerFactory {
  static Groovenizer instance;
  
  static {
    instance = new GroovenizerImpl();
  }
  
  static public Groovenizer newInstance() {
    return instance;
  }
}
