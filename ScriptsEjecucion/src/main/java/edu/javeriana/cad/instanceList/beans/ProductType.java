package edu.javeriana.cad.instanceList.beans;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class ProductType {

	private ProductAttributes attributes;
	private String sku;
	
	public ProductAttributes getAttributes() {
		return attributes;
	}

	public void setAttributes(ProductAttributes attributes) {
		this.attributes = attributes;
	}

	public String getSku() {
		return sku;
	}

	public void setSku(String sku) {
		this.sku = sku;
	}

	@Override
	public String toString() {
		if (attributes == null || StringUtils.isBlank(sku)) {
			return "";
		}
		return attributes.toString() + "\t" + sku;
	}

	
}
