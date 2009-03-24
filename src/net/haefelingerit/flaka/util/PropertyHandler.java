package net.haefelingerit.flaka.util;

import java.util.Hashtable;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.PropertyHelper;

public class PropertyHandler extends org.apache.tools.ant.PropertyHelper
{
  public PropertyHandler(org.apache.tools.ant.PropertyHelper handler)
  {
    super();
    setNext(handler);
    setProject(handler.getProject());
  }

  public void setProject(Project p)
  {
    this.getNext().setProject(p);
  }

  public Project getProject()
  {
    return getNext().getProject();
  }

  protected void debug(String msg)
  {
    Static.debug(getProject(), msg);
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

  public void copyInheritedProperties(Project other)
  {
    getNext().copyInheritedProperties(other);
  }

  public void copyUserProperties(Project other)
  {
    getNext().copyUserProperties(other);
  }

  public Hashtable getProperties()
  {
    return getNext().getProperties();
  }

  public synchronized Object getProperty(String ns, String name)
  {
    return getNext().getProperty(ns, name);
  }

  public Object getPropertyHook(String ns, String name, boolean user)
  {
    return getNext().getPropertyHook(ns, name, user);
  }

  public Hashtable getUserProperties()
  {
    return getNext().getUserProperties();
  }

  public synchronized Object getUserProperty(String ns, String name)
  {
    return getNext().getUserProperty(ns, name);
  }

 
  public synchronized void setInheritedProperty(String ns, String name, Object value)
  {
    getNext().setInheritedProperty(ns, name, value);
  }

  public synchronized void setNewProperty(String ns, String name, Object value)
  {
    getNext().setNewProperty(ns, name, value);
  }

  public synchronized boolean setProperty(String ns, String name, Object value, boolean verbose)
  {
    return getNext().setProperty(ns, name, value, verbose);
  }

  public boolean setPropertyHook(String ns, String name, Object value, boolean inherited,
      boolean user, boolean isNew)
  {
    return getNext().setPropertyHook(ns, name, value, inherited, user, isNew);
  }

  public synchronized void setUserProperty(String ns, String name, Object value)
  {
    this.getNext().setUserProperty(ns, name, value);
  }
  
  /**
   * This method is reponsible for resolving references ..
   *  (non-Javadoc)
   * @see org.apache.tools.ant.PropertyHelper#replaceProperties(java.lang.String, java.lang.String, java.util.Hashtable)
   */
  public String replaceProperties(String ns, String text, Hashtable keys) throws BuildException
  {
    PropertyHelper next;
    Project project;
    
    next = getNext();
    project = getProject();
    /* let underlying property helper resolve references to properties */
    text = next.replaceProperties(ns,text,keys);
    
    /* resolve references to embedded EL expressions */
    text = Static.elresolve(project, text);
    return text;
  }
 
}
