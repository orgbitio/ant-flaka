package it.haefelinger.flaka.util;

import groovy.lang.GroovyClassLoader;

import java.io.File;
import java.io.IOException;

import org.codehaus.groovy.control.CompilationFailedException;

public class GroovenizerImpl implements Groovenizer {
  public Class parse(String text) {
    Class clazz = null;
    if (text != null) {
      ClassLoader parent = getClass().getClassLoader();
      GroovyClassLoader loader = new GroovyClassLoader(parent);
      clazz = loader.parseClass(text);
    }
    return clazz;
  }
  
  public Class parse(File file) {
    Class clazz = null;
    if (file != null) {
      ClassLoader parent = getClass().getClassLoader();
      GroovyClassLoader loader = new GroovyClassLoader(parent);
      try {
        clazz = loader.parseClass(file);
      } catch (CompilationFailedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
        return null;
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
        return null;
      }
    }
    return clazz;
  }

}
