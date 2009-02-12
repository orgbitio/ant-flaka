package net.haefelingerit.flaka;

import org.apache.tools.ant.util.GlobPatternMapper;

/**
 * Provides same functionality as Ant's GlobPatternMapper except * that it never
 * returns null. If there's no match then the * input filename <code>s</code>gets
 * returned. * * Ant behaves very strangely regarding Mappers. The idear behind *
 * Mappers is to map filenames. One would surely expect that non * matching
 * files are simply left intact. The original version of * GlobPatternMapper
 * however returns null, meaning that a non * matching file is removed from
 * further processing. But that's * the job of a selector. *
 */

public class GlobMap extends GlobPatternMapper
{
  public String[] mapFileName(String s) {
    String[] r;
    r = super.mapFileName(s);
    if (r == null) {
      r = new String[] { s };
    }
    return r;
  }
}
