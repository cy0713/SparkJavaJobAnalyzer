package test.resources.test_jobs;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.StreamingContext;
import org.apache.spark.streaming.dstream.DStream;

public class SparkStreamingJavaHelloWorld {
	
	public static void main(String[] args) {
		
		SparkConf conf = new SparkConf().setAppName("Simple Streaming Application").setMaster("local[*]");
		SparkContext sc = new SparkContext(conf);
		StreamingContext ssc = new StreamingContext(sc, Durations.seconds(60));
		DStream<String> lines = ssc.textFileStream("swift2d://data1.lvm/hamlet.txt");
		lines.print();		
	}
	

}
