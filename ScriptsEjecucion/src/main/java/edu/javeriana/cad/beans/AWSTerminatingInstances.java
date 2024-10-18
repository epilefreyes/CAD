package edu.javeriana.cad.beans;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSetter;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class AWSTerminatingInstances {

	private AWSTerminatingInstance[] terminatingInstances;

	@JsonGetter("TerminatingInstances")
	public AWSTerminatingInstance[] getTerminatingInstances() {
		return terminatingInstances;
	}

	@JsonSetter("TerminatingInstances")
	public void setTerminatingInstances(AWSTerminatingInstance[] terminatingInstances) {
		this.terminatingInstances = terminatingInstances;
	}

	@Override
	public String toString() {
		return "AWSTerminatingInstances [terminatingInstances=" + Arrays.toString(terminatingInstances) + "]";
	}

}
