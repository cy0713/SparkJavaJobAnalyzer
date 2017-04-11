package test.java.cases;

import test.java.AbstractAnalyzerTest;
import test.resources.java8streams_jobs.SimpleListCollector;

public class SimpleListCollectorTest extends AbstractAnalyzerTest{	
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.inputStorletFile = "test_data/hamlet.txt";
		this.analyticsJob = new SimpleListCollector();
		this.jobToAnalyze = "/java8streams_jobs/SimpleListCollector.java";
	}	
}