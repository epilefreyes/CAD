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

/**
 * Clase con el proceso completo de unificación de datos obtenidos de todas las máquinas ejecutadas
 * @author FelipeReyesPalacio
 *
 */
public class UnifierUtils {

	/**
	 * Algoritmo primario de ejecución (main)
	 * @param args Argumentos.  No utilizados
	 * @throws Exception Si se producen errores durante la recopilación, lanza este error
	 */
	public static void main(String[] args) throws Exception {
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

	/**
	 * Función privada que extrae la velocidad de un procesador de su nombre descriptivo
	 * @param procesador Nombre del procesador
	 * @return Velocidad, si aplica (0 si no)
	 */
	private static int getVelocidadNombreProcesador(String procesador) {
		if (procesador.contains("@")) {
			String aux = procesador.substring(procesador.lastIndexOf("@")+1);
			return getValorMhz(aux);
		}
		return 0;
	}

	/**
	 * Función que extrae el tamaño de la caché, En Kb, si está presente en el archivo de salida "lscpu"
	 * @param propsLSCPU Propiedades tomadas del archivo lscpu
	 * @param numCache Número de la caché a buscar (1,2,3 o 4)
	 * @return Tamaño del caché, en Kb.
	 */
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

	/**
	 * Retorna el tamaño de la caché en un texto dado
	 * @param propValue Valor del texto de la caché
	 * @return Tamaño de la caché en Kb
	 */
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

	/**
	 * Función que busca el mejor nombre (el más largo) del procesador, entre las propiedades del comando lshw y las del comando lscpu
	 * @param datos Datos iniciales de la instancia 
	 * @param datosHW Datos del comando lshw
	 * @param propsLSCPU Datos del comando lscpu
	 * @return Mejor nombre del procesador encontrado
	 */
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

	/**
	 * Función que retorna la cantidad de CPUs (cores) disponibles, segùn el comando LSCPU
	 * @param propsLSCPU Datos del comando LSCPU
	 * @return Número de CPUs, si aplica.
	 */
	private static int getCpusLSCPU(Properties propsLSCPU) {
		return Integer.parseInt(propsLSCPU.getProperty("CPU(s)", "0"));
	}

	/**
	 * Función que toma la salida del archivo LSCPU (si existe) y lo convierte en un objeto de propiedades (campo=valor)
	 * @param lscpuPath Ruta general donde se almacenaron las salidas de los archivos LSCPU
	 * @param instancia Instancia a validar
	 * @return Propiedades extraídas del archivo LSCPU, u objeto vacìo (no nulo) si no existe 
	 * @throws IOException SI se producen errores de lectura, lanza esta excepción
	 */
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

	/**
	 * Función que genera el tamaño de la caché, de acuerdo a los datos de hardware provistos por el comando LSHW
	 * @param datosHW Datos del archivo LSHW 
	 * @param numCache Número de la caché a buscar (1,2,3 o 4)
	 * @return
	 */
	private static long getTamanoCache(DatoHardware datosHW, int numCache) {
		String ruta = "";
		if (numCache==1) {
			ruta = datosHW.evaluarRutaExistente("core/cache/size","core/cache:0/size","core/cpu/cache/size","core/cpu:0/cache:0/size","core/cpu/cache:0/size");
		} else {
			ruta = datosHW.evaluarRutaExistente(String.format("core/cpu/cache:%d/size",numCache-1),String.format("core/cache:%d/size",numCache-1));
		}
		return getValorKB(datosHW.getValorHijo(ruta, "0"));
	}

	/**
	 * Funciòn que busca el mejor dato de tamaño de palabra (32 o 64 bits) en los datos de hardware
	 * @param datosHW Datos del archivo LSHW 
	 * @return Tamaño de palabra encontrado (32 o 64) o 0 si no fue encontrado.
	 */
	private static int getTamanoPalabra(DatoHardware datosHW) {
		String aux = getOnlyDigits(
				datosHW.getValorHijo(
						datosHW.evaluarRutaExistente("width","core/cpu:0/width","core/cpu/width"), 
						"0"));
		return Integer.parseInt(aux);
	}

	/**
	 * Trae el tiempo de ejecución según el log de ejecución de comandos
	 * @param instancia Instancia a validar
	 * @param logsSalidaPath Ruta de salida de los logs de ejecución
	 * @return Tiempo de ejecución (si aplica) 
	 * @throws Exception Si ocurren errores de lectura o interpretación, lanza este error
	 */
	private static long traerTiempoSegunLog(String instancia, Path logsSalidaPath) throws Exception {
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

	/**
	 * Retorna el valor en Mhz de una frecuencia dada
	 * @param texto Valor del parámetro con una frecuencia en Khz, Mhz o Khz
	 * @return Velocidad en Mhz, o 0 si no aplica.
	 */
	private static int getValorMhz(String texto) {
		if (StringUtils.isBlank(texto)) {
			return 0;
		}
		texto = texto.toUpperCase().trim();
		double multiplicador = 1;
		if (texto.contains("KHZ")) {
			multiplicador = 0.1f;
		} else if (texto.contains("GHZ")) {
			multiplicador = 1000.0f;
		}
		
		char[] chars = texto.toCharArray();
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

	/**
	 * Función que determina si un carácter es un dígito, coma o punto
	 * @param c Carácter a validar
	 * @return True si un carácter es un dígito, coma o punto
	 */
	private static boolean isDigitOrComma(char c) {
		return (c >= '0' && c <= '9') || c == ',' || c=='.';
	}

	/**
	 * Retorna un texto dado con una medida de almacenamiento, en Megabytes
	 * @param valor Texto a validar
	 * @return Valor en Megabytes
	 */
	private static int getValorMB(String valor) {
		double valorBytes = (double)getValorBytes(valor);
		double valorMB = valorBytes / (1024.0f * 1024.0f);
		return (int)Math.round(valorMB);
	}

	/**
	 * Retorna un texto dado con una medida de almacenamiento, en Kilobytes
	 * @param valor Texto a validar
	 * @return Valor en Kilobytes
	 */
	private static int getValorKB(String valor) {
		double valorBytes = (double)getValorBytes(valor);
		double valorMB = valorBytes / 1024.0f;
		return (int)Math.round(valorMB);
	}

	/**
	 * Toma un texto con una medida de almacenamiento y lo retorna en bytes
	 * @param valor Valor a analizar
	 * @return Valor en Bytes encontrado.
	 */
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

	/**
	 * Función que toma un texto y retorna únicamente aquellos caracteres que sean dígitos numéricos
	 * @param cad Cadena de texto a analizar
	 * @return Caracteres únicamente de dígitos
	 */
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
	
	/**
	 * Función que cuenta cuántas CPU existen en el dato de Hardware "core"
	 * @param core Dato de hardware "core"
	 * @return Número de CPUs encontradas.
	 */
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

	/**
	 * Función que lee un archivo de salida del comando LSHW y lo retorna en forma de árbol
	 * @param archivoHW Ruta del archivo de salida del comando LSHW
	 * @return Arbol de datos del archivo
	 * @throws IOException Si se producen errores de lectura del archivo, lanza esta excepción.
	 */
	private static DatoHardware leerDatosHardware(Path archivoHW) throws IOException {
		List<String> lineas = Files.readAllLines(archivoHW);

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
	
	/**
	 * Función para actualizar de forma recursiva la profundidad de todos los nodos del árbol (originalmente con valores en espacios) 
	 * con valores simples 0,1,2... 
	 * @param nodo Nodo a actualizar
	 * @param profundidad Profundidad actual a establecer.
	 */
	private static void updateProfundidad(DatoHardware nodo, int profundidad) {
		nodo.setProfundidad(profundidad);
		
		if (nodo.getChildren()!=null) {
			for(DatoHardware child : nodo.getChildren()) {
				updateProfundidad(child, profundidad+1);
			}
		}
	}

	/**
	 * Función que lee una línea de propiedad de un nodo de datos de hardware y lo añade como propiedad campo=valor
	 * @param padre Elemento padre donde colocar la propiedad
	 * @param linea Linea de datos
	 */
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

	/**
	 * Función de ayuda que determina si una línea del archivo de salida del comando LSHW es un título o no (inicia con *-)
	 * @param linea Linea a evaluar
	 * @return True si es un título, false si no
	 */
	private static boolean esLineaTitulo(String linea) {
		return !linea.startsWith(" ") || linea.trim().startsWith("*-");
	}

	/**
	 * Función que copia los resultados parciales obtenidos por una instancia, al archivo final compilado
	 * @param instancia Instancia a analizar
	 * @param origenResultados Origen de los resultados
	 * @param archivoDatos Archivo de datos de salida
	 * @return Suma de los tiempos de todos los resultados, como un posible tiempo de ejecución a utilizar
	 * @throws IOException Si se producen errores en la lectura de los archivos o escritura del archivo de salida, lanza esta excepción
	 */
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
