package com.servicelibre.jxsl.dstest.validations;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.servicelibre.jxsl.dstest.Document;
import com.servicelibre.jxsl.scenario.XslScenario;

public class JavaXslOutputValidation implements XslOutputValidation {

	static Logger logger = LoggerFactory.getLogger(JavaXslOutputValidation.class);

	private XslScenario xslScenario;
	private List<OutputValidator> outputValidators;
	private String ouputNameToValidate;

	public JavaXslOutputValidation(XslScenario scenario, List<OutputValidator> outputValidators) {

		this(scenario, XslScenario.MAIN_OUTPUT_KEY, outputValidators);
	}

	public JavaXslOutputValidation(XslScenario scenario, String ouputNameToValidate, List<OutputValidator> outputValidators) {

		this.xslScenario = scenario;
		this.ouputNameToValidate = ouputNameToValidate;
		this.outputValidators = outputValidators;
	}

	public XslScenario getXslScenario() {
		return xslScenario;
	}

	public void setXslScenario(XslScenario xslScenario) {
		this.xslScenario = xslScenario;

	}

	@Override
	public boolean isSuccessful() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int run(Document xmlDoc) {

	    ValidationReport validationReport = new ValidationReport(this);
	    
		logger.debug("Going to validate the transformation output of {} through {} validators...", xmlDoc.id + "["+xslScenario+"]", outputValidators.size());

		int successCount = 0;
		int failureCount = 0;
		
		Map<String, String> result = xslScenario.apply(xmlDoc.getFile());
		String output = result.get(XslScenario.MAIN_OUTPUT_KEY);
		
		for (OutputValidator outputValidator : outputValidators) {
		    logger.debug("Running validaton [{}]...", outputValidator.getName());
			if(outputValidator.isValid(output)){
			    successCount++;
			}
			else {
			    logger.warn("{} - validation failed: {}", outputValidator.getName(), outputValidator.getMessage());
			    
			    failureCount--; 
			}
		}

		return failureCount < 0? failureCount : successCount;
	}

	public List<OutputValidator> getOutputValidators() {
		return outputValidators;
	}

	public void setOutputValidators(List<OutputValidator> outputValidators) {
		this.outputValidators = outputValidators;
	}

	public String getOuputNameToValidate() {
		return ouputNameToValidate;
	}

	public void setOuputNameToValidate(String ouputNameToValidate) {
		this.ouputNameToValidate = ouputNameToValidate;
	}

}
