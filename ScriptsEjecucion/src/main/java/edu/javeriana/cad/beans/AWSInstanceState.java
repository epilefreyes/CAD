package edu.javeriana.cad.beans;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSetter;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class AWSInstanceState {

	private int code;
	private String name;
	
	@JsonGetter("Code")
	public int getCode() {
		return code;
	}
	
	@JsonSetter("Code")
	public void setCode(int code) {
		this.code = code;
	}
	
	@JsonGetter("Name")
	public String getName() {
		return name;
	}
	
	@JsonSetter("Name")
	public void setName(String name) {
		this.name = name;
	}
	
	@JsonIgnore
	public boolean isRunning() {
		return (code == 16);
	}

	@Override
	public String toString() {
		return "AWSInstanceState [code=" + code + ", name=" + name + "]";
	}
	
	
}
