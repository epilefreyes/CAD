package edu.javeriana.cad;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.LocalDateTime;

import org.apache.commons.io.FileUtils;

import edu.javeriana.cad.beans.AWSInstanceElement;
import edu.javeriana.cad.beans.AWSInstances;
import edu.javeriana.cad.beans.AWSReservations;
import edu.javeriana.cad.beans.AWSTerminatingInstances;
import edu.javeriana.cad.beans.SSHConnectionInfo;

public class CADExecuteFull {

	public static void executeFullTest(Path outputPath, String instanceType, String availabilityZone, boolean isx86) throws Exception {
		LocalDateTime startTime = LocalDateTime.now();
		
		//1. Crea la máquina...
		AWSInstanceElement instance = AWSRemoteUtils.createNewAWSMachine(instanceType,availabilityZone, isx86);
		String ipAddress = instance.getInstances()[0].getPublicIpAddress();
		String idInstancia = instance.getInstances()[0].getInstanceId();
		
		//2. Ejecuta la conexión y actualiza la máquina...
		String pemTesting = SSHUtils.getCadTestingPem();
		Path tmpPem = Files.createTempFile("tmp", ".pem");
		try{
			Files.writeString(tmpPem, pemTesting, StandardOpenOption.CREATE);
			String passphrase = tmpPem.toAbsolutePath().toString();
			
			SSHConnectionInfo connInfo = new SSHConnectionInfo(ipAddress, 22, "ubuntu", passphrase, null);
			
			CADScriptsExecutor.waitForConnection(connInfo,Duration.ofMinutes(3));
			
			CADScriptsExecutor.updateUbuntuLibs(connInfo);

			//Ejecuta  el test completo, y trae los resultados
			CADScriptsExecutor.executeTestFull(outputPath, instanceType, connInfo);
		} finally {
			FileUtils.deleteQuietly(tmpPem.toFile());
			AWSTerminatingInstances terminating = AWSRemoteUtils.terminateInstance(idInstancia);
			if (terminating.getTerminatingInstances()==null || terminating.getTerminatingInstances().length==0) {
				throw new Exception("Terminating instances is invalid: " + terminating.toString());
			}
			
			System.out.println("Waiting for machine termination...");
			Thread.sleep(5000);
			AWSReservations statusInstance = AWSRemoteUtils.getInstanceStatus(idInstancia);
			while (isNotTerminated(statusInstance)) {
				Thread.sleep(5000);
				statusInstance = AWSRemoteUtils.getInstanceStatus(idInstancia);
			}
			
			Duration totalTime = Duration.between(startTime, LocalDateTime.now());
			System.out.println("Total execution time for machine (seconds):" + totalTime.toSeconds());
			
		}
		
		System.out.println("Test completed!");
		
	}

	private static boolean isNotTerminated(AWSReservations statusInstance) {
		if (statusInstance == null || statusInstance.getReservations()==null || statusInstance.getReservations().length==0) {
			return true;
		}
		
		AWSInstanceElement elem = statusInstance.getReservations()[0];
		if (elem.getInstances() == null || elem.getInstances().length==0) {
			return true;
		}
		
		AWSInstances instance = elem.getInstances()[0];
		
		return instance.getState().isTerminated();
	}
	
}
