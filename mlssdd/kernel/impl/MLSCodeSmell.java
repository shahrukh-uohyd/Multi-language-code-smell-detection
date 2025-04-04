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

package mlssdd.kernel.impl;

import java.util.Arrays;
import java.util.List;

public class MLSCodeSmell {

	private String codeSmellName;

	private String variableName;
	private String methodName;
	private String className;
	private String packageName;
	private String filePath;

	public MLSCodeSmell(final String aMethod, final String aClass) {
		this.methodName = aMethod;
		this.className = aClass;
	}

	public MLSCodeSmell(
		final String aCodeSmell,
		final String aVariable,
		final String aMethod,
		final String aClass,
		final String aPackage,
		final String aPath) {
		this.codeSmellName = aCodeSmell;
		this.variableName = aVariable;
		this.methodName = aMethod;
		this.className = aClass;
		this.packageName = aPackage;
		this.filePath = aPath;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null) {
			return false;
		}

		if (!(o instanceof MLSCodeSmell)) {
			return false;
		}

		if (o == this) {
			return true;
		}

		return this
			.getElementsList()
			.equals(((MLSCodeSmell) o).getElementsList());
	}

	public String getClassName() {
		return this.className;
	}

	public String getCodeSmellName() {
		return this.codeSmellName;
	}

	protected List<String> getElementsList() {
		return Arrays
			.asList(
				this.packageName,
				this.className,
				this.methodName,
				this.variableName);
	}

	public String getFileName() {
		final String[] parts = this.filePath.split("[\\/\\\\]");
		return parts[parts.length - 1];
	}

	public String getFilePath() {
		return this.filePath;
	}

	public String getMethodName() {
		return this.methodName;
	}

	public String getPackageName() {
		return this.packageName;
	}

	public String getVariableName() {
		return this.variableName;
	}

	@Override
	public int hashCode() {
		return this.getElementsList().hashCode();
	}

	public void setAntiPatternName(final String antiPatternName) {
		this.codeSmellName = antiPatternName;
	}

	public void setClassName(final String className) {
		this.className = className;
	}

	public void setFilePath(final String filePath) {
		this.filePath = filePath;
	}

	public void setMethodName(final String methodName) {
		this.methodName = methodName;
	}

	public void setPackageName(final String packageName) {
		this.packageName = packageName;
	}

	public void setVariableName(final String variableName) {
		this.variableName = variableName;
	}

	public String toCSVLine() {
		final List<String> elements = Arrays
			.asList(
				this.codeSmellName,
				this.variableName,
				this.methodName,
				this.className,
				this.packageName,
				this.filePath,
				this.getFileName());
		return String.join(",", elements);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("{ Code Smell \"");
		sb.append(this.codeSmellName);
		sb.append("\"");
		if (!this.variableName.isEmpty()) {
			sb.append("\n\tVariable: ");
			sb.append(this.variableName);
		}
		if (!this.methodName.isEmpty()) {
			sb.append("\n\tMethod: ");
			sb.append(this.methodName);
		}
		if (!this.className.isEmpty()) {
			sb.append("\n\tClass: ");
			sb.append(this.className);
		}
		if (!this.packageName.isEmpty()) {
			sb.append("\n\tPackage: ");
			sb.append(this.packageName);
		}
		if (!this.filePath.isEmpty()) {
			sb.append("\n\tFile path: ");
			sb.append(this.filePath);
		}
		final String fileName = this.getFileName();
		if (!fileName.isEmpty()) {
			sb.append("\n\tFile name: ");
			sb.append(fileName);
		}
		sb.append("\n}");
		return sb.toString();
	}

}
