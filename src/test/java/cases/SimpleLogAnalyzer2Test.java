package test.java.cases;

import test.java.AbstractAnalyzerTest;
import test.resources.test_jobs.Java8SimpleLogAnalyzer2;

public class SimpleLogAnalyzer2Test extends AbstractAnalyzerTest{	
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.inputStorletFile = "test_data/hamlet.txt";
		this.analyticsJob = new Java8SimpleLogAnalyzer2();
		this.jobToAnalyze = "/test_jobs/Java8SimpleLogAnalyzer2.java";
	}	
}