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
		Path lscpuPath = rutaBase.resolve("lscpu");
		
		
		if (Files.exists(compiladoPath)) {
			FileUtils.deleteDirectory(compiladoPath.toFile());
		}
		
		Files.createDirectories(compiladoPath);
		
		Path archivoInstancias = compiladoPath.resolve("datosInstancias.csv");
		
		Files.write(archivoInstancias, "Instancia,Arquitectura,Fabricante,CPUs,Memoria,Velocidad_memoria,Hypervisor,Procesador,Velocidad_Procesador,USD_Hour,Zona_Disponibilidad,Tiempo_ejecucion,Sistema_Operativo,Tamano_Palabra,CACHE1,CACHE2,CACHE3,CACHE4\n"
				.getBytes(), StandardOpenOption.CREATE);
		
		Path archivoDatos = compiladoPath.resolve("resultados.csv");
		Files.write(archivoDatos, "Instancia,Programa,Threads,Tamano,Tiempo,Iteracion\n"
				.getBytes(), StandardOpenOption.CREATE);
		
		Map<String,DatosInstancia> datosInstancias = 
				Files.readAllLines(datosBaseInstanciasPath)
				.stream()
				.filter(elem -> StringUtils.isNotBlank(elem))
				.map(elem -> elem.split("\t"))
				.filter(elem -> elem.length>=2)
				.map(elem -> new DatosInstancia(elem))
				.collect(Collectors.toMap(inst -> inst.getInstancia(), inst -> inst));
		
		
		int numInstancia = 1;
		for(String instancia : datosInstancias.keySet()) {
			System.out.println(String.format("[%d/%d] Procesando instancia: %s...",numInstancia,datosInstancias.size(),instancia));
			Properties propsLSCPU = getPropertiesLSCPU(lscpuPath,instancia);
			
			DatosInstancia datos = datosInstancias.get(instancia);
			
			Path carpetaSalida = inputPath.resolve(instancia);
			if (!Files.exists(carpetaSalida)) {
				throw new IOException("La carpeta " + carpetaSalida.toAbsolutePath().toString() + " no existe!");
			}
			
			datos.setSistemaOperativo(getSistemaOperativo(carpetaSalida));
			
			long totalDatosSec = copiarResultados(instancia,carpetaSalida.resolve("resultados.csv"),archivoDatos);
			long tiempoSegunLog = traerTiempoSegunLog(instancia, logsSalidaPath)+30;
			
			DatoHardware datosHW = leerDatosHardware(carpetaSalida.resolve("hardware.txt"));
			datos.setProcesador(getProcesador(datos, datosHW, propsLSCPU));
			datos.setFabricante(datosHW.getValorHijo("vendor",datos.getFabricante()));
			datos.setCPUs(Math.max(datos.getCPUs(), Math.max(contarCPUs(datosHW.getHijoByName("core")),getCpusLSCPU(propsLSCPU))));
			
			datos.setMemoria(Integer.max(datos.getMemoria(), getValorMB(datosHW.getValorHijo("core/memory/size","0 MB"))));
			datos.setVelocidadMemoriaMhz(Integer.max(datos.getVelocidadMemoriaMhz(), getValorMhz(datosHW.getValorHijo(datosHW.evaluarRutaExistente("core/memory/bank/clock","core/memory/bank:0/clock"), "0 Mhz"))));
			datos.setTiempoEjecucionSeg(Math.max(totalDatosSec, tiempoSegunLog) + (propsLSCPU.size()>0 ? 30 : 0));
			datos.setVelocidadProcesadorMhz(Integer.max(getVelocidadNombreProcesador(datos.getProcesador()),Integer.max(datos.getVelocidadProcesadorMhz(), getValorMhz(datosHW.getValorHijo(datosHW.evaluarRutaExistente("core/cpu/size","core/cpu:0/size"), "0 Mhz")))));
			datos.setTamanoPalabra(Integer.max(datos.getTamanoPalabra(), getTamanoPalabra(datosHW)));
			
			datos.setCache1KB(Math.max(getTamanoCachelscpu(propsLSCPU,1),getTamanoCache(datosHW,1)));
			datos.setCache2KB(Math.max(getTamanoCachelscpu(propsLSCPU,2),getTamanoCache(datosHW,2)));
			datos.setCache3KB(Math.max(getTamanoCachelscpu(propsLSCPU,3),getTamanoCache(datosHW,3)));
			datos.setCache4KB(Math.max(getTamanoCachelscpu(propsLSCPU,4),getTamanoCache(datosHW,4)));
			
			numInstancia++;
		}
		
		List<String> lineasInstancia = datosInstancias.values().stream().map(datosInst -> datosInst.toCSVFileLine()).toList();
		Files.write(archivoInstancias, lineasInstancia, StandardOpenOption.APPEND);
		System.out.println("Finalizado!!!");
	}

	private static int getVelocidadNombreProcesador(String procesador) {
		if (procesador.contains("@")) {
			String aux = procesador.substring(procesador.lastIndexOf("@")+1);
			return getValorMhz(aux);
		}
		return 0;
	}

	private static long getTamanoCachelscpu(Properties propsLSCPU, int numCache) {
		String propNameBase = "L" + numCache + " cache";
		if (propsLSCPU.containsKey(propNameBase)) {
			return getTamanoCachePropertyLSCPU(propsLSCPU.getProperty(propNameBase));
		}
		
		String propNameData = "L" + numCache + "d cache";
		String propNameInst = "L" + numCache + "i cache";

		if (propsLSCPU.containsKey(propNameData) || propsLSCPU.containsKey(propNameInst)) {
			return getTamanoCachePropertyLSCPU(propsLSCPU.getProperty(propNameData)) + getTamanoCachePropertyLSCPU(propsLSCPU.getProperty(propNameInst));
		}

		return 0;
	}

	private static long getTamanoCachePropertyLSCPU(String propValue) {
		if (StringUtils.isNotBlank(propValue)) {
			//512 KiB (16 instances)
			
			int numInstances = 1;
			int posicParenthesis = propValue.indexOf("(");
			if (posicParenthesis>0) {
				String aux = getOnlyDigits(propValue.substring(posicParenthesis+1));
				if (StringUtils.isNotBlank(aux)) {
					numInstances = Integer.parseInt(aux);
				}
				propValue = propValue.substring(0,posicParenthesis).trim();
			}
			return getValorKB(propValue) * numInstances;
		}
		return 0;
	}

	private static String getProcesador(DatosInstancia datos, DatoHardware datosHW, Properties propsLSCPU) {
		String modelName = propsLSCPU.getProperty("Model name", "");
		String modelBiosName = propsLSCPU.getProperty("BIOS Model name", "");
		
		if (StringUtils.isNotBlank(modelName) || StringUtils.isNotBlank(modelBiosName)) {
			if (modelName.length() > modelBiosName.length()) {
				return modelName;
			} else {
				return modelBiosName;
			}
		}
		
		return datosHW.getValorHijo(datosHW.evaluarRutaExistente("core/cpu:0/product","core/cpu/product"),datos.getProcesador());
	}

	private static int getCpusLSCPU(Properties propsLSCPU) {
		return Integer.parseInt(propsLSCPU.getProperty("CPU(s)", "0"));
	}

	private static Properties getPropertiesLSCPU(Path lscpuPath, String instancia) throws IOException {
		Path lscpuFile = lscpuPath.resolve(instancia + ".txt");
		Properties props = new Properties();
		if (Files.exists(lscpuFile)) {
			List<String> lines = Files.readAllLines(lscpuFile);
			for(String line : lines) {
				int twoPoints = line.indexOf(":");
				if (twoPoints > 0) {
					String propName = line.substring(0,twoPoints).trim();
					String propValue = line.substring(twoPoints+1).trim();
					props.setProperty(propName, propValue);
				}
			}
		}
		
		return props;
	}

	private static long getTamanoCache(DatoHardware datosHW, int i) {
		String ruta = "";
		if (i==1) {
			ruta = datosHW.evaluarRutaExistente("core/cache/size","core/cache:0/size","core/cpu/cache/size","core/cpu:0/cache:0/size","core/cpu/cache:0/size");
		} else {
			ruta = datosHW.evaluarRutaExistente(String.format("core/cpu/cache:%d/size",i-1),String.format("core/cache:%d/size",i-1));
		}
		return getValorKB(datosHW.getValorHijo(ruta, "0"));
	}

	private static int getTamanoPalabra(DatoHardware datosHW) {
		String aux = getOnlyDigits(
				datosHW.getValorHijo(
						datosHW.evaluarRutaExistente("width","core/cpu:0/width","core/cpu/width"), 
						"0"));
		return Integer.parseInt(aux);
	}

	private static long traerTiempoSegunLog(String instancia, Path logsSalidaPath) throws NumberFormatException, IOException {
		//Total execution time for machine (seconds):1090
		Path archivoLog = logsSalidaPath.resolve(String.format("%s.log", instancia));
		long totalTimeFound = 0;
		if (Files.exists(archivoLog)) {
			for(String line : Files.readAllLines(archivoLog)) {
				if (line.trim().startsWith("Total execution time for machine (seconds):")) {
					int twoPoints = line.indexOf(":");
					totalTimeFound = Long.parseLong(line.substring(twoPoints+1));
					break;
				}
			}
		}

		return totalTimeFound;
	}

	private static int getValorMhz(String valorHijo) {
		if (StringUtils.isBlank(valorHijo)) {
			return 0;
		}
		valorHijo = valorHijo.toUpperCase().trim();
		double multiplicador = 1;
		if (valorHijo.contains("KHZ")) {
			multiplicador = 0.1f;
		} else if (valorHijo.contains("GHZ")) {
			multiplicador = 1000.0f;
		}
		
		char[] chars = valorHijo.toCharArray();
		int i = 0;
		StringBuffer str = new StringBuffer();
		while (i < chars.length &&  isDigitOrComma(chars[i])) {
			str.append(chars[i]);
			i++;
		}
		
		if (str.isEmpty()) {
			return 0;
		} else {
			double valBase = (Double.parseDouble(str.toString().replace(",", ".")) * multiplicador);
			return (int)Math.round(valBase);
		}
	}

	private static boolean isDigitOrComma(char c) {
		return (c >= '0' && c <= '9') || c == ',' || c=='.';
	}

	private static int getValorMB(String valor) {
		double valorBytes = (double)getValorBytes(valor);
		double valorMB = valorBytes / (1024.0f * 1024.0f);
		return (int)Math.round(valorMB);
	}

	private static int getValorKB(String valor) {
		double valorBytes = (double)getValorBytes(valor);
		double valorMB = valorBytes / 1024.0f;
		return (int)Math.round(valorMB);
	}

	private static long getValorBytes(String valor) {
		valor = valor.toUpperCase().trim();
		long multiplicador = 1;
		if (valor.endsWith("KIB")) {
			multiplicador = 1000L;
		} else if (valor.endsWith("KB")) {
			multiplicador = 1024L;
		} else if (valor.endsWith("MIB")) {
			multiplicador = 1000L * 1000L;
		} else if (valor.endsWith("MB")) {
			multiplicador = 1024L * 1024L;
		} else if (valor.endsWith("GIB")) {
			multiplicador = 1000L * 1000L * 1000L;
		} else if (valor.endsWith("GB")) {
			multiplicador = 1024L * 1024L * 1024L;
		}
		
		valor = getOnlyDigits(valor);
		if (StringUtils.isBlank(valor)) {
			return 0;
		}
		
		long valorBase = Long.parseLong(valor);

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
