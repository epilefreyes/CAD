package edu.javeriana.cad.test.beans;

public class InstanceInformation {

	private String instanceType;
	private String availabilityZone;
	private boolean isX86;
	
	public InstanceInformation(String instanceType, String availabilityZone, boolean isX86) {
		this.instanceType = instanceType;
		this.availabilityZone = availabilityZone;
		this.isX86 = isX86;
	}
	
	public String getInstanceType() {
		return instanceType;
	}
	public void setInstanceType(String instanceType) {
		this.instanceType = instanceType;
	}
	public String getAvailabilityZone() {
		return availabilityZone;
	}
	public void setAvailabilityZone(String availabilityZone) {
		this.availabilityZone = availabilityZone;
	}
	public boolean isX86() {
		return isX86;
	}
	public void setX86(boolean isX86) {
		this.isX86 = isX86;
	}

	@Override
	public String toString() {
		return "InstanceInformation [instanceType=" + instanceType + ", availabilityZone=" + availabilityZone
				+ ", isX86=" + isX86 + "]";
	}
	
	
	
}
