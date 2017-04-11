package test.java.storlet;

import java.util.Map;
import java.util.AbstractMap.SimpleEntry;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import main.java.compiler.JobCompiler;

public class CollectorCompilationHelper {
	
	private static final String COMPILED_JOB_PATH = "test.java.storlet";
	
	@SuppressWarnings("rawtypes")
	public static Collector getCollectorObject(String collectorSignature, String collectorType) {
		
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
		JobCompiler compiler = new JobCompiler();
		IGetCollector getCollector = (IGetCollector) compiler.compileFromString(COMPILED_JOB_PATH, className, javaCode);
		System.out.println("NANO TIME COMPILATION COLLECTOR: " + (System.currentTimeMillis()-iniTime));
		return getCollector.getCollector();
	}
	/**
	 * Initialize widely used collectors so we can minimize compilation overhead.
	 * 
	 * @param collectorCacheBuilder
	 */
	void initializeCollectorCache(Map<String, Collector> collectorCacheBuilder) {
		collectorCacheBuilder.put("collect(Collectors.toList())", Collectors.toList());
		collectorCacheBuilder.put("collect(Collectors.toSet())", Collectors.toSet());
		collectorCacheBuilder.put("collect(Collectors.maxBy(String::compareTo))", Collectors.maxBy(String::compareTo));
		collectorCacheBuilder.put("collect(Collectors.maxBy(Integer::compareTo))", Collectors.maxBy(Integer::compareTo));
		collectorCacheBuilder.put("collect(Collectors.maxBy(Long::compareTo))", Collectors.maxBy(Long::compareTo));
		collectorCacheBuilder.put("collect(Collectors.minBy(String::compareTo))", Collectors.minBy(String::compareTo));
		collectorCacheBuilder.put("collect(Collectors.minBy(Integer::compareTo))", Collectors.minBy(Integer::compareTo));
		collectorCacheBuilder.put("collect(Collectors.minBy(Long::compareTo))", Collectors.minBy(Long::compareTo));		
		collectorCacheBuilder.put("collect(Collectors.groupingBy(SimpleEntry<String, Long>::getKey, Collectors.counting()))", 
				Collectors.groupingBy(SimpleEntry<String, Long>::getKey, Collectors.counting()));
		collectorCacheBuilder.put("collect(Collectors.groupingBy(SimpleEntry<String, Integer>::getKey, Collectors.counting())", 
				Collectors.groupingBy(SimpleEntry<String, Integer>::getKey, Collectors.counting()));
		collectorCacheBuilder.put("collect(Collectors.counting())", Collectors.counting());
	}
}
