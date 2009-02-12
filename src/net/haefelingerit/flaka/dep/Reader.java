package net.haefelingerit.flaka.dep;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collection;

import net.haefelingerit.flaka.Static;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class Reader
{
  static public Document getxmldoc(File file) throws Exception {
    return Static.getxmldoc(new FileInputStream(file));
  }

  static private char aliaschar(char c) {
    if (Character.isDigit(c))
      return c;
    if (Character.isLetter(c))
      return Character.toUpperCase(c);
    if (c == '_')
      return c;
    return '_';
  }

  static private String name2alias(String v) {
    String s;
    /* `v' is assumed to be not null */
    s = "";
    for (int k = 0; k < v.length(); ++k)
      s += aliaschar(v.charAt(k));
    return s;
  }

  static public void parse(InputStream stream, Collection list)
      throws Exception {
    Node node, kid;
    NodeList nodes, kids;
    Dependency dep;
    Document doc;
    String alias, type, tag, value;

    /* digest the given stream, return XML "document" */
    doc = Static.getxmldoc(stream);

    if (doc == null) {
      Static.debug("XML input stream parsed - `null' document returned");
      return;
    }

    /*
     * we just grep for <dependency ..> and do not care about it's envelope tag
     * at all
     */

    nodes = doc.getElementsByTagNameNS("*", "dependency");

    for (int i = 0; i < nodes.getLength(); i++) {
      dep = new Dependency();
      node = nodes.item(i);

      /*
       * check whether name has attribute "name". If so then * autoset
       * dependencies with property variables based * on "name".
       */
      alias = Static.nodeattribute(node, "alias");
      if (alias == null) {
        // TODO: allow this only in case of legacy mode
        // TODO: enable/disable legacy mode
        alias = Static.nodeattribute(node, "name");
      }
      type = Static.nodeattribute(node, "type");

      /* use "jar" as default type */
      if (type == null) {
        type = "jar";
      }

      /* set alias to be "<name>.<type>" */
      if (alias != null) {
        alias = name2alias(alias);
        alias += ".";
        alias += type;
        dep.setAlias(alias);
      }

      /* fetch relevant attributes */
      value = Static.nodeattribute(node, "id");
      if (value != null)
        dep.setId(value);
      value = Static.nodeattribute(node, "url");
      if (value != null)
        dep.setUrl(value);
      value = Static.nodeattribute(node, "version");
      if (value != null)
        dep.setVersion(value);
      value = Static.nodeattribute(node, "groupid");
      if (value != null)
        dep.setGroupId(value);
      value = Static.nodeattribute(node, "artifactid");
      if (value != null)
        dep.setArtifactId(value);
      value = Static.nodeattribute(node, "type");
      if (value != null)
        dep.setType(value);
      value = Static.nodeattribute(node, "jar");
      if (value != null)
        dep.setJar(value);
      value = Static.nodeattribute(node, "scope");
      if (value != null)
        dep.setScope(value);

      /* allow for Maven style deps */
      kids = node.getChildNodes();

      for (int j = 0; j < kids.getLength(); j++) {
        kid = kids.item(j);
        short nodeType = kid.getNodeType();
        if (nodeType != Node.ELEMENT_NODE) {
          continue;
        }

        tag = kid.getLocalName();
        value = getTextNodeValue((Element) kid);

        if (value == null) {
          continue;
        }

        value = value.trim();
        if (value.equals("")) {
          continue;
        }

        if ("id".equalsIgnoreCase(tag)) {
          dep.setId(value);
        } else if ("url".equalsIgnoreCase(tag)) {
          dep.setUrl(value);
        } else if ("version".equalsIgnoreCase(tag)) {
          dep.setVersion(value);
        } else if ("groupId".equalsIgnoreCase(tag)) {
          dep.setGroupId(value);
        } else if ("artifactId".equalsIgnoreCase(tag)) {
          dep.setArtifactId(value);
        } else if ("type".equalsIgnoreCase(tag)) {
          dep.setType(value);
        } else if ("jar".equalsIgnoreCase(tag)) {
          dep.setJar(value);
        } else if ("properties".equalsIgnoreCase(tag)) {
          getProperties(kid, dep);
        } else if ("scope".equalsIgnoreCase(tag)) {
          dep.setScope(value);
        }
      }
      /* add dependencies in arrival order !! */
      list.add(dep);
    }
  }

  /**
   * Adds arbitrary properties to the dependency
   * 
   * @param el
   *          a Node object from the DOM
   * @param dep
   *          the dependency to populate with any properties found
   */
  static private void getProperties(Node el, Dependency dep) {
    NodeList children = el.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node child = children.item(i);
      if (child.getNodeType() == Node.ELEMENT_NODE) {
        String textValue = getTextNodeValue((Element) child);
        if (textValue != null) {
          textValue = textValue.trim();
        }
        dep.putProperty(child.getLocalName(), textValue);
      }
    }
  }

  /**
   * Gets the text content of an element
   * 
   * @param node
   * @return
   */
  static private String getTextNodeValue(Element el) {
    NodeList children = el.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node child = children.item(i);
      if (child.getNodeType() == Node.TEXT_NODE) {
        return child.getNodeValue();
      }
    }
    return null;
  }

}
