<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:fo="http://www.w3.org/1999/XSL/Format"
	xmlns:svg="http://www.w3.org/2000/svg">
	<xsl:output method="xml" indent="yes"/>
	<xsl:include href="fi-impact.xsl"/>
	<xsl:template match="/">
  
<fo:root xsl:use-attribute-sets="fi-impact">
	<fo:layout-master-set>
		<fo:simple-page-master master-name="A4-portrait" page-height="29.7cm" page-width="21.0cm" margin="2cm">
			<fo:region-body />
		</fo:simple-page-master>
	</fo:layout-master-set>
	<fo:page-sequence master-reference="A4-portrait">
		<fo:flow flow-name="xsl-region-body">
			<fo:block>
				<fo:external-graphic height="50pt" width="auto" content-height="50pt" content-width="auto" src="url('logo1.png')" />
            	<fo:block xsl:use-attribute-sets="fi-title"><xsl:value-of select="survey/sections/S_1/answers/Q1_4/value"/></fo:block>
				<fo:block xsl:use-attribute-sets="fi-heading">Project Summary</fo:block>
			</fo:block>
	
			<fo:block>
			
	<!-- table start -->
    <fo:table xsl:use-attribute-sets="fi-table">
      <fo:table-column column-width="50mm" />
      <fo:table-column />
      <fo:table-body>
	  <xsl:if test='survey/sections/S_0/answers/Q0_1/value = "Impact Assessment"'>
        <fo:table-row>
          <fo:table-cell><fo:block xsl:use-attribute-sets="fi-bold">Organisation</fo:block></fo:table-cell>
          <fo:table-cell>
			<fo:block><fo:inline xsl:use-attribute-sets="fi-bold"><xsl:value-of select="survey/sections/S_1/answers/Q1_3/value" /></fo:inline> (<xsl:value-of select="survey/sections/S_1/answers/Q1_16/value"/> year<xsl:if test="survey/sections/S_1/answers/Q1_16/value > 1">s</xsl:if>)</fo:block>
			<fo:block font-style="italic"><xsl:value-of select="survey/sections/S_1/answers/Q1_6/value"/></fo:block>
			<fo:block><xsl:value-of select="survey/sections/S_1/answers/Q1_5/value"/></fo:block>
			<fo:block><xsl:value-of select="survey/sections/S_1/answers/Q1_2/value"/></fo:block>
		  </fo:table-cell>
        </fo:table-row>
	</xsl:if>
        <fo:table-row>
          <fo:table-cell><fo:block xsl:use-attribute-sets="fi-bold">Employees</fo:block></fo:table-cell>
          <fo:table-cell>
			<fo:block>Implementing team size: <xsl:value-of select="survey/sections/S_1/answers/Q1_7/value"/></fo:block>
			<fo:block>Full time employees: <xsl:value-of select="survey/sections/S_1/answers/Q1_8/value"/></fo:block>
			</fo:table-cell>
        </fo:table-row>
	<xsl:if test='survey/sections/S_0/answers/Q0_1/value = "Impact Assessment"'>
        <fo:table-row>
          <fo:table-cell><fo:block xsl:use-attribute-sets="fi-bold">Accelerator</fo:block></fo:table-cell>
          <fo:table-cell>
			<fo:block><fo:inline xsl:use-attribute-sets="fi-bold"><xsl:value-of select="survey/sections/S_1/answers/Q1_1/value"/></fo:inline> (<xsl:value-of select="survey/sections/S_1/answers/Q1_13/value"/>€)</fo:block>
			<fo:block>Coordinator: <xsl:value-of select="survey/sections/S_1/answers/Q1_14/value"/></fo:block>
		  </fo:table-cell>
        </fo:table-row>
	</xsl:if>
        <fo:table-row>
          <fo:table-cell><fo:block xsl:use-attribute-sets="fi-bold">Solution Type</fo:block></fo:table-cell>
          <fo:table-cell><fo:block><xsl:value-of select="survey/sections/S_1/answers/Q1_10/value"/>, <xsl:value-of select="survey/sections/S_1/answers/Q1_11/value"/></fo:block></fo:table-cell>
        </fo:table-row>
	<xsl:if test='survey/sections/S_0/answers/Q0_1/value = "Self Assessment"'>
        <fo:table-row>
          <fo:table-cell><fo:block xsl:use-attribute-sets="fi-bold">Project</fo:block></fo:table-cell>
          <fo:table-cell><fo:block><xsl:value-of select="survey/sections/S_1/answers/Q1_19/value"/></fo:block></fo:table-cell>
        </fo:table-row>
		<fo:table-row>
          <fo:table-cell><fo:block xsl:use-attribute-sets="fi-bold">FIWARE enablers use</fo:block></fo:table-cell>
          <fo:table-cell><fo:block><xsl:value-of select="survey/sections/S_1/answers/Q1_20/value"/></fo:block></fo:table-cell>
        </fo:table-row>
	</xsl:if>
		<fo:table-row><fo:table-cell number-columns-spanned="2"><fo:block xsl:use-attribute-sets="fi-table-heading">FIWARE enablers</fo:block></fo:table-cell></fo:table-row>
		<fo:table-row><fo:table-cell number-columns-spanned="2"><fo:block>
			<xsl:for-each select="survey/sections/S_1/answers/list_Q1_12/Q1_12">
				<fo:inline><xsl:value-of select="label"/><xsl:if test="last() > position()">, </xsl:if></fo:inline>
			</xsl:for-each>.
		</fo:block></fo:table-cell></fo:table-row>
	<xsl:for-each select="survey/sections/S_1/answers/list_Q1_18/Q1_18">
		<fo:table-row>
			<fo:table-cell><fo:block xsl:use-attribute-sets="fi-bold"><xsl:value-of select="label"/></fo:block></fo:table-cell>
			<fo:table-cell><fo:block><xsl:value-of select="list"/></fo:block></fo:table-cell>
		</fo:table-row>
	</xsl:for-each>
	<xsl:if test='survey/sections/S_0/answers/Q0_1/value = "Self Assessment"'>
		<fo:table-row><fo:table-cell number-columns-spanned="2">
			<fo:block xsl:use-attribute-sets="fi-table-heading">Focus and benefits of your project</fo:block>
			<fo:block font-size="10pt"><xsl:value-of select="survey/sections/S_1/answers/Q1_21/value"/></fo:block>
		</fo:table-cell></fo:table-row>
	</xsl:if>
      </fo:table-body>
    </fo:table>
    <!-- table end -->
	<fo:block xsl:use-attribute-sets="fi-heading">Innovation</fo:block>
	<fo:block xsl:use-attribute-sets="fi-subtext">The Innovation indicator expresses the level of originality, maturity and sustainability of innovation to a product or service in an organization’s go to market strategy. The single measures are used to create the innovation indicator but are also used in the calculation of Market Attractiveness and Feasibility Indicators. Several conditions affect the innovation Indicator. An innovative product or service can make a significant Impact in the market if you are ready to implement it, but can conceal substantial engineering, business planning, development, testing and marketing effort if the product or service is still in the planning phases. If the innovation is being developed and validated among colleagues and potential clients the innovation’s real market potential is increased. Innovation is quite healthy in organizations if it is part of a strategy but can be quite resource consuming where stand-alone.</fo:block>

	<fo:table xsl:use-attribute-sets="fi-table">
      <fo:table-column />
      <fo:table-column column-width="60mm" />
      <fo:table-body>
        <fo:table-row>
          <fo:table-cell>
			<fo:block><xsl:value-of select="survey/sections/S_2/result/interpretation"/></fo:block>
			<fo:block>Please visit the FI-IMPACT Library to access reading material that may be useful.</fo:block>
		  </fo:table-cell>
		  <fo:table-cell>
			<fo:block>
<fo:instream-foreign-object>
<svg:svg xmlns:svg="http://www.w3.org/2000/svg" width="160px" height="100px">
	<svg:g transform="translate(80, 100) scale(0.7)">
		<xsl:for-each select="survey/sections/S_2/result/speedometer_svg/list_segments/segments">
			<svg:path><xsl:attribute name="fill"><xsl:value-of select="color"/></xsl:attribute><xsl:attribute name="d"><xsl:value-of select="arc"/></xsl:attribute></svg:path>
			<svg:path opacity="0.5"><xsl:attribute name="fill"><xsl:value-of select="color"/></xsl:attribute><xsl:attribute name="d"><xsl:value-of select="histogram"/></xsl:attribute></svg:path>
		</xsl:for-each>
		<svg:g fill="#000"><svg:circle cx="0" cy="0" r="8" /><svg:path><xsl:attribute name="d"><xsl:value-of select="survey/sections/S_2/result/speedometer_svg/result"/></xsl:attribute></svg:path></svg:g>
		<svg:g fill="#000"><!-- average -->
			<svg:circle r="3">
				<xsl:attribute name="cx"><xsl:value-of select="survey/sections/S_2/result/speedometer_svg/average_x"/></xsl:attribute>
				<xsl:attribute name="cy"><xsl:value-of select="survey/sections/S_2/result/speedometer_svg/average_y"/></xsl:attribute>
			</svg:circle>
			<svg:path><xsl:attribute name="d"><xsl:value-of select="survey/sections/S_2/result/speedometer_svg/average"/></xsl:attribute></svg:path>
		</svg:g>
	</svg:g>
</svg:svg>
</fo:instream-foreign-object>
			</fo:block>
		  </fo:table-cell>
        </fo:table-row>
      </fo:table-body>
    </fo:table>
	
	<!-- table start -->
    <fo:table xsl:use-attribute-sets="fi-table">
      <fo:table-column column-width="50mm" />
      <fo:table-column />
      <fo:table-body>
		<fo:table-row><fo:table-cell number-columns-spanned="2"><fo:block xsl:use-attribute-sets="fi-table-heading">Concept</fo:block></fo:table-cell></fo:table-row>
        <fo:table-row>
          <fo:table-cell><fo:block xsl:use-attribute-sets="fi-bold">Concept developed by:</fo:block></fo:table-cell>
          <fo:table-cell><fo:block><xsl:value-of select="survey/sections/S_2/answers/Q2_4/value"/></fo:block></fo:table-cell>
        </fo:table-row>
		<fo:table-row>
          <fo:table-cell><fo:block xsl:use-attribute-sets="fi-bold">TRL Level:</fo:block></fo:table-cell>
          <fo:table-cell><fo:block><xsl:value-of select="survey/sections/S_2/answers/Q2_1/value"/><!--  (<xsl:value-of select="survey/sections/S_2/answers/Q2_1/value"/>) --></fo:block></fo:table-cell>
        </fo:table-row>
		<fo:table-row><fo:table-cell number-columns-spanned="2"><fo:block xsl:use-attribute-sets="fi-table-heading">Business idea</fo:block></fo:table-cell></fo:table-row>
        <fo:table-row>
          <fo:table-cell><fo:block xsl:use-attribute-sets="fi-bold">Innovation Type</fo:block></fo:table-cell>
          <fo:table-cell><fo:block><xsl:value-of select="survey/sections/S_2/answers/Q2_2/value"/></fo:block></fo:table-cell>
        </fo:table-row>
		<fo:table-row>
          <fo:table-cell><fo:block xsl:use-attribute-sets="fi-bold">Existence of similar solution</fo:block></fo:table-cell>
          <fo:table-cell><fo:block><xsl:value-of select="survey/sections/S_2/answers/Q2_3/value"/></fo:block></fo:table-cell>
        </fo:table-row>
		<fo:table-row>
          <fo:table-cell><fo:block xsl:use-attribute-sets="fi-bold">Strategy</fo:block></fo:table-cell>
          <fo:table-cell><fo:block><xsl:value-of select="survey/sections/S_2/answers/Q2_5/value"/></fo:block></fo:table-cell>
        </fo:table-row>
      </fo:table-body>
    </fo:table>
    <!-- table end -->


	
	<fo:block xsl:use-attribute-sets="fi-heading">Market</fo:block>
	<fo:block xsl:use-attribute-sets="fi-subtext">The Market Focus indicator reflects the quality and relevance of your knowledge of customer needs in your target market(s), the extent to which you have gathered knowledge about customers in the target market, and if your initiative has a strategy and plan to reach the target market. This indicator measures the level of your “customer development” activities: whether you have already approached customers to collect feedback on your product, and to what extent you have developed a strategy to acquire them.</fo:block>
	
	<fo:table xsl:use-attribute-sets="fi-table">
      <fo:table-column />
      <fo:table-column column-width="60mm" />
      <fo:table-body>
        <fo:table-row>
          <fo:table-cell>
			<fo:block><xsl:value-of select="survey/sections/S_3/result/interpretation"/></fo:block>
			<fo:block>Please visit the FI-IMPACT Library to access reading material that may be useful.</fo:block>
		  </fo:table-cell>
		  <fo:table-cell>
			<fo:block>
<fo:instream-foreign-object>
<svg:svg xmlns:svg="http://www.w3.org/2000/svg" width="160px" height="100px">
	<svg:g transform="translate(80, 100) scale(0.7)">
		<xsl:for-each select="survey/sections/S_3/result/speedometer_svg/list_segments/segments">
			<svg:path><xsl:attribute name="fill"><xsl:value-of select="color"/></xsl:attribute><xsl:attribute name="d"><xsl:value-of select="arc"/></xsl:attribute></svg:path>
			<svg:path opacity="0.5"><xsl:attribute name="fill"><xsl:value-of select="color"/></xsl:attribute><xsl:attribute name="d"><xsl:value-of select="histogram"/></xsl:attribute></svg:path>
		</xsl:for-each>
		<svg:g fill="#000"><svg:circle cx="0" cy="0" r="8" /><svg:path><xsl:attribute name="d"><xsl:value-of select="survey/sections/S_3/result/speedometer_svg/result"/></xsl:attribute></svg:path></svg:g>
		<svg:g fill="#000"><!-- average -->
			<svg:circle r="3">
				<xsl:attribute name="cx"><xsl:value-of select="survey/sections/S_3/result/speedometer_svg/average_x"/></xsl:attribute>
				<xsl:attribute name="cy"><xsl:value-of select="survey/sections/S_3/result/speedometer_svg/average_y"/></xsl:attribute>
			</svg:circle>
			<svg:path><xsl:attribute name="d"><xsl:value-of select="survey/sections/S_3/result/speedometer_svg/average"/></xsl:attribute></svg:path>
		</svg:g>
	</svg:g>
</svg:svg>
</fo:instream-foreign-object>
			</fo:block>
		  </fo:table-cell>
        </fo:table-row>
      </fo:table-body>
    </fo:table>
	
	<!-- table start -->
    <fo:table xsl:use-attribute-sets="fi-table">
      <fo:table-column column-width="50mm" />
      <fo:table-column />
      <fo:table-body>
		<fo:table-row><fo:table-cell number-columns-spanned="2"><fo:block xsl:use-attribute-sets="fi-table-heading">Business model</fo:block></fo:table-cell></fo:table-row>
        <fo:table-row>
          <fo:table-cell><fo:block xsl:use-attribute-sets="fi-bold">Model</fo:block></fo:table-cell>
          <fo:table-cell><fo:block><xsl:value-of select="survey/sections/S_3/answers/Q3_1/value"/>.</fo:block></fo:table-cell>
        </fo:table-row>
		<fo:table-row>
          <fo:table-cell><fo:block xsl:use-attribute-sets="fi-bold">Revenue Division</fo:block></fo:table-cell>
          <fo:table-cell><fo:block><xsl:for-each select="survey/sections/S_3/answers/list_Q3_2/Q3_2/list_answers/answers">
			<fo:inline><xsl:value-of select="value" />% <xsl:value-of select="label" /><xsl:if test="last() > position()">, </xsl:if></fo:inline>
		  </xsl:for-each>.</fo:block></fo:table-cell>
        </fo:table-row>
		<fo:table-row><fo:table-cell number-columns-spanned="2"><fo:block xsl:use-attribute-sets="fi-table-heading">Customers</fo:block></fo:table-cell></fo:table-row>
        <fo:table-row>
          <fo:table-cell><fo:block xsl:use-attribute-sets="fi-bold">Value Proposition Verification</fo:block></fo:table-cell>
          <fo:table-cell><fo:block><xsl:value-of select="survey/sections/S_3/answers/Q3_8/value"/></fo:block></fo:table-cell>
        </fo:table-row>
		<fo:table-row>
          <fo:table-cell><fo:block xsl:use-attribute-sets="fi-bold">Commercial Strategy Status</fo:block></fo:table-cell>
          <fo:table-cell><fo:block><xsl:value-of select="survey/sections/S_3/answers/Q3_9/value"/></fo:block></fo:table-cell>
        </fo:table-row>
		<xsl:for-each select="survey/sections/S_3/answers/Q3_3a">
			<fo:table-row>
			  <fo:table-cell><fo:block xsl:use-attribute-sets="fi-bold">Primary Market</fo:block></fo:table-cell>
			  <fo:table-cell><fo:block><xsl:value-of select="value" /></fo:block><fo:block><xsl:value-of select="survey/sections/S_3/answers/Q3_3b" /></fo:block></fo:table-cell>
			</fo:table-row>
		</xsl:for-each>
		<fo:table-row>
          <fo:table-cell><fo:block xsl:use-attribute-sets="fi-bold">Secondary Market Sectors</fo:block></fo:table-cell>
          <fo:table-cell><fo:block><xsl:value-of select="survey/sections/S_3/answers/Q3_3/value"/></fo:block><fo:block><xsl:value-of select="survey/sections/S_3/answers/Q3_3c" /></fo:block></fo:table-cell>
        </fo:table-row>
		<fo:table-row><fo:table-cell number-columns-spanned="2"><fo:block xsl:use-attribute-sets="fi-table-heading">Market</fo:block></fo:table-cell></fo:table-row>
		<fo:table-row>
          <fo:table-cell><fo:block xsl:use-attribute-sets="fi-bold">Channel</fo:block></fo:table-cell>
          <fo:table-cell><fo:block><xsl:value-of select="survey/sections/S_3/answers/Q3_4/value"/></fo:block></fo:table-cell>
        </fo:table-row>
		<fo:table-row>
          <fo:table-cell><fo:block xsl:use-attribute-sets="fi-bold">Target Market in Three Years</fo:block></fo:table-cell>
          <fo:table-cell><fo:block><xsl:value-of select="survey/sections/S_3/answers/Q3_5/value"/></fo:block></fo:table-cell>
        </fo:table-row>
		<fo:table-row>
          <fo:table-cell><fo:block xsl:use-attribute-sets="fi-bold">Entry into the Open Market</fo:block></fo:table-cell>
          <fo:table-cell><fo:block><xsl:value-of select="survey/sections/S_3/answers/Q3_6/value"/></fo:block></fo:table-cell>
        </fo:table-row>
		<fo:table-row>
          <fo:table-cell><fo:block xsl:use-attribute-sets="fi-bold">Level of competition</fo:block></fo:table-cell>
          <fo:table-cell><fo:block><xsl:value-of select="survey/sections/S_3/answers/Q3_7/value"/></fo:block></fo:table-cell>
        </fo:table-row>
		<fo:table-row>
          <fo:table-cell><fo:block xsl:use-attribute-sets="fi-bold">Market Strategy Status</fo:block></fo:table-cell>
          <fo:table-cell><fo:block><xsl:value-of select="survey/sections/S_3/answers/Q3_10/value"/></fo:block><fo:block><xsl:value-of select="survey/sections/S_3/answers/Q3_11/value"/></fo:block></fo:table-cell>
        </fo:table-row>
      </fo:table-body>
    </fo:table>
	<!-- table end -->
	
	<fo:block xsl:use-attribute-sets="fi-heading">Feasibility</fo:block>
	<fo:block xsl:use-attribute-sets="fi-subtext">The Feasibility indicator assesses to what extent you have assessed the economic viability of your business, and if you have already provided for the necessary funds for the startup phase. This indicator assesses whether you are aware of the funds required to start and grow your business, and whether you have secured sources for these funds.</fo:block>
	
	<fo:table xsl:use-attribute-sets="fi-table">
      <fo:table-column />
      <fo:table-column column-width="60mm" />
      <fo:table-body>
        <fo:table-row>
          <fo:table-cell>
			<fo:block><xsl:value-of select="survey/sections/S_4/result/interpretation"/></fo:block>
			<fo:block>Please visit the FI-IMPACT Library to access reading material that may be useful.</fo:block>
		  </fo:table-cell>
		  <fo:table-cell>
			<fo:block>
<fo:instream-foreign-object>
<svg:svg xmlns:svg="http://www.w3.org/2000/svg" width="160px" height="100px">
	<svg:g transform="translate(80, 100) scale(0.7)">
		<xsl:for-each select="survey/sections/S_4/result/speedometer_svg/list_segments/segments">
			<svg:path><xsl:attribute name="fill"><xsl:value-of select="color"/></xsl:attribute><xsl:attribute name="d"><xsl:value-of select="arc"/></xsl:attribute></svg:path>
			<svg:path opacity="0.5"><xsl:attribute name="fill"><xsl:value-of select="color"/></xsl:attribute><xsl:attribute name="d"><xsl:value-of select="histogram"/></xsl:attribute></svg:path>
		</xsl:for-each>
		<svg:g fill="#000"><svg:circle cx="0" cy="0" r="8" /><svg:path><xsl:attribute name="d"><xsl:value-of select="survey/sections/S_4/result/speedometer_svg/result"/></xsl:attribute></svg:path></svg:g>
		<svg:g fill="#000"><!-- average -->
			<svg:circle r="3">
				<xsl:attribute name="cx"><xsl:value-of select="survey/sections/S_4/result/speedometer_svg/average_x"/></xsl:attribute>
				<xsl:attribute name="cy"><xsl:value-of select="survey/sections/S_4/result/speedometer_svg/average_y"/></xsl:attribute>
			</svg:circle>
			<svg:path><xsl:attribute name="d"><xsl:value-of select="survey/sections/S_4/result/speedometer_svg/average"/></xsl:attribute></svg:path>
		</svg:g>
	</svg:g>
</svg:svg>
</fo:instream-foreign-object>
			</fo:block>
		  </fo:table-cell>
        </fo:table-row>
      </fo:table-body>
    </fo:table>
	
	<!-- table start -->
    <fo:table xsl:use-attribute-sets="fi-table">
      <fo:table-column column-width="50mm" />
      <fo:table-column />
      <fo:table-body>
		<fo:table-row><fo:table-cell number-columns-spanned="2"><fo:block xsl:use-attribute-sets="fi-table-heading">Capital</fo:block></fo:table-cell></fo:table-row>
        <fo:table-row>
          <fo:table-cell><fo:block xsl:use-attribute-sets="fi-bold">Required investments until sustainable</fo:block></fo:table-cell>
          <fo:table-cell><fo:block><xsl:value-of select="survey/sections/S_4/answers/Q4_1/value"/></fo:block>
		  </fo:table-cell>
        </fo:table-row>
		<fo:table-row>
          <fo:table-cell><fo:block xsl:use-attribute-sets="fi-bold">Secured funding</fo:block></fo:table-cell>
          <fo:table-cell><fo:block><xsl:value-of select="survey/sections/S_4/answers/Q4_6/value"/></fo:block></fo:table-cell>
        </fo:table-row>
		<fo:table-row><fo:table-cell number-columns-spanned="2"><fo:block xsl:use-attribute-sets="fi-table-heading">Growth rate</fo:block></fo:table-cell></fo:table-row>
        <fo:table-row>
          <fo:table-cell><fo:block xsl:use-attribute-sets="fi-bold">Sales</fo:block></fo:table-cell>
          <fo:table-cell><fo:block><xsl:value-of select="survey/sections/S_4/answers/Q4_2/value"/></fo:block></fo:table-cell>
        </fo:table-row>
		<fo:table-row>
          <fo:table-cell><fo:block xsl:use-attribute-sets="fi-bold">Revenue (4 years)</fo:block></fo:table-cell>
          <fo:table-cell><fo:block><xsl:value-of select="survey/sections/S_4/answers/Q4_3/value"/></fo:block></fo:table-cell>
        </fo:table-row>
		<fo:table-row>
          <fo:table-cell><fo:block xsl:use-attribute-sets="fi-bold">Customer acquisition</fo:block></fo:table-cell>
          <fo:table-cell><fo:block><xsl:value-of select="survey/sections/S_4/answers/Q4_4/value"/></fo:block></fo:table-cell>
		</fo:table-row>
		<fo:table-row>
			<fo:table-cell><fo:block xsl:use-attribute-sets="fi-bold">Sales and marketing expansion</fo:block></fo:table-cell>
			<fo:table-cell><fo:block><xsl:value-of select="survey/sections/S_4/answers/Q4_5/value"/></fo:block></fo:table-cell>
        </fo:table-row>
      </fo:table-body>
    </fo:table>
    <!-- table end -->
	
	<fo:block xsl:use-attribute-sets="fi-heading">Market Needs</fo:block>
	<fo:block xsl:use-attribute-sets="fi-subtext">The Market Needs Indicator reflects the extent to which perceived user benefits associated with a product or service are aligned with real-market needs, based on an analysis of IDC Vertical Market Survey results. One set of indicators are outlined for business and government sectors (B2B and B2G) and a second set of indicators are outlined for consumer markets (B2C), based on different market needs.</fo:block>
	
	<xsl:for-each select="survey/sections/S_5A/answers/list_Q5A_1/Q5A_1">
		<fo:block xsl:use-attribute-sets="fi-marketneeds-subtitle"><xsl:value-of select="label"/></fo:block>
		<fo:block>Your score in Target Market Needs Understanding is <fo:inline xsl:use-attribute-sets="fi-marketneeds-score"><xsl:value-of select="result"/></fo:inline>.</fo:block>
		<fo:table xsl:use-attribute-sets="fi-marketneeds">
			<fo:table-column /><fo:table-column column-width="60mm" />
			<fo:table-body>
				<fo:table-row>
					<fo:table-cell><fo:block>
						<fo:table xsl:use-attribute-sets="fi-marketneeds-table">
							<fo:table-column column-width="85mm" /><fo:table-column column-width="40mm" />
							<fo:table-body>
								<xsl:for-each select="list_answers/answers">
									<fo:table-row>
										<fo:table-cell><fo:block><xsl:value-of select="label"/></fo:block></fo:table-cell>
										<fo:table-cell><fo:block>
											<!-- <fo:instream-foreign-object>
												<svg:svg xmlns:svg="http://www.w3.org/2000/svg" width="60" height="10">
													<svg:defs><svg:pattern id="Pattern" x="0" y="0" width="6" height="12" patternUnits="userSpaceOnUse">
														<svg:g transform="scale(0.03)" fill="#5D8AAE"><svg:polygon points="100,10 40,198 190,78 10,78 160,198" style="fill-rule: nonzero;" /></svg:g>
													</svg:pattern></svg:defs>
													<svg:g transform="translate(0, -1)"><svg:rect fill="url(#Pattern)" x="0" y="0" height="10">
														<xsl:attribute name="width"><xsl:value-of select="6*value"/></xsl:attribute>
													</svg:rect></svg:g>
												</svg:svg>
											</fo:instream-foreign-object> -->
											<xsl:for-each select="list_star/star">
											<fo:instream-foreign-object>
												<svg:svg xmlns:svg="http://www.w3.org/2000/svg" width="6" height="6">
													<svg:g transform="scale(0.03)" fill="#5D8AAE"><svg:polygon points="100,10 40,198 190,78 10,78 160,198" style="fill-rule: nonzero;" /></svg:g>
												</svg:svg>
											</fo:instream-foreign-object>
											</xsl:for-each>
										</fo:block></fo:table-cell>
									</fo:table-row>
								</xsl:for-each>
							</fo:table-body>
						</fo:table>
					</fo:block></fo:table-cell>
					<fo:table-cell><fo:block>
						<fo:block xsl:use-attribute-sets="fi-marketneeds-top-head">TOP 5 business needs</fo:block><fo:block xsl:use-attribute-sets="fi-marketneeds-top-source">(Source: IDC Vertical Market Survey)</fo:block>
						<fo:table>
							<fo:table-column />
							<fo:table-body>
								<xsl:for-each select="list_top_list/top_list">
									<fo:table-row>
										<fo:table-cell xsl:use-attribute-sets="fi-marketneeds-top"><fo:block><xsl:value-of select="position()"/>. <xsl:value-of select="label"/></fo:block></fo:table-cell>
									</fo:table-row>
								</xsl:for-each>
							</fo:table-body>
						</fo:table>
					</fo:block></fo:table-cell>
				</fo:table-row>
			</fo:table-body>
		</fo:table>
	</xsl:for-each>
	
	<fo:block xsl:use-attribute-sets="fi-heading">Social Impact</fo:block>
	<fo:block xsl:use-attribute-sets="fi-subtext">The Social Impact Indicator reflects the extent to which the project has social impact in 11 key areas. It focuses on identifying specific social benefits that your project will support and the contribution to quality of life for specific social groups. It also contextualises the impact of your project against the average social impact of all surveyed projects in these areas.</fo:block>
	
	<fo:table xsl:use-attribute-sets="fi-table">
      <fo:table-column column-width="100mm" />
      <fo:table-column />
      <fo:table-body>
        <fo:table-row>
          <fo:table-cell>
			<fo:block><fo:instream-foreign-object>
		<svg:svg xmlns:svg="http://www.w3.org/2000/svg" width="3000" height="250">
			<svg:g transform="translate(30,30) scale(0.5)">
				<svg:g stroke="rgb(128, 128, 128)" stroke-opacity="0.5" stroke-width="0.3px" fill-opacity="0">
					<svg:polygon transform="translate(160, 160)" points="40,0 18.374367301776097,6.349858686752752 3.614720185819267,23.38339947992454 0.4071423247626882,45.692593530931404 9.770017025829665,66.1944293578114 28.73069772634281,78.3797189445799 51.269302273657175,78.3797189445799 70.22998297417033,66.1944293578114 79.59285767523731,45.69259353093141 76.38527981418073,23.383399479924556 61.625632698223896,6.349858686752752" />
					<svg:polygon transform="translate(120, 120)" points="80,0 36.748734603552194,12.699717373505504 7.229440371638534,46.76679895984908 0.8142846495253764,91.38518706186281 19.54003405165933,132.3888587156228 57.46139545268562,156.7594378891598 102.53860454731435,156.7594378891598 140.45996594834065,132.3888587156228 159.18571535047462,91.38518706186282 152.77055962836147,46.76679895984911 123.25126539644779,12.699717373505504" />
					<svg:polygon transform="translate(80, 80)" points="120,0 55.12310190532829,19.049576060258254 10.844160557457801,70.15019843977363 1.2214269742880646,137.0777805927942 29.310051077488993,198.5832880734342 86.19209317902843,235.1391568337397 153.8079068209715,235.1391568337397 210.68994892251098,198.58328807343423 238.77857302571192,137.07778059279423 229.1558394425422,70.15019843977367 184.8768980946717,19.049576060258254" />
					<svg:polygon transform="translate(40, 40)" points="160,0 73.49746920710439,25.399434747011007 14.458880743277067,93.53359791969817 1.6285692990507528,182.77037412372562 39.08006810331866,264.7777174312456 114.92279090537124,313.5188757783196 205.0772090946287,313.5188757783196 280.9199318966813,264.7777174312456 318.37143070094925,182.77037412372565 305.54111925672294,93.53359791969822 246.50253079289558,25.399434747011007" />
				</svg:g>
				<svg:g stroke="rgb(128, 128, 128)" stroke-width="1px" font-size="20px"><!-- axis -->
					<svg:line x1="200" y1="200" x2="200" y2="0" /><svg:text text-anchor="middle" x="200" y="-20">A</svg:text>
					<svg:line x1="200" y1="200" x2="91.87183650888049" y2="31.74929343376376" /><svg:text text-anchor="middle" x="75.65261198521257" y="14.924222777140137">B</svg:text>
					<svg:line x1="200" y1="200" x2="18.073600929096333" y2="116.91699739962272" /><svg:text text-anchor="middle" x="-9.215358931539214" y="108.60869713958499">C</svg:text>
					<svg:line x1="200" y1="200" x2="2.035711623813441" y2="228.462967654657" /><svg:text text-anchor="middle" x="-27.65893163261454" y="231.3092644201227">D</svg:text>
					<svg:line x1="200" y1="200" x2="48.850085129148326" y2="330.972146789057" /><svg:text text-anchor="middle" x="26.177597898520574" y="344.06936146796266">E</svg:text>
					<svg:line x1="200" y1="200" x2="143.65348863171405" y2="391.8985947228995" /><svg:text text-anchor="middle" x="135.20151192647117" y="411.0884541951894">F</svg:text>
					<svg:line x1="200" y1="200" x2="256.34651136828586" y2="391.8985947228995" /><svg:text text-anchor="middle" x="264.79848807352874" y="411.0884541951894">G</svg:text>
					<svg:line x1="200" y1="200" x2="351.1499148708516" y2="330.97214678905704" /><svg:text text-anchor="middle" x="373.8224021014793" y="344.0693614679627">H</svg:text>
					<svg:line x1="200" y1="200" x2="397.9642883761866" y2="228.46296765465706" /><svg:text text-anchor="middle" x="427.6589316326145" y="231.30926442012276">I</svg:text>
					<svg:line x1="200" y1="200" x2="381.9263990709037" y2="116.91699739962277" /><svg:text text-anchor="middle" x="409.2153589315393" y="108.60869713958505">J</svg:text>
					<svg:line x1="200" y1="200" x2="308.1281634911195" y2="31.74929343376376" /><svg:text text-anchor="middle" x="324.3473880147874" y="14.924222777140137">K</svg:text>
				</svg:g>
				<svg:g fill-opacity="0.5" stroke-width="2px"><!-- polygons -->
					<svg:polygon stroke="rgb(107, 174, 214)" fill="rgb(107, 174, 214)"><xsl:attribute name="points"><xsl:value-of select="survey/sections/S_6A/answers/Q6A_1/line_average"/></xsl:attribute></svg:polygon>
					<svg:polygon stroke="rgb(31, 62, 111)" fill="rgb(31, 62, 111)"><xsl:attribute name="points"><xsl:value-of select="survey/sections/S_6A/answers/Q6A_1/line_result"/></xsl:attribute></svg:polygon>
				</svg:g>
				<svg:g fill-opacity="0.9" fill="rgb(107, 174, 214)"><!-- circles -->
					<xsl:for-each select="survey/sections/S_6A/answers/Q6A_1/list_answers/answers/avg_coord">
						<svg:circle r="5"><xsl:attribute name="cx"><xsl:value-of select="x" /></xsl:attribute><xsl:attribute name="cy"><xsl:value-of select="y" /></xsl:attribute></svg:circle>
					</xsl:for-each>
				</svg:g>
				<svg:g fill-opacity="0.9" fill="rgb(31, 62, 111)"><!-- circles -->
					<xsl:for-each select="survey/sections/S_6A/answers/Q6A_1/list_answers/answers/result_coord">
						<svg:circle r="5"><xsl:attribute name="cx"><xsl:value-of select="x" /></xsl:attribute><xsl:attribute name="cy"><xsl:value-of select="y" /></xsl:attribute></svg:circle>
					</xsl:for-each>
				</svg:g>
			</svg:g>
		</svg:svg>
	</fo:instream-foreign-object></fo:block>
		  </fo:table-cell>
          <fo:table-cell>
			<fo:block>Axis legend</fo:block>
			<fo:list-block xsl:use-attribute-sets="fi-subtext">
				<fo:list-item>
					<fo:list-item-label><fo:block>A</fo:block></fo:list-item-label>
					<fo:list-item-body start-indent="body-start()"><fo:block>Perceived security of communities, neighbourhoods and housing</fo:block></fo:list-item-body>
				</fo:list-item>
				<fo:list-item>
					<fo:list-item-label><fo:block>B</fo:block></fo:list-item-label>
					<fo:list-item-body start-indent="body-start()"><fo:block>Protection of privacy and security of personal digital data</fo:block></fo:list-item-body>
				</fo:list-item>
				<fo:list-item>
					<fo:list-item-label><fo:block>C</fo:block></fo:list-item-label>
					<fo:list-item-body start-indent="body-start()"><fo:block>Citizens involvement and participation in open government</fo:block></fo:list-item-body>
				</fo:list-item>
				<fo:list-item>
					<fo:list-item-label><fo:block>D</fo:block></fo:list-item-label>
					<fo:list-item-body start-indent="body-start()"><fo:block>E-inclusion</fo:block></fo:list-item-body>
				</fo:list-item>
				<fo:list-item>
					<fo:list-item-label><fo:block>E</fo:block></fo:list-item-label>
					<fo:list-item-body start-indent="body-start()"><fo:block>Fitness and well-being</fo:block></fo:list-item-body>
				</fo:list-item>
				<fo:list-item>
					<fo:list-item-label><fo:block>F</fo:block></fo:list-item-label>
					<fo:list-item-body start-indent="body-start()"><fo:block>Health</fo:block></fo:list-item-body>
				</fo:list-item>
				<fo:list-item>
					<fo:list-item-label><fo:block>G</fo:block></fo:list-item-label>
					<fo:list-item-body start-indent="body-start()"><fo:block>Quality of life in urban areas</fo:block></fo:list-item-body>
				</fo:list-item>
				<fo:list-item>
					<fo:list-item-label><fo:block>H</fo:block></fo:list-item-label>
					<fo:list-item-body start-indent="body-start()"><fo:block>Quality of life as a result of better access to information and data</fo:block></fo:list-item-body>
				</fo:list-item>
				<fo:list-item>
					<fo:list-item-label><fo:block>I</fo:block></fo:list-item-label>
					<fo:list-item-body start-indent="body-start()"><fo:block>Social inclusion</fo:block></fo:list-item-body>
				</fo:list-item>
				<fo:list-item>
					<fo:list-item-label><fo:block>J</fo:block></fo:list-item-label>
					<fo:list-item-body start-indent="body-start()"><fo:block>Access and use of e-learning and innovative learning methodologies</fo:block></fo:list-item-body>
				</fo:list-item>
				<fo:list-item>
					<fo:list-item-label><fo:block>K</fo:block></fo:list-item-label>
					<fo:list-item-body start-indent="body-start()"><fo:block>Demand and use of sustainable transport solutions</fo:block></fo:list-item-body>
				</fo:list-item>
			</fo:list-block>
		  </fo:table-cell>
        </fo:table-row>
      </fo:table-body>
    </fo:table>
	
	<fo:table xsl:use-attribute-sets="fi-table">
      <fo:table-column column-width="100mm" />
      <fo:table-column />
      <fo:table-body>
        <fo:table-row>
          <fo:table-cell>
			<fo:block><fo:instream-foreign-object>
		<svg:svg xmlns:svg="http://www.w3.org/2000/svg" width="300" height="250">
			<svg:g transform="translate(60,30) scale(0.5)">
				<svg:g stroke="rgb(128, 128, 128)" stroke-opacity="0.5" stroke-width="0.3px" fill-opacity="0">
					<svg:polygon transform="translate(160, 160)" points="40,0 5.358983848622456,19.999999999999996 5.358983848622452,59.99999999999999 39.99999999999999,80 74.64101615137753,60.000000000000014 74.64101615137754,19.999999999999996" />
					<svg:polygon transform="translate(120, 120)" points="80,0 10.717967697244912,39.99999999999999 10.717967697244903,119.99999999999999 79.99999999999999,160 149.28203230275506,120.00000000000003 149.28203230275508,39.99999999999999" />
					<svg:polygon transform="translate(80, 80)" points="120,0 16.076951545867367,59.999999999999986 16.076951545867356,179.99999999999997 119.99999999999999,240 223.9230484541326,180.00000000000006 223.92304845413264,59.999999999999986" />
					<svg:polygon transform="translate(40, 40)" points="160,0 21.435935394489825,79.99999999999999 21.435935394489807,239.99999999999997 159.99999999999997,320 298.5640646055101,240.00000000000006 298.56406460551017,79.99999999999999" />
				</svg:g>
				<svg:g stroke="rgb(128, 128, 128)" stroke-width="1px" font-size="20px"><!-- axis -->
					<svg:line x1="200" y1="200" x2="200" y2="0" /><svg:text text-anchor="middle" x="200" y="-20">Disabled</svg:text>
					<svg:line x1="200" y1="200" x2="26.794919243112282" y2="99.99999999999997" /><svg:text text-anchor="middle" x="0.8141571295791223" y="89.99999999999997">Elderly</svg:text>
					<svg:line x1="200" y1="200" x2="26.794919243112258" y2="299.99999999999994" /><svg:text text-anchor="middle" x="0.8141571295791152" y="309.99999999999994">Ethnic or cultural minorities</svg:text>
					<svg:line x1="200" y1="200" x2="199.99999999999997" y2="400" /><svg:text text-anchor="middle" x="199.99999999999997" y="420">Low income</svg:text>
					<svg:line x1="200" y1="200" x2="373.2050807568877" y2="300.0000000000001" /><svg:text text-anchor="middle" x="399.18584287042086" y="310.0000000000001">Socially excluded groups</svg:text>
					<svg:line x1="200" y1="200" x2="373.2050807568877" y2="99.99999999999997" /><svg:text text-anchor="middle" x="399.1858428704209" y="89.99999999999997">Unemployed</svg:text>
				</svg:g>
				<svg:g fill-opacity="0.5" stroke-width="2px"><!-- polygons -->
					<svg:polygon stroke="rgb(107, 174, 214)" fill="rgb(107, 174, 214)"><xsl:attribute name="points"><xsl:value-of select="survey/sections/S_6B/answers/Q6B_1/line_average"/></xsl:attribute></svg:polygon>
					<svg:polygon stroke="rgb(31, 62, 111)" fill="rgb(31, 62, 111)"><xsl:attribute name="points"><xsl:value-of select="survey/sections/S_6B/answers/Q6B_1/line_result"/></xsl:attribute></svg:polygon>
				</svg:g>
				<svg:g fill-opacity="0.9" fill="rgb(107, 174, 214)"><!-- circles -->
					<xsl:for-each select="survey/sections/S_6B/answers/Q6B_1/list_answers/answers/avg_coord">
						<svg:circle r="5"><xsl:attribute name="cx"><xsl:value-of select="x" /></xsl:attribute><xsl:attribute name="cy"><xsl:value-of select="y" /></xsl:attribute></svg:circle>
					</xsl:for-each>
				</svg:g>
				<svg:g fill-opacity="0.9" fill="rgb(31, 62, 111)"><!-- circles -->
					<xsl:for-each select="survey/sections/S_6B/answers/Q6B_1/list_answers/answers/result_coord">
						<svg:circle r="5"><xsl:attribute name="cx"><xsl:value-of select="x" /></xsl:attribute><xsl:attribute name="cy"><xsl:value-of select="y" /></xsl:attribute></svg:circle>
					</xsl:for-each>
				</svg:g>
			</svg:g>
		</svg:svg>
	</fo:instream-foreign-object></fo:block>
		
		  </fo:table-cell>
          <fo:table-cell>
			<fo:block></fo:block>
			
		  </fo:table-cell>
        </fo:table-row>
      </fo:table-body>
    </fo:table>
	
	<fo:block xsl:use-attribute-sets="fi-heading">Overview</fo:block>
	<fo:block xsl:use-attribute-sets="fi-subtext">In this section you can evaluate your Impact Assessment scores against the average score calculated from all completed surveys to date. Scores are represented in a spider diagram so that you can easily identify your strengths or weaknessess compared to the average score. Details about a specific section of the report can be accessed by clicking on the node on the axis representing the respective section.</fo:block>
	
	<fo:table xsl:use-attribute-sets="fi-table">
      <fo:table-column column-width="100mm" />
      <fo:table-column />
      <fo:table-body>
        <fo:table-row>
          <fo:table-cell>
			<fo:block><fo:instream-foreign-object>
		<svg:svg xmlns:svg="http://www.w3.org/2000/svg" width="650" height="600">
			<svg:g transform="translate(80,30) scale(0.5)">
				<svg:g stroke="rgb(128, 128, 128)" stroke-opacity="0.5" stroke-width="0.3px" fill-opacity="0">
					<svg:polygon transform="translate(200, 200)" points="50,0 0,49.99999999999999 49.99999999999999,100 100,50.000000000000014" />
					<svg:polygon transform="translate(150, 150)" points="100,0 0,99.99999999999999 99.99999999999999,200 200,100.00000000000003" />
					<svg:polygon transform="translate(100, 100)" points="150,0 0,149.99999999999997 149.99999999999997,300 300,150.00000000000003" />
					<svg:polygon transform="translate(50, 50)" points="200,0 0,199.99999999999997 199.99999999999997,400 400,200.00000000000006" />
				</svg:g>
				<svg:g stroke="rgb(128, 128, 128)" stroke-width="1px" font-size="20px"><!-- axis -->
					<svg:line x1="250" y1="250" x2="250" y2="0" /><svg:text text-anchor="middle" x="250" y="-20">Innovation</svg:text>
					<svg:line x1="250" y1="250" x2="0" y2="249.99999999999997" /><svg:text text-anchor="middle" x="-22.499999999999993" y="249.99999999999997">Market</svg:text>
					<svg:line x1="250" y1="250" x2="249.99999999999997" y2="500" /><svg:text text-anchor="middle" x="249.99999999999997" y="520">Feasibility</svg:text>
					<svg:line x1="250" y1="250" x2="500" y2="250.00000000000006" /><svg:text text-anchor="middle" x="522.5" y="250.00000000000006">Business</svg:text>
				</svg:g>
				<svg:g fill-opacity="0.5" stroke-width="2px"><!-- polygons -->
					<svg:polygon stroke="rgb(107, 174, 214)" fill="rgb(107, 174, 214)"><xsl:attribute name="points"><xsl:value-of select="survey/overview/line_average"/></xsl:attribute></svg:polygon>
					<svg:polygon stroke="rgb(31, 62, 111)" fill="rgb(31, 62, 111)"><xsl:attribute name="points"><xsl:value-of select="survey/overview/line_result"/></xsl:attribute></svg:polygon>
				</svg:g>
				<svg:g fill-opacity="0.9" fill="rgb(107, 174, 214)"><!-- circles -->
					<xsl:for-each select="survey/overview/list_points/points/avg_coord">
						<svg:circle r="5"><xsl:attribute name="cx"><xsl:value-of select="x" /></xsl:attribute><xsl:attribute name="cy"><xsl:value-of select="y" /></xsl:attribute></svg:circle>
					</xsl:for-each>
				</svg:g>
				<svg:g fill-opacity="0.9" fill="rgb(31, 62, 111)"><!-- circles -->
					<xsl:for-each select="survey/overview/list_points/points/result_coord">
						<svg:circle r="5"><xsl:attribute name="cx"><xsl:value-of select="x" /></xsl:attribute><xsl:attribute name="cy"><xsl:value-of select="y" /></xsl:attribute></svg:circle>
					</xsl:for-each>
				</svg:g>
			</svg:g>
		</svg:svg>
	</fo:instream-foreign-object></fo:block>
		
		  </fo:table-cell>
          <fo:table-cell>
			<fo:block></fo:block>
			
		  </fo:table-cell>
        </fo:table-row>
      </fo:table-body>
    </fo:table>
	
	<fo:block xsl:use-attribute-sets="fi-heading">Speedometer legend</fo:block>
	<fo:block><fo:external-graphic src="speedometer.svg" /></fo:block>	
	
			</fo:block>
		</fo:flow>
	</fo:page-sequence>
</fo:root>

	</xsl:template>
</xsl:stylesheet>