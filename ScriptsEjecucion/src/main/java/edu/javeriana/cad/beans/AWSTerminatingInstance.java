package edu.javeriana.cad.beans;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSetter;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class AWSTerminatingInstance {

	private AWSInstanceState currentState;
	private AWSInstanceState previousState;
	private String instanceId;
	
	@JsonGetter("CurrentState")
	public AWSInstanceState getCurrentState() {
		return currentState;
	}
	@JsonSetter("CurrentState")
	public void setCurrentState(AWSInstanceState currentState) {
		this.currentState = currentState;
	}
	@JsonGetter("PreviousState")
	public AWSInstanceState getPreviousState() {
		return previousState;
	}
	@JsonSetter("PreviousState")
	public void setPreviousState(AWSInstanceState previousState) {
		this.previousState = previousState;
	}
	@JsonGetter("InstanceId")
	public String getInstanceId() {
		return instanceId;
	}
	@JsonSetter("InstanceId")
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}
	
	
	
}
