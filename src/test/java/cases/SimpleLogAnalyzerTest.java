package test.java.cases;

import test.java.AbstractAnalyzerTest;
import test.resources.java8streams_jobs.SimpleLogAnalyzer;

public class SimpleLogAnalyzerTest extends AbstractAnalyzerTest{

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.inputStorletFile = "test_data/hamlet.txt";
		this.analyticsJob = new SimpleLogAnalyzer();
		this.jobToAnalyze = "/java8streams_jobs/SimpleLogAnalyzer.java";
	}	
}
