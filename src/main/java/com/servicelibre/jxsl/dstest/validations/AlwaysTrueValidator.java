package com.servicelibre.jxsl.dstest.validations;

public class AlwaysTrueValidator implements OutputValidator {
	
	

	public AlwaysTrueValidator() {
		super();
	}

	@Override
	public String getName() {
		return "Dummy always true validator";
	}

	@Override
	public String getDescription() {
		return "This validator always return true.";
	}

	@Override
	public boolean isValid(String output) {
		return true;
	}

	@Override
	public String getMessage() {
		return "";
	}

}
