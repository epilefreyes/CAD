package edu.javeriana.cad;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.javeriana.cad.beans.AWSInstanceElement;
import edu.javeriana.cad.beans.AWSInstanceState;
import edu.javeriana.cad.beans.AWSReservations;
import edu.javeriana.cad.beans.AWSTerminatingInstances;

public class AWSRemoteUtils {

	private static final String AWS_PATH = "C:\\Program Files\\Amazon\\AWSCLIV2\\aws.exe";
	private static final String AWS_PROFILE = "admon";
	
	private static final String AMI_x86 = "ami-0ea3c35c5c3284d82";
	private static final String AMI_ARM = "ami-01ebf7c0e446f85f9";
	private static final String KEY_NAME = "cad-testing";
	private static final String SECURITY_GROUPID = "sg-03e839dafcf838502";
	
	
	public static AWSReservations getInstanceStatus(String instanceId) throws Exception {
		return executeCommandAWS(AWSReservations.class, 
				AWS_PATH, "ec2", "describe-instances", "--max-items", "100",
				"--profile", AWS_PROFILE, "--instance-ids", instanceId);
	}

	private static <T> T executeCommandAWS(Class<T> clazz, String...params)
			throws Exception {
		ProcessBuilder pb = new ProcessBuilder(params);
		Path tmpOut = Files.createTempFile("tmp", ".out");
		pb.redirectErrorStream(true);
		pb.redirectOutput(tmpOut.toFile());
		
		try {
			Process proc = pb.start();
			int finishedCode = proc.waitFor();
			if (finishedCode!=0) {
				throw new IOException(String.format("getInstanceStatus: Exit code == %d, outData:\n %s",finishedCode, file2Str(tmpOut)));
			}

			ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(tmpOut.toFile(), clazz);
			
		} finally {
			FileUtils.deleteQuietly(tmpOut.toFile());
		}
	}

	private static String file2Str(Path file) throws IOException {
		return Files.readString(file);
	}
	
	public static AWSInstanceElement createNewAWSMachine(String instanceType, String availabilityZone, boolean isx86) throws Exception, DatabindException, IOException, InterruptedException {
		AWSInstanceElement instance = 
		executeCommandAWS(AWSInstanceElement.class,
				AWS_PATH
				,"ec2"
				,"run-instances"
				,"--image-id"
				,(isx86 ? AMI_x86 : AMI_ARM)
				,"--count"
				,"1"
				,"--instance-type"
				,instanceType
				,"--key-name"
				,KEY_NAME
				,"--security-group-ids"
				,SECURITY_GROUPID
				,"--profile"
				,AWS_PROFILE
				,"--placement"
				,"AvailabilityZone=" + availabilityZone
			);
		
		validateInstance(instance);
		
		System.out.println("Instance " + instanceType + " created.  Waiting for running state...");
		String instanceId = instance.getInstances()[0].getInstanceId();
		AWSInstanceState state = instance.getInstances()[0].getState();
		
		while (!state.isRunning()) {
			Thread.sleep(5000);
			System.out.println("Current state:" + state.toString());
			
			AWSReservations reservation = getInstanceStatus(instanceId);
			if (reservation.getReservations()==null || reservation.getReservations().length!=1) {
				throw new Exception("What happens HERE? The machine doesn't has reservation info! Instance:" + reservation.toString());
			}
			
			instance = reservation.getReservations()[0];
			validateInstance(instance);
			state = instance.getInstances()[0].getState();
		}
		
		return instance;
	}

	public static AWSTerminatingInstances terminateInstance(String instanceId) throws Exception {
		return executeCommandAWS(AWSTerminatingInstances.class, 
				AWS_PATH, "ec2", "terminate-instances", "--profile", AWS_PROFILE, "--instance-ids", instanceId);
	}
	
	private static void validateInstance(AWSInstanceElement instance) throws Exception {
		if (instance.getInstances()==null || instance.getInstances().length!=1) {
			throw new Exception("What happens? The machine hasn't been created! instance:" + instance.toString());
		}
	}
	
	

}
