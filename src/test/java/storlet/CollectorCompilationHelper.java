package test.java.storlet;

import java.util.stream.Collector;

import main.java.compiler.JobCompiler;
import main.java.pl.joegreen.lambdaFromString.LambdaFactory;

public class CollectorCompilationHelper {
	
	private static final String COMPILED_JOB_PATH = "test.java.storlet";
	
	@SuppressWarnings("rawtypes")
	public static Collector getCollectorObject(String collectorSignature, 
			String collectorType, LambdaFactory factory) {
			 
		 String className = "Collector"+String.valueOf(Math.abs(collectorSignature.hashCode()));
		 String javaCode = "package " + COMPILED_JOB_PATH + ";\n" +
				 			"import java.util.stream.Collectors; \n" +
				 			"import java.util.stream.Collector; \n" +
				 			"import java.util.AbstractMap.SimpleEntry; \n" +
				 			"import java.util.Map; \n" +
				 			"import test.java.storlet.IGetCollector; \n" +
				 			"import static java.util.stream.Collectors.joining; \n"+
				 			"import static java.util.stream.Collectors.groupingBy; \n"+
				 			"import static java.util.stream.Collectors.counting; \n"+
				 			
		                    "public class " + className + " implements IGetCollector {\n" +
		                    "    public " + collectorType +" getCollector() {\n" +
		                    "        return (" + collectorType + ")" + collectorSignature + ";\n" +
		                    "    }\n" +
		                    "}\n";
		 
		System.out.println(javaCode);
		long iniTime = System.currentTimeMillis();
		/*javaCode = collectorSignature;
		Collector result = factory.createLambdaUnchecked(javaCode,  
		new TypeReference<Collector<SimpleEntry<String, Long>, ?, Map<String, Long>>>(){});*/		
		JobCompiler compiler = new JobCompiler();
		IGetCollector getCollector = (IGetCollector) compiler.compileFromString(COMPILED_JOB_PATH, className, javaCode);
		System.out.println("NANO TIME COMPILATION COLLECTOR: " + (System.currentTimeMillis()-iniTime));
		return getCollector.getCollector();
	}
}
