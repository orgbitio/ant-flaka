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

import it.haefelinger.flaka.util.Static;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.regex.Pattern;

import org.apache.tools.ant.BuildException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * @author merzedes
 * @since 1.0
 */
public class XmlMerger extends Task {
  protected String dst = null;
  protected String src = null;
  protected String pattern = "/.*\\.xml/";
  protected Pattern filter = null;
  protected FileFilter ffilter = null;
  protected boolean removeProperties = true;
  protected Element accu = null; /*
                                  * accumulated XML content
                                  */
  protected boolean doinit = true;

  public void setDst(String s) {
    this.dst = Static.trim2(s, this.src);
  }

  public void setSrc(String s) {
    this.src = Static.trim2(s, this.src);
  }

  public void setRoot(String s) {
    String root = Static.trim2(s, null);
    if (root != null) {
      this.accu = new Element(s);
      this.accu.addContent("\n");
    }
  }

  public void setPattern(String pattern) {
    this.pattern = Static.trim2(pattern, this.pattern);
    this.doinit = true;
  }

  public Pattern useFilter(Pattern filter) {
    Pattern r = this.filter;
    this.filter = filter;
    this.doinit = true;
    return r;
  }

  public Pattern getFilter() {
    return this.filter;
  }

  public Element getAccu() {
    return this.accu;
  }

  public Element useAccu(Element e) {
    Element r = this.accu;
    this.accu = e;
    return r;
  }

  public void setRemoveProperties(boolean b) {
    this.removeProperties = b;
  }

  public void validate() throws BuildException {
    /* not used */
  }

  public void initialize() throws BuildException {
    this.doinit = false;
    initFilter();
  }

  private void initFilter() throws BuildException {
    try {
      if (this.filter == null && this.pattern != null) {
        this.filter = Static.patterncompile(this.pattern, 0);
      }

      this.ffilter = new FileFilter() {
        public boolean accept(File file) {
          String s;
          if (file.isDirectory())
            return true;
          if (XmlMerger.this.filter == null)
            return true;
          s = file.getName();
          if (s == null)
            return false;
          return XmlMerger.this.filter.matcher(s).matches();
        }
      };
    } catch (Exception e) {
      throw new BuildException(e);
    }
  }

  public class TagFilter extends XMLFilterImpl {
    private final String tagName;
    private int depth = 0;

    public TagFilter(String tagName) {
      super();
      this.tagName = tagName;
    }

    public TagFilter(String tagName, XMLReader arg0) {
      super(arg0);
      this.tagName = tagName;
    }

    public void characters(char[] ch, int start, int length)
        throws SAXException {
      if (!isPruning()) {
        super.characters(ch, start, length);
      }
    }

    public void ignorableWhitespace(char[] ch, int start, int length)
        throws SAXException {
      if (!isPruning()) {
        super.ignorableWhitespace(ch, start, length);
      }
    }

    public void endElement(String uri, String localName, String qName)
        throws SAXException {
      if (isPruning()) {
        this.depth--;
      } else {
        super.endElement(uri, localName, qName);
      }
    }

    public void startElement(String uri, String localName, String qName,
        Attributes atts) throws SAXException {
      if (!isPruning() && localName.equals(this.tagName)) {
        this.depth = 1;
        return;
      }
      if (isPruning()) {
        this.depth++;
        return;
      }
      super.startElement(uri, localName, qName, atts);
    }

    private boolean isPruning() {
      return this.depth > 0;
    }
  }

  public SAXBuilder builder() throws Exception {
    SAXBuilder builder = new SAXBuilder();
    if (this.removeProperties) {
      builder.setXMLFilter(new TagFilter("properties"));
    }
    return builder;
  }

  public Element asElement(File file) throws BuildException {
    Element r = null;
    try {
      r = builder().build(file).getRootElement();
    } catch (JDOMException e) {
      warn("Could not read loc " + file + ".  Skipping...", e);
    } catch (IOException e) {
      warn("Could not read loc " + file + ".  Skipping...", e);
    } catch (Exception e) {
      throw new BuildException(e);
    }
    return r;
  }

  protected void merge(String[] args) {
    if (args != null)
      for (int i = 0; i < args.length; ++i)
        merge(new File(args[i]));
  }

  protected void merge(String s) {
    if (s != null) {
      merge(new File(s));
    }
  }

  protected void merge(File file) {
    File[] kids;
    Element e;

    if (file == null)
      return;

    /* initialize filters */
    if (this.doinit) {
      initialize();
    }

    if (!file.exists()) {
      info(file.toString() + " does not exist. Skipping ...");
      return;
    }
    if (file.isDirectory()) {
      kids = file.listFiles(this.ffilter);
      Arrays.sort(kids);
      for (int j = 0; j < kids.length; j++) {
        merge(kids[j]);
      }
      return;
    }
    if (!file.isFile()) {
      warn(file.toString() + " is not a directory or a loc. Skipping ...");
      return;
    }
    if (file.length() == 0) {
      warn(file.toString() + " is empty. Skipping ...");
      return;
    }
    e = asElement(file);
    if (e == null) {
      warn(file.toString() + " has no (wellformed) XML content. Skipping ...");
      return;
    }

    if (this.accu == null) {
      e.detach();
      this.accu = e;
    } else {
      this.accu.addContent(e.detach());
      this.accu.addContent("\n");
    }
  }

  public void execute() throws BuildException {
    try {
      /* accumulate */
      merge(this.src);
      /* and dump */
      if (this.accu != null)
        writeTo(this.dst);
    } catch (BuildException e) {
      throw e;
    } catch (Exception e) {
      throw new BuildException(e);
    }
  }

  protected void writeTo(OutputStream out) throws IOException {
    if (this.accu != null) {
      XMLOutputter sink;
      Document doc;
      OutputStream to = out;
      if (to == null) {
        to = System.out;
      }
      sink = new XMLOutputter();
      doc = new Document(this.accu);
      sink.output(doc, to);
      doc.detachRootElement();
    }
  }

  protected void writeTo(String s) throws IOException {
    if (s == null || s.trim().length() < 1 || s.trim().equals("-"))
      writeTo(System.out);
    else {
      File f = new File(s);
      if (f.exists() == false) {
        File p = f.getParentFile();
        if (p.exists() == false) {
          p.mkdirs();
        }
      }
      writeTo(new FileOutputStream(f));
    }
  }

  public static void main(String[] args) {
    try {
      XmlMerger M;

      M = new XmlMerger();
      M.setRoot("cruisecontrol");
      M.info("xx");

      /* accumulate */
      M.merge(args);

      /* write accumulated XML stuff to .. */
      M.writeTo(System.out);
    } catch (Exception e) {
      System.err.println("error: " + e.getMessage());
      e.printStackTrace();
    }
  }
}
