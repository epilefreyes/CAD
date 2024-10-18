package edu.javeriana.cad;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import edu.javeriana.cad.beans.DatoHardware;
import edu.javeriana.cad.beans.DatosInstancia;

public class UnifierUtils {

	public static void main(String[] args) throws IOException {
		Path inputPath = Paths.get("G:\\Mi unidad\\Personales\\felipe\\Maestrías Ingenieria de Sistemas - IA\\2024-03\\Computacion_Alto_Desempeño\\proyecto2\\salidas");
		Path datosBaseInstanciasPath = Paths.get("G:\\Mi unidad\\Personales\\felipe\\Maestrías Ingenieria de Sistemas - IA\\2024-03\\Computacion_Alto_Desempeño\\proyecto2\\datosInstancias.txt");
		Path logsSalidaPath = Paths.get("G:\\Mi unidad\\Personales\\felipe\\Maestrías Ingenieria de Sistemas - IA\\2024-03\\Computacion_Alto_Desempeño\\proyecto2\\logs");
		Path compiladoPath = Paths.get("G:\\Mi unidad\\Personales\\felipe\\Maestrías Ingenieria de Sistemas - IA\\2024-03\\Computacion_Alto_Desempeño\\proyecto2\\compilado");
		
		if (Files.exists(compiladoPath)) {
			FileUtils.deleteDirectory(compiladoPath.toFile());
		}
		
		Files.createDirectories(compiladoPath);
		
		Path archivoInstancias = compiladoPath.resolve("datosInstancias.csv");
		
		Files.write(archivoInstancias, "Instancia,Arquitectura,Fabricante,CPUs,Memoria,Velocidad_memoria,Hypervisor,Procesador,Velocidad_Procesador,USD_Hour,Zona_Disponibilidad,Tiempo_ejecucion,Sistema_Operativo,Tamano_Palabra,CACHE1,CACHE2,CACHE3,CACHE4"
				.getBytes(), StandardOpenOption.CREATE);
		
		Path archivoDatos = compiladoPath.resolve("resultados.csv");
		Files.write(archivoDatos, "Instancia,Programa,Threads,Tamano,Tiempo,Iteracion"
				.getBytes(), StandardOpenOption.CREATE);
		
		Map<String,DatosInstancia> datosInstancias = 
				Files.readAllLines(datosBaseInstanciasPath)
				.stream()
				.filter(elem -> StringUtils.isNotBlank(elem))
				.map(elem -> elem.split("\t"))
				.filter(elem -> elem.length>=2)
				.map(elem -> new DatosInstancia(elem))
				.collect(Collectors.toMap(inst -> inst.getInstancia(), inst -> inst));
		
		for(String instancia : datosInstancias.keySet()) {
			DatosInstancia datos = datosInstancias.get(instancia);
			
			Path carpetaSalida = inputPath.resolve(instancia);
			if (!Files.exists(carpetaSalida)) {
				throw new IOException("La carpeta " + carpetaSalida.toAbsolutePath().toString() + " no existe!");
			}
			
			datos.setSistemaOperativo(getSistemaOperativo(carpetaSalida));
			
			copiarResultados(instancia,carpetaSalida.resolve("resultados.csv"),archivoDatos);
			
			DatoHardware datosHW = leerDatosHardware(carpetaSalida.resolve("hardware.txt"));
			
		}
		
	}

	private static DatoHardware leerDatosHardware(Path archivoHW) throws IOException {
		List<String> datosArchivo = Files.readAllLines(archivoHW);
		DatoHardware padre = new DatoHardware();
		padre.setProfundidad(0);
		padre.setEspaciosOriginal(0);
		padre.setTitleName(datosArchivo.remove(0));
		padre.setProperties(new Properties());
		while (!siguienteEsTituloHw(datosArchivo)) {
			setPropiedadHW(datosArchivo, padre);
		}
		
		
		leerLineasHw(padre,datosArchivo);
		return padre;
	}

	private static void setPropiedadHW(List<String> datosArchivo, DatoHardware padre) {
		String propiedadValor = datosArchivo.remove(0).trim();
		if (StringUtils.isNotBlank(propiedadValor)) {
			int dosPuntos = propiedadValor.indexOf(":");
			if (dosPuntos <=0) {
				padre.getProperties().setProperty(propiedadValor, "");
			} else {
				String propiedad = propiedadValor.substring(0,dosPuntos);
				String valorPropiedad = propiedadValor.substring(dosPuntos+1);
				padre.getProperties().setProperty(propiedad, valorPropiedad);
			}
		}
	}

	private static boolean siguienteEsTituloHw(List<String> datosArchivo) {
		if (datosArchivo.isEmpty()) {
			return true;
		}
		String linea = datosArchivo.get(0);
		return linea.trim().startsWith("*-");
	}

	private static void leerLineasHw(DatoHardware padre, List<String> datosArchivo) {
		Map<Integer,DatoHardware> ultimoPorNivel = new HashMap<Integer, DatoHardware>();
		ultimoPorNivel.put(0, padre);
		int ultimoNivel = 0;
		
		while (!datosArchivo.isEmpty()) {
			String linea = datosArchivo.remove(0);
			if (linea.trim().startsWith("*-")) {
				int espaciosAntes = linea.indexOf("*-");
				
				
				
			}
			
			
		}
		
		
		
		
	}

	private static void copiarResultados(String instancia, Path origenResultados, Path archivoDatos) throws IOException {
		if (!Files.exists(origenResultados)) {
			throw new IOException("El archivo de resultados " + origenResultados.toAbsolutePath().toString() + " no existe!");
		}
		
		List<String> lineas = Files.readAllLines(origenResultados);
		lineas.remove(0); //Encabezado
		
		List<String> lineasOut = lineas.stream().filter(str -> StringUtils.isNotBlank(str)).map(linea -> instancia + "," + linea).toList();
		Files.write(archivoDatos, lineasOut, StandardOpenOption.APPEND);
	}

	private static String getSistemaOperativo(Path carpetaSalida) throws IOException {
		Path osReleaseFile = carpetaSalida.resolve("os-release");
		if (!Files.exists(osReleaseFile)) {
			for(Path p : Files.list(carpetaSalida).toList()) {
				if (p.getFileName().toString().startsWith("os-release")) {
					osReleaseFile = p;
					break;
				}
			}
		}
		
		if (Files.exists(osReleaseFile)) {
			Properties props = new Properties();
			try(InputStream instr = Files.newInputStream(osReleaseFile)){
				props.load(instr);
			}
			
			if (props.containsKey("NAME") && props.containsKey("VERSION")) {
				return props.getProperty("NAME") + " " + props.getProperty("VERSION");
			}
			
			return props.getProperty("PRETTY_NAME");
		}
		
		return null;
	}
	
}
