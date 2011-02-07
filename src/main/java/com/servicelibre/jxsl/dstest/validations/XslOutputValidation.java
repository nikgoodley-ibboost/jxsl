package com.servicelibre.jxsl.dstest.validations;

import com.servicelibre.jxsl.dstest.Document;


public interface XslOutputValidation {

	public boolean isSuccessful();
	public int run(Document xmlDoc);
}
