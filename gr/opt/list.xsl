<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:output 
    method   = "text" 
    indent   = "yes" 
    encoding = "US-ASCII"
    />

  <!--:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::-->
  <!--::           The initial template - everything starts here         ::-->
  <!--:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::-->
  
  <xsl:template match="/">
    <xsl:apply-templates select="antlib/typedef">
      <xsl:sort select="@name" />
    </xsl:apply-templates>
    
    <xsl:apply-templates select="antlib/macrodef">
      <xsl:sort select="@name" />
    </xsl:apply-templates>
 
    <xsl:apply-templates select="antlib/taskdef">
      <xsl:sort select="@name" />
    </xsl:apply-templates>
    
   </xsl:template>
  
  
  <xsl:template match="macrodef">
    <xsl:text>macro </xsl:text>
      <xsl:value-of select="@name"/>
    <xsl:text>
</xsl:text>
  </xsl:template>
 
  <xsl:template match="typedef">
    <xsl:text>type </xsl:text>
      <xsl:value-of select="@name"/>
    <xsl:text>
</xsl:text>
  </xsl:template>
  
  <xsl:template match="taskdef">
    <xsl:text>task </xsl:text>
      <xsl:value-of select="@name"/>
    <xsl:text>
</xsl:text>
  </xsl:template>

  
</xsl:stylesheet>
