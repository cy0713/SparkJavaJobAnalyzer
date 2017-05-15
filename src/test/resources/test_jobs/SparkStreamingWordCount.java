package test.resources.test_jobs;

import java.util.Arrays;

import org.apache.spark.SparkConf;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;

import scala.Tuple2;

public class SparkStreamingWordCount {
	
	public static void main(String[] args) throws Exception {

	    SparkConf sparkConf = new SparkConf().setAppName("SparkJavaStreamingWordCount");
	    // Create the context with 2 seconds batch size
	    JavaStreamingContext jssc = new JavaStreamingContext(sparkConf, new Duration(2000));

	    JavaDStream<String> text = jssc.textFileStream("swift2d://data1.lvm/hamlet.txt");

	    JavaPairDStream<String, Integer> wordCounts = text.flatMap(x -> Arrays.asList(x.split(" ")).iterator())
	    												  .map(word -> word.replaceAll("[^a-zA-Z]", "").toLowerCase().trim())
	    												  .mapToPair(s -> new Tuple2<String, Integer>(s, 1))
	    												  .reduceByKey((i1, i2) -> i1 + i2);

	    wordCounts.print();
	    jssc.start();
	    jssc.awaitTermination();
	}
}
