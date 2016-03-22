package si.ijs.ailab.fiimpact.project;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class test {
	//TODO ali je id isti kot v iodefinitionu in zakaj je to dobro?
	static Map<String, IOListDefinition> ioDefinitions;
	/*
	public void importIOdef(File listIoDef){
		
		Document doc;
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			try {
				doc = dBuilder.parse(listIoDef);
				doc.getDocumentElement().normalize();
				
				NodeList nList = doc.getElementsByTagName("list");
				for (int i = 0; i < nList.getLength(); i++) {
					String id = null;
					Node nNode = nList.item(i);
					NodeList nodeList = nNode.getChildNodes();
					if (nNode.getNodeType() == Node.ELEMENT_NODE) {
						Element eElement = (Element) nNode;
						id=eElement.getAttribute("name");
					}
					IOListDefinition ioListDefinition=new IOListDefinition(id);
					

					for (int j = 0; j < nodeList.getLength(); j++) {
						if (nodeList.item(j).getNodeType() == Node.ELEMENT_NODE) {

							Element eElement = (Element) nodeList.item(j);
							IOListField ioListField=new IOListField(eElement.getAttribute("column"),
									eElement.getAttribute("label"),
									eElement.getAttribute("fieldid"),
									eElement.getAttribute("usage"),
									eElement.getAttribute("missing"),
									eElement.getAttribute("include_record_when"));
							ioListDefinition.addArrayList(ioListField);
						}
						
						

					}
				
				ioDefinitions.put(id, ioListDefinition);
				}
				
			} catch (SAXException | IOException e) {
				logger.
				e.printStackTrace();
			}
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {

		ioDefinitions=new HashMap();
		Path webappRoot = new File("C:\\Users\\Matej\\Desktop\\Tomcat7\\webapps\\fi-impact").toPath();
		File listIoDef = webappRoot.resolve("WEB-INF").resolve("lists-io-def.xml").toFile();
		importIOdef(listIoDef);



	
		
				for (Map.Entry<String, IOListDefinition> entry : ioDefinitions.entrySet())
				{
				    System.out.println(entry.getKey() + "/" + entry.getValue());
				}
	}
	*/

}
