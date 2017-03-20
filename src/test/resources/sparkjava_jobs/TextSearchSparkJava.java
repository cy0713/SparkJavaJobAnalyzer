package test.resources.sparkjava_jobs;

public class TextSearchSparkJava {
	
	public static void main(String[] args) {
		
		// Creates a DataFrame having a single column named "line"
		/*JavaRDD<String> textFile = sc.textFile("hdfs://...");
		JavaRDD<Row> rowRDD = textFile.map(RowFactory::create);
		List<StructField> fields = Arrays.asList(
		  DataTypes.createStructField("line", DataTypes.StringType, true));
		StructType schema = DataTypes.createStructType(fields);
		DataFrame df = sqlContext.createDataFrame(rowRDD, schema);

		DataFrame errors = df.filter(col("line").like("%ERROR%"));
		// Counts all the errors
		errors.count();
		// Counts errors mentioning MySQL
		errors.filter(col("line").like("%MySQL%")).count();
		// Fetches the MySQL errors as an array of strings
		errors.filter(col("line").like("%MySQL%")).collect();*/
		
	}

}
