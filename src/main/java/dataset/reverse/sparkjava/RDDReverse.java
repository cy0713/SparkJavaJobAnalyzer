package main.java.dataset.reverse.sparkjava;

import main.java.dataset.SparkDatasetTranslation;

public class RDDReverse implements SparkDatasetTranslation{
	
	public String applyDatasetTranslation(String datasetName, String datasetType, String jobCode) {
		String mainType = datasetType;
		String currenType = "Stream";
		if (mainType.contains("<")) {
			mainType = mainType.substring(0, mainType.indexOf("<"));
			currenType += datasetType.substring(datasetType.indexOf("<"));
		}
		//Change any possible reference to the JavaRDD class by Stream
		jobCode = jobCode.replace("java.util.stream.Stream", "org.apache.spark.api.java." + mainType);
		//Change the type declaration of the variable
		String newDataseType = datasetType.replace("Stream", mainType);
		return jobCode.replaceAll(currenType + "\\s*" + datasetName, newDataseType + " " + datasetName);		
	}

}
