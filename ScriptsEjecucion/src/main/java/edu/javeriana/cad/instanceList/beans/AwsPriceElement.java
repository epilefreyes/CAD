package edu.javeriana.cad.instanceList.beans;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class AwsPriceElement {

	private ProductType product;
	private Terms terms;
	public ProductType getProduct() {
		return product;
	}
	public void setProduct(ProductType product) {
		this.product = product;
	}
	public Terms getTerms() {
		return terms;
	}
	public void setTerms(Terms terms) {
		this.terms = terms;
	}
	@Override
	public String toString() {
		if (product == null || terms == null) {
			return "";
		}
		String aux = product.toString();
		String aux2 = terms.toString();
		if (StringUtils.isBlank(aux) || StringUtils.isBlank(aux2)) {
			return "";
		}
		return aux + "\t" + aux2;
	}
	
	
	
	
}
