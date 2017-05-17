package test.java.storlet;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import pl.joegreen.lambdaFromString.TypeReference;

import java.util.AbstractMap.*;


public class SupportedLambdaTypes {
	
	private static Map<String, TypeReference> supportedMapTypes = new HashMap<>();
	private static Map<String, TypeReference> supportedFilterTypes = new HashMap<>();
	private static Map<String, TypeReference> supportedFlatMapTypes = new HashMap<>();
	private static Map<String, TypeReference> supportedReduceTypes = new HashMap<>();
	
	static {
		supportedMapTypes.put("java.util.function.Function<java.lang.String, java.lang.String>", 
				new TypeReference<Function<String, String>>(){});
		supportedMapTypes.put("java.util.function.Function<java.lang.String, java.lang.Integer>", 
				new TypeReference<Function<String, Integer>>(){});
		supportedMapTypes.put("java.util.function.Function<java.lang.Integer, java.lang.String>", 
				new TypeReference<Function<Integer, String>>(){});
		supportedMapTypes.put("java.util.function.Function<java.lang.Integer, java.lang.Integer>", 
				new TypeReference<Function<Integer, Integer>>(){});
		supportedMapTypes.put("java.util.function.Function<java.lang.Long, java.lang.String>", 		
				new TypeReference<Function<Long, String>>(){});		
		supportedMapTypes.put("java.util.function.Function<java.lang.String, java.lang.Long>", 		
				new TypeReference<Function<String, Long>>(){});		
		supportedMapTypes.put("java.util.function.Function<java.lang.Long, java.lang.Long>", 		
				new TypeReference<Function<Long, Long>>(){});		
		supportedMapTypes.put("java.util.function.Function<java.lang.Integer, java.lang.Long>", 		
				new TypeReference<Function<Integer, Long>>(){});		
		supportedMapTypes.put("java.util.function.Function<java.lang.Long, java.lang.Integer>", 		
				new TypeReference<Function<Long, Integer>>(){});		
		supportedMapTypes.put("java.util.function.Function<java.lang.String, java.util.AbstractMap.SimpleEntry<java.lang.String, java.lang.Long>>", 
				new TypeReference<Function<String, SimpleEntry<String, Long>>>(){});
		supportedMapTypes.put("java.util.function.Function<java.lang.String, java.util.AbstractMap.SimpleEntry<java.lang.String, java.lang.Integer>>", 		
				new TypeReference<Function<String, SimpleEntry<String, Integer>>>(){});
		supportedMapTypes.put("java.util.function.Function<java.lang.String, java.util.AbstractMap.SimpleEntry<java.lang.String, java.lang.String>>", 		
				new TypeReference<Function<String, SimpleEntry<String, String>>>(){});
		
		supportedFilterTypes.put("java.util.function.Predicate<java.lang.String>", 
				new TypeReference<Predicate<String>>() {});
		supportedFilterTypes.put("java.util.function.Predicate<java.lang.Integer>", 
				new TypeReference<Predicate<Integer>>() {});
		supportedFilterTypes.put("java.util.function.Predicate<java.util.AbstractMap.SimpleEntry<java.lang.String, java.lang.Long>>", 
				new TypeReference<Predicate<SimpleEntry<String, Long>>>() {});
		supportedFilterTypes.put("java.util.function.Predicate<java.lang.Long>", 		
				new TypeReference<Predicate<Long>>() {});
		
		supportedFlatMapTypes.put("java.util.function.Function<java.lang.String, java.util.stream.Stream<java.lang.String>>", 
				new TypeReference<Function<String, Stream<String>>>(){});	
		supportedFlatMapTypes.put("java.util.function.Function<java.lang.String, java.util.stream.Stream<java.lang.Integer>>", 
				new TypeReference<Function<String, Stream<Integer>>>(){});	
		supportedFlatMapTypes.put("java.util.function.Function<java.lang.String, java.util.stream.Stream<java.lang.Long>>", 
				new TypeReference<Function<String, Stream<Long>>>(){});	
		
		supportedReduceTypes.put("java.util.function.BinaryOperator<java.lang.Integer>", 
				new TypeReference<BinaryOperator<Integer>>(){});
		supportedReduceTypes.put("java.util.function.BinaryOperator<java.lang.Long>", 
				new TypeReference<BinaryOperator<Long>>(){});
	}
	
	public static TypeReference getMapType(String mapType){return supportedMapTypes.get(mapType);}
	public static TypeReference getFilterType(String filterType){return supportedFilterTypes.get(filterType);}
	public static TypeReference getFlatMapType(String flatMapType){return supportedFlatMapTypes.get(flatMapType);}
	public static TypeReference getReduceType(String reduceType){return supportedReduceTypes.get(reduceType);}

}
