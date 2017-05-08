package main.java.dataset.translation.sparkjava;

import main.java.dataset.SparkDatasetTranslation;

public class RDDTranslator implements SparkDatasetTranslation{
	
	public String applyDatasetTranslation(String datasetName, String datasetType, String jobCode) {
		String mainType = datasetType;
		if (mainType.contains("<")) 
			mainType = mainType.substring(0, mainType.indexOf("<"));
		//Change any possible reference to the JavaRDD class by Stream
		jobCode = jobCode.replace("org.apache.spark.api.java." + mainType, "java.util.stream.Stream");
		//Change the type declaration of the variable		
		String newDataseType = datasetType.replace(mainType, "Stream");
		return jobCode.replaceAll(datasetType + "\\s*" + datasetName, newDataseType + " " + datasetName);
	}

}
