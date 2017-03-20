package test.resources.sparkjava_jobs;

public class SparkWordCount {
	
	public static void main(String[] args) {
		
		/*JavaRDD<String> textFile = sc.textFile("hdfs://...");
		JavaPairRDD<String, Integer> counts = textFile
		    .flatMap(s -> Arrays.asList(s.split(" ")).iterator())
		    .mapToPair(word -> new Tuple2<>(word, 1))
		    .reduceByKey((a, b) -> a + b);
		counts.saveAsTextFile("hdfs://...");*/
		
	}

}
