package com.servicelibre.jxsl.dstest.validations;

public interface OutputValidator {

	public String getName();
	public String getDescription();
	public boolean isValid(String output);
	public String getMessage();
}
