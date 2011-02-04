package com.servicelibre.jxsl.xsltestengine;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.servicelibre.jxsl.scenario.XslScenario;

public class XslJavaTestSuite implements XslTestSuite {

	static Logger logger = LoggerFactory.getLogger(XslJavaTestSuite.class);

	private XslScenario xslScenario;
	private List<OutputValidator> outputValidators;
	private String ouputNameToValidate;

	public XslJavaTestSuite(XslScenario scenario, List<OutputValidator> outputValidators) {

		this(scenario, XslScenario.MAIN_OUTPUT_KEY, outputValidators);
	}

	public XslJavaTestSuite(XslScenario scenario, String ouputNameToValidate, List<OutputValidator> outputValidators) {

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
		
		logger.debug("Going to validate the transformation output of {} through {} validators...", xmlDoc.id + "["+xslScenario+"]", outputValidators.size());
		
		Map<String, String> result = xslScenario.apply(xmlDoc.getFile());
		String output = result.get(XslScenario.MAIN_OUTPUT_KEY);
		
		for (OutputValidator outputValidator : outputValidators) {
			outputValidator.isValid(output);
		}

		return 0;
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
