package edu.javeriana.cad.test;

import java.nio.file.Paths;
import java.time.Duration;

import org.junit.Test;

import edu.javeriana.cad.CADScriptsExecutor;
import edu.javeriana.cad.beans.SSHConnectionInfo;

public class TestOtherMachines {

	@Test
	public void ejecutarPruebas() throws Exception {
		SSHConnectionInfo connInfo = new SSHConnectionInfo("89.117.23.46", "root", null, "D3l1noal0ch0");
		CADScriptsExecutor.waitForConnection(connInfo, Duration.ofMinutes(5));
		CADScriptsExecutor.executeTestFull(Paths.get("C:\\testCAD\\salidas"), "contaboInfodigg", connInfo);
		
	}


	
}
