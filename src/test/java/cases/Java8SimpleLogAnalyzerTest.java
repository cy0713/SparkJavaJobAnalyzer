package test.java.cases;

import test.java.AbstractAnalyzerTest;
import test.resources.test_jobs.Java8SimpleLogAnalyzer;

public class Java8SimpleLogAnalyzerTest extends AbstractAnalyzerTest{

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.inputStorletFile = "test_data/hamlet.txt";
		this.analyticsJob = new Java8SimpleLogAnalyzer();
		this.jobToAnalyze = "/test_jobs/Java8SimpleLogAnalyzer.java";
	}	
}
