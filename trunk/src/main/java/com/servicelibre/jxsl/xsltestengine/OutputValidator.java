package com.servicelibre.jxsl.xsltestengine;

public interface OutputValidator {

	public String getName();
	public String getDescription();
	public boolean isValid(String output);
	public String getMessage();
}
