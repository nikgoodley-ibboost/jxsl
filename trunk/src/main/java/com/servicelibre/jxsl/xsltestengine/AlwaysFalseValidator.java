package com.servicelibre.jxsl.xsltestengine;

public class AlwaysFalseValidator implements OutputValidator {

	
	
	public AlwaysFalseValidator() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getName() {
		return "Dummy always false validator";
	}

	@Override
	public String getDescription() {
		return "This validator always return false.";
	}

	@Override
	public boolean isValid(String output) {
		return false;
	}

	@Override
	public String getMessage() {
		return "Never valid...";
	}

}
