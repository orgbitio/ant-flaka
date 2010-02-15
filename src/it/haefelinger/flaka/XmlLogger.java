/*
 * Copyright (c) 2009 Haefelinger IT 
 *
 * Licensed  under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required  by  applicable  law  or  agreed  to in writing, 
 * software distributed under the License is distributed on an "AS 
 * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied.
 
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */

package it.haefelinger.flaka;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Enumeration;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import org.apache.tools.ant.util.DOMElementWriter;
import org.apache.tools.ant.util.StringUtils;
import org.apache.tools.ant.BuildLogger;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.UnknownElement;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.util.DateUtils;

/**
 * This logger is basically the same as XmlLogger provided by Ant except: 1.
 * Tasks do not participate in logging 2. Messages containing newline chars are
 * split up, i.e. each "line" will be spit out in it's own message element (see
 * below). 3. Leading whitespace chars are stripped from messages 4. Tab
 * characters are normalized into " " (two blank chars).
 * 
 * Regarding 2: A message like "<echo>a\nb</echo>" will be logged as if written
 * like <echo>a</echo><echo>b</echo>. * @author merzedes
 * 
 * @since 1.0
 * 
 */
public class XmlLogger implements BuildLogger
{
  static protected Pattern pat = Pattern.compile("$", Pattern.MULTILINE | Pattern.UNIX_LINES);

  protected int msgOutputLevel = Project.MSG_DEBUG;
  protected PrintStream outStream;

  /** DocumentBuilder to use when creating the document to start with. */
  protected static DocumentBuilder builder = getDocumentBuilder();

  /**
   * Returns a default DocumentBuilder instance or throws an
   * ExceptionInInitializerError if it can't be created.
   * 
   * @return a default DocumentBuilder instance.
   */
  private static DocumentBuilder getDocumentBuilder()
  {
    try
    {
      return DocumentBuilderFactory.newInstance().newDocumentBuilder();
    } catch (Exception exc)
    {
      throw new ExceptionInInitializerError(exc);
    }
  }

  /** XML element name for a build. */
  private static final String BUILD_TAG = "build";
  /** XML element name for a target. */
  private static final String TARGET_TAG = "target";
  /** XML element name for a message. */
  private static final String MESSAGE_TAG = "message";
  /** XML attribute name for a name. */
  private static final String NAME_ATTR = "name";
  /** XML attribute name for a time. */
  private static final String TIME_ATTR = "time";
  /** XML attribute name for a message priority. */
  private static final String PRIORITY_ATTR = "priority";
  /** XML attribute name for an error description. */
  private static final String ERROR_ATTR = "error";
  /** XML element name for a stack trace. */
  private static final String STACKTRACE_TAG = "stacktrace";

  /** The complete log document for this build. */
  private Document doc = builder.newDocument();
  /** Mapping for when tasks started (Task to TimedElement). */
  private Hashtable tasks = new Hashtable();
  /** Mapping for when targets started (Task to TimedElement). */
  private Hashtable targets = new Hashtable();
  /**
   * Mapping of threads to stacks of elements (Thread to Stack of TimedElement).
   */
  private Hashtable threadStacks = new Hashtable();
  /**
   * When the build started.
   */
  protected TimedElement buildElement = null;

  /** Utility class representing the time an element started. */
  protected static class TimedElement
  {
    /**
     * Start time in milliseconds (as returned by
     * <code>System.currentTimeMillis()</code>).
     */
    protected long startTime;
    /** Element created at the start time. */
    protected Element element;

    public String toString()
    {
      return this.element.getTagName() + ":" + this.element.getAttribute("name");
    }
  }

  static public String formatTime(long ms)
  {
    return DateUtils.formatElapsedTime(ms);
  }

  /**
   * Fired when the build starts, this builds the top-level element for the
   * document and remembers the time of the start of the build.
   * 
   * @param event
   *          Ignored.
   */
  public void buildStarted(BuildEvent event)
  {
    this.buildElement = new XmlLogger.TimedElement();
    this.buildElement.startTime = System.currentTimeMillis();
    this.buildElement.element = this.doc.createElement(BUILD_TAG);
  }

  /**
   * Fired when the build finishes, this adds the time taken and any error
   * stacktrace to the build element and writes the document to disk.
   * 
   * @param event
   *          An event with any relevant extra information. Will not be
   *          <code>null</code>.
   */
  public void buildFinished(BuildEvent event)
  {
    long totalTime = System.currentTimeMillis() - this.buildElement.startTime;
    this.buildElement.element.setAttribute(TIME_ATTR, formatTime(totalTime));

    if (event.getException() != null)
    {
      this.buildElement.element.setAttribute(ERROR_ATTR, event.getException().toString());
      // print the stacktrace in the build loc it is always useful...
      // better have too much info than not enough.
      Throwable t = event.getException();
      Text errText = this.doc.createCDATASection(StringUtils.getStackTrace(t));
      Element stacktrace = this.doc.createElement(STACKTRACE_TAG);
      stacktrace.appendChild(errText);
      this.buildElement.element.appendChild(stacktrace);
    }

    String outFilename = event.getProject().getProperty("XmlLogger.file");
    if (outFilename == null)
    {
      outFilename = "log.xml";
    }
    String xslUri = event.getProject().getProperty("ant.XmlLogger.stylesheet.uri");
    if (xslUri == null)
    {
      xslUri = "log.xsl";
    }
    Writer out = null;
    try
    {
      // specify output in UTF8 otherwise accented characters will blow
      // up everything
      OutputStream stream = this.outStream;
      if (stream == null)
      {
        stream = new FileOutputStream(outFilename);
      }
      out = new OutputStreamWriter(stream, "UTF8");
      out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
      if (xslUri.length() > 0)
      {
        out.write("<?xml-stylesheet type=\"text/xsl\" href=\"" + xslUri + "\"?>\n\n");
      }
      (new DOMElementWriter()).write(this.buildElement.element, out, 0, "\t");
      out.flush();
    } catch (IOException exc)
    {
      throw new BuildException("Unable to write log loc", exc);
    } finally
    {
      if (out != null)
      {
        try
        {
          out.close();
        } catch (IOException e)
        {
          // ignore
        }
      }
    }
    this.buildElement = null;
  }

  /**
   * Returns the stack of timed elements for the current thread.
   * 
   * @return the stack of timed elements for the current thread
   */
  private Stack getStack()
  {
    Stack threadStack = (Stack) this.threadStacks.get(Thread.currentThread());
    if (threadStack == null)
    {
      threadStack = new Stack();
      this.threadStacks.put(Thread.currentThread(), threadStack);
    }
    return threadStack;
  }

  /**
   * Fired when a target starts building, this pushes a timed element for the
   * target onto the stack of elements for the current thread, remembering the
   * current time and the name of the target.
   * 
   * @param event
   *          An event with any relevant extra information. Will not be
   *          <code>null</code>.
   */
  public void targetStarted(BuildEvent event)
  {
    Target target = event.getTarget();
    TimedElement targetElement = new TimedElement();
    targetElement.startTime = System.currentTimeMillis();
    targetElement.element = this.doc.createElement(TARGET_TAG);
    targetElement.element.setAttribute(NAME_ATTR, target.getName());
    this.targets.put(target, targetElement);
    getStack().push(targetElement);
  }

  /**
   * Fired when a target finishes building, this adds the time taken and any
   * error stacktrace to the appropriate target element in the log.
   * 
   * @param event
   *          An event with any relevant extra information. Will not be
   *          <code>null</code>.
   */
  public void targetFinished(BuildEvent event)
  {
    Target target = event.getTarget();
    TimedElement targetElement = (TimedElement) this.targets.get(target);
    if (targetElement != null)
    {
      long totalTime = System.currentTimeMillis() - targetElement.startTime;
      targetElement.element.setAttribute(TIME_ATTR, formatTime(totalTime));

      TimedElement parentElement = null;
      Stack threadStack = getStack();
      if (!threadStack.empty())
      {
        TimedElement poppedStack = (TimedElement) threadStack.pop();
        if (poppedStack != targetElement)
        {
          throw new RuntimeException("Mismatch - popped element = " + poppedStack
              + " finished target element = " + targetElement);
        }
        if (!threadStack.empty())
        {
          parentElement = (TimedElement) threadStack.peek();
        }
      }
      if (parentElement == null)
      {
        this.buildElement.element.appendChild(targetElement.element);
      } else
      {
        parentElement.element.appendChild(targetElement.element);
      }
    }
    this.targets.remove(target);
  }

  /**
   * Fired when a task starts building, this pushes a timed element for the task
   * onto the stack of elements for the current thread, remembering the current
   * time and the name of the task.
   * 
   * @param event
   *          An event with any relevant extra information. Will not be
   *          <code>null</code>.
   */
  public void taskStarted(BuildEvent event)
  { /* not used */

  }

  /**
   * Fired when a task finishes building, this adds the time taken and any error
   * stacktrace to the appropriate task element in the log.
   * 
   * @param event
   *          An event with any relevant extra information. Will not be
   *          <code>null</code>.
   */
  public void taskFinished(BuildEvent event)
  { /* not used */

  }

  /**
   * Get the TimedElement associated with a task.
   * 
   * Where the task is not found directly, search for unknown elements which may
   * be hiding the real task
   */
  private TimedElement getTaskElement(Task task)
  {
    TimedElement element = (TimedElement) this.tasks.get(task);
    if (element != null)
    {
      return element;
    }

    for (Enumeration e = this.tasks.keys(); e.hasMoreElements();)
    {
      Task key = (Task) e.nextElement();
      if (key instanceof UnknownElement)
      {
        if (((UnknownElement) key).getTask() == task)
        {
          return (TimedElement) this.tasks.get(key);
        }
      }
    }

    return null;
  }

  /**
   * Fired when a message is logged, this adds a message element to the most
   * appropriate parent element (task, target or build) and records the priority
   * and text of the message.
   * 
   * @param event
   *          An event with any relevant extra information. Will not be
   *          <code>null</code>.
   */
  public void messageLogged(BuildEvent event)
  {
    int priority;
    String name;
    Throwable ex;
    TimedElement parent;
    Element elem;
    Text text;

    priority = event.getPriority();
    if (priority > this.msgOutputLevel)
    {
      return;
    }

    switch (priority)
    {
      case Project.MSG_ERR:
        name = "error";
        break;
      case Project.MSG_WARN:
        name = "warn";
        break;
      case Project.MSG_INFO:
        name = "info";
        break;
      default:
        name = "debug";
        break;
    }

    ex = event.getException();
    if (ex != null && Project.MSG_DEBUG <= this.msgOutputLevel)
    {
      text = this.doc.createCDATASection(StringUtils.getStackTrace(ex));
      elem = this.doc.createElement(STACKTRACE_TAG);
      elem.appendChild(text);
      this.buildElement.element.appendChild(elem);
    }

    parent = getParentEL(event);

    String[] line = XmlLogger.pat.split(event.getMessage());

    for (int i = 0; i < line.length; ++i)
    {
      String s = line[i].replaceAll("\\n|\\r", " ");
      s = s.replaceAll("^\\s+", ""); /* strip leading ws */
      s = s.replaceAll("\\t", "  "); /* normalize tabs */
      text = this.doc.createCDATASection(s);
      elem = this.doc.createElement(MESSAGE_TAG);
      elem.setAttribute(PRIORITY_ATTR, name);
      elem.appendChild(text);
      parent.element.appendChild(elem);
    }

  }

  TimedElement getParentEL(BuildEvent event)
  {
    TimedElement p = null;
    Task task = event.getTask();

    if (task != null)
    {
      p = getTaskElement(task);
    }
    if (p == null)
    {
      Target target = event.getTarget();
      if (target != null)
      {
        p = (TimedElement) this.targets.get(target);
      }
    }
    return p != null ? p : this.buildElement;
  }

  // -------------------------------------------------- BuildLogger interface

  /**
   * Set the logging level when using this as a Logger
   * 
   * @param level
   *          the logging level - see
   *          {@link org.apache.tools.ant.Project#MSG_ERR Project} class for
   *          level definitions
   */
  public void setMessageOutputLevel(int level)
  {
    this.msgOutputLevel = level;
  }

  /**
   * Set the output stream to which logging output is sent when operating as a
   * logger.
   * 
   * @param output
   *          the output PrintStream.
   */
  public void setOutputPrintStream(PrintStream output)
  {
    this.outStream = new PrintStream(output, true);
  }

  /**
   * Ignore emacs mode, as it has no meaning in XML format
   * 
   * @param emacsMode
   *          true if logger should produce emacs compatible output
   */
  public void setEmacsMode(boolean emacsMode)
  { /* not used */

  }

  /**
   * Ignore error print stream. All output will be written to either the XML log
   * loc or the PrintStream provided to setOutputPrintStream
   * 
   * @param err
   *          the stream we are going to ignore.
   */
  public void setErrorPrintStream(PrintStream err)
  { /* not used */

  }

}
