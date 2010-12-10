/**
 * Java XSL code library
 *
 * Copyright (C) 2010 Benoit Mercier <info@servicelibre.com> — All rights reserved.
 *
 * This file is part of jxsl.
 *
 * jxsl is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * jxsl is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with jxsl.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.servicelibre.jxsl.scenario;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.Controller;
import net.sf.saxon.lib.OutputURIResolver;
import net.sf.saxon.trans.XPathException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.sun.org.apache.xml.internal.resolver.tools.CatalogResolver;

/**
 * An XSL transformation scenario, summarized as:
 * 
 * <ul>
 * <li>xsl URL;</li>
 * <li>xsl parameters (optional);</li>
 * <li>xsl ouput location : where to optionaly save xsl outputs (optional, default to OS temp directory);</li>
 * <li>TransformerFactory class name (optional, default to net.sf.saxon.TransformerFactoryImpl).</li>
 * </ul>
 * 
 * XslScenario is built on top of JAXP.
 */
public class XslScenario {
	private static final String OUTPUT_FILE_EXT = ".output";

    static Logger logger = LoggerFactory.getLogger(XslScenario.class);

	public static final String SAXON_TRANSFORMER_FACTORY_FQCN = "net.sf.saxon.TransformerFactoryImpl";
	public static final String XALAN_TRANSFORMER_FACTORY_FQCN = "org.apache.xalan.xsltc.trax.TransformerFactoryImpl";
	public static final String DEFAULT_TRANSFORMER_FACTORY = SAXON_TRANSFORMER_FACTORY_FQCN;
	public static final String MAIN_OUTPUT_KEY = "output";

	public int executionCount = 0;

	private String name = String.valueOf(System.identityHashCode(this));

	private String description;

	private String xslPath;

	private Map<String, Object> parameters = new HashMap<String, Object>();

	private MultipleOutputURIResolver multipleOutputs = new MultipleOutputURIResolverImpl();

	private String mainOutputKey = MAIN_OUTPUT_KEY;

	private XMLReader reader;

	private Transformer transformer;

	private boolean outputSavedOnDisk = false;

	private File mainOutputDir = new File(System.getProperty("java.io.tmpdir"));

	private TransformerFactory transformerFactory;

	private RunReport lastRunReport;

	private String mainOutputName;

	private SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd-HHmmss-S");

	private String timestamp;

	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

	private DocumentBuilder xmlBuilder;

	private boolean runReportSavedOnDisk;

	private boolean useTimeStampedSubDir;

	/* Constructors */
	public XslScenario() {
		super();
		init();
	}

	public XslScenario(String xslPath) {
		super();
		setXslPath(xslPath);
		init();
	}

	public XslScenario(URL xslUrl) {
		this(xslUrl.toString());
	}

	public XslScenario(File xslFile) {
		this(xslFile.getAbsolutePath());
	}

	/* Business methods */
	protected void init() {
		try {
			reader = XMLReaderFactory.createXMLReader();
			reader.setEntityResolver(new CatalogResolver());
		} catch (SAXException e) {
			logger.error("Error while creating XMLReader", e);
		}

		try {
			this.xmlBuilder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			logger.error("Error while creating XML DocumentBuilder.", e);
		}

	}

	public Map<String, String> apply(File xmlFile) {
		try {
			return apply(FileUtils.readFileToByteArray(xmlFile), xmlFile.getAbsolutePath());
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}

		return new HashMap<String, String>();
	}

	public Map<String, String> apply(String xmlString) {
		return apply(xmlString.getBytes(), "");
	}
	
	public Map<String, String> apply(byte[] xmlBytes) {
	    return apply(xmlBytes, "");
	}

	public Map<String, String> apply(String xmlString, String charsetName) {
		Map<String, String> outputs = new HashMap<String, String>();
		try {
			outputs = apply(xmlString.getBytes(charsetName), "");
		} catch (UnsupportedEncodingException e) {
			logger.error("Error while getting bytes from xmlString supposed to be encoded in {} : {}.", charsetName, xmlString);
		}
		return outputs;
	}

	/**
	 * Apply XSL transformation on XML bytes.
	 * 
	 * @param xmlBytes
	 * @return
	 */
	public Map<String, String> apply(byte[] xmlBytes, String systemId) {

		Transformer transformer = getTransformer();

		Map<String, String> xslOutputs = new HashMap<String, String>(1);

		multipleOutputs.clearResults();

		xslOutputs.put(mainOutputKey, "");
		try {

			setTimestamp(df.format(new Date()));

			logger.debug("Going to execute [{}]", this.xslPath);

			// Pass parameters to XSL
			for (String paramName : parameters.keySet()) {
				logger.debug("Setting up parameter {} to {}", paramName, parameters.get(paramName));
				transformer.setParameter(paramName, parameters.get(paramName));
			}

			InputSource inputSource = new InputSource(new ByteArrayInputStream(xmlBytes));

			// To prevent error such FORG0002 (Base URI {} is not an absolute
			// URI), etc.
			inputSource.setSystemId(systemId);
			SAXSource saxSource = new SAXSource(reader, inputSource);

			StringWriter xslMainStringOutput = new StringWriter();

			logger.debug("Start execution of [{}]", this.xslPath);
			Date startDate = new Date();
			long startTime = System.nanoTime();
			transformer.transform(saxSource, new StreamResult(xslMainStringOutput));
			long executionTime = System.nanoTime() - startTime;
			logger.debug("Stop execution of [{}] ({}ms)", this.xslPath, (double) executionTime / 1000000);

			executionCount++;

			// Add main result output
			logger.debug("Storing main output (key={})", mainOutputKey);
			xslOutputs.put(mainOutputKey, xslMainStringOutput.toString());

			// Add potential other result outputs
			Map<String, StringWriter> outputs = multipleOutputs.getOutputs();
			for (String outputName : outputs.keySet()) {
				logger.debug("Storing additional output (key={})", outputName);
				xslOutputs.put(outputName, outputs.get(outputName).toString());
			}

			setLastRunReport(storeResults(xslOutputs, startDate, executionTime));

		} catch (TransformerConfigurationException e) {
			logger.error("Erreur lors de la transformation XSL.", e);

		} catch (TransformerException e) {
			logger.error("Erreur lors de la transformation XSL.", e);
		}

		return xslOutputs;

	}

	private RunReport storeResults(Map<String, String> xslOutputs, Date startDate, long executionTime) {

		RunReport runReport = new RunReport();

		runReport.executionTime = executionTime;
		runReport.executionDate = startDate;

		File currentOuputDir = getCurrentOutputDir();

		if (outputSavedOnDisk) {
			List<File> savedOutputs = saveOutputs(currentOuputDir, xslOutputs);
			runReport.mainOutputFile = savedOutputs.get(0);
			runReport.otherOutputFiles = savedOutputs.subList(1, savedOutputs.size());
		}

		if (runReportSavedOnDisk) {
			saveRunReport(currentOuputDir, runReport);
		}

		return runReport;
	}

	/**
	 * 
	 * @param currentOuputDir
	 * @param xslOutputs
	 * @return
	 */
	private List<File> saveOutputs(File currentOuputDir, Map<String, String> xslOutputs) {

		File mainOutputFile = null;
		File outputFile = null;
		List<File> outputFiles = new ArrayList<File>(1);

		if (isValidOutputDir()) {

			for (String key : xslOutputs.keySet()) {
				String outputContent = xslOutputs.get(key);

				if (key.equals(getMainOutputKey())) {
					outputFile = new File(currentOuputDir, getMainOutputName());
					mainOutputFile = outputFile;
					// Insert the main output at hte first position
					outputFiles.add(0, mainOutputFile);
				} else {
					outputFile = new File(currentOuputDir, key);
					outputFiles.add(outputFile);
				}

				logger.info("Saving output [{}] in file {}", key, outputFile.getAbsolutePath());

				try {
					FileUtils.writeStringToFile(outputFile, outputContent);
				} catch (IOException e) {
					logger.error("Error while saving XSL output {} to {}.\n" + e.getMessage(), key, outputFile);
				}
			}
		}

		return outputFiles;

	}

	private boolean isValidOutputDir() {
		if (mainOutputDir == null) {
			logger.error("outputDir is NULL. Unable to save xslOutputs created by {}", this);
			return false;
		}

		if (mainOutputDir == null || !mainOutputDir.isDirectory()) {
			logger.error("outputDir is not a directory : {}", mainOutputDir);
			return false;
		}
		return true;
	}

	/**
	 * Save RunReport - scenario XML config file (xslPath, transformer,
	 * parameters, execution time, etc.)
	 * 
	 * @param currentOuputDir
	 * @param runReport
	 */
	private void saveRunReport(File currentOuputDir, RunReport runReport) {
		if (isValidOutputDir()) {
			saveXmlDocToFile(asXml(runReport), new File(currentOuputDir, "runReport.xml"));
		}
	}

	private Document asXml(RunReport runReport) {
		Document reportDoc = xmlBuilder.newDocument();
		Element root = reportDoc.createElement("runReport");
		reportDoc.appendChild(root);

		Element executionTimeElem = reportDoc.createElement("executionTime");
		executionTimeElem.setTextContent(Long.toString(runReport.executionTime));
		executionTimeElem.setAttribute("SIUnit", "ns");
		root.appendChild(executionTimeElem);

		Element mainOutputFileElem = reportDoc.createElement("mainOutputFile");
		if (runReport.mainOutputFile != null && runReport.mainOutputFile.exists()) {
			mainOutputFileElem.setTextContent(runReport.mainOutputFile.getAbsolutePath());
			root.appendChild(mainOutputFileElem);
		}

		reportDoc.normalizeDocument();

		return reportDoc;
	}

	private void saveXmlDocToFile(Document xmlDoc, File runReportFile) {

		TransformerFactory tf = TransformerFactory.newInstance();

		try {
			tf.newTransformer().transform(new DOMSource(xmlDoc), new StreamResult(runReportFile));
		} catch (TransformerException e) {
			logger.error("Error while saving runReport {runReportFile}", runReportFile, e);
		}

	}

	private File getCurrentOutputDir() {

		File currentOuputDir = null;

		if (useTimeStampedSubDir) {
			currentOuputDir = new File(mainOutputDir, getTimestamp() + "-" + getName());
		} else {
			currentOuputDir = mainOutputDir;
		}

		if (!currentOuputDir.exists() && !currentOuputDir.mkdirs()) {
			logger.error("Unable to create output directory {}.", currentOuputDir);
		}
		return currentOuputDir;
	}

	/**
	 * Returns the default transformer
	 * 
	 * 
	 * @return
	 */
	public Transformer getTransformer() {

		if (this.transformer == null) {

			if (getXslPath() == null || getXslPath().trim().isEmpty()) {
				logger.error("xslPath is NULL or empty.  Cannot create Transformer.");
				return null;
			}

			try {

				this.transformer = compileXsl().newTransformer();

				// Saxon specific
				if ((this.transformer instanceof net.sf.saxon.Controller)) {
					Controller saxonController = (Controller) this.transformer;
					saxonController.setOutputURIResolver(this.multipleOutputs);

					logger.info("Transformer used by this scenario: {}", saxonController.getConfiguration().getProductTitle());
				} else {
					logger.info("Transformer used by this scenario: {}", this.transformer.getClass().getName());
				}

			} catch (TransformerConfigurationException e) {
				logger.error(e.getMessage(), e);
			}

		}

		return this.transformer;
	}

	public void setInitialTemplate(String initialTemplate) {
		if (getTransformer() instanceof net.sf.saxon.Controller) {
			Controller saxonController = (Controller) this.transformer;
			try {
				saxonController.setInitialTemplate(initialTemplate);
			} catch (XPathException e) {
				logger.error("Error while setting initialTemplate", e);
			}
		} else {
			throw new UnsupportedOperationException("Only Saxon support initialTemplate");
		}
	}

	private Templates compileXsl() {
		try {
			return getTransformerFactory().newTemplates(new StreamSource(getXslPath()));
		} catch (TransformerConfigurationException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	/* Getters and Setters */

	public String getXslPath() {
		return xslPath;
	}

	public void setXslPath(String xslPath) {

		this.xslPath = xslPath;

		setMainOutputName(extractMainOutputFileName(xslPath));

		// To force new transformer creation
		this.transformer = null;
	}

	private String extractMainOutputFileName(String xslPath2)
    {
	    String outputFilePathWithoutExt = xslPath;
        
        int startExt = xslPath.lastIndexOf(".");
        if(startExt > 0) {
            outputFilePathWithoutExt = xslPath.substring(0, startExt) ;
        }
        
        return new File(outputFilePathWithoutExt + OUTPUT_FILE_EXT).getName();
        
    }

    public Map<String, Object> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}

	public OutputURIResolver getOutputURIResolver() {
		return multipleOutputs;
	}

	public void setOutputURIResolver(MultipleOutputURIResolver outputURIResolver) {
		this.multipleOutputs = outputURIResolver;
	}

	public String getMainOutputKey() {
		return mainOutputKey;
	}

	public void setMainOutputKey(String mainOutputKey) {
		this.mainOutputKey = mainOutputKey;
	}

	public void setParameter(String key, Object value) {
		this.parameters.put(key, value);
	}

	/**
	 * Return or create the TransformerFactory
	 * <ol>
	 * <li>via setter (Spring)</li>
	 * <li>via system property</li>
	 * <li>default (DEFAULT_TRANSFORMER_FACTORY)</li>
	 * </ol>
	 * 
	 */
	public TransformerFactory getTransformerFactory() {

		if (transformerFactory == null) {
			String transformerFactoryFQCN = System.getProperty("javax.xml.transform.TransformerFactory", DEFAULT_TRANSFORMER_FACTORY);
			try {
				transformerFactory = (TransformerFactory) Class.forName(transformerFactoryFQCN).newInstance();
			} catch (ClassNotFoundException e) {
				logger.error(e.getMessage(), e);
			} catch (InstantiationException e) {
				logger.error(e.getMessage(), e);
			} catch (IllegalAccessException e) {
				logger.error(e.getMessage(), e);
			}
		}

		return transformerFactory;
	}

	public void setTransformerFactory(TransformerFactory transformerFactory) {
		this.transformerFactory = transformerFactory;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		// name=>xslPath(parameters)
		return new StringBuilder(name).append("=>").append(xslPath).append("(").append(parameters).append(")").toString();
	}

	public File getMainOutputDir() {
		return mainOutputDir;
	}

	public void setMainOutputDir(File mainOutputDir) {
		if (mainOutputDir == null) {
			logger.error("outputDir is null");
			return;
		}
		else if(!mainOutputDir.exists()){
		    logger.info("Creation of output directory {}.", mainOutputDir);
		    mainOutputDir.mkdirs();
		}
		else {
			this.mainOutputDir = mainOutputDir;
		}
	}

	public boolean isOutputSavedOnDisk() {
		return outputSavedOnDisk;
	}

	public void setOutputSavedOnDisk(boolean resultsPersistedOnDisk) {
		this.outputSavedOnDisk = resultsPersistedOnDisk;
	}

	public int getExecutionCount() {
		return executionCount;
	}

	public void setMainOutputName(String mainOutputName) {
		this.mainOutputName = mainOutputName;
	}

	public String getMainOutputName() {
		return mainOutputName;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public RunReport getLastRunReport() {
		return lastRunReport;
	}

	public void setLastRunReport(RunReport lastRunReport) {
		this.lastRunReport = lastRunReport;
	}

	public boolean isRunReportSavedOnDisk() {
		return runReportSavedOnDisk;
	}

	public void setRunReportSavedOnDisk(boolean runReportSavedOnDisk) {
		this.runReportSavedOnDisk = runReportSavedOnDisk;
	}

	public boolean isUseTimeStampedSubDir() {
		return useTimeStampedSubDir;
	}

	public void setUseTimeStampedSubDir(boolean useTimeStampedSubDir) {
		this.useTimeStampedSubDir = useTimeStampedSubDir;
	}

}