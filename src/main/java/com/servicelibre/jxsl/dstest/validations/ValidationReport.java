package com.servicelibre.jxsl.dstest.validations;

import java.util.ArrayList;
import java.util.List;

import com.servicelibre.jxsl.dstest.DocumentId;

public class ValidationReport
{

    protected XslValidation outputValidation;
    public List<ValidationFailure> failures = new ArrayList<ValidationFailure>();
	private DocumentId documentId;

    public ValidationReport(XslValidation outputValidation, DocumentId documentId)
    {
        super();
        this.outputValidation = outputValidation;
        this.documentId = documentId;
    }

    public void addValidationFailure(ValidationFailure failure) {
    	failures.add(failure);
    }

	public List<ValidationFailure> getFailures() {
		return failures;
	}

	public DocumentId getDocumentId() {
		return documentId;
	}

	public void setDocumentId(DocumentId documentId) {
		this.documentId = documentId;
	}
	
	public boolean isValidationSuccessful() {
	    return failures.size() == 0;
	}


	
}
