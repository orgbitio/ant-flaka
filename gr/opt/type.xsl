
<!DOCTYPE stylesheet [
<!ENTITY R "Romance">
<!ENTITY WAR "War">
<!ENTITY COM "Comedy">
<!ENTITY SF "Science Fiction">
<!ENTITY ACT "Action">
<!ENTITY NL "<text>&#10;</text>">
]>
<stylesheet xmlns="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <output 
    method   = "text" 
    indent   = "yes" 
    omit-xml-declaration = "yes"
    />

  <template match="/">
    <apply-templates select="antlib/typedef"/>
  </template>
  
  <template match="typedef" >
    &NL;
    <value-of select="concat('= Type ',@name,' =')" />
    &NL;
    
    <text>== Attribute ==</text>
    &NL;
    <text>* attr1&lt;br/> The first attribute</text>
    &NL;
    
    <text>== Element ==</text>
    &NL;
    <text>* attr1&lt;br/> The first element</text>
    &NL;

    <text>== Example ==</text>
    &NL;
    <text>The first example</text>
    &NL;
    <text>== Reference ==</text>
    &NL;
    <text>* Implementation: {{ViewCVS|1=flaka/src/</text>
    <value-of select="translate(@classname,'.','/')" />
    <text>.java}}</text>
    &NL;
    <text>* Javadoc: {{FlakaJavadoc|1=</text>
    <value-of select="translate(@classname,'.','/')" />
    <text>.html}}</text>
    &NL;
    
    
  </template>
  


</stylesheet>
    