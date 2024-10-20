package edu.javeriana.cad;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import edu.javeriana.cad.beans.SSHConnectionInfo;

/**
 * Funciopnes
 * @author FelipeReyesPalacio
 *
 */
public class CADScriptsExecutor {

	/**
	 * Bandera utilizada para marcar el fin de ejecución de un comando múltiple
	 */
	private static final String FINALIZED_FLAG = "-----FINALIZADO-----";

	/**
	 * Comandos para actualizar una instancia de UBuntu e instalar las librerías que se necesitarían para ejecución del script Perl
	 */
	private static final String[] UPDATE_UBUNTU_LIBS_COMMANDS = new String[] {
			"apt-get -o Acquire::Check-Valid-Until=false -o Acquire::Check-Date=false update",
			"apt-get -y full-upgrade",
			"apt install -y gcc make cmake libomp-dev zip unzip wget perl gzip tar build-essential",
			"reboot"
	};

	/**
	 * Comando (secuencia de comandos) utilizados para ejecutar una prueba completa en un equipo remoto
	 */
	private static final String EXECUTE_TEST_FULL = 
			"rm -rf cad" +
			"&& mkdir cad" + 
			"&& cd cad" +
			"&& wget https://d9y40m57d7b3c.cloudfront.net/freyes/baseScript_V2.tar.gz" +
			"&& tar -xvf baseScript_V2.tar.gz" +
			"&& chmod +x lanzador.pl" +
			"&& ./lanzador.pl" +
			"&& cd salidas " +
			"&& zip ../salidas.zip *" +
			"&& cd .. " +
			"&& echo " + FINALIZED_FLAG +
			"&& pwd";

	/**
	 * Función para ejecutar la actualización completa de un equipo remoto y lo reinicia.
	 * La función retorna cuando la máquina esté ya disponible de nuevo
	 * @param connectionInfo Información SSH de conexión a la máquina Ubuntu Remota
	 * @throws Exception Si se produce un error en la ejecución de los comandos, lanza esta excepción
	 */
	public static void updateUbuntuLibs(SSHConnectionInfo connectionInfo) throws Exception{
		for(String command : UPDATE_UBUNTU_LIBS_COMMANDS) {
			System.out.println("Command executed: " + command);
			System.out.println("------------------------------------------------");
			SSHUtils.executeRemoteCommand(connectionInfo, command, true,true);
			System.out.println("------------------------------------------------");
		}
		
		System.out.println("Waiting for reboot...");
		Thread.sleep(5000L);
		
		while (!SSHUtils.isRunningMachine(connectionInfo)) {
			Thread.sleep(2000L);
		}
		
		System.out.println("Machine restarted!");
	}

	/**
	 * Función que espera a que una máquina reiniciada (o recién creada) esté disponible vía SSH
	 * @param connectionInfo Información SSH de conexión a la máquina Ubuntu Remota
	 * @param maxWaitTime Tiempo máximo para esperar a que el equipo remoto esté disponible
	 * @throws Exception Si se producen errores durante la ejecución del comando, lanza esta excepción
	 */
	public static void waitForConnection(SSHConnectionInfo connectionInfo, Duration maxWaitTime) throws Exception {
		LocalDateTime lastTimeWait = LocalDateTime.now().plus(maxWaitTime);

		System.out.println("Waiting for machine availability...");
		while (!SSHUtils.isRunningMachine(connectionInfo)) {
			Thread.sleep(2000L);
			
			if (LocalDateTime.now().isAfter(lastTimeWait)) {
				throw new Exception("Timeout!");
			}
		}
	}

	/**
	 * Función para ejecutar el script Perl (y todas las pruebas) en un equipo remoto
	 * @param localDestinationPath Ruta donde dejar los resultados (se crea una carpeta con el nombre de la máquina)
	 * @param machineName Nombre de la máquina donde se ejecutarán las pruebas
	 * @param connectionInfo Información SSH de conexión a la máquina Ubuntu Remota
	 * @return Retorna la ruta donde han quedado los archivos de salida almacenados.
	 * @throws Exception Si se producen errores durante la ejecución del comando, lanza esta excepción
	 */
	public static Path executeTestFull(Path localDestinationPath, String machineName, SSHConnectionInfo connectionInfo) throws Exception{
		byte[] result = SSHUtils.executeRemoteCommand(connectionInfo, EXECUTE_TEST_FULL, false,true);
		String testOutput = new String(result,StandardCharsets.UTF_8);
		System.out.println("Test output:");
		System.out.println(testOutput);
		
		int finalizedFlag = testOutput.indexOf(FINALIZED_FLAG);
		if (finalizedFlag <=0) {
			throw new Exception("No hay bandera de finalizacion!");
		}
		
		String finalFolder = testOutput.substring(finalizedFlag + FINALIZED_FLAG.length()+1).replace("\n", "").trim();
		System.out.println("Final folder:" + finalFolder);
		String fileToGet = finalFolder + "/salidas.zip";
		fileToGet = fileToGet.replace("//", "/");
		
		Path tmpZipFile = SSHUtils.scpRemoteFile(connectionInfo, fileToGet);
		Path destinationFolder = localDestinationPath.resolve(machineName);
		int num = 1;
		while (Files.exists(destinationFolder)) {
			destinationFolder = localDestinationPath.resolve(machineName + "_" + num);
			num++;
		}
		
		Files.createDirectories(destinationFolder);
		
		try(ZipInputStream inStr = new ZipInputStream(Files.newInputStream(tmpZipFile, StandardOpenOption.READ))){
			ZipEntry entry = inStr.getNextEntry();
			while (entry!=null) {
				String name = entry.getName();
				if (!entry.isDirectory()) {
					int posicSlash = name.lastIndexOf("/");
					if (posicSlash>0) {
						name = name.substring(posicSlash+1);
					}
					Path destinationPath = destinationFolder.resolve(name);
					Files.copy(inStr, destinationPath, StandardCopyOption.REPLACE_EXISTING);
				}
				inStr.closeEntry();
				entry = inStr.getNextEntry();
			}
		}

		System.out.println("Resultados guardados en:" + destinationFolder.toAbsolutePath().toString());
		return destinationFolder;
	}

	
	
}
