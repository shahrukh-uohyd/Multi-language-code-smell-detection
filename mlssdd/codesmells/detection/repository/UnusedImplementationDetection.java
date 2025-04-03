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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.xml.xpath.XPathConstants;
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

public class UnusedImplementationDetection extends AbstractCodeSmellDetection
		implements ICodeSmellDetection {

	public void detect(final Document xml) {

		XPathFactory xpathfactory = XPathFactory.newInstance();
    XPath xpath = xpathfactory.newXPath();
		final Map<String, MLSCodeSmell> unusedImplMap = new HashMap<>();

		try {
			final NodeList cList =
				(NodeList) AbstractCodeSmellDetection.C_FILES_EXP
					.evaluate(xml, XPathConstants.NODESET);
			final NodeList javaList =
				(NodeList) AbstractCodeSmellDetection.JAVA_FILES_EXP
					.evaluate(xml, XPathConstants.NODESET);
			final int cLength = cList.getLength();
			final int javaLength = javaList.getLength();

			final Set<String> implSet = new HashSet<>();
			final Set<String> callSet = new HashSet<>();

			// List native methods declared but never called from host language
			for (int i = 0; i < javaLength; i++) {
				final Node javaFile = javaList.item(i);
				final String javaFilePath =
					AbstractCodeSmellDetection.FILEPATH_EXP.evaluate(javaFile);

				XPathExpression e5 = xpath.compile("descendant::function_decl[type/specifier='native']/name");
      			final NodeList declList = (NodeList) e5.evaluate(javaFile, XPathConstants.NODESET);
				final int declLength = declList.getLength();
				final NodeList callList =
					(NodeList) AbstractCodeSmellDetection.HOST_CALL_EXP
						.evaluate(javaFile, XPathConstants.NODESET);
				
				final int callLength = callList.getLength();
				System.out.println(declLength);
				for (int j = 0; j < declLength; j++) {
					final Node thisNode = declList.item(j);
					final String thisNativeFunction = thisNode.getTextContent();
					final String thisClass =
						AbstractCodeSmellDetection.CLASS_EXP.evaluate(thisNode);
					final String thisPackage =
						AbstractCodeSmellDetection.PACKAGE_EXP
							.evaluate(thisNode);
					unusedImplMap
						.put(
							thisNativeFunction,
							new MLSCodeSmell(
								this.getCodeSmellName(),
								"",
								thisNativeFunction,
								thisClass,
								thisPackage,
								javaFilePath));
				}
				for (int j = 0; j < callLength; j++) {
					callSet.add(callList.item(j).getTextContent());
				}
			}

			// Check whether they are implemented
			for (int i = 0; i < cLength; i++) {
				final NodeList implList =
					(NodeList) AbstractCodeSmellDetection.IMPL_EXP
						.evaluate(cList.item(i), XPathConstants.NODESET);
				final int implLength = implList.getLength();
				for (int j = 0; j < implLength; j++) {
					// WARNING: This only keeps the part of the function name after the last
					// underscore ("_") in respect to JNI syntax. Therefore, it does not work for
					// functions with _ in their names. This should not happen if names are written
					// in lowerCamelCase.
					final String[] partsOfName =
						implList.item(j).getTextContent().split("_");
					implSet.add(partsOfName[partsOfName.length - 1]);
				}
			}
			unusedImplMap.keySet().retainAll(implSet);
			unusedImplMap.keySet().removeAll(callSet);
			this.setSetOfSmells(new HashSet<>(unusedImplMap.values()));
		}
		catch (final XPathExpressionException e) {
			e.printStackTrace();
		}
	}

}
