package edu.javeriana.cad.beans;

import org.apache.commons.lang3.StringUtils;

/**
 * Datos recopilados de cada instancia, que se recopilan y generan en el archivo de salida
 * @author FelipeReyesPalacio
 *
 */
public class DatosInstancia {

	private String instancia;
	private String arquitectura;
	private String fabricante;
	private int CPUs;
	private int memoria;
	private int velocidadMemoriaMhz;
	private String hypervisor;
	private String procesador;
	private int velocidadProcesadorMhz;
	private double costoHoraUSD;
	private String zonaDisponibilidad;
	private long tiempoEjecucionSeg;
	private String sistemaOperativo;
	private int tamanoPalabra;
	private long cache1KB;
	private long cache2KB;
	private long cache3KB;
	private long cache4KB;
	
	public DatosInstancia() {
	}

	public DatosInstancia(String[] datosLinea) {
		this.instancia = getDato(0,datosLinea);
		this.arquitectura =  getDato(1,datosLinea);
		this.fabricante =  getDato(2,datosLinea);
		CPUs  = Integer.parseInt(getDato(3,datosLinea,"0"));
		this.memoria = Integer.parseInt(getDato(4,datosLinea,"0"));
		this.velocidadMemoriaMhz = Integer.parseInt(getDato(5,datosLinea,"0"));
		this.hypervisor = getDato(6,datosLinea);
		this.procesador = getDato(7,datosLinea);
		this.velocidadProcesadorMhz = (int)Math.floor(1000.0f * getDouble(getDato(8,datosLinea,"0")));
		this.costoHoraUSD = getDouble(getDato(9,datosLinea,"0"));
		this.zonaDisponibilidad = getDato(10,datosLinea);
		this.tiempoEjecucionSeg = Integer.parseInt(getDato(11,datosLinea,"0"));
		this.sistemaOperativo = getDato(12,datosLinea);
		this.tamanoPalabra = Integer.parseInt(getDato(13,datosLinea,"0"));
		this.cache1KB = Long.parseLong(getDato(14,datosLinea,"0"));
		this.cache2KB = Long.parseLong(getDato(15,datosLinea,"0"));
		this.cache3KB = Long.parseLong(getDato(16,datosLinea,"0"));
		this.cache4KB = Long.parseLong(getDato(17,datosLinea,"0"));
		
	}

	private double getDouble(String dato) {
		return Double.parseDouble(dato.replace(",", "."));
	}

	private String getDato(int posic, String[] datosLinea, String valorDefecto) {
		String aux = getDato(posic, datosLinea);
		if (StringUtils.isNotBlank(aux)) {
			return aux;
		}
		return valorDefecto;
	}
	
	private String getDato(int posic, String[] datosLinea) {
		if (datosLinea.length>posic) {
			return datosLinea[posic];
		}
		
		return null;
	}

	public String getInstancia() {
		return instancia;
	}
	public void setInstancia(String instancia) {
		this.instancia = instancia;
	}
	public String getArquitectura() {
		return arquitectura;
	}
	public void setArquitectura(String arquitectura) {
		this.arquitectura = arquitectura;
	}
	public String getFabricante() {
		return fabricante;
	}
	public void setFabricante(String fabricante) {
		this.fabricante = fabricante;
	}
	public int getCPUs() {
		return CPUs;
	}
	public void setCPUs(int cPUs) {
		CPUs = cPUs;
	}
	public int getMemoria() {
		return memoria;
	}
	public void setMemoria(int memoria) {
		this.memoria = memoria;
	}
	public int getVelocidadMemoriaMhz() {
		return velocidadMemoriaMhz;
	}
	public void setVelocidadMemoriaMhz(int velocidadMemoriaMhz) {
		this.velocidadMemoriaMhz = velocidadMemoriaMhz;
	}
	public String getHypervisor() {
		return hypervisor;
	}
	public void setHypervisor(String hypervisor) {
		this.hypervisor = hypervisor;
	}
	public String getProcesador() {
		return procesador;
	}
	public void setProcesador(String procesador) {
		this.procesador = procesador;
	}
	public int getVelocidadProcesadorMhz() {
		return velocidadProcesadorMhz;
	}
	public void setVelocidadProcesadorMhz(int velocidadProcesadorMhz) {
		this.velocidadProcesadorMhz = velocidadProcesadorMhz;
	}
	public double getCostoHoraUSD() {
		return costoHoraUSD;
	}
	public void setCostoHoraUSD(double costoHoraUSD) {
		this.costoHoraUSD = costoHoraUSD;
	}
	public String getZonaDisponibilidad() {
		return zonaDisponibilidad;
	}
	public void setZonaDisponibilidad(String zonaDisponibilidad) {
		this.zonaDisponibilidad = zonaDisponibilidad;
	}
	public long getTiempoEjecucionSeg() {
		return tiempoEjecucionSeg;
	}
	public void setTiempoEjecucionSeg(long tiempoEjecucionSeg) {
		this.tiempoEjecucionSeg = tiempoEjecucionSeg;
	}
	public String getSistemaOperativo() {
		return sistemaOperativo;
	}
	public void setSistemaOperativo(String sistemaOperativo) {
		this.sistemaOperativo = sistemaOperativo;
	}
	public int getTamanoPalabra() {
		return tamanoPalabra;
	}
	public void setTamanoPalabra(int tamanoPalabra) {
		this.tamanoPalabra = tamanoPalabra;
	}
	public long getCache1KB() {
		return cache1KB;
	}
	public void setCache1KB(long cache1kb) {
		cache1KB = cache1kb;
	}
	public long getCache2KB() {
		return cache2KB;
	}
	public void setCache2KB(long cache2kb) {
		cache2KB = cache2kb;
	}
	public long getCache3KB() {
		return cache3KB;
	}
	public void setCache3KB(long cache3kb) {
		cache3KB = cache3kb;
	}
	public long getCache4KB() {
		return cache4KB;
	}
	public void setCache4KB(long cache4kb) {
		cache4KB = cache4kb;
	}

	@Override
	public String toString() {
		return "DatosInstancia [" + (instancia != null ? "instancia=" + instancia + ", " : "")
				+ (arquitectura != null ? "arquitectura=" + arquitectura + ", " : "")
				+ (fabricante != null ? "fabricante=" + fabricante + ", " : "") + "CPUs=" + CPUs + ", memoria="
				+ memoria + ", velocidadMemoriaMhz=" + velocidadMemoriaMhz + ", "
				+ (hypervisor != null ? "hypervisor=" + hypervisor + ", " : "")
				+ (procesador != null ? "procesador=" + procesador + ", " : "") + "velocidadProcesadorMhz="
				+ velocidadProcesadorMhz + ", costoHoraUSD=" + costoHoraUSD + ", "
				+ (zonaDisponibilidad != null ? "zonaDisponibilidad=" + zonaDisponibilidad + ", " : "")
				+ "tiempoEjecucionSeg=" + tiempoEjecucionSeg + ", "
				+ (sistemaOperativo != null ? "sistemaOperativo=" + sistemaOperativo + ", " : "") + "tamanoPalabra="
				+ tamanoPalabra + ", cache1KB=" + cache1KB + ", cache2KB=" + cache2KB + ", cache3KB=" + cache3KB
				+ ", cache4KB=" + cache4KB + "]";
	}

	public String toCSVFileLine() {
		//"Instancia,Arquitectura,Fabricante,CPUs,Memoria,Velocidad_memoria,Hypervisor,Procesador,Velocidad_Procesador,USD_Hour,Zona_Disponibilidad,Tiempo_ejecucion,Sistema_Operativo,Tamano_Palabra,CACHE1,CACHE2,CACHE3,CACHE4"
		StringBuffer str = new StringBuffer();
		str.append(this.instancia);
		str.append("," + obj2String(this.arquitectura));
		str.append("," + obj2String(this.fabricante));
		str.append("," + this.CPUs);
		str.append("," + this.memoria);
		str.append("," + this.velocidadMemoriaMhz);
		str.append("," + obj2String(this.hypervisor));
		str.append("," + obj2String(this.procesador));
		str.append("," + this.velocidadProcesadorMhz);
		str.append("," + this.costoHoraUSD);
		str.append("," + obj2String(this.zonaDisponibilidad));
		str.append("," + this.tiempoEjecucionSeg);
		str.append("," + obj2String(this.sistemaOperativo));
		str.append("," + this.tamanoPalabra);
		str.append("," + this.cache1KB);
		str.append("," + this.cache2KB);
		str.append("," + this.cache3KB);
		str.append("," + this.cache4KB);
		return str.toString();
	}

	private String obj2String(String obj) {
		if (obj == null) {
			return "";
		}
		String aux = obj.replace(",", " ").replace("\t", " ").replace("\n", " ").replace("\r", "").trim().replace("\"", "\"\"");
		while (aux.contains("  ")) {
			aux = aux.replace("  ", " ");
		}
		
		return String.format("\"%s\"", aux);
	}
	
	
}
