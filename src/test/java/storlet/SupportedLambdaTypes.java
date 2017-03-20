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
		supportedMapTypes.put("Function<String, String>", new TypeReference<Function<String, String>>(){});
		supportedMapTypes.put("Function<String, Integer>", new TypeReference<Function<String, Integer>>(){});
		supportedMapTypes.put("Function<Integer, String>", new TypeReference<Function<Integer, String>>(){});
		
		supportedFilterTypes.put("Predicate<String>", new TypeReference<Predicate<String>>() {});
		supportedFilterTypes.put("Predicate<Integer>", new TypeReference<Predicate<Integer>>() {});
		
		supportedFlatMapTypes.put("Function<String, Stream<String>>", new TypeReference<Function<String, Stream<String>>>(){});
	}
	
	public static TypeReference getMapType(String mapType){ return supportedMapTypes.get(mapType);}
	public static TypeReference getFilterType(String filterType){ return supportedFilterTypes.get(filterType);}
	public static TypeReference getFlatMapType(String flatMapType){ return supportedFlatMapTypes.get(flatMapType);}

}
