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

import java.util.HashSet;
import java.util.Set;
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

public class TooMuchClusteringDetection extends AbstractAntiPatternDetection
		implements IAntiPatternDetection {

	@Override
	public void detect(final Document xml) {
		final int minNbOfMethodsPerClass = PropertyGetter
			.getIntProp("TooMuchClustering.MinNbOfMethodsPerClass", 6);

		final Set<MLSAntiPattern> antiPatternSet = new HashSet<>();

		try {
			final XPathExpression PACKAGE_EXP = this.xPath
				.compile(
					"ancestor::unit/" + IAntiPatternDetection.PACKAGE_QUERY);
			final XPathExpression FILEPATH_EXP = this.xPath
				.compile(
					"ancestor::unit/" + IAntiPatternDetection.FILEPATH_QUERY);
			final XPathExpression NATIVE_EXP =
				this.xPath.compile(IAntiPatternDetection.NATIVE_QUERY);
			final XPathExpression NAME_EXP = this.xPath.compile("name");

			// Java classes
			final NodeList classList = (NodeList) this.xPath
				.evaluate("descendant::class", xml, XPathConstants.NODESET);
			final int nbClasses = classList.getLength();

			for (int j = 0; j < nbClasses; j++) {
				final Node thisClassNode = classList.item(j);
				// Native method declaration
				final NodeList nativeDeclList = (NodeList) NATIVE_EXP
					.evaluate(thisClassNode, XPathConstants.NODESET);
					System.out.println("number of native methods: "+nativeDeclList.getLength());
				if (nativeDeclList.getLength() > minNbOfMethodsPerClass) {
					final String thisClass = NAME_EXP.evaluate(thisClassNode);
					final String thisPackage =
						PACKAGE_EXP.evaluate(thisClassNode);
					final String thisFilePath =
						FILEPATH_EXP.evaluate(thisClassNode);
					antiPatternSet
						.add(
							new MLSAntiPattern(
								this.getAntiPatternName(),
								"",
								"",
								thisClass,
								thisPackage,
								thisFilePath));
				}
			}
			//			}
			this.setSetOfAntiPatterns(antiPatternSet);
		}
		catch (final XPathExpressionException e) {
			e.printStackTrace();
		}
	}

}
