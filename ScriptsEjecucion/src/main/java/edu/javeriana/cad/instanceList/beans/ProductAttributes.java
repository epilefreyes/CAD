package edu.javeriana.cad.instanceList.beans;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class ProductAttributes {

	private String regionCode;
	private String physicalProcessor;
	private String clockSpeed;
	private String instanceType;
	public String getRegionCode() {
		return regionCode;
	}
	public void setRegionCode(String regionCode) {
		this.regionCode = regionCode;
	}
	public String getPhysicalProcessor() {
		return physicalProcessor;
	}
	public void setPhysicalProcessor(String physicalProcessor) {
		this.physicalProcessor = physicalProcessor;
	}
	public String getClockSpeed() {
		return clockSpeed;
	}
	public void setClockSpeed(String clockSpeed) {
		this.clockSpeed = clockSpeed;
	}
	public String getInstanceType() {
		return instanceType;
	}
	public void setInstanceType(String instanceType) {
		this.instanceType = instanceType;
	}
	@Override
	public String toString() {
		if (StringUtils.isBlank(regionCode) || StringUtils.isBlank(instanceType)) {
			return "";
		}
		return String.format("%s\t%s\t%s\t%s\t", instanceType, regionCode, physicalProcessor, clockSpeed);
	}
	
	
}
