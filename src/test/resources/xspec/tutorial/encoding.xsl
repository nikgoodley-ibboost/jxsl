<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs" version="2.0">
    
    <xsl:template match="/">
        <document>
            <xsl:apply-templates/>
        </document>
    </xsl:template>
    
    <xsl:template match="néme">
        <nôm>
            <xsl:value-of select="."/>
        </nôm>
    </xsl:template>
    
</xsl:stylesheet>
