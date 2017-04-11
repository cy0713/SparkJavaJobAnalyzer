package test.java.cases;

import test.java.AbstractAnalyzerTest;
import test.resources.java8streams_jobs.SimpleLogAnalyzer3;

public class SimpleLogAnalyzer3Test extends AbstractAnalyzerTest{	
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.inputStorletFile = "test_data/hamlet.txt";
		this.analyticsJob = new SimpleLogAnalyzer3();
		this.jobToAnalyze = "/java8streams_jobs/SimpleLogAnalyzer3.java";
	}	
}