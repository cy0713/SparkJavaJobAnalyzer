package test.java.cases;

import test.java.AbstractAnalyzerTest;
import test.resources.test_jobs.Java8SimpleListCollector;

public class SimpleListCollectorTest extends AbstractAnalyzerTest{	
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.inputStorletFile = "test_data/hamlet.txt";
		this.analyticsJob = new Java8SimpleListCollector();
		this.jobToAnalyze = "/test_jobs/Java8SimpleListCollector.java";
	}	
}