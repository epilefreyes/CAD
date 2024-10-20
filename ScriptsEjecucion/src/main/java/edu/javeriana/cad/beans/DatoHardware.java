package edu.javeriana.cad.beans;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

/**
 * Clase (tipo árbol) que almacena la salida del comando LSHW
 * @author FelipeReyesPalacio
 *
 */
public class DatoHardware {

	private String titleName;
	private Properties properties;
	private List<DatoHardware> children;
	private int profundidad;
	
	public DatoHardware(String titleName, int profundidad) {
		this.titleName = titleName;
		this.profundidad = profundidad;
		this.properties = new Properties();
		this.children = new ArrayList<>();
	}
	
	public String getTitleName() {
		return titleName;
	}
	public void setTitleName(String titleName) {
		this.titleName = titleName;
	}
	public Properties getProperties() {
		return properties;
	}
	public void setProperties(Properties properties) {
		this.properties = properties;
	}
	public List<DatoHardware> getChildren() {
		return children;
	}
	public void setChildren(List<DatoHardware> children) {
		this.children = children;
	}
	public int getProfundidad() {
		return profundidad;
	}
	public void setProfundidad(int profundidad) {
		this.profundidad = profundidad;
	}
	@Override
	public String toString() {
		StringBuffer str = new StringBuffer();
		str.append(espacios()+ "+ <" + this.titleName+">\n");

		if (properties!=null) {
			for(Object propKey : properties.keySet()) {
				String propName = propKey.toString();
				String propValue = this.properties.getProperty(propName);
				
				str.append(espacios() + "  -" + propName + "=" + propValue + "\n");
			}
		}
		
		if (children!=null) {
			for(DatoHardware dato : children) {
				str.append(dato.toString());
			}
		}
		
		return str.toString();
	}
	
	private String espacios() {
		int espacios = this.profundidad*4;
		return StringUtils.leftPad("", espacios);
	}

	public String evaluarRutaExistente(String... rutas) {
		for(String ruta : rutas) {
			if (isRutaExiste(ruta)) {
				return ruta;
			}
		}
		return null;
	}
	
	public boolean isRutaExiste(String rutaBusqueda) {
		int posicSlash = rutaBusqueda.indexOf("/");
		if (posicSlash<=0) {
			return this.properties.containsKey(rutaBusqueda);
		}
		
		DatoHardware hijo = getHijoByName(rutaBusqueda.substring(0,posicSlash));
		if (hijo==null) {
			return false;
		}
		return hijo.isRutaExiste(rutaBusqueda.substring(posicSlash+1));
		
		
	}
	
	public String getValorHijo(String rutaBusqueda, String valorDefecto) {
		if (StringUtils.isBlank(rutaBusqueda)) {
			return valorDefecto;
		}
		int posicSlash = rutaBusqueda.indexOf("/");
		if (posicSlash<=0) {
			return this.properties.getProperty(rutaBusqueda,valorDefecto);
		} else {
			DatoHardware hijo = getHijoByName(rutaBusqueda.substring(0,posicSlash));
			if (hijo==null) {
				return valorDefecto;
			}
			return hijo.getValorHijo(rutaBusqueda.substring(posicSlash+1), valorDefecto);
		}
	}

	public DatoHardware getHijoByName(String nombreHijo) {
		if (children != null) {
			for(DatoHardware hijo : children) {
				if (hijo.getTitleName().equalsIgnoreCase(nombreHijo)) {
					return hijo;
				}
			}
		}
		return null;
	}
	
}
