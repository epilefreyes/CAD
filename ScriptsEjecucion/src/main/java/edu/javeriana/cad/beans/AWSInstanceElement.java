package edu.javeriana.cad.beans;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSetter;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class AWSInstanceElement {
	
	private AWSInstances[] instances;

	@JsonGetter("Instances")
	public AWSInstances[] getInstances() {
		return instances;
	}

	@JsonSetter("Instances")
	public void setInstances(AWSInstances[] instances) {
		this.instances = instances;
	}

	@Override
	public String toString() {
		return "AWSInstanceElement [instances=" + Arrays.toString(instances) + "]";
	}

	
}
