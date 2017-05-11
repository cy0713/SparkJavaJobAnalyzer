package test.java.cases;

import test.java.AbstractAnalyzerTest;
import test.resources.test_jobs.Java8WordCount;

public class Java8WordCountStreamsTest extends AbstractAnalyzerTest{	
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.inputStorletFile = "test_data/hamlet.txt";
		this.analyticsJob = new Java8WordCount();
		this.jobToAnalyze = "/test_jobs/Java8WordCount.java";
	}	
}
