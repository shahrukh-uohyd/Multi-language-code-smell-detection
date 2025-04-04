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
import java.util.*;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.*;
import javax.xml.xpath.XPathConstants;
import javax.xml.XMLConstants;

import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import mlssdd.codesmells.detection.AbstractCodeSmellDetection;
import mlssdd.codesmells.detection.ICodeSmellDetection;
import mlssdd.kernel.impl.MLSCodeSmell;

public class NotUsingSafePoints extends AbstractCodeSmellDetection
		implements ICodeSmellDetection {

	public void detect(final Document xml) {

		XPathFactory xpathfactory = XPathFactory.newInstance();
		try
		{
			xpathfactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false); // Disable secure processing
		} catch (Exception e) {
            e.printStackTrace();
        }
    XPath xpath = xpathfactory.newXPath();

		final Set<MLSCodeSmell> Not_Using_Safe_Points = new HashSet<>();
		String paramQuery;
		final String funcQuery = "descendant::function";
		// Query to select variables used in a function
		final String varQuery = "ancestor::function//expr//name";

		try {
			final XPathExpression funcExpr =
				AbstractCodeSmellDetection.xPath.compile(funcQuery);
			final XPathExpression typeExpr = AbstractCodeSmellDetection.xPath
				.compile("../type | ../../type");
			final XPathExpression indexExpr =
				AbstractCodeSmellDetection.xPath.compile("../../name/index");

			// final NodeList fileList =
			// 	(NodeList) AbstractCodeSmellDetection.FILE_EXP
			// 		.evaluate(xml, XPathConstants.NODESET);
			
			//C/C++ files.
		    XPathExpression fn= xpath.compile("//unit[@language='C'] | //unit[@language='C++']");
		    final NodeList fileList = (NodeList) fn.evaluate(xml, XPathConstants.NODESET);

			final int fileLength = fileList.getLength();
			for (int i = 0; i < fileLength; i++) {
				final Node thisFileNode = fileList.item(i);
				// final String language = AbstractCodeSmellDetection.LANGUAGE_EXP
				// 	.evaluate(thisFileNode);
				final String paramTemplate =
					"parameter_list/parameter%s/decl/name%s";
				

				//if (language.equals("C") || language.equals("C++")) {
					// Skip first two parameters, which are the JNI environment and the class for a
					// static method or the object for a non-static method
					paramQuery =
						String.format(paramTemplate, "[position()>2]", "");
				//}
				// else if (language.equals("Java")) {
				// 	// Select the parameters, excluding '[]' from the name of arrays
				// 	paramQuery = String
				// 		.format(
				// 			"(%s | %s)",
				// 			String
				// 				.format(
				// 					paramTemplate,
				// 					"",
				// 					"[not(contains(., '[]'))]"),
				// 			String
				// 				.format(
				// 					paramTemplate,
				// 					"",
				// 					"[contains(., '[]')]/name"));
				// }
				// else {
				// 	continue;
				// }
				final String filePath = AbstractCodeSmellDetection.FILEPATH_EXP
					.evaluate(thisFileNode);
				// Query to select parameters that are not used as a variable
				final String interQuery =
					String.format("%s[not(. = %s)]", paramQuery, varQuery);
				final XPathExpression interExpr =
					AbstractCodeSmellDetection.xPath.compile(interQuery);
				
				//this selects all function, modifying it to select only native functions which starts with JNIEXPORT
				// final NodeList funcList = (NodeList) funcExpr
				// 	.evaluate(thisFileNode, XPathConstants.NODESET);

				XPathExpression e5 = xpath.compile("descendant::function[type/name='JNIEXPORT']");
			    final NodeList funcList = (NodeList) e5.evaluate(thisFileNode, XPathConstants.NODESET);

					
				final int funcLength = funcList.getLength();
				for (int j = 0; j < funcLength; j++) {
					// final NodeList nodeList = (NodeList) interExpr
					// 	.evaluate(funcList.item(j), XPathConstants.NODESET);
					// final int length = nodeList.getLength();

					// for (int k = 0; k < length; k++) {
					// 	final Node thisNode = nodeList.item(k);
					// 	final String thisParam = thisNode.getTextContent();
						// final String thisFunc =
						// 	AbstractCodeSmellDetection.FUNC_EXP
						// 		.evaluate(thisNode);
						// final String thisClass =
						// 	AbstractCodeSmellDetection.CLASS_EXP
						// 		.evaluate(thisNode);
						// final String thisPackage =
						// 	AbstractCodeSmellDetection.PACKAGE_EXP
						// 		.evaluate(thisNode);

					// 	// Check if the current method is not the main method,
					// 	// in which case it is frequent not to use the args[]
					// 	final String thisType = typeExpr.evaluate(thisNode);
					// 	final boolean isStringArray = thisType.equals("String")
					// 			&& indexExpr.evaluate(thisNode).equals("[]")
					// 			|| thisType.equals("String[]");
					// 	if (!(length == 1 && thisFunc.equals("main")
					// 			&& isStringArray)) {
					// 		Not_Using_Safe_Points
					// 			.add(
					// 				new MLSCodeSmell(
					// 					this.getCodeSmellName(),
					// 					thisParam,
					// 					thisFunc,
					// 					thisClass,
					// 					thisPackage,
					// 					filePath));
					// 	}
					// }

					//name of the function
					XPathExpression e7 = xpath.compile("name");
					String funcName = e7.evaluate(funcList.item(j));

					// XPathExpression e8 = xpath.compile("descendant::call[name/name='AttachCurrentThread'] | descendant::call[name='AttachCurrentThread']");
					// 		NodeList thread = (NodeList) e8.evaluate(thisFileNode, XPathConstants.NODESET);
					// 		System.out.println(thread.getLength());

					boolean no_safe_points = false;

					//extract the functions which uses loops.
					XPathExpression e6 = xpath.compile("descendant::for | descendant::while");
					NodeList loops = (NodeList) e6.evaluate(funcList.item(j), XPathConstants.NODESET);
					if(loops.getLength()>0)
					{
						
						for(int l=0; l<loops.getLength();l++)
						{
							//check whether attachcurrentthread method was called inside the loop, if yes then its using safe point mechanism
							XPathExpression e9 = xpath.compile("descendant::call[name/name='FindClass'] | "+
							 "descendant::call[name/name='NewGlobalReference'] | "+
							 "descendant::call[name/name='AllocObject'] | "+
							 "descendant::call[name/name='NewObject'] | "+
							 "descendant::call[name/name='NewObjectA'] | "+
							 "descendant::call[name/name='NewObjectV']  | "+
							 "descendant::call[name/name='GetFieldID'] | "+
							 "descendant::call[name/name='GetMethodID'] | "+
							 "descendant::call[name/name='GetStaticFieldID'] | "+
							 "descendant::call[name/name='GetStaticMethodID'] | "+
							 "descendant::call[name/name='NewString']");
							XPathExpression e9_1 = xpath.compile("descendant::call[name/name='NewStringUTF'] | "+
							 "descendant::call[name/name='NewObjectArray'] | "+
							 "descendant::call[name/name='GetObjectArrayElement'] | "+
							 "descendant::call[name/name='NewBooleanArray'] | "+
							 "descendant::call[name/name='NewByteArray'] | "+
							 "descendant::call[name/name='NewCharArray'] | "+
							 "descendant::call[name/name='NewShortArray'] | "+
							 "descendant::call[name/name='NewIntArray'] | "+
							 "descendant::call[name/name='NewLongArray'] | "+
							 "descendant::call[name/name='NewFloatArray'] | "+
							 "descendant::call[name/name='NewDoubleArray']");
							// final List<String> selectorList = new LinkedList<>();
							// final List<String> jniFunctions = Arrays
							// .asList(
							// 	"FindClass", "NewGlobalReference", "AllocObject", "NewObject", "NewObjectA",
							// 	"NewObjectV", "GetFieldID", "GetMethodID", "GetStaticFieldID", "GetStaticMethodID",
							// 	"NewString", "NewStringUTF", "NewObjectArray", "GetObjectArrayElement", "NewBooleanArray",
							// 	"NewByteArray",	"NewCharArray", "NewShortArray", "NewIntArray", "NewLongArray", "NewFloatArray", "NewDoubleArray"
							// 	);
							// for (final String jniFunction : jniFunctions) {
							// selectorList
							// 	.add(
							// 		String
							// 			.format(
							// 				"descendant::call/name/name = '%s'",
							// 				jniFunction));
							// }
							// final String selector = String.join(" or ", selectorList);
							// final String declQuery = String
							// .format(
							// 	"descendant::decl_stmt[%s]/decl/name | descendant::expr_stmt[%s]/expr/name",
							// 	selector,
							// 	selector);
							NodeList jni_func_called_1 = (NodeList) e9.evaluate(loops.item(l), XPathConstants.NODESET);
							NodeList jni_func_called_2 = (NodeList) e9_1.evaluate(loops.item(l), XPathConstants.NODESET);
							//if any of the JNI functions are present inside the loop then it is smelly.
							if(jni_func_called_1.getLength()>0 || jni_func_called_2.getLength()>0)
							{

								no_safe_points = true;

								//check if PushLocalFrame is present inside the loop, if yes then its not smelly.
								XPathExpression e10 = xpath.compile("descendant::call[name/name='PushLocalFrame']");
								NodeList push_frame = (NodeList) e10.evaluate(loops.item(l), XPathConstants.NODESET);
								if(push_frame.getLength()>0)
								{
									no_safe_points=false;
								}
							}
						}
						
					}
					if(no_safe_points)
					{
						System.out.println(funcName);
						Not_Using_Safe_Points
								.add(
									new MLSCodeSmell(
										this.getCodeSmellName(),
										"",
										funcName,
										"",
										"",
										filePath));
					}
					
				}
			}
			this.setSetOfSmells(Not_Using_Safe_Points);
		}
		catch (final XPathExpressionException e) {
			e.printStackTrace();
		}
	}

	public String getCodeSmellName() {
		return "NotUsingSafePoints";
	}

}
