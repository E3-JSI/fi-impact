<?xml version='1.0'?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:fo="http://www.w3.org/1999/XSL/Format">
	
<xsl:attribute-set name="fi-impact">
	<xsl:attribute name="font-family">sans-serif</xsl:attribute>
	<xsl:attribute name="font-size">10pt</xsl:attribute>
</xsl:attribute-set>
	
<xsl:attribute-set name="fi-table">
	<xsl:attribute name="table-layout">fixed</xsl:attribute>
	<xsl:attribute name="width">100%</xsl:attribute>
	<xsl:attribute name="border-collapse">separate</xsl:attribute>
	<xsl:attribute name="margin-top">10pt</xsl:attribute>
	<xsl:attribute name="border-separation">5pt</xsl:attribute>
</xsl:attribute-set>
<xsl:template match="fi-table">
    <xsl:for-each select="fo:block">
		<xsl:attribute name="color">#999</xsl:attribute>
	</xsl:for-each>
</xsl:template>

<xsl:attribute-set name="fi-marketneeds">
	<xsl:attribute name="font-size">10pt</xsl:attribute>
	<xsl:attribute name="table-layout">fixed</xsl:attribute>
	<xsl:attribute name="width">100%</xsl:attribute>
	<xsl:attribute name="border-collapse">separate</xsl:attribute>
	<xsl:attribute name="margin-top">10pt</xsl:attribute>
	<xsl:attribute name="margin-bottom">2pt</xsl:attribute>
</xsl:attribute-set>

<xsl:attribute-set name="color1">
	<xsl:attribute name="fill">#923933</xsl:attribute>
</xsl:attribute-set>
<xsl:attribute-set name="color2">
	<xsl:attribute name="fill">#F4B900</xsl:attribute>
</xsl:attribute-set>
<xsl:attribute-set name="color3">
	<xsl:attribute name="fill">#00A54F</xsl:attribute>
</xsl:attribute-set>
<xsl:attribute-set name="black">
	<xsl:attribute name="fill">black</xsl:attribute>
</xsl:attribute-set>

<!-- titles ========================================================= -->
<xsl:attribute-set name="fi-title">
	<xsl:attribute name="color">#345281</xsl:attribute>
	<xsl:attribute name="font-size">32pt</xsl:attribute>
	<xsl:attribute name="font-family">serif</xsl:attribute>
	<xsl:attribute name="font-weight">bold</xsl:attribute>
	<xsl:attribute name="line-height">26pt</xsl:attribute>
	<xsl:attribute name="space-before.optimum">10pt</xsl:attribute>
	<xsl:attribute name="text-align">start</xsl:attribute>
	<xsl:attribute name="margin-left">77pt</xsl:attribute>
	<xsl:attribute name="padding-top">3pt</xsl:attribute>
</xsl:attribute-set>
<xsl:attribute-set name="fi-heading">
	<xsl:attribute name="color">#5D8AAE</xsl:attribute>
	<xsl:attribute name="font-size">18pt</xsl:attribute>
	<xsl:attribute name="font-family">serif</xsl:attribute>
	<xsl:attribute name="line-height">20pt</xsl:attribute>
	<xsl:attribute name="space-before.optimum">10pt</xsl:attribute>
	<xsl:attribute name="text-align">start</xsl:attribute>
	<xsl:attribute name="margin-top">40pt</xsl:attribute>
	<xsl:attribute name="border-top">0.5pt solid #585D76</xsl:attribute>
	<xsl:attribute name="padding-top">5pt</xsl:attribute>
</xsl:attribute-set>
<xsl:attribute-set name="fi-subheading">
	<xsl:attribute name="font-size">12pt</xsl:attribute>
	<xsl:attribute name="font-family">sans-serif</xsl:attribute>
	<xsl:attribute name="line-height">14pt</xsl:attribute>
	<xsl:attribute name="space-before.optimum">10pt</xsl:attribute>
	<xsl:attribute name="text-align">start</xsl:attribute>
	<xsl:attribute name="font-weight">bold</xsl:attribute>
	<xsl:attribute name="margin-top">15pt</xsl:attribute>
	<xsl:attribute name="padding-top">10pt</xsl:attribute>
</xsl:attribute-set>
<xsl:attribute-set name="fi-table-heading">
	<xsl:attribute name="color">#849CB4</xsl:attribute>
	<xsl:attribute name="font-family">serif</xsl:attribute>
	<xsl:attribute name="font-size">11pt</xsl:attribute>
	<xsl:attribute name="font-weight">bold</xsl:attribute>
	<xsl:attribute name="margin-top">30pt</xsl:attribute>
	<xsl:attribute name="border-bottom">0.5pt solid #ccc</xsl:attribute>
	<xsl:attribute name="padding-bottom">2pt</xsl:attribute>
	<xsl:attribute name="margin-bottom">10pt</xsl:attribute>
</xsl:attribute-set>
<xsl:attribute-set name="fi-bold">
	<xsl:attribute name="font-weight">bold</xsl:attribute>
</xsl:attribute-set>

<xsl:attribute-set name="fi-subtext">
	<xsl:attribute name="font-size">10pt</xsl:attribute>
	<xsl:attribute name="color">#666</xsl:attribute>
	<xsl:attribute name="font-family">sans-serif</xsl:attribute>
	<xsl:attribute name="line-height">12pt</xsl:attribute>
	<xsl:attribute name="space-before.optimum">10pt</xsl:attribute>
	<xsl:attribute name="text-align">justify</xsl:attribute>
	<xsl:attribute name="padding-top">3pt</xsl:attribute>
</xsl:attribute-set>
<xsl:attribute-set name="fi-marketneeds-subtitle">
	<xsl:attribute name="color">#849CB4</xsl:attribute>
	<xsl:attribute name="font-family">serif</xsl:attribute>
	<xsl:attribute name="font-size">11pt</xsl:attribute>
	<xsl:attribute name="font-weight">bold</xsl:attribute>
	<xsl:attribute name="margin-top">30pt</xsl:attribute>
	<xsl:attribute name="border-bottom">0.5pt solid #ccc</xsl:attribute>
	<xsl:attribute name="padding-bottom">2pt</xsl:attribute>
	<xsl:attribute name="margin-bottom">10pt</xsl:attribute>
</xsl:attribute-set>
<xsl:attribute-set name="fi-marketneeds-table">
	<xsl:attribute name="font-size">8.5pt</xsl:attribute>
	<xsl:attribute name="color">#666</xsl:attribute>
</xsl:attribute-set>
<xsl:attribute-set name="fi-marketneeds-top">
	<xsl:attribute name="font-size">8.5pt</xsl:attribute>
	<xsl:attribute name="color">#666</xsl:attribute>
	<xsl:attribute name="margin-top">2pt</xsl:attribute>
	<xsl:attribute name="margin-bottom">2pt</xsl:attribute>
</xsl:attribute-set>
<xsl:attribute-set name="fi-marketneeds-top-source">
	<xsl:attribute name="font-size">8.5pt</xsl:attribute>
	<xsl:attribute name="color">#999</xsl:attribute>
	<xsl:attribute name="font-family">sans-serif</xsl:attribute>
	<xsl:attribute name="font-style">italic</xsl:attribute>
	<xsl:attribute name="line-height">8.5pt</xsl:attribute>
	<xsl:attribute name="space-before.optimum">10pt</xsl:attribute>
	<xsl:attribute name="text-align">right</xsl:attribute>
	<xsl:attribute name="margin-bottom">5pt</xsl:attribute>
</xsl:attribute-set>
<xsl:attribute-set name="fi-marketneeds-top-head">
	<xsl:attribute name="font-family">serif</xsl:attribute>
	<xsl:attribute name="text-align">left</xsl:attribute>
</xsl:attribute-set>
<xsl:attribute-set name="fi-marketneeds-subtext">
	<xsl:attribute name="font-size">8.5pt</xsl:attribute>
	<xsl:attribute name="color">#999</xsl:attribute>
	<xsl:attribute name="font-family">sans-serif</xsl:attribute>
	<xsl:attribute name="line-height">12pt</xsl:attribute>
	<xsl:attribute name="space-before.optimum">10pt</xsl:attribute>
	<xsl:attribute name="text-align">center</xsl:attribute>
	<xsl:attribute name="padding-top">3pt</xsl:attribute>
	<xsl:attribute name="margin-bottom">10pt</xsl:attribute>
</xsl:attribute-set>
<xsl:attribute-set name="fi-marketneeds-text">
	<xsl:attribute name="font-family">serif</xsl:attribute>
	<xsl:attribute name="text-align">center</xsl:attribute>
	<xsl:attribute name="padding-top">3pt</xsl:attribute>
	<xsl:attribute name="margin-bottom">10pt</xsl:attribute>
</xsl:attribute-set>
<xsl:attribute-set name="fi-marketneeds-score">
	<xsl:attribute name="font-size">12pt</xsl:attribute>
	<xsl:attribute name="color">#849CB4</xsl:attribute>
	<xsl:attribute name="font-family">serif</xsl:attribute>
	<xsl:attribute name="font-style">italic</xsl:attribute>
	<xsl:attribute name="font-weight">bold</xsl:attribute>
	<xsl:attribute name="text-align">center</xsl:attribute>
	<xsl:attribute name="padding-top">3pt</xsl:attribute>
	<xsl:attribute name="margin-bottom">10pt</xsl:attribute>
</xsl:attribute-set>

<!-- boxes ============================================================== -->
<xsl:attribute-set name="simple_block_1">
	<!-- note that you can add an use-attribute-sets attribute to this attribute-set -->
	<xsl:attribute name="font-size">11pt</xsl:attribute>
	<xsl:attribute name="font-family">sans-serif</xsl:attribute>
	<xsl:attribute name="space-after">1.5mm</xsl:attribute>
</xsl:attribute-set>

<xsl:template name="simple_block_2">
	<!-- this is an alternative to attribute-set, when formatting depends on context -->
	<xsl:attribute name="font-size">11pt</xsl:attribute>
	<xsl:attribute name="font-family">sans-serif</xsl:attribute>
	<xsl:if test="following-sibling::*">
		<xsl:attribute name="space-after">1.5mm</xsl:attribute>
	</xsl:if>
</xsl:template>

</xsl:stylesheet>