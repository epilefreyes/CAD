package edu.javeriana.cad;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import edu.javeriana.cad.beans.DatoHardware;
import edu.javeriana.cad.beans.DatosInstancia;

public class UnifierUtils {

	public static void main(String[] args) throws IOException {
		Path rutaBase = Paths.get("G:\\Mi unidad\\Personales\\felipe\\Maestrías Ingenieria de Sistemas - IA\\2024-03\\Computacion_Alto_Desempeño\\proyecto2");
		Path inputPath = rutaBase.resolve("salidas");
		Path datosBaseInstanciasPath = rutaBase.resolve("datosInstancias.txt");
		Path logsSalidaPath = rutaBase.resolve("logs");
		Path compiladoPath = rutaBase.resolve("compilado");
		
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
			
			long totalDatosSec = copiarResultados(instancia,carpetaSalida.resolve("resultados.csv"),archivoDatos);
			
			DatoHardware datosHW = leerDatosHardware(carpetaSalida.resolve("hardware.txt"));
			datos.setProcesador(datosHW.getValorHijo(datosHW.evaluarRutaExistente("core/cpu:0/product","core/cpu/product"),datos.getProcesador()));
			datos.setFabricante(datosHW.getValorHijo("vendor",datos.getFabricante()));
			datos.setCPUs(Math.max(datos.getCPUs(), contarCPUs(datosHW.getHijoByName("core"))));
			datos.setMemoria(Integer.max(datos.getMemoria(), getValorMB(datosHW.getValorHijo("core/memory/size","0 MB"))));
			datos.setVelocidadMemoriaMhz(Integer.max(datos.getVelocidadMemoriaMhz(), getValorMhz(datosHW.getValorHijo(datosHW.evaluarRutaExistente("core/memory/bank/clock","core/memory/bank:0/clock"), "0 Mhz"))));

		}
		
	}

	private static int getValorMhz(String valorHijo) {
		if (StringUtils.isBlank(valorHijo)) {
			return 0;
		}
		valorHijo = valorHijo.toUpperCase().trim();
		int multiplicador = 1000;
		if (valorHijo.contains("KHZ")) {
			multiplicador = 1;
		} else if (valorHijo.contains("GHZ")) {
			multiplicador = 1000000;
		}
		
		
		
		
		return 0;
	}

	private static int getValorMB(String valor) {
		valor = valor.toUpperCase().trim();
		int multiplicador = 1;
		if (valor.endsWith("GIB")) {
			multiplicador = 1000;
		} else if (valor.endsWith("GB")) {
			multiplicador = 1024;
		}
		
		valor = getOnlyDigits(valor);
		if (StringUtils.isBlank(valor)) {
			return 0;
		}
		int valorBase = Integer.parseInt(valor);

		return valorBase * multiplicador;
	}

	public static String getOnlyDigits(String cad) {
		if (StringUtils.isBlank(cad)) {
			return "";
		}
		StringBuffer str = new StringBuffer();
		for(char ch : cad.toCharArray()) {
			if (ch >= '0' && ch <= '9') {
				str.append(ch);
			}
		}
		return str.toString();
	}
	
	private static int contarCPUs(DatoHardware core) {
		if (core == null || core.getChildren() == null) {
			return 0;
		}
		
		int conteo = 0;
		while(core.getHijoByName("cpu:" + conteo)!=null) {
			conteo++;
		}
		
		return conteo;
	}

	private static DatoHardware leerDatosHardware(Path archivoHW) throws IOException {
		List<String> datosArchivo = Files.readAllLines(archivoHW);
//		DatoHardware padre = new DatoHardware();
//		padre.setProfundidad(0);
//		padre.setTitleName(datosArchivo.remove(0));
//		padre.setProperties(new Properties());
//		while (!siguienteEsTituloHw(datosArchivo)) {
//			setPropiedadHW(datosArchivo, padre);
//		}
//		int espaciosHijo = datosArchivo.get(0).indexOf("*-");
//		
		return crearArbol(datosArchivo);
	}

	private static DatoHardware crearArbol(List<String> lineas) {
        Stack<DatoHardware> pila = new Stack<>();
        DatoHardware raiz = null;
        DatoHardware ultimoNodo = null;
        
        for (String linea : lineas) {
        	if (StringUtils.isBlank(linea)) {
        		continue;
        	}
        	
        	if (!esLineaTitulo(linea)) {
        		setPropiedadHW(ultimoNodo, linea);
        		continue;
        	}
        	
            int profundidad = linea.indexOf("*-");
            String titulo = null;
            if (profundidad >0) {
            	titulo = linea.substring(profundidad+2);
            } else {
            	titulo = linea.trim();
            }
            
            ultimoNodo = new DatoHardware(titulo,profundidad);

            while (!pila.isEmpty() && pila.peek().getProfundidad() >= profundidad) {
                pila.pop();
            }

            if (!pila.isEmpty()) {
                pila.peek().getChildren().add(ultimoNodo);
            } else {
                raiz = ultimoNodo;
            }

            pila.push(ultimoNodo);
        }

        //Actualiza la profundidad con valores 0,1,2...
        updateProfundidad(raiz,0);
        
        return raiz;
    }
	
	private static void updateProfundidad(DatoHardware nodo, int profundidad) {
		nodo.setProfundidad(profundidad);
		
		if (nodo.getChildren()!=null) {
			for(DatoHardware child : nodo.getChildren()) {
				updateProfundidad(child, profundidad+1);
			}
		}
	}

	private static void setPropiedadHW(DatoHardware padre,String linea) {
		if (StringUtils.isNotBlank(linea)) {
			int dosPuntos = linea.indexOf(":");
			if (dosPuntos <=0) {
				padre.getProperties().setProperty(linea.trim(), "");
			} else {
				String propiedad = linea.substring(0,dosPuntos).trim();
				String valorPropiedad = linea.substring(dosPuntos+1).trim();
				padre.getProperties().setProperty(propiedad, valorPropiedad);
			}
		}
	}

	private static boolean esLineaTitulo(String linea) {
		return !linea.startsWith(" ") || linea.trim().startsWith("*-");
	}

	private static long copiarResultados(String instancia, Path origenResultados, Path archivoDatos) throws IOException {
		if (!Files.exists(origenResultados)) {
			throw new IOException("El archivo de resultados " + origenResultados.toAbsolutePath().toString() + " no existe!");
		}
		
		List<String> lineas = Files.readAllLines(origenResultados);
		lineas.remove(0); //Encabezado
		
		long totalTiempoSec = Math.round(lineas.stream().map(linea -> linea.split("\\,")).map(datos -> Double.parseDouble(datos[3])/1000000.0f).reduce((double)0.0f, (x,y) -> x+y));
		
		List<String> lineasOut = lineas.stream().filter(str -> StringUtils.isNotBlank(str)).map(linea -> instancia + "," + linea).toList();
		Files.write(archivoDatos, lineasOut, StandardOpenOption.APPEND);
		return totalTiempoSec;
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
