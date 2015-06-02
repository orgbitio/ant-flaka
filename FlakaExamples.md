**This is work in progress ...**

Flaka provides some very innovative features and syntactic sugar.
This page will show how to tackle common ant problems with Flaka.
Examples are taken from 'real-life' => production code, questions from stackoverflow.com, the Ant user list .. etc.



# Categories #

## Properties ##
One of the most common question is how to overwrite or edit existing properties in ant.

**Question** : How do i overwrite an existing property ?<br>
<b>Solution</b> : use the let task<br>
<br>
Properties once set are immutable in ant by design.<br>
<br>
<pre><code>&lt;!-- set a new property --&gt;<br>
&lt;fl:let&gt;foo := 'bar'&lt;/fl:let&gt;<br>
</code></pre>

<pre><code>&lt;!-- overwrite an existing property or userproperty<br>
     (those properties defined on the commandline via -Dfoo=bar ..) --&gt;<br>
&lt;fl:let&gt;foo ::= 'baz'&lt;/fl:let&gt;<br>
</code></pre>

notice the double '::' in foo ::= 'baz'<br>
<br>
<b>Question</b> : How do i extract a specific part from a csv separated property ?<br>
<b>Solution</b> : use the split function with index<br>
<br>
<pre><code>&lt;project xmlns:fl="antlib:it.haefelinger.flaka"&gt;<br>
<br>
  &lt;property name="module.list" value="mod1,mod2,mod3,mod4,mod5"/&gt;<br>
<br>
  &lt;target name="main"&gt;<br>
    &lt;!-- simple echo --&gt;<br>
    &lt;fl:echo&gt;xtractedvalue =&gt; #{split('${module.list}',',')[0]}&lt;/fl:echo&gt;<br>
    &lt;!-- create property for further processing.. --&gt;<br>
    &lt;fl:let&gt;<br>
      xtractedvalue := split('${module.list}',',')[0]<br>
    &lt;/fl:let&gt;<br>
    &lt;echo&gt;$${xtractedvalue} =&gt; ${xtractedvalue}&lt;/echo&gt;<br>
  &lt;/target&gt;<br>
<br>
&lt;/project&gt;<br>
</code></pre>

output :<br>
<br>
<pre><code> [fl:echo] xtractedvalue =&gt; mod1<br>
 [echo] ${xtractedvalue} =&gt; mod1<br>
</code></pre>

a similar one :<br>
<br>
<b>Question</b> : Given is an ant property which has value of the type 1.0.0.123<br>
How to extract the value after the last dot, in this case that would be '123' ?<br>
<b>Solution</b> : use the split function with index<br>
<br>
<pre><code>&lt;project xmlns:fl="antlib:it.haefelinger.flaka"&gt;<br>
<br>
 &lt;property name="foobar" value="1.0.0.123"/&gt;<br>
<br>
  &lt;target name="main"&gt;<br>
   &lt;!-- simple echo --&gt;<br>
   &lt;fl:echo&gt;xtractedvalue =&gt; #{split('${foobar}','\.')[3]}&lt;/fl:echo&gt;<br>
   &lt;!-- create property for further processing.. --&gt;<br>
   &lt;fl:let&gt;<br>
    xtractedvalue := split('${foobar}','\.')[3]<br>
   &lt;/fl:let&gt;<br>
   &lt;echo&gt;$${xtractedvalue} =&gt; ${xtractedvalue}&lt;/echo&gt;<br>
  &lt;/target&gt;<br>
&lt;/project&gt;<br>
</code></pre>

output :<br>
<br>
<pre><code> [fl:echo] xtractedvalue =&gt; 123<br>
 [echo] ${xtractedvalue} =&gt; 123<br>
</code></pre>

<b>Question</b> : For some reason i need to strip of the drive name of some property<br>
${basedir} = "D:\some\directory\blah\blah"<br>
${cwd} = some\directory\blah\blah"<br>
<b>Solution</b> : use the replace function<br>
<br>
<pre><code>&lt;project xmlns:fl="antlib:it.haefelinger.flaka" name="World"&gt;<br>
  &lt;!-- simple echo --&gt;<br>
  &lt;fl:echo&gt;#{replace('${basedir}', '$1' , '.:\\\\(.+)' )}&lt;/fl:echo&gt;<br>
  &lt;!-- set property --&gt;<br>
  &lt;fl:let&gt;cwd := replace('${basedir}', '$1' , '.:\\\\(.+)' )&lt;/fl:let&gt;<br>
&lt;/project&gt;<br>
</code></pre>

<h2>Files / Directories</h2>

<b>Question</b> : After compiling my java sources how to run the corresponding classes ?<br>
<b>Solution</b> : Iterate over the fileset which contains the java sources and use the<br>
replace function to call the corresponding class files.<br>
<br>
This example shows also how to iterate over a fileset with the ant builtin ${toString:filesetid} property.<br>
<br>
<pre><code>&lt;project xmlns:fl="antlib:it.haefelinger.flaka"&gt;<br>
<br>
  &lt;property name="srcroot" value="path/to/srcrootdir"/&gt;<br>
  &lt;property name="classroot" value="path/to/classrootdir"/&gt;<br>
<br>
  &lt;!-- seek all classes with main method --&gt;<br>
  &lt;fileset dir="${srcroot}" includes="**/*.java" id="mainclasses"&gt;<br>
    &lt;contains text="public static void main"/&gt;<br>
  &lt;/fileset&gt;<br>
<br>
  &lt;!-- iterate over classes with main method and call<br>
       corresponding classfile --&gt;<br>
  &lt;fl:for var="file" in="split('${toString:mainclasses}', ';')"&gt;<br>
    &lt;fl:let&gt;<br>
      ; strip the '.java' extension<br>
      file = replace(file, '', '.java')<br>
      ; replace fileseparator with '.'<br>
      ; when running on windows you have to use :<br>
      ; replace(file, '\.', '${file.separator}${file.separator}')<br>
      file = replace(file, '\.', '${file.separator}')<br>
      &lt;/fl:let&gt;<br>
    &lt;fl:echo&gt;<br>
      starting =&gt; #{file} in ${classroot}..<br>
    &lt;/fl:echo&gt;<br>
    &lt;java classname="#{file}"&gt;<br>
      &lt;classpath&gt;<br>
       &lt;!--<br>
         when using a fileset you'll get a<br>
         java.util.zip.ZipException because you're<br>
         referencing not jarfiles but classfiles<br>
         therefore you've to use pathelement location<br>
       --&gt;<br>
       &lt;pathelement location="${classroot}"/&gt;<br>
      &lt;/classpath&gt;<br>
    &lt;/java&gt;<br>
  &lt;/fl:for&gt;<br>
<br>
&lt;/project&gt;<br>
</code></pre>

<b>Question</b> : In ant, how can i check if a set of files (comma-separated list of paths) exists or not?<br>
<b>Solution</b> : use<br>
<blockquote>a) when with combined file function calls<br>
or<br>
b) use list function to iterate over the commaseparated pathentries</blockquote>

<pre><code>&lt;project xmlns:fl="antlib:it.haefelinger.flaka"&gt;<br>
<br>
  &lt;!-- when you have a cvs property use split function<br>
       to get your list to iterate over --&gt;<br>
  &lt;property name="checkpath" value="/foo/bar,/foo/baz,/foo/bazz"/&gt;<br>
  &lt;fl:for var="file" in="split('${checkpath}', ',')"&gt;<br>
    &lt;fl:fail message="#{file} does not exist !!" test="!file.tofile.exists"/&gt;<br>
  &lt;/fl:for&gt;<br>
<br>
  &lt;!-- creating the list inline --&gt;<br>
  &lt;fl:for var="file" in="list('/foo/bar','/foo/baz', '/foo/bazz')"&gt;<br>
    &lt;fl:fail message="#{file} does not exist !!" test="!file.tofile.exists"/<br>
  &lt;/fl:for&gt;<br>
<br>
  &lt;!-- using some if then else construct with choose --&gt;<br>
  &lt;fl:choose&gt;<br>
    &lt;fl:when test="file('/foo/bar').exists and file('/foo/baz' and file('/foo/bazz').exists"&gt;<br>
      &lt;!-- create property based on existence --&gt;<br>
      &lt;fl:let&gt;pathExist := true&lt;/fl:let&gt;<br>
      &lt;!-- .. other nested tasks .. --&gt;<br>
    &lt;/fl:when&gt;<br>
    &lt;fl:otherwise&gt;<br>
      &lt;fl:let&gt;pathExist := false&lt;/fl:let&gt;<br>
      &lt;!-- .. other nested tasks .. --&gt;<br>
    &lt;/fl:otherwise&gt;<br>
  &lt;/fl:choose&gt;<br>
<br>
  &lt;echo&gt;${pathExist}&lt;/echo&gt;<br>
<br>
&lt;/project&gt;<br>
</code></pre>

<h2>Conditions</h2>

<b>Question</b> : I would like to do something like this<br>
<pre><code>&lt;target name="clean" description="clean"&gt;<br>
    &lt;if&gt;<br>
        &lt;available file="${build}" type="dir" /&gt;<br>
        &lt;then&gt;<br>
            &lt;delete dir="${build}" /&gt;<br>
        &lt;/then&gt;<br>
    &lt;/if&gt;<br>
&lt;/target&gt;<br>
</code></pre>

<b>Solution</b> :<br>

The standard way in vanilla ant would be something like<br>
<pre><code>&lt;target name="check"&gt;<br>
  &lt;condition property="delbuild"&gt;<br>
    &lt;available file="${build}" type="dir"/&gt;<br>
  &lt;/condition&gt;<br>
 &lt;/target&gt;<br>
<br>
 &lt;target name="clean" depends="check" if="delbuild"&gt;<br>
 &lt;delete dir="${build}"/&gt;<br>
    &lt;!-- .. --&gt;<br>
 &lt;/target&gt;<br>
</code></pre>

with Flaka its straightforward<br>
<pre><code>&lt;fl:when test=" '${build}'.isdir "&gt;<br>
  &lt;delete dir="${build}"/&gt;<br>
&lt;/fl:when&gt;<br>
</code></pre>

<b>Question</b> : Is there any if then else construct in ant ?<br>
<b>Solution</b> : Use choose/when/otherwise<br>
<br>
<pre><code>&lt;project xmlns:fl="antlib:it.haefelinger.flaka"&gt;<br>
  &lt;!-- some if/then/else construct --&gt;<br>
  &lt;fl:choose&gt;<br>
    &lt;!-- if --&gt;<br>
    &lt;when test=" '${buildtype}' eq 'prod' "&gt;<br>
      &lt;!-- then --&gt;<br>
      &lt;echo&gt;..starting ProductionBuild&lt;/echo&gt;<br>
    &lt;/when&gt;<br>
    &lt;when test=" '${buildtype}' eq 'test' "&gt;<br>
      &lt;!-- then --&gt;<br>
      &lt;echo&gt;..starting TestBuild&lt;/echo&gt;<br>
    &lt;/when&gt;<br>
    &lt;!-- else --&gt;<br>
    &lt;otherwise&gt;<br>
      &lt;fl:unless test="has.property.dummybuild"&gt;<br>
        &lt;fail message="No valid buildtype !, found =&gt; '${buildtype}'"/&gt;<br>
      &lt;/fl:unless&gt;<br>
      &lt;echo&gt;.. is DummyBuild&lt;/echo&gt;<br>
    &lt;/otherwise&gt;<br>
  &lt;/fl:choose&gt;<br>
&lt;/project&gt;<br>
</code></pre>

output with ant -f build.xml -Dbuildtype=prod or<br>
ant -f build.xml -Dbuildtype=prod -Ddummybuild=whatever<br>
<br>
<pre><code>[echo] ..starting ProductionBuild<br>
</code></pre>

output with typo => ant - build.xml -Dbuildtype=testt<br>
<br>
<pre><code>BUILD FAILED<br>
/home/rosebud/workspace/AntTest/build.xml:21: No valid buildtype !, found =&gt; 'testt'<br>
</code></pre>

output with ant -f build.xml -Ddummybuild=whatever<br>
<pre><code>[echo] .. is DummyBuild<br>
</code></pre>

