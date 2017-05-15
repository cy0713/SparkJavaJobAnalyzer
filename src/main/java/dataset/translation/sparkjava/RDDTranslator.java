package main.java.dataset.translation.sparkjava;

import main.java.dataset.SparkDatasetTranslation;

public class RDDTranslator implements SparkDatasetTranslation{
	
	public String applyDatasetTranslation(String datasetName, String datasetType, String jobCode) {
		String mainType = datasetType;
		String streamType = "Stream";
		if (mainType.contains("<")) 
			mainType = mainType.substring(0, mainType.indexOf("<"));
		//Change any possible reference to the JavaRDD class by Stream
		jobCode = jobCode.replace("org.apache.spark.api.java." + mainType, "java.util.stream.Stream");
		//Change the type declaration of the variable		
		String newDataseType = datasetType.replace(mainType, streamType);
		//There is no equivalent of JavaPairRDD in Streams. We assume that a JavaPairRDD
		//comes after a mapToPair or map function and it represents a Map of tupes in Java8
		if (mainType.equals("JavaPairRDD") || mainType.equals("JavaPairDStream")){
			newDataseType = newDataseType.replace("Stream<", "Map<");
			jobCode = jobCode.replaceFirst("java.util.stream.Stream;", 
					  "java.util.stream.Stream;\nimport java.util.Map;\n"
					+ "import java.util.AbstractMap.SimpleEntry;\n");
		}
		return jobCode.replaceAll(datasetType + "\\s*" + datasetName, newDataseType + " " + datasetName);
	}
}
