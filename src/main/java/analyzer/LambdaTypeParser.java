package main.java.analyzer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import main.java.graph.GraphNode;

public class LambdaTypeParser {

	private String lambdaRawType;
	
	private String functionalInterface;
	
	private List<String> arguments = new ArrayList<>();
	
	Pattern functionBasedLambdas = Pattern.compile("(map|flatMap)");

	private static final Map<String, Integer> argumentsToInfer = new HashMap<>();
	private static final int FIRST_ARGUMENT = 0;
	private static final int SECOND_ARGUMENT = 1;
	
	static{
		argumentsToInfer.put("java.util.function.Predicate", 1);
		argumentsToInfer.put("java.util.function.Function", 2);	
	}

	public LambdaTypeParser(String lambdaRawType) {
		this.lambdaRawType = lambdaRawType;
		//We may get as input a null type from the analyzer
		if (lambdaRawType==null) return;
		//Get the rest of information from a proper string type description
		this.functionalInterface = lambdaRawType.substring(0, lambdaRawType.indexOf("<"));
		parseArgumentsFromRawType(lambdaRawType);
		
	}	
	
	public boolean isTypeWellDefined(){
		return lambdaRawType!=null && hasFunctionalInterface()
				&& hasDefinedGenerics();
	}
	
	public String solveTypeFromGraph(GraphNode node) {
		//First, try to address the problem of unknown input type for first lambda
		if (node.getPreviousNode()==null)
			setFirstLambdaInputTypeAsString();		
		if (isTypeWellDefined()) return formattedLambdaType();
		
		String lambdaMethod = node.getToExecute().substring(0, node.getToExecute().indexOf("("));
		//Second, check if the type of the lambda is unknown at all
		if (functionalInterface==null){
			Matcher matcher = functionBasedLambdas.matcher(lambdaMethod);
			if (matcher.matches()) functionalInterface = "java.util.function.Function";
			else functionalInterface = "java.util.function.Predicate";
		}
		String inferredArgument = null;
		for (int i=0; i<argumentsToInfer.get(functionalInterface); i++){
			//First, check that the argument is actually inexistent/incorrect
			if (!arguments.isEmpty() && arguments.size()>i){
				if (undefinedGeneric(arguments.get(i))) arguments.remove(i);
				else continue;
			}
			//If we have to infer the first argument, look backwards on graph
			if (i==FIRST_ARGUMENT){				
				List<String> previousNodeArguments = new LambdaTypeParser(
						node.getPreviousNode().getFunctionType()).getArguments();
				inferredArgument = previousNodeArguments.get(previousNodeArguments.size()-1);
				//System.out.println("Inferred first argument from previous lambda: " + inferredArgument);				
				inferredArgument = checkArgument(inferredArgument, node);
				arguments.add(0,inferredArgument);
			//If we have to infer the second argument, look onwards on graph						
			}else if (i==SECOND_ARGUMENT){
				List<String> nextNodeArguments = new LambdaTypeParser(
						node.getNextNode().getFunctionType()).getArguments();
				//Check if previous node has arguments
				nextNodeArguments = checkArgument(nextNodeArguments, node);
				//Check if the argument is correct	
				inferredArgument = nextNodeArguments.get(0);
				if (lambdaMethod.equals("flatMap")) {
					inferredArgument = inferredArgument.replace("? super ", "").replace("? extends ", "");
					inferredArgument = "? extends Stream<"+inferredArgument.substring(0)+">";
				}
				if (arguments.size() > 1) arguments.remove(arguments.size()-1);
				arguments.add(inferredArgument);
			}		
		}		
		return formattedLambdaType();	
	}

	private String checkArgument(String inferredArgument, GraphNode node) {
		if (undefinedGeneric(inferredArgument)){
			System.err.println("WARNING! We cannot infer the INPUT type for: " + node.getToExecute());
			System.err.println("We are going to fallback to typeString, but this may crash at the server side!");
			return "? extends java.lang.String";
		}		
		return inferredArgument;
	}
	
	private List<String> checkArgument(List<String> nextNodeArguments, GraphNode node) {
		if (nextNodeArguments.isEmpty()){
			System.err.println("WARNING! We cannot infer the OUTPUT type for: " + node.getToExecute());
			System.err.println("We are going to fallback to typeString, but this may crash at the server side!");
			return Arrays.asList("? extends java.lang.String");
		}
		return nextNodeArguments;
	}

	private String formattedLambdaType() {
		StringBuilder builder = new StringBuilder(functionalInterface);
		builder.append("<");
		for (String argument: arguments)
			builder.append(argument).append(", ");
		builder.delete(builder.length()-2, builder.length());
		builder.append(">");
		return builder.toString();
	}

	/**
	 * In general, when JavaSymbolSolver does not solve the input type for
	 * the first lambda, we will initialize it to String. This assumption holds
	 * as in the storage side we will have as input Streams of Strings initially.
	 */
	private void setFirstLambdaInputTypeAsString() {
		if (!arguments.isEmpty()) arguments.remove(0);
		arguments.add(0, "? extends java.lang.String");
	}
	
	private void parseArgumentsFromRawType(String rawTypeOfLambda) {
		rawTypeOfLambda = rawTypeOfLambda.substring(rawTypeOfLambda.indexOf("<")+1);
		int endParamPos;
		int iniParamPos = endParamPos = 0;
		//If there is any argument, let's go for it
		while (true){
			//Check if there is an argument to parse
			Optional<Integer> checkForParam = getCorrectParameterIndex(
					rawTypeOfLambda.substring(iniParamPos));
			if (!checkForParam.isPresent()) break;
			//Parse and extract the argument as it comes from JavaSymbolSolver
			iniParamPos += checkForParam.get();
			if (rawTypeOfLambda.startsWith("? super ")) 
				rawTypeOfLambda = rawTypeOfLambda.substring("? super ".length());
			if (rawTypeOfLambda.startsWith("? extends ")) 
				rawTypeOfLambda = rawTypeOfLambda.substring("? extends ".length());
			//System.out.println(rawTypeOfLambda.substring(iniParamPos));
			checkForParam = getCorrectParameterIndex(
					rawTypeOfLambda.substring(iniParamPos));
			if (!checkForParam.isPresent()) endParamPos = rawTypeOfLambda.lastIndexOf(">");
			else endParamPos = checkForParam.get()-2;
			String foundParam = rawTypeOfLambda.substring(iniParamPos, 
					iniParamPos+endParamPos);
			arguments.add("? extends " + foundParam);
			rawTypeOfLambda = rawTypeOfLambda.substring(foundParam.length());
			if (rawTypeOfLambda.startsWith(", ")) 
				rawTypeOfLambda = rawTypeOfLambda.substring(2);
		}		
	}

	private Optional<Integer> getCorrectParameterIndex(String toParse) {
		Stream<Integer> indexes = Arrays.asList(toParse.indexOf("? super "),
				toParse.indexOf("? extends ")).stream();
		return indexes.filter(i -> i>-1).min((a,b) -> a.compareTo(b));
	}
	
	private boolean hasDefinedGenerics() {
		for (String parameter: arguments)
			if ((parameter==null) || undefinedGeneric(parameter)) 
				return false;
		return true;
	}

	private boolean undefinedGeneric(String parameter){
		return parameter.startsWith("? extends ")?
			parameter.substring("? extends ".length()).length()==1:
			parameter.substring("? super ".length()).length()==1;
	}
	
	private boolean hasFunctionalInterface() {
		return functionalInterface!=null && functionalInterface!="";
	}

	public String getLambdaRawType() {
		return lambdaRawType;
	}

	public void setLambdaRawType(String lambdaRawType) {
		this.lambdaRawType = lambdaRawType;
	}

	public String getFunctionalInterface() {
		return functionalInterface;
	}

	public void setFunctionalInterface(String functionalInterface) {
		this.functionalInterface = functionalInterface;
	}

	public List<String> getArguments() {
		return arguments;
	}

	public void setArguments(List<String> arguments) {
		this.arguments = arguments;
	}	
}