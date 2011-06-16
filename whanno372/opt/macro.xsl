<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:output 
    method   = "xml" 
    indent   = "yes" 
    encoding = "US-ASCII"
    
    omit-xml-declaration = "yes"
    />

  <xsl:param name="name" select="run-in-subdirs" />

  <xsl:template name="copynode">
    <xsl:param name="node"/>
{{{
    <xsl:copy-of select="$node" />
}}}
  </xsl:template>

  <xsl:template name="attributelist">
    <xsl:param name="node"/>
    <xsl:for-each select="$node/attribute">
* <xsl:value-of select="@name"/> 
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="elementlist">
    <xsl:param name="node"/>
    <xsl:for-each select="$node/element|$node/text">
* <xsl:value-of select="@name"/> 
    </xsl:for-each>
  </xsl:template>



  <xsl:template match="/">
    <xsl:apply-templates select="antlib/typedef[@name = $name]"/>
    <xsl:apply-templates select="antlib/taskdef[@name = $name]"/>
    <xsl:apply-templates select="antlib/macrodef[@name = $name]"/>
  </xsl:template>

  
  <xsl:template match="macrodef[@name = $name]" >
!!! Macro ''<xsl:value-of select="$name" />''
    Please describe here what this macro is all about.

!! Attributes
<xsl:if test="count(attribute) = 0">
  This macro does not support any attributes.
</xsl:if>

<xsl:if test="count(attribute) = 1">
  This macro supports one attribute.
</xsl:if>

<xsl:if test="count(attribute) > 1">
Macro {{<xsl:value-of select="@name"/>}} supports <xsl:value-of select="count(attribute)"/> attributes
</xsl:if>

<xsl:if test="count(attribute) > 0">

<xsl:call-template name="attributelist">
  <xsl:with-param name="node" select="." />
</xsl:call-template>

Each attribute is described further down.

<xsl:for-each select="attribute">
! <xsl:value-of select="@name"/>
Please document attribute {{<xsl:value-of select="@name"/>}}.
</xsl:for-each>
</xsl:if>

!! Elements
<xsl:if test="count(elements|text) = 0">
  This macro does not support any elements.
</xsl:if>

<xsl:if test="count(elements|text) = 1">
  This macro supports one element.
</xsl:if>

<xsl:if test="count(elements|text) > 1">
  This macro does supports <xsl:value-of select="count(elements|text)"/> elements.
</xsl:if>

<xsl:if test="count(elements|text) > 0">
  <xsl:call-template name="elementlist">
    <xsl:with-param name="node" select="." />
  </xsl:call-template>
Each element is described further down.
  <xsl:for-each select="element|text">
! <xsl:value-of select="@name"/>
Please document element {{<xsl:value-of select="@name"/>}}.
  </xsl:for-each>
</xsl:if>

!! Examples
Please document examples.

!! Known Issues
None known. Please edit this section if you spot a bug or a
problem. Thanks in advance.
    
!! Implementation
    <xsl:call-template name="copynode">
      <xsl:with-param name="node" select="." />
    </xsl:call-template>
  </xsl:template>
  

  <xsl:template match="taskdef[@name = $name]" >
!!! Task ''<xsl:value-of select="$name" />''
    Please describe here what this task is all about.
  </xsl:template>

  <xsl:template match="typedef[@name = $name]" >
!!! Type ''<xsl:value-of select="$name" />''
    Please describe here what this type is all about.
  </xsl:template>
  


</xsl:stylesheet>
    