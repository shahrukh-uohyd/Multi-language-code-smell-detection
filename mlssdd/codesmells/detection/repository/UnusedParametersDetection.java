/* (c) Copyright 2019 and following years, MounaA and PalmyreB.
 *
 * Use and copying of this software and preparation of derivative works
 * based upon this software are permitted. Any copy of this software or
 * of any derivative work must include the above copyright notice of
 * the author, this paragraph and the one after it.
 *
 * This software is made available AS IS, and THE AUTHOR DISCLAIMS
 * ALL WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE, AND NOT WITHSTANDING ANY OTHER PROVISION CONTAINED HEREIN,
 * ANY LIABILITY FOR DAMAGES RESULTING FROM THE SOFTWARE OR ITS USE IS
 * EXPRESSLY DISCLAIMED, WHETHER ARISING IN CONTRACT, TORT (INCLUDING
 * NEGLIGENCE) OR STRICT LIABILITY, EVEN IF THE AUTHOR IS ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * All Rights Reserved.
 */

package mlssdd.codesmells.detection.repository;

import java.util.HashSet;
import java.util.Set;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import mlssdd.codesmells.detection.AbstractCodeSmellDetection;
import mlssdd.codesmells.detection.ICodeSmellDetection;
import mlssdd.kernel.impl.MLSCodeSmell;

public class UnusedParametersDetection extends AbstractCodeSmellDetection
		implements ICodeSmellDetection {

	public void detect(final Document xml) {
		final Set<MLSCodeSmell> unusedParamsSet = new HashSet<>();
		String paramQuery;
		final String funcQuery = "descendant::function";
		// Query to select variables used in a function
		final String varQuery = "ancestor::function//expr//name";

		try {
			final XPathExpression funcExpr =
				AbstractCodeSmellDetection.xPath.compile(funcQuery);
			final XPathExpression typeExpr = AbstractCodeSmellDetection.xPath
				.compile("../type | ../../type");
			final XPathExpression indexExpr =
				AbstractCodeSmellDetection.xPath.compile("../../name/index");

			final NodeList fileList =
				(NodeList) AbstractCodeSmellDetection.FILE_EXP
					.evaluate(xml, XPathConstants.NODESET);
			final int fileLength = fileList.getLength();
			for (int i = 0; i < fileLength; i++) {
				final Node thisFileNode = fileList.item(i);
				final String language = AbstractCodeSmellDetection.LANGUAGE_EXP
					.evaluate(thisFileNode);
				final String paramTemplate =
					"parameter_list/parameter%s/decl/name%s";
				if (language.equals("C") || language.equals("C++")) {
					// Skip first two parameters, which are the JNI environment and the class for a
					// static method or the object for a non-static method
					paramQuery =
						String.format(paramTemplate, "[position()>2]", "");
				}
				else if (language.equals("Java")) {
					// Select the parameters, excluding '[]' from the name of arrays
					paramQuery = String
						.format(
							"(%s | %s)",
							String
								.format(
									paramTemplate,
									"",
									"[not(contains(., '[]'))]"),
							String
								.format(
									paramTemplate,
									"",
									"[contains(., '[]')]/name"));
				}
				else {
					continue;
				}
				final String filePath = AbstractCodeSmellDetection.FILEPATH_EXP
					.evaluate(thisFileNode);
				// Query to select parameters that are not used as a variable
				final String interQuery =
					String.format("%s[not(. = %s)]", paramQuery, varQuery);
				final XPathExpression interExpr =
					AbstractCodeSmellDetection.xPath.compile(interQuery);
				final NodeList funcList = (NodeList) funcExpr
					.evaluate(thisFileNode, XPathConstants.NODESET);
				final int funcLength = funcList.getLength();
				for (int j = 0; j < funcLength; j++) {
					final NodeList nodeList = (NodeList) interExpr
						.evaluate(funcList.item(j), XPathConstants.NODESET);
					final int length = nodeList.getLength();

					for (int k = 0; k < length; k++) {
						final Node thisNode = nodeList.item(k);
						final String thisParam = thisNode.getTextContent();
						final String thisFunc =
							AbstractCodeSmellDetection.FUNC_EXP
								.evaluate(thisNode);
						final String thisClass =
							AbstractCodeSmellDetection.CLASS_EXP
								.evaluate(thisNode);
						final String thisPackage =
							AbstractCodeSmellDetection.PACKAGE_EXP
								.evaluate(thisNode);

						// Check if the current method is not the main method,
						// in which case it is frequent not to use the args[]
						final String thisType = typeExpr.evaluate(thisNode);
						final boolean isStringArray = thisType.equals("String")
								&& indexExpr.evaluate(thisNode).equals("[]")
								|| thisType.equals("String[]");
						if (!(length == 1 && thisFunc.equals("main")
								&& isStringArray)) {
							unusedParamsSet
								.add(
									new MLSCodeSmell(
										this.getCodeSmellName(),
										thisParam,
										thisFunc,
										thisClass,
										thisPackage,
										filePath));
						}
					}
				}
			}
			this.setSetOfSmells(unusedParamsSet);
		}
		catch (final XPathExpressionException e) {
			e.printStackTrace();
		}
	}

	public String getCodeSmellName() {
		return "UnusedParameters";
	}

}
