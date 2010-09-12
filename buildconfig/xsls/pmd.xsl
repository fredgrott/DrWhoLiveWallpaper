<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="html" 
     indent="yes"  />
<xsl:param name="project" select="files"/>
<xsl:param name="rulesets" select="'not defined'"/>
<xsl:param name="today" select="today"/>
<xsl:param name="company" select="company"/>
<xsl:param name="context-root" select="context-root"/>

<xsl:template match="/pmd">
    <html>
    <head>
    <meta http-equiv="Content-Style-Type" content="text/css"/>
    <title>PMD Results for <xsl:value-of select="$project"/></title>
    <link rel="stylesheet" type="text/css" href="reports.css"/>
    </head>
    <body>
    <h1>Static Analysis of <xsl:value-of select="$project"/> source code for <xsl:value-of select="$company"/> on <xsl:value-of select="$today"/></h1>
    <p align="right">Run with <a href="http://pmd.sourceforge.net">PMD <xsl:value-of select="//pmd/@version"/></a></p>
    <table class="summary">
        <tr><th>Rulesets Used</th> 
            <th>Files</th>
            <th>Items Found</th>
            <th>Priority 1</th>
            <th>Priority 2</th>
            <th>Priority 3</th>
            <th>Priority 4</th>
            <th>Priority 5</th>
        </tr>
        <tr><td><xsl:value-of select="$rulesets"/></td>
            <td><xsl:value-of select="count(//file)"/></td>
            <td><xsl:value-of select="count(//violation)" /></td>
            <td><div class="p1"><xsl:value-of select="count(//violation[@priority = 1])"/></div></td>
            <td><div class="p2"><xsl:value-of select="count(//violation[@priority = 2])"/></div></td>
            <td><div class="p3"><xsl:value-of select="count(//violation[@priority = 3])"/></div></td>
            <td><div class="p4"><xsl:value-of select="count(//violation[@priority = 4])"/></div></td>
            <td><div class="p5"><xsl:value-of select="count(//violation[@priority = 5])"/></div></td>
        </tr>
    </table>
    <hr size="2" />
    <xsl:apply-templates/>
    </body>
    </html>
</xsl:template>

<xsl:template name="priorityDiv">
<xsl:if test="@priority = 1">p1</xsl:if>
<xsl:if test="@priority = 2">p2</xsl:if>
<xsl:if test="@priority = 3">p3</xsl:if>
<xsl:if test="@priority = 4">p4</xsl:if>
<xsl:if test="@priority = 5">p5</xsl:if>
</xsl:template>

<xsl:template match="//file">
    <xsl:variable name="filename" select="@name"/>
    <table class="details">
    <tr>
    <th colspan="4">
        <xsl:value-of select="$filename"/></th>
    </tr>
    <xsl:for-each select="violation">
        <tr>
        <td style="padding: 3px" align="right"><div><xsl:attribute name="class"><xsl:call-template name="priorityDiv"/></xsl:attribute><xsl:value-of disable-output-escaping="yes" select="@priority"/></div></td>
        <!-- PMD 3.x uses the 'line' attribute, but 4.0 uses 'beginline' and 'endline' -->
        <xsl:if test="@line">
            <xsl:variable name="lineNum" select="@line"/>
            <td><a href="{$context-root}/{$filename}.html#{$lineNum}">Line <xsl:value-of select="@line"/></a></td>
        </xsl:if>
        <xsl:if test="@beginline">
            <xsl:variable name="lineNum" select="@beginline"/>
            <td><a href="{$context-root}/{$filename}.html#{$lineNum}">Line <xsl:value-of select="@beginline"/></a></td>
        </xsl:if>
        <td><xsl:value-of select="."/></td>
        <td><xsl:if test="@externalInfoUrl">
                <a><xsl:attribute name="href"><xsl:value-of select="@externalInfoUrl"/></xsl:attribute><xsl:value-of select="@rule"/></a>
            </xsl:if>
            <xsl:if test="not(@externalInfoUrl)">
                <xsl:value-of select="@rule"/>
            </xsl:if></td>
        </tr>
    </xsl:for-each>
    </table>
    <p/>
</xsl:template>

</xsl:stylesheet>