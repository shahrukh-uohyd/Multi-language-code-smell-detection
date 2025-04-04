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

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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

public class TooMuchScatteringDetection extends AbstractAntiPatternDetection
		implements IAntiPatternDetection {

	@Override
	public void detect(final Document xml) {
		final int minNbOfClasses =
			PropertyGetter.getIntProp("TooMuchScattering.MinNbOfClasses", 2);
		final int maxNbOfMethods =
			PropertyGetter.getIntProp("TooMuchScattering.MaxNbOfMethods", 5);

		final Set<MLSAntiPattern> shortClassesSet = new HashSet<>();

		try {
			final XPathExpression JAVA_FILES_EXP = this.xPath
				.compile(
					IAntiPatternDetection.JAVA_FILES_QUERY
							+ "[package/name != '']");
			final XPathExpression PACKAGE_EXP =
				this.xPath.compile(IAntiPatternDetection.PACKAGE_QUERY);
			final XPathExpression CLASS_EXP =
				this.xPath.compile(IAntiPatternDetection.CLASS_QUERY);
			final XPathExpression FILEPATH_EXP =
				this.xPath.compile(IAntiPatternDetection.FILEPATH_QUERY);
			final XPathExpression NATIVE_EXP =
				this.xPath.compile(IAntiPatternDetection.NATIVE_QUERY);

			final Map<String, List<MLSAntiPattern>> classesInPackage =
				new HashMap<>();

			final NodeList javaList =
				(NodeList) JAVA_FILES_EXP.evaluate(xml, XPathConstants.NODESET);
			final int javaLength = javaList.getLength();
			for (int i = 0; i < javaLength; i++) {
				final Node javaXml = javaList.item(i);
				// Native method declaration
				final NodeList nativeDeclList = (NodeList) NATIVE_EXP
					.evaluate(javaXml, XPathConstants.NODESET);
				final int nativeDeclLength = nativeDeclList.getLength();
				System.out.println(nativeDeclLength);
				if (nativeDeclLength > 0
						&& nativeDeclLength <= 5) {
					final String thisClass = CLASS_EXP.evaluate(javaXml);
					final String thisPackage = PACKAGE_EXP.evaluate(javaXml);
					final String thisFilePath = FILEPATH_EXP.evaluate(javaXml);
					final MLSAntiPattern ap = new MLSAntiPattern(
						this.getAntiPatternName(),
						"",
						"",
						thisClass,
						thisPackage,
						thisFilePath);
					System.out.println(thisClass+", "+thisPackage);
					if (!classesInPackage.containsKey(thisPackage)) {
						classesInPackage.put(thisPackage, new LinkedList<>());
					}
					classesInPackage.get(thisPackage).add(ap);
					//System.out.println(classesInPackage);
				}
			}

			for (final Map.Entry<String, List<MLSAntiPattern>> entry : classesInPackage
				.entrySet()) {
				if (entry.getValue().size() >= 2) {
					
					entry.getValue().forEach(shortClassesSet::add);
					
				}
			}
			//System.out.println(shortClassesSet);
			this.setSetOfAntiPatterns(shortClassesSet);

		}
		catch (final XPathExpressionException e) {
			e.printStackTrace();
		}
	}

}
