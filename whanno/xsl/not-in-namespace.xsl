<?xml version="1.0"?>
<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xalan="http://xml.apache.org/xalan"
    xmlns:z="ant:current"
    version="1.0"
    >

  <xsl:output 
      method="xml"
      indent="yes"
      media-type="application/xml"
      omit-xml-declaration="yes"
      />


  <xsl:template match="/">
    <xsl:variable name="topnodes" select="/antlib/macrodef/sequential//*" />
    <xsl:variable name="nodes" select="$topnodes[namespace-uri(.) != 'ant:current']" />
    <xsl:for-each select="$nodes">
      <xsl:value-of select="name(.)" /><xsl:text> 
</xsl:text>
    </xsl:for-each>
  </xsl:template>
</xsl:stylesheet>

