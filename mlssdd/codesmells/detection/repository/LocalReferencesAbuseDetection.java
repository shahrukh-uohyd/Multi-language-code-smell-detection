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
import java.util.LinkedList;
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
import mlssdd.utils.PropertyGetter;

public class LocalReferencesAbuseDetection extends AbstractCodeSmellDetection
		implements ICodeSmellDetection {

	public void detect(final Document xml) {
		// TODO jint EnsureLocalCapacity(JNIEnv *env, jint capacity);
		// jint PushLocalFrame(JNIEnv *env, jint capacity);
		// jobject PopLocalFrame(JNIEnv *env, jobject result);
		// jobject NewLocalRef(JNIEnv *env, jobject ref);

		/*
		 * GetObjectArrayElement PopLocalFrame (special) NewLocalRef NewWeakGlobalRef ?
		 * AllocObject NewObject NewObjectA NewObjectV NewDirectByteBuffer
		 * ToReflectedMethod ToReflectedField
		 */

		// final int minNbOfRefs =
		// 	PropertyGetter.getIntProp("LocalReferencesAbuse.MinNbOfRefs", 20);
		int nbOfRefsOutsideLoops = 0;
		final Set<MLSCodeSmell> refSet = new HashSet<>();
		final Set<MLSCodeSmell> refsInLoopSet = new HashSet<>();

		final List<String> jniFunctions = Arrays
			.asList(
				"GetObjectArrayElement",
				"NewLocalRef",
				"AllocObject",
				"NewObject",
				"NewObjectA",
				"NewObjectV",
				"NewDirectByteBuffer",
				"ToReflectedMethod",
				"ToReflectedField");
		final List<String> selectorList = new LinkedList<>();
		for (final String jniFunction : jniFunctions) {
			selectorList
				.add(
					String
						.format(
							"descendant::call/name/name = '%s'",
							jniFunction));
		}
		final String selector = String.join(" or ", selectorList);
		// TODO What if a function is called as the argument of another function
		// e.g.: GetObjectRefType(env, GetObjectArrayElement(...));
		final String declQuery = String
			.format(
				"descendant::decl_stmt[%s]/decl/name | descendant::expr_stmt[%s]/expr/name",
				selector,
				selector);
		final String funcQuery = "descendant::function";
		final String deleteQuery =
			"descendant::call[name/name='DeleteLocalRef' and argument_list/argument[last()]/expr/name='%s']";

		try {
			final XPathExpression declExpr =
				AbstractCodeSmellDetection.xPath.compile(declQuery);
			final XPathExpression funcExpr =
				AbstractCodeSmellDetection.xPath.compile(funcQuery);
			final XPathExpression loopExpr = AbstractCodeSmellDetection.xPath
				.compile("ancestor::for | ancestor::while");

			final NodeList cList =
				(NodeList) AbstractCodeSmellDetection.C_FILES_EXP
					.evaluate(xml, XPathConstants.NODESET);
			final int cLength = cList.getLength();

			for (int i = 0; i < cLength; i++) {
				final Node cXml = cList.item(i);
				final String cFilePath =
					AbstractCodeSmellDetection.FILEPATH_EXP.evaluate(cXml);

				final NodeList funcList =
					(NodeList) funcExpr.evaluate(cXml, XPathConstants.NODESET);
				final int funcLength = funcList.getLength();

				// Analysis for each function
				for (int j = 0; j < funcLength; j++) {
					final Node thisJNIFunction = funcList.item(j);
					final NodeList declList = (NodeList) declExpr
						.evaluate(thisJNIFunction, XPathConstants.NODESET);
					for (int k = 0; k < declList.getLength(); k++) {
						final Node thisDecl = declList.item(k);
						final String var = thisDecl.getTextContent();
						final String thisFunction =
							AbstractCodeSmellDetection.FUNC_EXP
								.evaluate(thisDecl);
						final MLSCodeSmell codeSmell = new MLSCodeSmell(
							this.getCodeSmellName(),
							var,
							thisFunction,
							"",
							"",
							cFilePath);

						// If the reference is in a loop, check whether it is deleted in the loop
						// There can be several nested loops
						// TODO Look only inside the innermost loop?
						final XPathExpression deleteVarExpr =
							AbstractCodeSmellDetection.xPath
								.compile(String.format(deleteQuery, var));
						final NodeList loops = (NodeList) loopExpr
							.evaluate(thisDecl, XPathConstants.NODESET);
						boolean refIsDeletedInsideLoop = false;
						for (int l = 0; l < loops.getLength(); l++) {
							if (!deleteVarExpr
								.evaluate(loops.item(l))
								.equals("")) {
								refIsDeletedInsideLoop = true;
								break;
							}
						}

						// If the reference is not deleted
						if (!refIsDeletedInsideLoop) {
							if (loops.getLength() > 0) {
								refsInLoopSet.add(codeSmell);
							}
							else if (deleteVarExpr
								.evaluate(thisJNIFunction)
								.equals("")) {
								nbOfRefsOutsideLoops++;
								refSet.add(codeSmell);
							}
						}

					}
				}
			}
			//if (nbOfRefsOutsideLoops > minNbOfRefs) {
			if (nbOfRefsOutsideLoops > 20) {
				refsInLoopSet.addAll(refSet);
			}
			this.setSetOfSmells(refsInLoopSet);

		}
		catch (final XPathExpressionException e) {
			e.printStackTrace();
		}
	}

}
