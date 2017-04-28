package test.resources.test_jobs;

import java.util.stream.Stream;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

public class SparkJavaSimpleTextAnalysis {
	
	public static void main(String[] args) {
		
		SparkConf conf = new SparkConf().setAppName("SimpleTextAnalysisSparkJava");
		JavaSparkContext sc = new JavaSparkContext(conf);
		Stream<String> distFile = Stream.of(""); //sc.textFile("data.txt");
		distFile.map(s -> s.length()); //.reduce((a, b) -> a + b);
		
	}

}
