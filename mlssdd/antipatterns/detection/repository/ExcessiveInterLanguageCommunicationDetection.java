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

package mlssdd.antipatterns.detection.repository;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;


import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import mlssdd.antipatterns.detection.AbstractAntiPatternDetection;
import mlssdd.antipatterns.detection.IAntiPatternDetection;
import mlssdd.kernel.impl.MLSAntiPattern;
import mlssdd.utils.PropertyGetter;

public class ExcessiveInterLanguageCommunicationDetection
		extends AbstractAntiPatternDetection implements IAntiPatternDetection {

	@Override
	public void detect(final Document xml) {
		/*
		 * UNTREATED CASE Calls in both ways: Java to C and C to Java
		 */

		XPathFactory xpathfactory = XPathFactory.newInstance();
    XPath xpath = xpathfactory.newXPath();

		final int minNbOfCallsToSameMethod = PropertyGetter
			.getIntProp(
				"ExcessiveInterLanguageCommunication.MinNbOfCallsToSameMethod",
				5);
		final int minNbOfCallsToNativeMethods = PropertyGetter
			.getIntProp(
				"ExcessiveInterLanguageCommunication.MinNbOfCallsToNativeMethods",
				20);

		final Set<MLSAntiPattern> antiPatternSet = new HashSet<>();
		final Set<MLSAntiPattern> allNativeCalls = new HashSet<>();
		// Key: (argument of a native function, Java method in which this function is called)
		// Value: anti-pattern
		final Map<AbstractMap.SimpleImmutableEntry<String, String>, MLSAntiPattern> variablesAsArguments =
			new HashMap<>();

		int nbOfNativeCalls;

		try {
			final XPathExpression CLASS_EXP = this.xPath
				.compile("ancestor::" + IAntiPatternDetection.CLASS_QUERY);
			final XPathExpression PACKAGE_EXP =
				this.xPath.compile(IAntiPatternDetection.PACKAGE_QUERY);
			final XPathExpression FILEPATH_EXP =
				this.xPath.compile(IAntiPatternDetection.FILEPATH_QUERY);
			final XPathExpression NATIVE_EXP =
				this.xPath.compile(IAntiPatternDetection.NATIVE_QUERY);
			final XPathExpression JAVA_METHOD_EXP =
				this.xPath.compile("ancestor::function/name");

			final XPathExpression loopExpr =
				this.xPath.compile("ancestor::for | ancestor::while");
			final XPathExpression argExpr =
				this.xPath.compile("argument_list/argument/expr/name");

			final NodeList javaList = (NodeList) this.xPath
				.evaluate(
					IAntiPatternDetection.JAVA_FILES_QUERY,
					xml,
					XPathConstants.NODESET);
			final int javaLength = javaList.getLength();

			for (int i = 0; i < javaLength; i++) {
				final Node javaXml = javaList.item(i);
				final String thisPackage = PACKAGE_EXP.evaluate(javaXml);
				final String filePath = FILEPATH_EXP.evaluate(javaXml);

				// Resets the count for the third case.
				// The detector assumes there is only one class per file
				// (and forgets about inner classes).
				nbOfNativeCalls = 0;
				allNativeCalls.clear();

				// Native method declaration
				final NodeList nativeDeclList = (NodeList) NATIVE_EXP
					.evaluate(javaXml, XPathConstants.NODESET);

				for (int j = 0; j < nativeDeclList.getLength(); j++) {
					XPathExpression name = xpath.compile("name");
        final String thisNativeMethod = name.evaluate(nativeDeclList.item(j));
						System.out.println(thisNativeMethod);
					final String callQuery = String
						.format(
							"descendant::call[name='%s'] | descendant::call[name/name='%s']",
							thisNativeMethod,
							thisNativeMethod);
					final NodeList callList = (NodeList) this.xPath
						.evaluate(callQuery, javaXml, XPathConstants.NODESET);
					final int nbOfCallsToThisMethod = callList.getLength();
					nbOfNativeCalls += nbOfCallsToThisMethod;

					for (int k = 0; k < nbOfCallsToThisMethod; k++) {
						System.out.println(callList.item(k).getTextContent());
						final String thisClass =
							CLASS_EXP.evaluate(callList.item(k));
						final MLSAntiPattern thisAntiPattern =
							new MLSAntiPattern(
								this.getAntiPatternName(),
								"",
								thisNativeMethod,
								thisClass,
								thisPackage,
								filePath);
						allNativeCalls.add(thisAntiPattern);

						/*
						 * FIRST CASE Too many calls to a native method
						 */
						if (nbOfCallsToThisMethod > minNbOfCallsToSameMethod) {
							antiPatternSet.add(thisAntiPattern);
						}
						else {
							// Checks whether the method is called in a loop, in which case it is
							// considered as called too many times in a first approximation
							final NodeList loops = (NodeList) loopExpr
								.evaluate(
									callList.item(k),
									XPathConstants.NODESET);
							if (loops.getLength() > 0) {
								System.out.println("third case");
								antiPatternSet.add(thisAntiPattern);
							}
						}

						/*
						 * SECOND CASE Calls to different native methods with at least one variable in
						 * common inside a Java method
						 */
						final String javaMethod =
							JAVA_METHOD_EXP.evaluate(callList.item(k));
						final NodeList argList = (NodeList) argExpr
							.evaluate(callList.item(k), XPathConstants.NODESET);
						for (int l = 0; l < argList.getLength(); l++) {
							final String var = argList.item(l).getTextContent();
							final MLSAntiPattern oldValue = variablesAsArguments
								.put(
									new AbstractMap.SimpleImmutableEntry<>(
										var,
										javaMethod),
									thisAntiPattern);
							if (oldValue != null
									&& !oldValue.equals(thisAntiPattern)
									&& oldValue
										.getClassName()
										.equals(
											thisAntiPattern.getClassName())) {
								System.out.println("second case.");
								antiPatternSet.add(oldValue);
								antiPatternSet.add(thisAntiPattern);
							}
						}
					}
				}

				/*
				* THIRD CASE Too many calls to native methods
				*/
				if (nbOfNativeCalls > minNbOfCallsToNativeMethods) {
					System.out.println("first case");
					antiPatternSet.addAll(allNativeCalls);
				}

			}

			this.setSetOfAntiPatterns(antiPatternSet);
		}
		catch (final XPathExpressionException e) {
			e.printStackTrace();
		}
	}

}
