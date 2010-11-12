package it.haefelinger.flaka.el;

import it.haefelinger.flaka.util.Static;

import java.text.ParsePosition;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.property.ParseNextProperty;
import org.apache.tools.ant.property.PropertyExpander;

/**
 * Flaka's property expander.
 * 
 * This property expander handles #{..} and ${..} references. The word expander is
 * a kind of misnomer introduced by Ant. The job of this class is to extract the
 * body of an (embedded) reference, i.e. it evaluates ${foo} into 'foo'.
 * 
 * This expander also handles escaped <code>#</code> references, i.e. <code>##</code>
 * evaluates to <code>#</code> each and everywhere thus <code>##{..}</code> will stay
 * as <code>#{..}</code>.
 * 
 * In addition, this class also handles ${..} references in the very same way as #{..}
 * references are being handled. Notice that this will override Ant's standard property
 * expander. Why is ${..} handled here? Cause the default does not remove leading and
 * trailing whitespace. Thus ${p} and ${ p } would not be the same using Ant's standard
 * handler. The latter would reference a (property) with name ' p ', i.e. a property
 * name that starts and ends with a blank character. How awkward.
 * 
 * @author geronimo
 *
 */
public class ELPropertyExpander implements PropertyExpander
{

  /* (non-Javadoc)
   * @see org.apache.tools.ant.property.PropertyExpander#parsePropertyName(java.lang.String, java.text.ParsePosition, org.apache.tools.ant.property.ParseNextProperty)
   */
  public String parsePropertyName(String s, ParsePosition pos, ParseNextProperty parseNextProperty)
  {
    String r = null;
    int index, delta;
    char c1,c2;
    
    index = pos.getIndex();
    delta = s.length() - index;
    
    if (delta<2)
      return null;
    
    c1 = s.charAt(index);
    
    // handle '##'
    if (c1 == '#' && s.charAt(index+1) == '#') {
      pos.setIndex(index+1);
      return null;
    }
    
    if (delta < 3)
      return null;

    c2 = s.charAt(index + 1);

    if (c2 != '{')
      return null;
    
    // directly check near, triggering characters:
    if ( c1 == '$' ||  c1 == '#')
    {
      int start,end;
      start = index + 2;
      end = s.indexOf('}', start);
      if (end < 0)
      {
        String fmt = "Syntax Error: missing closing '}' in property reference `%s'";
        String msg = String.format(fmt, s.substring(index));
        throw new BuildException(msg);
      }
      pos.setIndex(end + 1);
      // String trailing and leading whitespace.
      r = Static.trim2(s.substring(start, end), "");
    }
    return r;
  }

}
