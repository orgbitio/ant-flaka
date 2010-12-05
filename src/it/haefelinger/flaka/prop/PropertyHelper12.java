package it.haefelinger.flaka.prop;

import it.haefelinger.flaka.util.Static;

import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.PropertyHelper;

/**
 * Deals with properties - substitution, dynamic properties, etc.
 * 
 * <p>
 * This code has been heavily restructured for Ant 1.8.0. It is expected that
 * custom PropertyHelper implementation that used the older chaining mechanism
 * of Ant 1.6 won't work in all cases, and its usage is deprecated. The
 * preferred way to customize Ant's property handling is by {@link #add adding}
 * {@link PropertyHelper.Delegate delegates} of the appropriate subinterface and
 * have this implementation use them.
 * </p>
 * 
 * <p>
 * When {@link #parseProperties expanding a string that may contain properties}
 * this class will delegate the actual parsing to
 * {@link org.apache.tools.ant.property.ParseProperties#parseProperties
 * parseProperties} inside the ParseProperties class which in turn uses the
 * {@link org.apache.tools.ant.property.PropertyExpander PropertyExpander
 * delegates} to find properties inside the string and this class to expand the
 * propertiy names found into the corresponding values.
 * </p>
 * 
 * <p>
 * When {@link #getProperty looking up a property value} this class will first
 * consult all {@link PropertyHelper.PropertyEvaluator PropertyEvaluator}
 * delegates and fall back to an internal map of "project properties" if no
 * evaluator matched the property name.
 * </p>
 * 
 * <p>
 * When {@link #setProperty setting a property value} this class will first
 * consult all {@link PropertyHelper.PropertySetter PropertySetter} delegates
 * and fall back to an internal map of "project properties" if no setter matched
 * the property name.
 * </p>
 * 
 * @since Flaka 1.2
 */

public class PropertyHelper12 extends PropertyHelper implements IFPropertyHelper
{
  protected boolean enabled = true;

  public PropertyHelper12()
  {
    super();
  }

  public boolean enable(boolean b)
  {
    boolean c = this.enabled;
    this.enabled = b;
    return c;
  }

  protected void copyProperties(PropertyHelper ph)
  {
    // copy over all properties.
    Hashtable p;
    Enumeration e;
    Object k, v;

    p = ph.getProperties();
    e = p.keys();
    while (e.hasMoreElements())
    {
      k = e.nextElement();
      v = p.get(k);
      this.setProperty((String) k, v, false);
    }

    p = ph.getInheritedProperties();
    e = p.keys();
    while (e.hasMoreElements())
    {
      k = e.nextElement();
      v = p.get(k);
      this.setInheritedProperty((String) k, v);
    }

    p = ph.getUserProperties();
    e = p.keys();
    while (e.hasMoreElements())
    {
      k = e.nextElement();
      v = p.get(k);
      this.setUserProperty((String) k, v);
    }

  }

  public void setProject(Project project)
  {
    PropertyHelper otherhelper;
    super.setProject(project);
    otherhelper = PropertyHelper.getPropertyHelper(project);
    if (otherhelper == this)
    {
      throw new BuildException("uups, Ant's interface changed again.");
    }
    copyProperties(otherhelper);
  }

  /**
   * Decode properties from a String representation. If the entire contents of
   * the String resolve to a single property, that value is returned. Otherwise
   * a String is returned.
   * 
   * @param value
   *          The string to be scanned for property references. May be
   *          <code>null</code>, in which case this method returns immediately
   *          with no effect.
   * 
   * @exception BuildException
   *              if the string contains an opening <code>${</code> without a
   *              closing <code>}</code>
   * @return the original string with the properties replaced, or
   *         <code>null</code> if the original string is <code>null</code>.
   */
  public Object parseProperties(String value) throws BuildException
  {
    Object obj;
    // Ask the original handler to resolve ${..} references for me.
    obj = super.parseProperties(value);

    if (this.enabled && obj != null && obj instanceof String)
    {
      // Resolve EL References #{..}
      String text = obj.toString();
      Project p = this.getProject();
      obj = Static.elresolve(p, text);
    }
    return obj;
  }

}
