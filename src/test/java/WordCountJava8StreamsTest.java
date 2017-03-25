package test.java;

import test.resources.java8streams_jobs.WordCountJava8Streams;

public class WordCountJava8StreamsTest extends AbstractAnalyzerTest{	
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.inputStorletFile = "test_data/hamlet.txt";
		this.analyticsJob = new WordCountJava8Streams();
		this.jobToAnalyze = "/java8streams_jobs/WordCountJava8Streams.java";
	}	
}
