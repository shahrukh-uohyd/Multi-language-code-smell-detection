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

package mlssdd.codesmells.detection;

import java.io.PrintWriter;
import java.util.Set;
import org.w3c.dom.Document;

import mlssdd.kernel.impl.MLSCodeSmell;

//import util.help.IHelpURL;

public interface ICodeSmellDetection /* extends IHelpURL */ {
	final String FILE_QUERY = "//unit";

	final String C_FILES_QUERY = "//unit[@language='C++' or @language='C']";

	final String JAVA_FILES_QUERY = "//unit[@language='Java']";

	final String LANGUAGE_QUERY = "@language"; // Call on unit
	final String FUNC_QUERY = "ancestor::function/name";
	final String CLASS_QUERY =
		"ancestor::class/name | ancestor::interface/name";
	final String PACKAGE_QUERY = "ancestor::unit/package/name | ancestor::unit/namespace/name";
	final String FILEPATH_QUERY = "@filename"; // Call on unit
	final String NAME_QUERY = "name";
	final String NATIVE_DECL_QUERY =
		"descendant::function_decl[type/specifier='native']/name";
	final String IMPL_QUERY = "descendant::function/name";
	final String HOST_CALL_QUERY = "descendant::call//name[last()]";
	void detect(final Document xml);
	String getCodeSmellName();
	Set<MLSCodeSmell> getCodeSmells();

	String getName();

	void output(final PrintWriter aWriter);

	void output(final PrintWriter aWriter, int count);
}
