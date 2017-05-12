package test.java;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import test.java.cases.SparkSimpleTextAnalysisTest;
import test.java.cases.SparkWordCountTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    	SparkSimpleTextAnalysisTest.class,
    	SparkWordCountTest.class
})
public class SparkJobAnalyzerTestSuite {}