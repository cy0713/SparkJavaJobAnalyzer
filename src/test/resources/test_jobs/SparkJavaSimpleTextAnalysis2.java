package test.resources.test_jobs;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

public class SparkJavaSimpleTextAnalysis2 {
	
	public static void main(String[] args) {
		
		SparkConf conf = new SparkConf().setAppName("SimpleTextAnalysisSparkJava");
		JavaSparkContext sc = new JavaSparkContext(conf);
		JavaRDD<String> distFile = sc.textFile("swift2d://data1.lvm/hamlet.txt");
		distFile.map(s -> s.split(" ").length).reduce((a, b) -> a + b);		
	}

}
