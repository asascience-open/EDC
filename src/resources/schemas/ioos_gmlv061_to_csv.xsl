<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:gml="http://www.opengis.net/gml/3.2" xmlns:om="http://www.opengis.net/om/1.0" xmlns:swe="http://www.opengis.net/swe/1.0.2" xmlns:ioos="http://www.noaa.gov/ioos/0.6.1" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
	<xsl:output method="text" encoding='UTF-8'/>
	<!-- Global parameters accessible to all templates -->
	<xsl:param name="initialNames">Observable,Station,StationName,Latitude,Longitude,DateTime,</xsl:param>
	<xsl:param name="carriageReturn">&#013;</xsl:param>
	<xsl:param name="newline">&#010;</xsl:param>
	<xsl:param name="quote">&#034;</xsl:param>

	
	<xsl:template match="/om:CompositeObservation">
		<xsl:variable name="processPath" select="om:procedure/om:Process/ioos:CompositeContext/gml:valueComponents/ioos:ContextArray/gml:valueComponents/ioos:CompositeContext"/>
		<xsl:variable name="timeSeriesRecordPath" select="om:result/ioos:Composite/gml:valueComponents/ioos:Array/gml:valueComponents/ioos:Composite"/>
		<xsl:variable name="firstBinObsPath" 
select="$timeSeriesRecordPath[1]/gml:valueComponents/ioos:Array/gml:valueComponents/ioos:Composite[1]//ioos:CompositeValue[1]"/>
		<xsl:variable name="observedNames">
			<xsl:call-template name="getObservedNames">
				<xsl:with-param name="firstBinObsPath" select="$firstBinObsPath"/>
			</xsl:call-template>
		</xsl:variable>

		<xsl:variable name="names" select="concat($initialNames,$observedNames)"/>
		<xsl:value-of select="$names"/><!-- output header line -->
		<xsl:text disable-output-escaping="yes">&#010;</xsl:text>
    
		<xsl:variable name="observable" select="substring-after(om:observedProperty/@xlink:href,'#')"/>
		 <xsl:apply-templates select="$timeSeriesRecordPath" mode="station">
			 <xsl:with-param name="observedNames" select="concat($observedNames,',')"/>
			 <xsl:with-param name="observable" select="$observable"/>
			 <xsl:with-param name="processPath" select="$processPath"/>
		 </xsl:apply-templates>
	</xsl:template>
	
	<xsl:template name="getObservedNames">
		<xsl:param name="firstBinObsPath"/>
		<xsl:for-each select="$firstBinObsPath">
			<xsl:for-each select="gml:valueComponents/*">
				<xsl:value-of select="@name"/>
        <xsl:text>(</xsl:text>
        <xsl:value-of select="@uom"/>
        <xsl:text>)</xsl:text>
				<xsl:if test="position() != last()">
					<xsl:text>,</xsl:text>
				</xsl:if>
			</xsl:for-each>
		</xsl:for-each>
	</xsl:template>
	
	<xsl:template match="ioos:Composite" mode="station">
		<xsl:param name="observedNames"/>

		<xsl:param name="observable"/>
		<xsl:param name="processPath"/>
		<xsl:variable name="pos" select="position()"/>
		<xsl:variable name="stationNumber" select="$processPath[$pos]/gml:valueComponents/ioos:StationId"/>
		<xsl:variable name="stationName" select="$processPath[$pos]/gml:valueComponents/ioos:StationName"/>
		<xsl:variable name="quotedStationName" select="concat($quote,$stationName,$quote)"/>
		<!-- must quote CSV value with embedded comma -->
		<xsl:variable name="posLL" select="$processPath[$pos]/gml:valueComponents/gml:Point/gml:pos"/>

		<!-- string() function added to next two lines to avoid error when processed by XML/Spy v2008 rel 2 sp 1 -->
		<xsl:variable name="latitude" select="substring-before(string($posLL),' ')"/>
		<xsl:variable name="longitude" select="substring-after(string($posLL),' ')"/>

		<xsl:apply-templates select="gml:valueComponents/ioos:Array/gml:valueComponents/ioos:Composite" mode="profile">
			 <xsl:with-param name="observedNames" select="$observedNames"/>
			 <xsl:with-param name="observable" select="$observable"/>
			 <xsl:with-param name="stationNumber" select="$stationNumber"/>
       <xsl:with-param name="stationName" select="$quotedStationName"/>
       <xsl:with-param name="latitude" select="$latitude"/>
			 <xsl:with-param name="longitude" select="$longitude"/>
		</xsl:apply-templates>
	</xsl:template>
	
	<xsl:template match="ioos:Composite" mode="profile">
		<xsl:param name="observedNames"/>
		<xsl:param name="observable"/>
		<xsl:param name="stationNumber"/>
		<xsl:param name="stationName"/>

		<xsl:param name="latitude"/>
		<xsl:param name="longitude"/>
		<xsl:for-each select="gml:valueComponents">
			<xsl:variable name="timePosition" select="ioos:CompositeContext/gml:valueComponents/gml:TimeInstant/gml:timePosition"/>
			<!-- ValueArray in Profile -->
			<xsl:for-each select="ioos:ValueArray/gml:valueComponents/ioos:CompositeValue">
				<xsl:call-template name="binObservation">
					<xsl:with-param name="observedNames" select="$observedNames"/>
					 <xsl:with-param name="observable" select="$observable"/>
					 <xsl:with-param name="stationNumber" select="$stationNumber"/>
					 <xsl:with-param name="stationName" select="$stationName"/>
					 <xsl:with-param name="latitude" select="$latitude"/>
					 <xsl:with-param name="longitude" select="$longitude"/>
					<xsl:with-param name="timePosition" select="$timePosition"/>
				</xsl:call-template>
			</xsl:for-each>
			<!-- CompositeValue for Point -->
			<xsl:for-each select="ioos:CompositeValue">
				<xsl:call-template name="binObservation">
					<xsl:with-param name="observedNames" select="$observedNames"/>
					 <xsl:with-param name="observable" select="$observable"/>
					 <xsl:with-param name="stationNumber" select="$stationNumber"/>
					 <xsl:with-param name="stationName" select="$stationName"/>
					 <xsl:with-param name="latitude" select="$latitude"/>
					 <xsl:with-param name="longitude" select="$longitude"/>
					<xsl:with-param name="timePosition" select="$timePosition"/>
				</xsl:call-template>			
			</xsl:for-each>

		</xsl:for-each>
	</xsl:template>
	
	<xsl:template name="binObservation">
		<xsl:param name="observedNames"/>
		<xsl:param name="observable"/>
		<xsl:param name="stationNumber"/>
		<xsl:param name="stationName"/>
		<xsl:param name="latitude"/>
		<xsl:param name="longitude"/>

		<xsl:param name="timePosition"/>
		<xsl:for-each select="gml:valueComponents">
			<xsl:value-of select="$observable"/><xsl:text>,</xsl:text>
			<xsl:value-of select="$stationNumber"/><xsl:text>,</xsl:text>
			<xsl:value-of select="$stationName"/><xsl:text>,</xsl:text>
			<xsl:value-of select="$latitude"/><xsl:text>,</xsl:text>
			<xsl:value-of select="$longitude"/><xsl:text>,</xsl:text>

			<xsl:value-of select="$timePosition"/>
			<xsl:call-template name="getContextOrQuantity">
				<xsl:with-param name="observedNames" select="$observedNames"/>
			</xsl:call-template>
			<xsl:text disable-output-escaping="yes">&#010;</xsl:text>
		</xsl:for-each>
	</xsl:template>
	
  <xsl:template name="getVariableName">
    <xsl:param name="observedName"/>
    
    <xsl:choose>
      <xsl:when test="contains($observedName, '(')">
        <xsl:value-of select="substring-before($observedName,'(')"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$observedName"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
	<xsl:template name="getContextOrQuantity">
		<xsl:param name="observedNames"/>

		<xsl:if test="$observedNames != ''">
			<xsl:variable name="first" select="substring-before($observedNames,',')"/>
			<xsl:variable name="rest" select="substring-after($observedNames,',')"/>
      
			<xsl:if test="$first != ''">
        
        <xsl:variable name="fixed">
          <xsl:call-template name="getVariableName">
            <xsl:with-param name="observedName" select="$first"/>
          </xsl:call-template>
        </xsl:variable>
				<xsl:text>,</xsl:text>
				<xsl:variable name="namedElement" select="*[@name=$fixed]"/>
				<xsl:if test="$namedElement and not($namedElement/@xsi:nil='true')">
					<xsl:value-of select="$namedElement"/>
				</xsl:if>
				<xsl:call-template name="getContextOrQuantity">
					<xsl:with-param name="observedNames" select="$rest"/>
				</xsl:call-template>
			</xsl:if>
		</xsl:if>
	</xsl:template>
</xsl:stylesheet>
