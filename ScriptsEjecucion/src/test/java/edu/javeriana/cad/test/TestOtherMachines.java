package edu.javeriana.cad.test;

import java.nio.file.Paths;
import java.time.Duration;

import edu.javeriana.cad.CADScriptsExecutor;
import edu.javeriana.cad.beans.SSHConnectionInfo;

/**
 * Función para automatizar la ejecucón de pruebas completas sobre equipos remotos (no AWS)
 * @author FelipeReyesPalacio
 *
 */
public class TestOtherMachines {

	//@Test
	public void ejecutarPruebas() throws Exception {
		SSHConnectionInfo connInfo = new SSHConnectionInfo("89.117.23.46", "root", null, "");
		CADScriptsExecutor.waitForConnection(connInfo, Duration.ofMinutes(5));
		CADScriptsExecutor.executeTestFull(Paths.get("C:\\testCAD\\salidas"), "contaboInfodigg", connInfo);
		
	}


	
}
