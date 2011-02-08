package com.servicelibre.jxsl.dstest.validations;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.servicelibre.jxsl.dstest.Document;
import com.servicelibre.jxsl.scenario.XslScenario;

public class JavaXslValidation implements XslValidation {

    static Logger logger = LoggerFactory.getLogger(JavaXslValidation.class);

    private XslScenario xslScenario;
    private List<OutputValidator> outputValidators;
    private String ouputNameToValidate;

    public JavaXslValidation(XslScenario scenario, List<OutputValidator> outputValidators) {

	this(scenario, XslScenario.MAIN_OUTPUT_KEY, outputValidators);
    }

    public JavaXslValidation(XslScenario scenario, String ouputNameToValidate, List<OutputValidator> outputValidators) {

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
    public ValidationReport run(Document xmlDoc) {

	ValidationReport validationReport = new ValidationReport(this, xmlDoc.id);

	logger.debug("Going to validate the transformation output of {} through {} validators...", xmlDoc.id + "[" + xslScenario + "]", outputValidators.size());

	Map<String, String> result = xslScenario.apply(xmlDoc.getFile());
	String output = result.get(XslScenario.MAIN_OUTPUT_KEY);

	for (OutputValidator outputValidator : outputValidators) {
	    logger.debug("Running validaton [{}]...", outputValidator.getName());
	    if (!outputValidator.isValid(output)) {
		logger.warn("{} - validation failed: {}", outputValidator.getName(), outputValidator.getMessage());
		ValidationFailure failure = new ValidationFailure(outputValidator.getName(), outputValidator.getMessage());
		validationReport.addValidationFailure(failure);
	    }
	}

	return validationReport;
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
