package test.java.cases;

import test.java.AbstractAnalyzerTest;
import test.resources.java8streams_jobs.SimpleMaxCollector;

public class SimpleMaxCollectorTest extends AbstractAnalyzerTest{	
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.inputStorletFile = "test_data/hamlet.txt";
		this.analyticsJob = new SimpleMaxCollector();
		this.jobToAnalyze = "/java8streams_jobs/SimpleMaxCollector.java";
	}	
}