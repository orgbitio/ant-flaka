package it.haefelinger.flaka.el;

import it.haefelinger.flaka.util.Static;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.PropertyHelper;

public class ELPropertyEvaluator implements PropertyHelper.PropertyEvaluator {
  public Object evaluate(String property, PropertyHelper ph) {
    Object o;
    Project p;
    p = ph.getProject();
    o = Static.el2obj(p, property);
    return o;
  }
}
