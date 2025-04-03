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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
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

public class MemoryManagementMismatchDetection
		extends AbstractCodeSmellDetection implements ICodeSmellDetection {

	public void detect(final Document xml) {
		final Set<MLSCodeSmell> notReleasedSet = new HashSet<>();

		final List<String> types = Arrays
			.asList(
				"StringChars",
				"StringUTFChars",
				"BooleanArrayElements",
				"ByteArrayElements",
				"CharArrayElements",
				"ShortArrayElements",
				"IntArrayElements",
				"LongArrayElements",
				"FloatArrayElements",
				"DoubleArrayElements",
				"PrimitiveArrayCritical",
				"StringCritical");

		final String genericCallQuery = "descendant::call[name/name='%s']";
		final String argQuery = "argument_list/argument[%d]/expr/name";
		final String nodeWithGivenArg =
			"%s[argument_list/argument[%d]/expr/name='%s']";

		try {
			final XPathExpression firstArgExpr =
				AbstractCodeSmellDetection.xPath
					.compile(String.format(argQuery, 1));
			final XPathExpression secondArgExpr =
				AbstractCodeSmellDetection.xPath
					.compile(String.format(argQuery, 2));
			final XPathExpression funcExpr = AbstractCodeSmellDetection.xPath
				.compile("descendant::function");

			final NodeList cList =
				(NodeList) AbstractCodeSmellDetection.C_FILES_EXP
					.evaluate(xml, XPathConstants.NODESET);
			final int cLength = cList.getLength();

			for (int i = 0; i < cLength; i++) {
				final Node cXml = cList.item(i);
				final String cFilePath =
					AbstractCodeSmellDetection.FILEPATH_EXP.evaluate(cXml);

				// C: second argument
				// C++: first argument
				final boolean isC = AbstractCodeSmellDetection.LANGUAGE_EXP
					.evaluate(cXml)
					.equals("C");
				int nbArg;
				XPathExpression argExpr;
				if (isC) { // C file
					nbArg = 2;
					argExpr = secondArgExpr;
				}
				else { // C ++ file
					nbArg = 1;
					argExpr = firstArgExpr;
				}
				final NodeList funcList =
					(NodeList) funcExpr.evaluate(cXml, XPathConstants.NODESET);
				final int funcLength = funcList.getLength();
				// Analysis for each function
				for (int j = 0; j < funcLength; j++) {
					final Node thisFunction = funcList.item(j);
					final String funcName = AbstractCodeSmellDetection.NAME_EXP
						.evaluate(thisFunction);
					// Analysis for each type
					final Iterator<String> it = types.iterator();
					while (it.hasNext()) {
						final String thisType = it.next();

						final String getType = String.format("Get%s", thisType);
						final String releaseType =
							String.format("Release%s", thisType);

						final String getCallQuery =
							String.format(genericCallQuery, getType);
						final String releaseCallQuery =
							String.format(genericCallQuery, releaseType);

						final NodeList getList =
							(NodeList) AbstractCodeSmellDetection.xPath
								.evaluate(
									getCallQuery,
									thisFunction,
									XPathConstants.NODESET);

						final MLSCodeSmell codeSmell = new MLSCodeSmell(
							this.getCodeSmellName(),
							thisType,
							funcName,
							"",
							"",
							cFilePath);

						// Look for a call to the matching release function
						// The second argument should match (name of the Java object)
						for (int k = 0; k < getList.getLength(); k++) {
							// TODO Second argument in C, but first in C++?
							final String arg =
								argExpr.evaluate(getList.item(k));
							final String nodeWithGivenArgQuery = String
								.format(
									nodeWithGivenArg,
									releaseCallQuery,
									nbArg,
									arg);
							final String matchedRelease =
								AbstractCodeSmellDetection.xPath
									.evaluate(
										nodeWithGivenArgQuery,
										thisFunction);
							if (matchedRelease == "") {
								notReleasedSet.add(codeSmell);
							}
						}

					}
				}
			}
			this.setSetOfSmells(notReleasedSet);

		}
		catch (

		final XPathExpressionException e) {
			e.printStackTrace();
		}
	}

}
