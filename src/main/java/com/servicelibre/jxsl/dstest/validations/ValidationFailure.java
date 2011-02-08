package com.servicelibre.jxsl.dstest.validations;

public class ValidationFailure
{

	private String message;
	private String validatorName;

	public ValidationFailure(String validatorName, String message) {
		this.validatorName = validatorName;
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getValidatorName() {
		return validatorName;
	}

	public void setValidatorName(String validatorName) {
		this.validatorName = validatorName;
	}
    
	
}
