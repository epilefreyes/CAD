package edu.javeriana.cad.test;

import java.io.IOException;

import edu.javeriana.cad.AWSRemoteUtils;
import edu.javeriana.cad.beans.AWSReservations;

public class TestReadAWSJsons {

//	@Test
	public void testReadAwsBaseInstance() throws IOException, InterruptedException {
		AWSReservations element = AWSRemoteUtils.getInstanceStatus("i-05b1e4d692a9b2ea7");
		System.out.println(element);
	}
	
}
