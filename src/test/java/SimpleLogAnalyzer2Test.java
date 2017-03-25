package test.java;

import test.resources.java8streams_jobs.SimpleLogAnalyzer2;

public class SimpleLogAnalyzer2Test extends AbstractAnalyzerTest{	
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.inputStorletFile = "test_data/hamlet.txt";
		this.analyticsJob = new SimpleLogAnalyzer2();
		this.jobToAnalyze = "/java8streams_jobs/SimpleLogAnalyzer2.java";
	}	
}