package edu.javeriana.cad;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.javeriana.cad.beans.AWSReservations;

public class AWSRemoteUtils {

	private static final String AWS_PATH = "C:\\Program Files\\Amazon\\AWSCLIV2\\aws.exe";
	private static final String AWS_PROFILE = "admon";
	
	public static AWSReservations getInstanceStatus(String instanceId) throws IOException, InterruptedException {
		ProcessBuilder pb = new ProcessBuilder(AWS_PATH,"ec2","describe-instances","--max-items","100","--profile",AWS_PROFILE,"--instance-ids",instanceId);
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
			return mapper.readValue(tmpOut.toFile(), AWSReservations.class);
			
		} finally {
			FileUtils.deleteQuietly(tmpOut.toFile());
		}
	}

	private static String file2Str(Path file) throws IOException {
		return Files.readString(file);
	}
	
	
	
	

}
