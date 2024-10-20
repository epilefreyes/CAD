package edu.javeriana.cad;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.javeriana.cad.beans.AWSInstanceElement;
import edu.javeriana.cad.beans.AWSInstanceState;
import edu.javeriana.cad.beans.AWSReservations;
import edu.javeriana.cad.beans.AWSTerminatingInstances;

/**
 * Comprende las funciones utilizadas de automatización de los comandos de AWS para creación, validación y destrucción de instancias en
 * Amazon Web Services, mediante el uso de la API: AWS CLI  
 * @author Felipe Reyes Palacio
 *
 */
public class AWSRemoteUtils {

	/**
	 * Ruta de instalación del comando AWS para ejecución de comandos AWS CLI
	 */
	private static final String AWS_PATH = "C:\\Program Files\\Amazon\\AWSCLIV2\\aws.exe";
	/**
	 * Perfil de ejecución de comandos AWS CLI
	 */
	private static final String AWS_PROFILE = "admon";
	/**
	 * Imagen de sistema operativo Ubuntu para arquitectura X86
	 */
	private static final String AMI_x86 = "ami-0ea3c35c5c3284d82";
	/**
	 * Imagen de sistema operativo Ubuntu para arquitectura ARM
	 */
	private static final String AMI_ARM = "ami-01ebf7c0e446f85f9";
	/**
	 * Clave de seguridad utilizada
	 */
	private static final String KEY_NAME = "cad-testing";
	/**
	 * ID del grupo de seguridad utilizado (red)
	 */
	private static final String SECURITY_GROUPID = "sg-03e839dafcf838502";
	
	/**
	 * Función que genera el estado actual de una instancia AWS
	 * @param instanceId ID de la instancia a validar
	 * @return Objeto con los datos de la instancia
	 * @throws Exception Si se produce un error en la ejecución del comando, lanza esta excepción
	 */
	public static AWSReservations getInstanceStatus(String instanceId) throws Exception {
		return executeCommandAWS(AWSReservations.class, 
				AWS_PATH, "ec2", "describe-instances", "--max-items", "100",
				"--profile", AWS_PROFILE, "--instance-ids", instanceId);
	}

	/**
	 * Función privada utilizada para ejecutar comandos de AWS y capturar la salida en formato JSON
	 * @param <T> Objeto de salida, de tipo genérico
	 * @param clazz Clase Java de salida del comando
	 * @param params Parámetros de ejecución del comando
	 * @return Objeto JSON obtenido luego de la ejecución del comando
	 * @throws Exception Si se produce un error en la ejecución del comando, lanza esta excepción
	 */
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
				throw new IOException(String.format("getInstanceStatus: Exit code == %d, outData:\n %s",finishedCode, Files.readString(tmpOut)));
			}

			ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(tmpOut.toFile(), clazz);
			
		} finally {
			FileUtils.deleteQuietly(tmpOut.toFile());
		}
	}

	/**
	 * Función para la creación de una nueva instancia en AWS mediante el servicio EC2
	 * @param instanceType Tipo de instancia a crear
	 * @param availabilityZone Zona de disponibilidad de la instancia
	 * @param isx86 True si es una instancia de arquitectura X86, false si no
	 * @return Datos de la instancia creada
	 * @throws Exception Si se produce un error en la ejecución del comando, lanza esta excepción
	 */
	public static AWSInstanceElement createNewAWSMachine(String instanceType, String availabilityZone, boolean isx86) throws Exception {
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

	/**
	 * Función de terminación (eliminación) de una instancia AWS
	 * @param instanceId ID de la instancia a terminar
	 * @return Resultado del proceso de terminación
	 * @throws Exception Si se produce un error en la ejecución del comando, lanza esta excepción
	 */
	public static AWSTerminatingInstances terminateInstance(String instanceId) throws Exception {
		return executeCommandAWS(AWSTerminatingInstances.class, 
				AWS_PATH, "ec2", "terminate-instances", "--profile", AWS_PROFILE, "--instance-ids", instanceId);
	}

	/**
	 * Función privada, utilizada para determinar si el objeto generado de una instancia es válido 
	 * @param instance Instancia a validar
	 * @throws Exception Si el objeto de la instancia no es válido, lanza una excepción.
	 */
	private static void validateInstance(AWSInstanceElement instance) throws Exception {
		if (instance.getInstances()==null || instance.getInstances().length!=1) {
			throw new Exception("What happens? The machine hasn't been created! instance:" + instance.toString());
		}
	}
	
	

}
