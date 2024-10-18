package edu.javeriana.cad.test;

import edu.javeriana.cad.AWSRemoteUtils;
import edu.javeriana.cad.beans.AWSInstanceElement;
import edu.javeriana.cad.beans.AWSReservations;

public class TestCadAWS {

//	@Test
	public void testReadAwsBaseInstance() throws Exception {
		AWSReservations element = AWSRemoteUtils.getInstanceStatus("i-05b1e4d692a9b2ea7");
		System.out.println(element);
	}
	
	//@Test
	public void testCreateMachine() throws Exception{
		AWSInstanceElement instance = AWSRemoteUtils.createNewAWSMachine("t2.micro", true);
		
		System.out.println("Instance created:" + instance.toString());
	}

}
