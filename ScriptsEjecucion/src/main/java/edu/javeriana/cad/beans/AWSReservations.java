package edu.javeriana.cad.beans;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)

public class AWSReservations {

	private AWSInstanceElement[] reservations;

	@JsonGetter("Reservations")
	public AWSInstanceElement[] getReservations() {
		return reservations;
	}

	@JsonSetter("Reservations")
	public void setReservations(AWSInstanceElement[] reservations) {
		this.reservations = reservations;
	}

	@Override
	public String toString() {
		return "AWSReservations [reservations=" + Arrays.toString(reservations) + "]";
	}
	
	
	
}
