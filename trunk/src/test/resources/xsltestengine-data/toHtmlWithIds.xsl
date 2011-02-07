<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html"/>
  <xsl:template match="/">
        <html>
            <body>
                <xsl:for-each select="//book">
                    <button id="{generate-id(author)}" onclick="alert(this.id)">
                        <xsl:value-of select="author"/>
                    </button>
                </xsl:for-each>
            </body>
        </html>
  </xsl:template>
</xsl:stylesheet>