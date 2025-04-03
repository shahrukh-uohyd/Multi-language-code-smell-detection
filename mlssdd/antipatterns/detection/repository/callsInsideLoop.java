import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import mlssdd.utils.CreateXml;
import java.util.*;
import java.lang.*;

public class callsInsideLoop{
	public static void main(String[] args) {
		Document xml = CreateXml.parseSingleDocument("/home/shahrukh/smellDetection/Detection/projects/abidi/rocksdb-8.3.2");
		
		XPathFactory xpathfactory = XPathFactory.newInstance();
    XPath xpath = xpathfactory.newXPath();
    Set<String> nativeMethods = new HashSet<>();

    XPathExpression e5 = xpath.compile("descendant::function_decl[type/specifier='native']/name");
      			final NodeList declList = (NodeList) e5.evaluate(javaFile, XPathConstants.NODESET);
      			for(int d=0; d<declList.getLength(); d++)
      			{
      				nativeMethods.add(declList.item(d).getTextContent);
      			}
    XPathExpression x = xpath.compile("descendant::for | descendant::while");
    NodeList loops = (NodeList) x.evaluate(xml, XPathConstants.NODESET);

    for(int i=0; i<loops.getLength(); i++)
    {
    	XPathExpression x1 = xpath.compile("descendant::call");
    	NodeList calls = (NodeList) x1.evaluate(loops.item(i), XPathConstants.NODESET);

    	for(int j=0; j<calls.getLength(); j++)
    	{
    		XPathExpression e5 = xpath.compile("descendant::name/name | descendant::name");
      		String calledMethod = e5.evaluate(calls.item(j));
      		if(nativeMethods.contains(calledMethod))
    		System.out.println(calls.item(j).getTextContent());
    	}
    }

	}
}