package com.servicelibre.jxsl.dstest.validations;

import com.servicelibre.jxsl.dstest.Document;


public interface XslOutputValidation {

	public boolean isSuccessful();
	public ValidationReport run(Document xmlDoc);
}
