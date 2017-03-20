package test.java;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class SparkJavaJobAnalyzerTestRunner {
	

   public static void main(String[] args) {
      Result result = JUnitCore.runClasses(SparkJavaJobAnalyzerTestSuite.class);
		
      for (Failure failure : result.getFailures()) {
         System.out.println(failure.toString());
      }
		
      System.out.println(result.wasSuccessful());
   }
		

}
