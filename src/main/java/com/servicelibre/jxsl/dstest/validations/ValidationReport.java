package com.servicelibre.jxsl.dstest.validations;

import java.util.List;

public class ValidationReport
{

    protected XslOutputValidation outputValidation;
    public List<ValidationFailure> failures;

    public ValidationReport(XslOutputValidation outputValidation)
    {
        super();
        this.outputValidation = outputValidation;
    }

    
    
}
