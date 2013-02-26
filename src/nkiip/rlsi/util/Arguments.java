/****************************************************************************************
 *Project: Regularized Latent Semantic Indexing 1.0
 ****************************************************************************************
 *(C) Copyright 2013 Laboratory of Intelligent Information Processing, Nankai University
 *    All Rights Reserved.
 *Authors: Zhicheng He, Yingjie Xu, Jun Xu, MaoQiang xie, Yalou Huang
 *****************************************************************************************/
package nkiip.rlsi.util;

import java.util.Hashtable;
import java.util.regex.Pattern;

public class Arguments {
	// Variables
	private Hashtable<String, String> Parameters;

	// Constructor
	public Arguments(String[] Args) {
		Parameters = new Hashtable<String, String>();
		Pattern Spliter = Pattern.compile("^-{1,2}|^/|=",
				Pattern.CASE_INSENSITIVE);
		Pattern Remover = Pattern.compile("^['\"\"]?(.*?)['\"\"]?$",
				Pattern.CASE_INSENSITIVE);
		String Parameter = null;
		String[] Parts;

		// Valid parameters forms:
		// {-,/,--}param{ ,=,:}((",')value(",'))
		// Examples:
		// -param1 value1 --param2 /param3:"Test-:-work"
		// /param4=happy -param5 '--=nice=--'
		for (String Txt : Args) {
			// Look for new parameters (-,/ or --) and a
			// possible enclosed value (=,:)
			Parts = Spliter.split(Txt, 3);

			switch (Parts.length) {
			// Found a value (for the last parameter
			// found (space separator))
			case 1:
				if (Parameter != null) {
					if (!Parameters.containsKey(Parameter)) {
						Parts[0] = Remover.matcher(Parts[0]).replaceAll("$1");
						// Remover.Replace(Parts[0], "$1");

						Parameters.put(Parameter, Parts[0]);
					}
					Parameter = null;
				}
				// else Error: no parameter waiting for a value (skipped)
				break;

			// Found just a parameter
			case 2:
				// The last parameter is still waiting.
				// With no value, set it to true.
				if (Parameter != null) {
					if (!Parameters.containsKey(Parameter))
						Parameters.put(Parameter, "true");
				}
				Parameter = Parts[1];
				break;

			// Parameter with enclosed value
			case 3:
				// The last parameter is still waiting.
				// With no value, set it to true.
				if (Parameter != null) {
					if (!Parameters.containsKey(Parameter))
						Parameters.put(Parameter, "true");
				}

				Parameter = Parts[1];

				// Remove possible enclosing characters (",')
				if (!Parameters.containsKey(Parameter)) {
					Parts[2] = Remover.matcher(Parts[2]).replaceAll("$1");
					Parameters.put(Parameter, Parts[2]);
				}

				Parameter = null;
				break;
			}
		}
		// In case a parameter is still waiting
		if (Parameter != null) {
			if (!Parameters.containsKey(Parameter))
				Parameters.put(Parameter, "true");
		}
	}

	// Retrieve a parameter value if it exists
	public String getValue(String Param) {
		if (Parameters.containsKey(Param)) {
			return Parameters.get(Param);
		}
		return null;
	}
}
