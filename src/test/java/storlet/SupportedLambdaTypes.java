package test.java.storlet;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import main.java.pl.joegreen.lambdaFromString.TypeReference;

public class SupportedLambdaTypes {
	
	private static Map<String, TypeReference> supportedMapTypes = new HashMap<>();
	private static Map<String, TypeReference> supportedFilterTypes = new HashMap<>();
	private static Map<String, TypeReference> supportedFlatMapTypes = new HashMap<>();
	
	static {
		supportedMapTypes.put("java.util.function.Function<? extends java.lang.String, ? extends java.lang.String>", 
				new TypeReference<Function<String, String>>(){});
		supportedMapTypes.put("java.util.function.Function<? extends java.lang.String, ? extends java.lang.Integer>", 
				new TypeReference<Function<String, Integer>>(){});
		supportedMapTypes.put("java.util.function.Function<? extends java.lang.Integer, ? extends java.lang.String>", 
				new TypeReference<Function<Integer, String>>(){});
		
		supportedFilterTypes.put("java.util.function.Predicate<? extends java.lang.String>", 
				new TypeReference<Predicate<String>>() {});
		supportedFilterTypes.put("java.util.function.Predicate<? extends java.lang.Integer>", 
				new TypeReference<Predicate<Integer>>() {});
		
		supportedFlatMapTypes.put("java.util.function.Function<? extends java.lang.String, ? extends java.util.stream.Stream<String>>", 
				new TypeReference<Function<String, Stream<String>>>(){});
	}
	
	public static TypeReference getMapType(String mapType){return supportedMapTypes.get(mapType);}
	public static TypeReference getFilterType(String filterType){return supportedFilterTypes.get(filterType);}
	public static TypeReference getFlatMapType(String flatMapType){return supportedFlatMapTypes.get(flatMapType);}

}
