package test.java;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import test.resources.java8streams_jobs.WordCountJava8Streams;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        SimpleLogAnalyzerTest.class,
        SimpleLogAnalyzer2Test.class,
        WordCountJava8Streams.class
})
public class SparkJavaJobAnalyzerTestSuite {}