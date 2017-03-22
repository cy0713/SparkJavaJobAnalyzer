package main.java.analyzer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class LambdaTypeParser {
	
	private String lambdaRawType;
	
	private String functionalInterface;
	
	private List<String> arguments = new ArrayList<>();
	

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
			System.out.println(rawTypeOfLambda.substring(iniParamPos));
			checkForParam = getCorrectParameterIndex(
					rawTypeOfLambda.substring(iniParamPos));
			if (!checkForParam.isPresent()) endParamPos = rawTypeOfLambda.indexOf(">");
			else endParamPos = checkForParam.get()-2;
			String foundParam = rawTypeOfLambda.substring(iniParamPos, 
					iniParamPos+endParamPos);
			arguments.add(foundParam);
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
		// TODO Auto-generated method stub
		return false;
	}

	private boolean hasFunctionalInterface() {
		// TODO Auto-generated method stub
		return false;
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