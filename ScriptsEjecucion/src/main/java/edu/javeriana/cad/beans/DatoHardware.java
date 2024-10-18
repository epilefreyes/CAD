package edu.javeriana.cad.beans;

import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

public class DatoHardware {

	private String titleName;
	private Properties properties;
	private List<DatoHardware> children;
	private int profundidad;
	private int espaciosOriginal;
	
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
	public int getEspaciosOriginal() {
		return espaciosOriginal;
	}
	public void setEspaciosOriginal(int espaciosOriginal) {
		this.espaciosOriginal = espaciosOriginal;
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
	
}
