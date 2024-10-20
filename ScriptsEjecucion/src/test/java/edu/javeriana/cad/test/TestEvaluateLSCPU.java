package edu.javeriana.cad.test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.LocalDateTime;

import org.apache.commons.io.FileUtils;

import edu.javeriana.cad.AWSRemoteUtils;
import edu.javeriana.cad.CADExecuteFull;
import edu.javeriana.cad.CADScriptsExecutor;
import edu.javeriana.cad.SSHUtils;
import edu.javeriana.cad.beans.AWSInstanceElement;
import edu.javeriana.cad.beans.AWSReservations;
import edu.javeriana.cad.beans.AWSTerminatingInstances;
import edu.javeriana.cad.beans.SSHConnectionInfo;

/**
 * Clase utilizada para probar y validar la ejecución del comando LSCPU, cuando se hizo necesario para completar información
 * incompleta del comando LSHW
 * @author FelipeReyesPalacio
 *
 */
public class TestEvaluateLSCPU {

	//@Test
	public void testLSCPU() throws Exception{
		
		Path outputPath = Paths.get("C:\\testCAD\\lscpu\\");
		testMachine(outputPath, "c6g.8xlarge", "us-east-2c", false);
		testMachine(outputPath, "a1.metal", "us-east-2a", false);
		testMachine(outputPath, "c6g.16xlarge", "us-east-2c", false);
		testMachine(outputPath, "m6g.2xlarge", "us-east-2c", false);
		testMachine(outputPath, "c8g.8xlarge", "us-east-2c", false);
		testMachine(outputPath, "x1e.8xlarge", "us-east-2a", true);
		testMachine(outputPath, "x1e.4xlarge", "us-east-2a", true);
		testMachine(outputPath, "c7g.8xlarge", "us-east-2c", false);
		testMachine(outputPath, "m4.4xlarge", "us-east-2c", true);
		testMachine(outputPath, "c7g.16xlarge", "us-east-2c", false);
		testMachine(outputPath, "c7gn.4xlarge", "us-east-2c", false);
		testMachine(outputPath, "c4.4xlarge", "us-east-2c", true);
		testMachine(outputPath, "h1.4xlarge", "us-east-2c", true);
		testMachine(outputPath, "c7i-flex.4xlarge", "us-east-2c", true);
		testMachine(outputPath, "r8g.2xlarge", "us-east-2c", false);
		testMachine(outputPath, "h1.8xlarge", "us-east-2c", true);
		testMachine(outputPath, "c8g.4xlarge", "us-east-2c", false);
		testMachine(outputPath, "r7g.2xlarge", "us-east-2c", false);
		System.out.println("Finalizado!");
		
		
	}

	private void testMachine(Path basePath, String instanceType, String availabilityZone, boolean isX86) throws Exception{
		Path outputPath = basePath.resolve(instanceType + ".txt");
		Files.deleteIfExists(outputPath);
		LocalDateTime startTime = LocalDateTime.now();
		
		//1. Crea la máquina...
		AWSInstanceElement instance = AWSRemoteUtils.createNewAWSMachine(instanceType,availabilityZone, isX86);
		String ipAddress = instance.getInstances()[0].getPublicIpAddress();
		String idInstancia = instance.getInstances()[0].getInstanceId();
		
		//2. Ejecuta la conexión
		String pemTesting = SSHUtils.getCadTestingPem();
		Path tmpPem = Files.createTempFile("tmp", ".pem");
		try{
			Files.writeString(tmpPem, pemTesting, StandardOpenOption.CREATE);
			String passphrase = tmpPem.toAbsolutePath().toString();
			
			SSHConnectionInfo connInfo = new SSHConnectionInfo(ipAddress, 22, "ubuntu", passphrase, null);
			
			CADScriptsExecutor.waitForConnection(connInfo,Duration.ofMinutes(3));
			
			byte[] lscpuData = SSHUtils.executeRemoteCommand(connInfo, "lscpu", true, false);
			Files.write(outputPath, lscpuData, StandardOpenOption.CREATE);
			
		} finally {
			FileUtils.deleteQuietly(tmpPem.toFile());
			AWSTerminatingInstances terminating = AWSRemoteUtils.terminateInstance(idInstancia);
			if (terminating.getTerminatingInstances()==null || terminating.getTerminatingInstances().length==0) {
				throw new Exception("Terminating instances is invalid: " + terminating.toString());
			}
			
			System.out.println("Waiting for machine termination...");
			Thread.sleep(5000);
			AWSReservations statusInstance = AWSRemoteUtils.getInstanceStatus(idInstancia);
			while (CADExecuteFull.isNotTerminated(statusInstance)) {
				Thread.sleep(5000);
				statusInstance = AWSRemoteUtils.getInstanceStatus(idInstancia);
			}
			
			Duration totalTime = Duration.between(startTime, LocalDateTime.now());
			System.out.println("Total execution time for machine (seconds):" + totalTime.toSeconds());
			
		}
		
		System.out.println("Test completed!");		
	}
	
	
}
