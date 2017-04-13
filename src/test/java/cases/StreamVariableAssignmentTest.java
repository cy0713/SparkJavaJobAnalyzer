package test.java.cases;

import test.java.AbstractAnalyzerTest;
import test.resources.java8streams_jobs.StreamVariableAssignments;

public class StreamVariableAssignmentTest extends AbstractAnalyzerTest{	
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.inputStorletFile = "test_data/hamlet.txt";
		this.analyticsJob = new StreamVariableAssignments();
		this.jobToAnalyze = "/java8streams_jobs/StreamVariableAssignments.java";
	}	
}