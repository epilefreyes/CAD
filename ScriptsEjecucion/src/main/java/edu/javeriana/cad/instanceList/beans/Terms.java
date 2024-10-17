package edu.javeriana.cad.instanceList.beans;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSetter;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class Terms {

	private Map<String,Map<String,Object>> onDemand;

	@JsonGetter("OnDemand")
	public Map<String, Map<String,Object>> getOnDemand() {
		return onDemand;
	}

	@JsonSetter("OnDemand")
	public void setOnDemand(Map<String, Map<String,Object>> onDemand) {
		this.onDemand = onDemand;
	}

	@Override
	public String toString() {
		if (onDemand == null || onDemand.isEmpty()) {
			return "";
		}
		
		Map<String,Object> firstElem = onDemand.values().iterator().next();

		if (firstElem.containsKey("priceDimensions")) {
			Object priceDimensions= firstElem.get("priceDimensions");
			if (priceDimensions!=null && priceDimensions instanceof Map) {
				@SuppressWarnings("unchecked")
				Map<String,Object> priceDimensionsMap = (Map<String, Object>) priceDimensions;
				
				if (priceDimensionsMap.size()>0) {
					Object firstElemPriceDimensionsMap = priceDimensionsMap.values().iterator().next();

					if (firstElemPriceDimensionsMap!=null && priceDimensionsMap instanceof Map) {

						@SuppressWarnings("unchecked")
						Map<String,Object> priceDimensionsMapElem = (Map<String, Object>) firstElemPriceDimensionsMap;
						
						Object pricePerUnitObj = priceDimensionsMapElem.get("pricePerUnit");
						
						if (pricePerUnitObj != null && pricePerUnitObj instanceof Map) {
							
							@SuppressWarnings("unchecked")
							Map<String,Object> pricePerUnitMap =  (Map<String, Object>) pricePerUnitObj;
							
							if (pricePerUnitMap.size()>0) {
								
								if (pricePerUnitMap.containsKey("USD")) {
									return priceDimensionsMapElem.get("unit").toString() + "\t" + "USD\t" +pricePerUnitMap.get("USD").toString(); 
								}
								
								String firstKey = pricePerUnitMap.keySet().iterator().next();
								
								return priceDimensionsMapElem.get("unit").toString() + "\t" + firstKey + "\t" +pricePerUnitMap.get(firstKey).toString(); 
							}
							
						}
						
						
						return priceDimensionsMapElem.get("unit").toString() + "\t" + priceDimensionsMapElem.get("pricePerUnit").toString();

					}
				}
				return "";
			}
		}
		return "";
	}
	
	
	
}
