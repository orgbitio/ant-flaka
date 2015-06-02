# Introduction #

**THIS IS WORK IN PROGESS AND NOT YET RELEASED**

Flaka's EL has been charged with a documented list of functions and values. So far it was not possible to extend this list.

# Details #

Flaka's EL has been charged with a documented list of functions and values. So far it was not possible to extend this list. Some effort has been put into changing this. There will be a new task `<el-load />` accepting
  * a Java class name
  * embedded Groovy source code

The Groovy code will be compiled into a Java class on the fly, i.e. at runtime. In either way, the Java class is expected to contain a arbitrary list of methods which are
  * `static`
  * `public`
  * annotated by Annotation `it.haefelinger.flaka.el.Binding`

The number or arguments or return type does not matter. Especially is it possible to use variable-length arguments.

Let's start by adding a `sprintf()` function. Flaka contains already function `format()` which addresses the same purpose, namely generating a formated string. Furthermore, every `string` object contains a method `format` which would do the very same. Nevertheless, here is how an arbitrary Java class could look like

```
package my;
import it.haefelinger.flaka.el.Binding;

/**
 * A class containing functions to be imported into an EL context.
 */

public class ELBinding {
  @Binding
  static public String sprintf(String fmt, Object... args) {
    return String.format(fmt, args);
  }
}
```

Your Ant build script would then contain a task importing this class:

```
<project xmlns:fl="antlib:it.haefelinger.flaka" >
  ..
  <fl:el-load ns="my" type="file/class">
     ;;; The class containing my EL Bindings (see above).
     ;;; An arbitrary numer of classes may go here, one class per line.
     my.ELBinding
  </fl:el-load>
 ..
```

Here `format()` is be imported as `sprintf()` in namespace `my`. And off we go :
```
  <fl:echo>
  #{   my:sprintf("%s/%s",'foo','bar') }         ;; foo/bar
  </fl:echo>
```

This all works as expected if `my.ELBinding` made it's way into the JVM's classpath. Thus `my/ELBinding.java` must have been compiled in a previous step using a Java compiler. This compilation step can be omitted by adding a dependency on Groovy. In this case, just rename `my/ELBinding.java` into `my/ELBinding.groovy` and change `<fl:el-load />` into

```
  <fl:el-load ns="my" type="file/groovy">
     ;;; The Groovy file contains my EL Bindings (see above).
     ;;; An arbitrary numer of Groovy files may go here, one file per line.
     my/ELBinding.groovy
  </fl:el-load>
 ..
   <fl:echo>
   ;; This time sprintf() implemented in Groovy!
   #{   my:sprintf("%s/%s",'foo','bar') }         ;; foo/bar
  </fl:echo>
```

To make this work, `groovy-all.jar` must be added as runtime dependency. Due to it's size, `groovy-all.jar` has not been inlined into `ant-flaka.jar` as done with other jars Flaka depends on:

```
  > ant -lib ant-flaka.jar -lib groovy-all.jar ..
  [fl:echo] foo/bar
  ..
```

So far it's all fine and well, but how about defining my EL binding directly in my build script without outsourcing them?

```
  <fl:el-load ns="my" type="text/groovy">
     /* just Groovy .. */
     import it.haefelinger.flaka.el.Binding;
     
    @Binding
    static def sprintf(String fmt, Object... args) { String.format(fmt, args); }
  </fl:el-load>
```

That isn't that painful, is it? Of course you need to add groovy-all.jar as runtime dependency as shown before.

So far it has been shown how to charge EL with additional functions. How about adding some EL variables? Or how about adding a static function from an already existing class?

```
package my;
import it.haefelinger.flaka.el.Binding;

public class ELBinding {
  @Binding(type=Binding.Type.VARIABLE)                        /* adding variable "pi" */
  static public  Object pi () {
    return new Double(Math.PI);         
  }

  @Binding(type = Binding.Type.FUNCTION_INDIRECT)   /* adding function "rand()" */
  static public Method rand() throws SecurityException, NoSuchMethodException {
    return Math.class.getMethod("random");
  }
}
```

Both binding types, type _VARIABLE_ and type _FUNCTION\_INDIRECT_, are expected to annotate a parameter-less static and public method. While loading those bindings, the annotated static methods are invoked and expected to return an arbitrary instance if type is _VARIABLE_ and a method instance if type is _FUNCTION\_INDIRECT_. Note that this is different from type _FUNCTION_, being the default, where the static method is just added to EL's context when loading binding.

When loading bindings into EL's context, which names are assigned? By default it is the basic name of the annotated method. This is especially important for binding type _FUNCTION\_INDIRECT_, where the name of the method return is not taken into account. Therefore the name will be `rand` rather than `random`.

Is it possible to use a different name? Yes it is although a scenario where this is relevant needs still to be discovered. Here is an example:

```
package my;
import it.haefelinger.flaka.el.Binding;

public class ELBinding {
  @Binding(type=Binding.Type.VARIABLE,name="PI")          /* adding variable "PI" */
  static public  Object pi () {
    return new Double(Math.PI);         
  }
}
```

A note on namespaces. Functions can be imported into a namespace. If a namespace argument is given in task `<fl:el-load /> then the function will be referenced in an EL expression like `ns:name()`. Thus color is used to separate the namespace from the name. If no namespace is given or if the string given to argument is empty, then the function will be imported as is, like `name()`.

Does this principle apply to binding of type _VARIABLE_ as well? It turns out that importing objects into a namespace is not possible, at least not to my understand of EL's programming API.