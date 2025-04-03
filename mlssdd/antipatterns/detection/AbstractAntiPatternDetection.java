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

package mlssdd.antipatterns.detection;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Set;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;

import mlssdd.kernel.impl.MLSAntiPattern;

public abstract class AbstractAntiPatternDetection {
	protected XPath xPath = XPathFactory.newInstance().newXPath();

	private Set<MLSAntiPattern> setOfAntiPatterns;

	/**
	 * Sets the set of anti-patterns to the anti-patterns detected in the
	 * document.
	 *
	 * @param	xml	srcML representation of the source code to
	 * analyze
	 */
	public abstract void detect(final Document xml);

	public String getAntiPatternName() {
		final String name = this.getName();
		return name.substring(0, name.length() - "Detection".length());
	}

	public Set<MLSAntiPattern> getAntiPatterns() {
		return this.setOfAntiPatterns;
	}

	public String getHelpURL() {
		return "";
	}

	public String getName() {
		return this.getClass().getSimpleName();
	}

	/**
	 * Adds a line for each anti-pattern detected, starting with ID 0.
	 *
	 * @param	aWriter	PrintWriter in which to add the line
	 */
	public void output(final PrintWriter aWriter) {
		this.output(aWriter, 0);
	}

	/**
	 * Adds a line for each anti-pattern detected.
	 *
	 * @param	aWriter	PrintWriter in which to add the line
	 * @param	count	ID number of the first anti-pattern
	 */
	public void output(final PrintWriter aWriter, int count) {
		try {
			final Iterator<MLSAntiPattern> iter =
				this.getAntiPatterns().iterator();
			while (iter.hasNext()) {
				final MLSAntiPattern antiPattern = iter.next();
				count++;
				aWriter.println("AP" + count + "," + antiPattern.toCSVLine());
			}
		}
		catch (final NumberFormatException e) {
			e.printStackTrace();
		}
	}

	protected void setSetOfAntiPatterns(
		final Set<MLSAntiPattern> setOfAntiPatterns) {
		this.setOfAntiPatterns = setOfAntiPatterns;
	}
}
