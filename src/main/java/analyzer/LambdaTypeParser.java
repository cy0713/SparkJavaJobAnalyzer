package main.java.analyzer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import main.java.graph.GraphNode;

/**
 * Helper class to obtain the correct type information of a lambda.
 * This class is specially conceived as a fall-back mechanisms to infer 
 * the type of a lambda when the real inference library fails,
 * 
 * @author Raul Gracia
 *
 */
public class LambdaTypeParser {

	private String lambdaRawType;
	
	private String functionalInterface;
	
	private List<String> arguments = new ArrayList<>();
	
	Pattern functionBasedLambdas = Pattern.compile("(map|flatMap)");

	private static final int FIRST_ARGUMENT = 0;
	private static final int SECOND_ARGUMENT = 1;

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
		
		String lambdaMethod = node.getLambdaSignature().substring(0, node.getLambdaSignature().indexOf("("));
		//Second, check if the type of the lambda is unknown at all
		if (functionalInterface==null){
			Matcher matcher = functionBasedLambdas.matcher(lambdaMethod);
			if (matcher.matches()) functionalInterface = "java.util.function.Function";
			else functionalInterface = "java.util.function.Predicate";
		}
		String inferredArgument = null;
		//Get the number of types to infer for this specific lambda
		int numArgs = 1;
		try {
			numArgs = Class.forName(functionalInterface).getTypeParameters().length;
		} catch (SecurityException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		for (int i=0; i<numArgs; i++){
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
				inferredArgument = checkArgument(inferredArgument, node);
				arguments.add(0,inferredArgument);
			//If we have to infer the second argument, look onwards on graph						
			}else if (i==SECOND_ARGUMENT){
				List<String> nextNodeArguments = new LambdaTypeParser(
						node.getNextNode().getFunctionType()).getArguments();
				//Check if previous node has arguments
				nextNodeArguments = checkArgument(nextNodeArguments, node);
				//Check if the argument is correct	
				inferredArgument = checkArgument(nextNodeArguments.get(0), node);
				if (lambdaMethod.equals("flatMap")) {
					inferredArgument = "Stream<"+inferredArgument.substring(0)+">";
				}
				if (arguments.size() > 1) arguments.remove(arguments.size()-1);
				arguments.add(inferredArgument);
			}		
		}		
		return formattedLambdaType();	
	}

	private String checkArgument(String inferredArgument, GraphNode node) {
		if (undefinedGeneric(inferredArgument)){
			System.err.println("WARNING! We cannot infer the parameter type for: " +
					inferredArgument + " in " + node.getLambdaSignature());
			System.err.println("We are going to fallback to typeString, but "
					+ "this may crash at the server side!");
			return "java.lang.String";
		}		
		return inferredArgument;
	}
	
	private List<String> checkArgument(List<String> nextNodeArguments, GraphNode node) {
		if (nextNodeArguments.isEmpty()){
			System.err.println("WARNING! We cannot find next node parameters for: " + 
						node.getLambdaSignature());
			System.err.println("We are going to fallback to typeString, but this may "
					+ "crash at the server side!");
			return Arrays.asList("java.lang.String");
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
		arguments.add(0, "java.lang.String");
	}
	
	private void parseArgumentsFromRawType(String rawTypeOfLambda) {
		rawTypeOfLambda = rawTypeOfLambda.substring(rawTypeOfLambda.indexOf("<")+1);
		int argumentGenericsCount = 0;
		//If there is a single argument, just get it
		if (!rawTypeOfLambda.contains(", ")){
			arguments.add(rawTypeOfLambda.substring(0, rawTypeOfLambda.indexOf(">")));
			return;
		}
		rawTypeOfLambda = rawTypeOfLambda.substring(0, rawTypeOfLambda.length()-1);
		//If there are multiple with generics, then iterate
		StringBuilder foundParam = new StringBuilder();
		for (String arg: rawTypeOfLambda.split(", ")){
			if (arg.contains("<")) argumentGenericsCount++;
			if (arg.contains(">")) argumentGenericsCount--;
			foundParam.append(arg);
			if (argumentGenericsCount==0) {
				arguments.add(foundParam.toString());
				foundParam = new StringBuilder();
			}
		}		
	}
	
	private boolean hasDefinedGenerics() {
		for (String parameter: arguments)
			if ((parameter==null) || undefinedGeneric(parameter)) 
				return false;
		return true;
	}

	private boolean undefinedGeneric(String parameter){
		return parameter.trim().length()==1;
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