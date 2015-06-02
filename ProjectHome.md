
---

<font color='red' size='5' face='verdana'><b>Flaka 1.2.2</b> has been released!</font>

**Amsterdam, June 11<sup>th</sup> 2011**. Flaka 1.2.2 has been released today. This releases tackles some minor issues, like [issue 3](https://code.google.com/p/flaka/issues/detail?id=3) (ability to unset user properties), [issue 8](https://code.google.com/p/flaka/issues/detail?id=8) (EL function matches) and [issue 9](https://code.google.com/p/flaka/issues/detail?id=9) (EL function glob). Update to your liking.

---


Flaka is a plugin for [Ant](http://ant.apache.org) providing

  * An Expression language allowing access to data objects
  * Conditional and repetitive control structures like when, unless, while, for,  choose, switch ..
  * Exception handling
  * Additional tasks, types and macros ..

What Flaka is not:

  * No new syntax, it's still XML
  * It's still Ant.

A small script to list all files in build file's _base folder_  along with their last modification date:
```
<project name="demo" xmlns:c="antlib:it.haefelinger.flaka">
  <c:install-property-handler />
  <c:for var="f" in=" file(project).list ">
     <echo>
        #{  format('file %s, last modified %tD', f.name,f.mtime)  }
     </echo>
   </c:for>
</project>
```

Have a look at [Flaka's manual](http://workbench.haefelinger.it/flaka/download/manual/flaka-1.2.2.html) for further information or checkout [Manual](Manual.md)  for older versions or other formats.