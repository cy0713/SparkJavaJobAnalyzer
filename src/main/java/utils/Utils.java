package main.java.utils;

import java.util.ArrayList;
import java.util.List;

public class Utils {
	
	public static List<String> getParametersFromSignature(String parameters) {
		List<String> result = new ArrayList<>();
		int openBr = 0;
		int inipos = 0, pos = 0;
		while (pos<parameters.length()) {
			if (parameters.charAt(pos)=='<') openBr++;
			if (parameters.charAt(pos)=='>') openBr--;
			if ((parameters.charAt(pos)==',' && openBr==0) || (pos == parameters.length()-1)){
				if (pos == parameters.length()-1) pos++;
				result.add(parameters.substring(inipos, pos));
				inipos = pos+1; //avoid the comma
			}
			pos++;
		}
		return result;	
	}

}
