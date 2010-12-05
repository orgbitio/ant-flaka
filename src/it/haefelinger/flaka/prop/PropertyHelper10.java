package it.haefelinger.flaka.prop;

import it.haefelinger.flaka.util.Static;

import java.util.Hashtable;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.PropertyHelper;

public class PropertyHelper10 extends org.apache.tools.ant.PropertyHelper implements IFPropertyHelper
{
  protected Project project;
  protected boolean enabled = true;
  
  @SuppressWarnings("deprecation")
  public void setProject(Project project)
  {
    PropertyHelper otherhelper;
    super.setProject(project);
    this.project = project;
    otherhelper = PropertyHelper.getPropertyHelper(project);
    if (otherhelper == this)
    {
      throw new BuildException("uups, Ant's interface changed again.");
    }
    this.setNext(otherhelper);
  }

  public Project getProject() {
    return this.project;
  }
  
  public boolean enable(boolean b) {
    boolean c = this.enabled;
    this.enabled = b;
    return c;
  }
  
  protected Hashtable getInternalInheritedProperties()
  {
    /* should not be called */
    return null;
  }

  protected Hashtable getInternalProperties()
  {
    /* should not be called */
    return null;
  }

  protected Hashtable getInternalUserProperties()
  {
    /* should not be called */
    return null;
  }

  @SuppressWarnings("deprecation")
  public void copyInheritedProperties(Project other)
  {
    getNext().copyInheritedProperties(other);
  }

  @SuppressWarnings("deprecation")
  public void copyUserProperties(Project other)
  {
    getNext().copyUserProperties(other);
  }

  @SuppressWarnings("deprecation")
  public Hashtable getProperties()
  {
    return getNext().getProperties();
  }

  @SuppressWarnings("deprecation")
  public synchronized Object getProperty(String ns, String name)
  {
    return getNext().getProperty(ns, name);
  }

  @SuppressWarnings("deprecation")
  public Object getPropertyHook(String ns, String name, boolean user)
  {
    return getNext().getPropertyHook(ns, name, user);
  }

  @SuppressWarnings("deprecation")
  public Hashtable getUserProperties()
  {
    return getNext().getUserProperties();
  }

  @SuppressWarnings("deprecation")
  public synchronized Object getUserProperty(String ns, String name)
  {
    return getNext().getUserProperty(ns, name);
  }

  @SuppressWarnings("deprecation")
  public synchronized void setInheritedProperty(String ns, String name, Object value)
  {
    getNext().setInheritedProperty(ns, name, value);
  }

  @SuppressWarnings("deprecation")
  public synchronized void setNewProperty(String ns, String name, Object value)
  {
    getNext().setNewProperty(ns, name, value);
  }

  @SuppressWarnings("deprecation")
  public synchronized boolean setProperty(String ns, String name, Object value, boolean verbose)
  {
    return getNext().setProperty(ns, name, value, verbose);
  }

  @SuppressWarnings("deprecation")
  public boolean setPropertyHook(String ns, String name, Object value, boolean inherited,
      boolean user, boolean isNew)
  {
    return getNext().setPropertyHook(ns, name, value, inherited, user, isNew);
  }

  @SuppressWarnings("deprecation")
  public synchronized void setUserProperty(String ns, String name, Object value)
  {
    this.getNext().setUserProperty(ns, name, value);
  }

  /**
   * This method is reponsible for resolving references .. (non-Javadoc)
   * 
   * @see org.apache.tools.ant.PropertyHelper#replaceProperties(java.lang.String,
   *      java.lang.String, java.util.Hashtable)
   */
  @SuppressWarnings("deprecation")
  public String replaceProperties(String ns, String text, Hashtable keys) throws BuildException
  {
    PropertyHelper next;
    Project project;

    next = getNext();
    project = getProject();
    /* let underlying property helper resolve references to properties */
    text = next.replaceProperties(ns, text, keys);

    /* resolve references to embedded EL expressions */
    if (this.enabled)
      text = Static.elresolve(project, text);
    return text;
  }

}
