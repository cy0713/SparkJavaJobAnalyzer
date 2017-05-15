package test.java;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import test.java.cases.SparkStreamingJavaHelloWorldTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    	SparkStreamingJavaHelloWorldTest.class
})
public class SparkStreamingJobAnalyzerTestSuite {}