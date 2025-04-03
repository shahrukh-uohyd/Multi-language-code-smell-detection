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

public class NotUsingRelativePathDetection extends AbstractCodeSmellDetection
		implements ICodeSmellDetection {

	public void detect(final Document xml) {
		final Set<MLSCodeSmell> notRelativePathsSet = new HashSet<>();

		// TODO System.load and System.loadLibrary: only way to load a library?
		final String loadQuery =
			"descendant::call[name = 'System.loadLibrary' or name = 'System.load']//argument//literal";

		try {
			final XPathExpression loadExpr =
				AbstractCodeSmellDetection.xPath.compile(loadQuery);

			final NodeList javaList =
				(NodeList) AbstractCodeSmellDetection.JAVA_FILES_EXP
					.evaluate(xml, XPathConstants.NODESET);
			final int javaLength = javaList.getLength();

			for (int i = 0; i < javaLength; i++) {
				final NodeList loadList = (NodeList) loadExpr
					.evaluate(javaList.item(i), XPathConstants.NODESET);
				final int loadLength = loadList.getLength();
				for (int j = 0; j < loadLength; j++) {
					final Node thisLoad = loadList.item(j);
					final String lib = thisLoad.getTextContent();
					if (lib.charAt(1) != '.' && lib.charAt(1) != '/') {
						final String thisMethod =
							AbstractCodeSmellDetection.FUNC_EXP
								.evaluate(thisLoad);
						final String thisClass =
							AbstractCodeSmellDetection.CLASS_EXP
								.evaluate(thisLoad);
						final String thisPackage =
							AbstractCodeSmellDetection.PACKAGE_EXP
								.evaluate(thisLoad);
						final String javaFilePath =
							AbstractCodeSmellDetection.FILEPATH_EXP
								.evaluate(javaList.item(i));
						notRelativePathsSet
							.add(
								new MLSCodeSmell(
									this.getCodeSmellName(),
									lib,
									thisMethod,
									thisClass,
									thisPackage,
									javaFilePath));
					}
				}
			}
			this.setSetOfSmells(notRelativePathsSet);
		}
		catch (final XPathExpressionException e) {
			e.printStackTrace();
		}
	}

}
