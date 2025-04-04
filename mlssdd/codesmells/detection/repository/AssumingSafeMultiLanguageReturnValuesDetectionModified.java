

package mlssdd.codesmells.detection.repository;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
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

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

public class AssumingSafeMultiLanguageReturnValuesDetectionModified
		extends AbstractCodeSmellDetection implements ICodeSmellDetection {

	public void detect(final Document xml) {

		XPathFactory xpathfactory = XPathFactory.newInstance();
    XPath xpath = xpathfactory.newXPath();
		/**
		 * A JNI method is called and its result is not checked, that is to say there is
		 * neither any condition on its value nor an exception check.
		 */

		final String codeSmellName = this.getCodeSmellName();

		final Set<String> methods = new HashSet<>(
			Arrays
				.asList(
					"FindClass",
					"GetFieldID",
					"GetStaticFieldID",
					"GetMethodID",
					"GetStaticMethodID"));
		final Set<String> exceptions =
			new HashSet<>(Arrays.asList("ExceptionOccurred", "ExceptionCheck"));
		final Set<MLSCodeSmell> notCheckedSet = new HashSet<>();

		try {
			final String argQuery =
				"descendant::call/argument_list/argument[%d]/expr/name | descendant::call/argument_list/argument[%d]/expr/literal";
			final XPathExpression ifExpr = AbstractCodeSmellDetection.xPath
				.compile("descendant::if/condition");
			final XPathExpression firstArgExpr =
				AbstractCodeSmellDetection.xPath
					.compile(String.format(argQuery, 1, 1));
			final XPathExpression secondArgExpr =
				AbstractCodeSmellDetection.xPath
					.compile(String.format(argQuery, 2, 2));
			final XPathExpression thirdArgExpr =
				AbstractCodeSmellDetection.xPath
					.compile(String.format(argQuery, 3, 3));

			// Native functions that look up an ID
			final List<String> selectorList = new LinkedList<>();
			final List<String> exceptSelectorList = new LinkedList<>();
			for (final String method : methods) {
				selectorList
					.add(
						String
							.format(
								"descendant::call/name/name = '%s'",
								method));
			}
			for (final String exception : exceptions) {
				exceptSelectorList.add(String.format(". = '%s'", exception));
			}
			final String selector = String.join(" or ", selectorList);
			final String exceptSelector =
				String.join(" or ", exceptSelectorList);

			final String declQuery = String
				.format(
					"descendant::decl_stmt[%s]/decl | descendant::expr_stmt[%s]/expr",
					selector,
					selector);
			final String exceptQuery = String
				.format(
					"descendant::if/condition/expr/call/name/name[%s]",
					exceptSelector);

			final XPathExpression declExpr =
				AbstractCodeSmellDetection.xPath.compile(declQuery);
			final XPathExpression exceptExpr =
				AbstractCodeSmellDetection.xPath.compile(exceptQuery);

			final NodeList cList =
				(NodeList) AbstractCodeSmellDetection.C_FILES_EXP
					.evaluate(xml, XPathConstants.NODESET);
			final int cLength = cList.getLength();

			for (int i = 0; i < cLength; i++) {
				final Node cXml = cList.item(i);
				final String cFilePath =
					AbstractCodeSmellDetection.FILEPATH_EXP.evaluate(cXml);
				final boolean isC = AbstractCodeSmellDetection.LANGUAGE_EXP
					.evaluate(cXml)
					.equals("C");

				//extract functions to find the function returns the variable or not.
				//static linking of native methods
		    	XPathExpression func = xpath.compile("descendant::function");
		    	NodeList Cfunctions = (NodeList) func.evaluate(cXml, XPathConstants.NODESET);

		    	for(int c=0; c<Cfunctions.getLength(); c++)
		    	{
		    		Node thisFunction = Cfunctions.item(c);
		    		final NodeList declList =
					(NodeList) declExpr.evaluate(thisFunction, XPathConstants.NODESET);
					final NodeList exceptList = (NodeList) exceptExpr
						.evaluate(thisFunction, XPathConstants.NODESET);
					final NodeList ifList =
						(NodeList) ifExpr.evaluate(thisFunction, XPathConstants.NODESET);
					final int exceptLength = exceptList.getLength();
					final int ifLength = ifList.getLength();

					for (int j = 0; j < declList.getLength(); j++) {
						final Node thisDecl = declList.item(j);
						System.out.println(thisDecl.getTextContent());
						final String var =
							AbstractCodeSmellDetection.NAME_EXP.evaluate(thisDecl);
							System.out.println(var);
						String arg;

						// C file
						if (isC) {
							arg = thirdArgExpr.evaluate(thisDecl);
							if (arg.equals("")) {
								arg = secondArgExpr.evaluate(thisDecl);
							}
						}

						// C++ file
						else {
							arg = secondArgExpr.evaluate(thisDecl);
							if (arg.equals("")) {
								arg = firstArgExpr.evaluate(thisDecl);
							}
						}

						//check whether the variable was return by the function
						XPathExpression r = xpath.compile("descendant::return/expr/name");
						String returnedVariable = r.evaluate(thisFunction);
						System.out.println("returnedVariable: "+returnedVariable);
						if(!returnedVariable.equals("") && returnedVariable.equals(var)) //means returns now check conditions
						{
							boolean isNotChecked = true;

							// Check if there is a condition on the variable
							final XPathExpression usedVarExpr =
								AbstractCodeSmellDetection.xPath
									.compile(String.format("expr/name[. = '%s']", var));
							for (int k = 0; k < ifLength; k++) {
								final boolean isCorrectVar =
									usedVarExpr.evaluate(ifList.item(k)).equals(var);
								final boolean conditionIsAfterDeclaration = thisDecl
									.compareDocumentPosition(
										ifList
											.item(
												k)) == Node.DOCUMENT_POSITION_FOLLOWING;
								if (isCorrectVar && conditionIsAfterDeclaration) {
									isNotChecked = false;
								}

							}

							// Check if the exception is handled
							for (int k = 0; k < exceptLength; k++) {
								if (declList
									.item(j)
									.compareDocumentPosition(
										exceptList
											.item(
												k)) == Node.DOCUMENT_POSITION_FOLLOWING) {
									isNotChecked = false;
								}
							}

							if (isNotChecked) {
								final String function =
									AbstractCodeSmellDetection.FUNC_EXP
										.evaluate(declList.item(j));
								final String className =
									AbstractCodeSmellDetection.CLASS_EXP
										.evaluate(declList.item(j));
								notCheckedSet
									.add(
										new MLSCodeSmell(
											codeSmellName,
											arg,
											function,
											className,
											"",
											cFilePath));
							}
						}

						
					}	
		    	}

				
			}

			this.setSetOfSmells(notCheckedSet);
		}
		catch (final XPathExpressionException e) {
			e.printStackTrace();
		}
	}

}
