package test.java.cases;

import test.java.AbstractAnalyzerTest;
import test.resources.test_jobs.Java8SimpleReduce;

public class Java8SimpleReduceTest extends AbstractAnalyzerTest{	
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.inputStorletFile = "test_data/hamlet.txt";
		this.analyticsJob = new Java8SimpleReduce();
		this.jobToAnalyze = "/test_jobs/Java8SimpleReduce.java";
	}	
}