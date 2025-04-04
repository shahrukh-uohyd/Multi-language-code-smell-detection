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

public class NotSecuringLibrariesDetection extends AbstractCodeSmellDetection
		implements ICodeSmellDetection {

	public void detect(final Document xml) {
		final Set<MLSCodeSmell> notSecureLibraries = new HashSet<>();

		// TODO System.load and System.loadLibrary: only way to load a library?
		final String loadQuery =
			"call[name = 'System.loadLibrary' or name = 'System.load']//argument";
		final String secureQuery =
			"descendant::call[name = 'AccessController.doPrivileged']//"
					+ loadQuery;

		try {
			final XPathExpression loadExpr = AbstractCodeSmellDetection.xPath
				.compile("descendant::" + loadQuery);
			final XPathExpression secureExpr =
				AbstractCodeSmellDetection.xPath.compile(secureQuery);

			final NodeList javaList =
				(NodeList) AbstractCodeSmellDetection.JAVA_FILES_EXP
					.evaluate(xml, XPathConstants.NODESET);
			final int javaLength = javaList.getLength();

			for (int i = 0; i < javaLength; i++) {
				final Node javaXml = javaList.item(i);
				final String javaFilePath =
					AbstractCodeSmellDetection.FILEPATH_EXP.evaluate(javaXml);

				final NodeList loadList = (NodeList) loadExpr
					.evaluate(javaXml, XPathConstants.NODESET);
				final NodeList secureList = (NodeList) secureExpr
					.evaluate(javaXml, XPathConstants.NODESET);
				final int loadLength = loadList.getLength();
				final int secureLength = secureList.getLength();

				// TODO Refactor the loops
				for (int j = 0; j < loadLength; j++) {
					final Node thisNode = loadList.item(j);
					final String thisLibrary = thisNode.getTextContent();
					final String thisMethod =
						AbstractCodeSmellDetection.FUNC_EXP.evaluate(thisNode);
					final String thisClass =
						AbstractCodeSmellDetection.CLASS_EXP.evaluate(thisNode);
					final String thisPackage =
						AbstractCodeSmellDetection.PACKAGE_EXP
							.evaluate(thisNode);
					notSecureLibraries
						.add(
							new MLSCodeSmell(
								this.getCodeSmellName(),
								thisLibrary,
								thisMethod,
								thisClass,
								thisPackage,
								javaFilePath));
				}
				for (int j = 0; j < secureLength; j++) {
					final Node thisNode = secureList.item(j);
					final String thisLibrary = thisNode.getTextContent();
					final String thisMethod =
						AbstractCodeSmellDetection.FUNC_EXP.evaluate(thisNode);
					final String thisClass =
						AbstractCodeSmellDetection.CLASS_EXP.evaluate(thisNode);
					final String thisPackage =
						AbstractCodeSmellDetection.PACKAGE_EXP
							.evaluate(thisNode);
					notSecureLibraries
						.remove(
							new MLSCodeSmell(
								this.getCodeSmellName(),
								thisLibrary,
								thisMethod,
								thisClass,
								thisPackage,
								javaFilePath));
				}
			}
			this.setSetOfSmells(notSecureLibraries);
		}
		catch (final XPathExpressionException e) {
			e.printStackTrace();
		}
	}

}
