package edu.javeriana.cad.beans;

import org.apache.commons.lang3.StringUtils;

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
	private long cache1Bytes;
	private long cache2Bytes;
	private long cache3Bytes;
	private long cache4Bytes;
	
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
		this.cache1Bytes = Long.parseLong(getDato(14,datosLinea,"0"));
		this.cache2Bytes = Long.parseLong(getDato(15,datosLinea,"0"));
		this.cache3Bytes = Long.parseLong(getDato(16,datosLinea,"0"));
		this.cache4Bytes = Long.parseLong(getDato(17,datosLinea,"0"));
		
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
	public long getCache1Bytes() {
		return cache1Bytes;
	}
	public void setCache1Bytes(long cache1Bytes) {
		this.cache1Bytes = cache1Bytes;
	}
	public long getCache2Bytes() {
		return cache2Bytes;
	}
	public void setCache2Bytes(long cache2Bytes) {
		this.cache2Bytes = cache2Bytes;
	}
	public long getCache3Bytes() {
		return cache3Bytes;
	}
	public void setCache3Bytes(long cache3Bytes) {
		this.cache3Bytes = cache3Bytes;
	}
	public long getCache4Bytes() {
		return cache4Bytes;
	}
	public void setCache4Bytes(long cache4Bytes) {
		this.cache4Bytes = cache4Bytes;
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
				+ tamanoPalabra + ", cache1Bytes=" + cache1Bytes + ", cache2Bytes=" + cache2Bytes + ", cache3Bytes="
				+ cache3Bytes + ", cache4Bytes=" + cache4Bytes + "]";
	}

	
}
