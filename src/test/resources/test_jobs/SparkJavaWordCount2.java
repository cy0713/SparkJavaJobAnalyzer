package test.resources.test_jobs;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

import java.util.Arrays;

public class SparkJavaWordCount2 {
	
  private static final Logger LOGGER = LoggerFactory.getLogger(SparkJavaWordCount2.class);

  public static void main(String[] args) {
    String master = "local[*]";

    SparkConf conf = new SparkConf().setAppName(SparkJavaWordCount2.class.getName()).setMaster(master);
    JavaSparkContext context = new JavaSparkContext(conf);

    JavaRDD<String> textFile = context.textFile("swift2d://data1.lvm/hamlet.txt");
    
    textFile.flatMap(s -> Arrays.asList(s.split(" ")).iterator())
        	.mapToPair(word -> new Tuple2<String, Integer>(word, 1))
        	.reduceByKey((a, b) -> a + b)
        	.foreach(result -> LOGGER.info(String.format("Word [%s] count [%d].", result._1(), result._2())));
  }
}