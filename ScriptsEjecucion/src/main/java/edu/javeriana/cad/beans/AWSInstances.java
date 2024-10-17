package edu.javeriana.cad.beans;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSetter;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class AWSInstances {

	private String instanceId;
	private String instanceType;
	private String privateIpAddress;
	private String publicDnsName;
	private String publicIpAddress;
	private AWSInstanceState state;
	
	@JsonGetter("InstanceId")
	public String getInstanceId() {
		return instanceId;
	}

	@JsonSetter("InstanceId")
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	@JsonGetter("InstanceType")
	public String getInstanceType() {
		return instanceType;
	}

	@JsonSetter("InstanceType")
	public void setInstanceType(String instanceType) {
		this.instanceType = instanceType;
	}

	@JsonGetter("PrivateIpAddress")
	public String getPrivateIpAddress() {
		return privateIpAddress;
	}

	@JsonSetter("PrivateIpAddress")
	public void setPrivateIpAddress(String privateIpAddress) {
		this.privateIpAddress = privateIpAddress;
	}

	@JsonGetter("PublicDnsName")
	public String getPublicDnsName() {
		return publicDnsName;
	}

	@JsonSetter("PublicDnsName")
	public void setPublicDnsName(String publicDnsName) {
		this.publicDnsName = publicDnsName;
	}

	@JsonGetter("State")
	public AWSInstanceState getState() {
		return state;
	}

	@JsonSetter("State")
	public void setState(AWSInstanceState state) {
		this.state = state;
	}

	@JsonGetter("PublicIpAddress")
	public String getPublicIpAddress() {
		return publicIpAddress;
	}

	@JsonSetter("PublicIpAddress")
	public void setPublicIpAddress(String publicIpAddress) {
		this.publicIpAddress = publicIpAddress;
	}

	@Override
	public String toString() {
		return "AWSInstances [instanceId=" + instanceId + ", instanceType=" + instanceType + ", privateIpAddress="
				+ privateIpAddress + ", publicDnsName=" + publicDnsName + ", publicIpAddress=" + publicIpAddress
				+ ", state=" + state + "]";
	}

	
	
	
}
