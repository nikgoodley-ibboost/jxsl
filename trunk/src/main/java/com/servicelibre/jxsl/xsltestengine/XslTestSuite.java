package com.servicelibre.jxsl.xsltestengine;


public interface XslTestSuite {

	public boolean isSuccessful();
	public int run(Document xmlDoc);
}
