package test.java.cases;

import test.java.AbstractAnalyzerTest;
import test.resources.test_jobs.Java8SimpleMaxCollector;

public class Java8SimpleMaxCollectorTest extends AbstractAnalyzerTest{	
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.inputStorletFile = "test_data/hamlet.txt";
		this.analyticsJob = new Java8SimpleMaxCollector();
		this.jobToAnalyze = "/test_jobs/Java8SimpleMaxCollector.java";
	}	
}