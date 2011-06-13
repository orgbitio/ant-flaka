<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:output 
    method   = "xml" 
    indent   = "yes" 
    encoding = "US-ASCII"
    />

  
  <!--:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::-->
  <!--::           The initial template - everything starts here         ::-->
  <!--:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::-->
  
  <xsl:template match="/">
    <xsl:copy>
      <xsl:apply-templates />
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="macrodef[
                       @name='trace' or @name='info' or 
                       @name='error' or @name='log'  or
                       @name='verbose' or @name='debug' or
                       @name='warning' or @name='assert' or
                       @name='init' or @name='load' or
                       @name='import' or @name='auto-init'
                       @name='dependencies' or @name='dependency'
                       ]
                       ">
    <xsl:copy-of select="."/>
  </xsl:template>


  <xsl:template match="macrodef">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" mode="trace"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="sequential" mode="trace">
    <xsl:element name="sequential">
      <xsl:element name="trace">
        <xsl:attribute name="name"><xsl:value-of select="../@name"/></xsl:attribute>
        <xsl:copy-of select="child::node()" />
      </xsl:element>
    </xsl:element>
  </xsl:template>

  <xsl:template match="@*|node()" mode="trace">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>



</xsl:stylesheet>
