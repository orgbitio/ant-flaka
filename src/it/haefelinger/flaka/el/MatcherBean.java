package it.haefelinger.flaka.el;

import java.util.regex.Matcher;

/**
 * The sole purpose of this class is to answer questions on <em>properties</em>
 * asked by a resolver, usually a bean resolver (hence the name BeanMatcher).
 * 
 * 
 * @author merzedes
 * @since 1.0
 */
public class MatcherBean {
  final Matcher m;
  final int index;

  @SuppressWarnings("unused")
  private MatcherBean() {
    this.m = null;
    this.index = 0;
  }

  public MatcherBean(Matcher m, int index) {
    this.m = m;
    this.index = index;
  }

  public Matcher getMatcher() {
    return this.m;
  }

  public int getStart() {
    return this.m.start(this.index);
  }

  public int getS() {
    return getStart();
  }

  public int getEnd() {
    return this.m.end(this.index);
  }

  public int getE() {
    return getEnd();
  }

  public String getPattern() {
    return this.m.pattern().pattern();
  }

  public String getP() {
    return getPattern();
  }

  public int getGroups() {
    return this.m.groupCount();
  }

  public int getN() {
    return getGroups();
  }

  public int getLength() {
    return getGroups();
  }

  public int getSize() {
    return getGroups();
  }

  public String toString() {
    return this.m.group(this.index);
  }

}