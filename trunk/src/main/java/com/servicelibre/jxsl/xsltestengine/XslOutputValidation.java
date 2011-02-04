package com.servicelibre.jxsl.xsltestengine;


public interface XslOutputValidation {

	public boolean isSuccessful();
	public int run(Document xmlDoc);
}
