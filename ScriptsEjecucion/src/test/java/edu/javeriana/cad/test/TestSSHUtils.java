package edu.javeriana.cad.test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import edu.javeriana.cad.CADScriptsExecutor;
import edu.javeriana.cad.SSHUtils;
import edu.javeriana.cad.beans.SSHConnectionInfo;

public class TestSSHUtils {

	private SSHConnectionInfo generateConnectionInfo() {
		SSHConnectionInfo connInfo = new SSHConnectionInfo("190.131.236.58", 20000, "river", null, "");
//		SSHConnectionInfo connInfo = new SSHConnectionInfo("190.131.236.58", 20022, "summar", null, "");
		return connInfo;
	}

//	@Test
	public void testConnectWithPassword() throws Exception {
		SSHConnectionInfo connInfo = generateConnectionInfo();
		byte[] result = SSHUtils.executeRemoteCommand(connInfo, "ls -la",true,true);
		
		System.out.println("Resultado:");
		System.out.println(new String(result));
	}

	
//	@Test
	public void testCopyFileSCP() throws Exception{
		SSHConnectionInfo connInfo = generateConnectionInfo();
		Path fileCopied = SSHUtils.scpRemoteFile(connInfo, "/home/river/commons/metabase/metabase.db.mv.db");
		String md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(Files.readAllBytes(fileCopied));
		System.out.println(md5);
		FileUtils.deleteQuietly(fileCopied.toFile());
	}
	
	//@Test
	public void testUpdateServer() throws Exception{
		SSHConnectionInfo connInfo = generateConnectionInfo();
		CADScriptsExecutor.updateUbuntuLibs(connInfo);
	}
	
	@Test
	public void testExecuteFullTest() throws Exception{
		String pemTesting = SSHUtils.getCadTestingPem();
		Path tmpPem = Files.createTempFile("tmp", ".pem");
		try{
			Files.writeString(tmpPem, pemTesting, StandardOpenOption.CREATE);
			String passphrase = tmpPem.toAbsolutePath().toString();
			
			SSHConnectionInfo connInfo = new SSHConnectionInfo("3.144.161.174", 22, "ubuntu", passphrase, null);
			connInfo.setDebug(true);
			CADScriptsExecutor.updateUbuntuLibs(connInfo);
			
			Path outputPath = Paths.get("G:\\Mi unidad\\Personales\\felipe\\Maestrías Ingenieria de Sistemas - IA\\2024-03\\Computacion_Alto_Desempeño\\proyecto2\\salidas");
			String machineName = "summarTestServer";
			
			CADScriptsExecutor.executeTestFull(outputPath, machineName, connInfo);
		} finally {
			FileUtils.deleteQuietly(tmpPem.toFile());
		}
		
	}
	
}
