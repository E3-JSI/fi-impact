package si.ijs.ailab.fiimpact.project;

import javax.servlet.ServletOutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

import org.json.JSONWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import si.ijs.ailab.fiimpact.indicators.OverallResult;
import si.ijs.ailab.util.AIUtils;

/**
 * Created by flavio on 01/06/2015.
 */

class IOListDefinition {
	String id;
	ArrayList<IOListField> IOList;

	public IOListDefinition(String id) {
		this.id = id;
		IOList = new ArrayList<>();
	}

	public void addArrayList(IOListField IOListField) {
		IOList.add(IOListField);
	}
	
	public ArrayList<IOListField> getArrayList(){
		return IOList;
	}

	public String toString() {
		StringBuilder str = new StringBuilder();
		for (IOListField ioListField : IOList)
			str.append("\n" + ioListField);
		return str.toString();
	}
}

class IOListField {
	String column;
	String label;
	String fieldid;
	String usage;
	String missing;
	String include_record_when;

	public IOListField(String column, String label, String fieldid, String usage, String missing,
			String include_record_when) {
		this.column = column;
		this.label = label;
		this.fieldid = fieldid;
		this.usage = usage;
		this.missing = missing;
		this.include_record_when = include_record_when;
	}
	
	
	 public String getColumn() {
		return column;
	}
	 
	 public String getFieldid() {
		return fieldid;
	}
	 
	 public String getInclude_record_when() {
		return include_record_when;
	}
	 public String getLabel() {
		return label;
	}
	 public String getMissing() {
		return missing;
	}
	 
	 public String getUsage() {
		return usage;
	}

	public String toString() {
		return "Column: " + column + " Label: " + label + " Fieldid: " + fieldid + " Usage: " + usage + " Missing: "
				+ missing + " Include_record_when: " + include_record_when;
	}

}

public class ProjectManager {
	private final Path projectsList;
	private final Path projectsRoot;
	private final Path webappRoot;
	private final File listIoDef;
	Map<String, IOListDefinition> ioDefinitions;

	public Map<String, ProjectData> getProjects() {
		return projects;
	}

	private Map<String, ProjectData> projects = Collections.synchronizedMap(new HashMap<String, ProjectData>());

	final static Logger logger = LogManager.getLogger(ProjectManager.class.getName());
	private static ProjectManager projectManager;

	ProjectManager(String _webappRoot) {
		webappRoot = new File(_webappRoot).toPath();
		projectsList = new File(_webappRoot).toPath().resolve("WEB-INF").resolve("projects-id-list.txt");
		projectsRoot = new File(_webappRoot).toPath().resolve("WEB-INF").resolve("projects");
		logger.debug("Root: {}", _webappRoot);
		if (Files.notExists(projectsRoot)) {
			try {
				Files.createDirectory(projectsRoot);
				logger.debug("Created dir: {}", projectsRoot.toString());
			} catch (IOException e) {
				logger.error(e);
			}
		}
		loadProjects();
		listIoDef = webappRoot.resolve("WEB-INF").resolve("lists-io-def.xml").toFile();

		ioDefinitions = new HashMap<String, IOListDefinition>();
		File listIoDef = webappRoot.resolve("WEB-INF").resolve("lists-io-def.xml").toFile();
		importIOdef(listIoDef);
		// load import/export definitions from
		// webappRoot.resolve("WEB-INF").resolve("lists-io-def.xml");
		// Create a private structure to this class to handle the definitions.
		// Map<String, IOListDefinition> ioDefinitions
		/*
		 * class IOListDefinition { String id; maps for field lists... } class
		 * IOListField { String column; String label; String .... }
		 * 
		 */
		// loadListsDefinitions();

	}

	public void importIOdef(File listIoDef) {

		Document doc;
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;

		try {
			dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(listIoDef);
			doc.getDocumentElement().normalize();

			NodeList nList = doc.getElementsByTagName("list");
			for (int i = 0; i < nList.getLength(); i++) {
				String id = null;
				Node nNode = nList.item(i);
				NodeList nodeList = nNode.getChildNodes();
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					id = eElement.getAttribute("name");
				}
				IOListDefinition ioListDefinition = new IOListDefinition(id);

				for (int j = 0; j < nodeList.getLength(); j++) {
					if (nodeList.item(j).getNodeType() == Node.ELEMENT_NODE) {

						Element eElement = (Element) nodeList.item(j);
						IOListField ioListField = new IOListField(eElement.getAttribute("column"),
								eElement.getAttribute("label"), eElement.getAttribute("fieldid"),
								eElement.getAttribute("usage"), eElement.getAttribute("missing"),
								eElement.getAttribute("include-record-when"));
						ioListDefinition.addArrayList(ioListField);
					}

				}

				ioDefinitions.put(id, ioListDefinition);
			}

		} catch (SAXException | IOException | ParserConfigurationException e) {
			logger.error(e.getMessage().toString());

		}

	}

	public static synchronized ProjectManager getProjectManager(String _webappRoot)
			throws ParserConfigurationException, SAXException, IOException {
		if (projectManager == null) {
			projectManager = new ProjectManager(_webappRoot);// , _slots);
		}
		return projectManager;
	}

	public static ProjectManager getProjectManager() {
		return projectManager;
	}

	private void loadProjects() {
		logger.info("Load id list from: {}", projectsList.toString());
		projects.clear();
		try {
			BufferedReader br = new BufferedReader(
					new InputStreamReader(new FileInputStream(projectsList.toFile()), "utf-8"));
			String line = br.readLine();
			while (line != null) {
				ProjectData pd = loadProject(line);
				if (pd != null) {
					projects.put(line, pd);
				} else
					logger.error("Project {} does not exist.", line);
				line = br.readLine();
			}
			br.close();
			logger.info("loaded");
		} catch (IOException ioe) {
			logger.error("could not read text file " + projectsList.toString());
		}

	}

	private void saveProjectsList() {
		logger.info("Saving id list to: {}", projectsList.toString());

		try {
			synchronized (projects) {
				OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(projectsList.toFile()), "utf-8");
				for (String id : projects.keySet()) {
					w.write(id + "\n");
				}
				w.close();
			}
		} catch (IOException ioe) {
			logger.error("error writing file", ioe);
		}
		logger.info("Saved: {}", projectsList.toFile());

	}

	private ProjectData loadProject(String id) {

		Path p = projectsRoot.resolve("project-" + id + ".xml");
		ProjectData pd = new ProjectData();

		try {
			pd.read(new FileInputStream(p.toFile()));
		} catch (ParserConfigurationException | IOException | SAXException e) {
			logger.error("Cannot load project data {}", p.toString());
			pd = null;
		}
		return pd;
	}

	public synchronized void addProject(OutputStream outputStream, String[] arrFields, String id) throws IOException {
		ProjectData pd = projects.get(id);
		if (pd == null) {
			pd = new ProjectData();
			pd.setId(id);
			projects.put(id, pd);
			saveProjectsList();
		}

		pd.addFields(arrFields);
		pd.save(projectsRoot);
		pd.write(outputStream);
	}

	public synchronized void removeProject(ServletOutputStream outputStream, String id) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = null;
		try {
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			logger.error("Can't believe this", e);
		}
		Document doc = db.newDocument();
		Element root = doc.createElement("project");
		doc.appendChild(root);
		root.setAttribute("id", id);
		ProjectData pd = projects.get(id);

		if (pd == null) {
			root.setTextContent("Project not found, can't delete");
			logger.error("Project not found, can't delete: {}", id);
		} else {
			Path p = projectsRoot.resolve("project-" + id + ".xml");
			p.toFile().delete();
			root.setTextContent("Project removed.");
			logger.info("Project removed: {}", id);
			projects.remove(id);
			saveProjectsList();
		}
		AIUtils.save(doc, outputStream);
	}

	public void getJSONProject(OutputStream outputStream, String id) throws IOException {
		ProjectData pd = projects.get(id);
		if (pd == null) {
			OutputStreamWriter w = new OutputStreamWriter(outputStream, "utf-8");
			JSONWriter jsonProject = new JSONWriter(w);
			jsonProject.object().key("id").value(id).key("error").value("Project not found.").endObject();
			w.flush();
			w.close();
		} else {
			pd.writeUI(outputStream);
		}
	}

	public void addProject(String[] header,ArrayList<String> fields){
		String id = fields.get(1);
		ProjectData pd = projects.get(id);					
		if (pd == null) 
		{
			pd = new ProjectData();
			pd.setId(id);
			projects.put(id, pd);
			saveProjectsList();
		}

					
		pd.addFields(header, fields.toArray(new String[header.length]));
		pd.save(projectsRoot);
		fields.clear();
		
	}
	

	
	// TODO adapt this import to the definition in the file lists-io-def.xml,
	// <list name="project-list">
	public void importProjects(ServletOutputStream outputStream, String fileName) throws IOException {
	
		Path p = webappRoot.resolve("WEB-INF").resolve(fileName);
		logger.info("Load data from {}", p.toString());


		OutputStreamWriter w = new OutputStreamWriter(outputStream, "utf-8");
		JSONWriter json = new JSONWriter(w);
		JSONWriter array = json.array();
		array.object().key("total_before").value(projects.size()).endObject();

		
		IOListDefinition ioListDefinition=ioDefinitions.get("project-list");
		ArrayList<IOListField> listDefinition =ioListDefinition.getArrayList();
		
		String data=AIUtils.readFile(p);
		int pos=0;
		String state="OUT";
		ArrayList<String> fields=new ArrayList<String>();
		StringBuilder buffer=new StringBuilder();
		int index=0;
		ArrayList<IOListField> orderListDefinition=null;
		String[] header = null;
		int projectCounter=0;//number add projects
		int projectSkipCounter=0;//skiped projects
		boolean skipProject=false;
		while(pos<data.length()){
			String c=data.substring(pos,pos+1);
			switch(state){
				case "OUT":
					switch(c){
					case "\n":
						fields.add(buffer.toString());
						buffer.setLength(0);
						index=0;
						
						if(orderListDefinition==null)
						{
							//mapping header and definition
							header=new String[fields.size()];
							orderListDefinition=new ArrayList<>();
							for(String attributes: fields)
							{
								for(IOListField ioListField:listDefinition )
								{

									if(attributes.trim().equals(ioListField.getColumn()))
									{
										orderListDefinition.add(ioListField);
										header[index]=ioListField.getFieldid();
										break;
									}
								}

								index=index+1;
							}
						}
						else
						{
						
							//fix 
							for(IOListField ioListField:orderListDefinition )
							{
								
								if(ioListField.getMissing().length()!=0)
								{
									for(String missing:ioListField.getMissing().split(";"))
										if(missing.equals(fields.get(index)))
										{
											fields.set(index, "");
											break;
										}
								}
	
								if(ioListField.getInclude_record_when().length()!=0)
									if(!ioListField.getInclude_record_when().toLowerCase().equals(fields.get(index).toLowerCase().trim()))
									{
										projectSkipCounter=projectSkipCounter+1;
										System.out.println("skip project"+projectSkipCounter);
										skipProject=true;
									}
										
								index++;
	
							}

							if(skipProject==false)
							{	
								addProject(header,fields);
								System.out.println("Add project"+projectCounter);
								projectCounter=projectCounter+1;	
							}				

						}
						fields.clear();
						skipProject=false;
						break;
					case ",":
						fields.add(buffer.toString());
						buffer.setLength(0);
						
						break;
					case "\"":
						buffer.setLength(0);
						state="IN_STRING";
						
					default:
						buffer.append(c);
						//buffer.add(c);
						break;
					
					}
					
				break;
				case "IN_STRING":
					switch(c){
					case "\"":
						state="OUT";
						break;

						
					default:
						buffer.append(c);

						break;
					
					}	
				
					
				break;
					
			
			}	
		
		pos++;	
		}
		
		if(buffer.length()>0)
			fields.add(buffer.toString());
		
		if(fields.size()>0)
		{
			addProject(header,fields);
			projectCounter=projectCounter+1;	
			fields.clear();
		}
		
		logger.info("Added {} projects, total {}.", projectCounter, projects.size());
		logger.info("Skipped {} projects",projectSkipCounter);
		array.object().key("total_added").value(projectCounter).endObject();
		array.object().key("total_after").value(projects.size()).endObject();
		
		array.endArray();
		w.flush();
		w.close();

		
	}
	


	// TODO same as import projects - import Mattermark data
	public void importMattermark(ServletOutputStream outputStream, String fileName) throws IOException {

		IOListDefinition ioListDefinition=ioDefinitions.get("project-list");
		ArrayList<IOListField> listDefinition =ioListDefinition.getArrayList();
		
		// adapt this import to the definition in the file lists-io-def.xml,
		// <list name="mattermark-export">

		/*
		 * You have to expand the ProjectData class - add a new map to it (like
		 * the existing "fields" map). - getters/setters, persistence
		 * (load/save) -add getter for clean-url metadata
		 */

		/*
		 * 1. go through all ProjectData instances and clear mattermark
		 * information 2.load the file - use the clean-url usage information to
		 * match it with the correct ProjectData instance You may create a
		 * temporary map, where you have the clean-url as key in order to find
		 * the correct project. Save each ProjectData insance and the list.
		 */
	}

	public void listProjects(ServletOutputStream outputStream) throws IOException {
		OutputStreamWriter w = new OutputStreamWriter(outputStream, "utf-8");
		logger.info("Return {} projects", projects.size());
		JSONWriter json = new JSONWriter(w);
		json.object().key("total").value(projects.size());
		json.key("projects").array();
		for (ProjectData pd : projects.values()) {
			json.object();
			json.key("id").value(pd.getId());
			// TODO Add field keys for the list view.
			// addFieldKey(json, "Q1_1", pd.fields);
			json.endObject();
		}
		json.endArray();
		json.endObject();
		w.flush();
		w.close();
		logger.info("Returned {} projects", projects.size());

	}

	static final char newline = '\n';

	public static void writeLine(BufferedWriter w, String s) throws IOException {
		w.write(s, 0, s.length());
		w.write(newline);
	}

	public void exportProjects(ServletOutputStream outputStream, String exportDir, String type, String fieldsList)
			throws IOException {
		// TODO export to file (TXT/CSV)
		OutputStreamWriter w = new OutputStreamWriter(outputStream, "utf-8");
		logger.info("Save {} projects", projects.size());
		JSONWriter json = new JSONWriter(w);
		json.object().key("total").value(projects.size());

		SortedSet<String> columnsDef = new TreeSet<>();
		if (fieldsList != null) {
			String[] arr = fieldsList.split(";");
			for (String s : arr) {
				columnsDef.add(s);
			}
		}

		synchronized (projects) {
			if (columnsDef.size() == 0) {
				for (ProjectData pd : projects.values()) {
					for (String s : pd.fields.keySet())
						columnsDef.add(s);
				}
			}
		}

		String filename = "export_all_" + type + ".txt";
		Path root = new File(exportDir).toPath();
		Path fOut = root.resolve(filename);
		BufferedWriter writerAllTXT = Files.newBufferedWriter(fOut, Charset.forName("UTF-8"),
				StandardOpenOption.CREATE);
		StringBuilder sb = new StringBuilder();
		sb.append("id");
		for (String s : columnsDef) {
			sb.append("\t").append(s);
		}

		String sHeader = sb.toString();
		writeLine(writerAllTXT, sHeader);

		sb.setLength(0);

		synchronized (projects) {
			for (ProjectData pd : projects.values()) {
				sb.append(pd.getId());
				for (String s : columnsDef) {
					sb.append("\t");
					String val = pd.fields.get(s);
					if (val != null) {
						val = val.replace("\r\n", ", ");
						val = val.replace("\r", ", ");
						val = val.replace("\n", ", ");
						sb.append(val);
					}
				}
				writeLine(writerAllTXT, sb.toString());
				sb.setLength(0);
			}
		}
		json.endObject();
		w.flush();
		w.close();
		logger.info("Saved {} projects", projects.size());
	}

	// TODO You may need in the list function
	private void addFieldKey(JSONWriter json, String qID, Map<String, String> questions) {
		String val = questions.get(qID);
		if (val != null)
			json.key(qID).value(val);
	}

	synchronized public void clearAllProjects(ServletOutputStream outputStream) throws IOException {
		OutputStreamWriter w = new OutputStreamWriter(outputStream, "utf-8");
		logger.info("Remove all {} projects", projects.size());
		JSONWriter json = new JSONWriter(w);
		json.object().key("total").value(projects.size());
		synchronized (projects) {
			for (String id : projects.keySet()) {
				Path p = projectsRoot.resolve("project-" + id + ".xml");
				p.toFile().delete();
				logger.info("Project removed: {}", id);
			}
		}
		projects.clear();
		saveProjectsList();
		json.endObject();
		w.flush();
		w.close();
	}

	// TODO return the list of "indicator" field identifiers
	public ArrayList<String> getMattermarkIndicators() {
		return null;
	}

	// TODO when loading or importing mattermark data store min/max/n values for
	// "indicator" fields in a data structure
	// and return them here
	public double getMattermarkMinValue(String fieldId) {
		return 0.0;
	}

	public double getMattermarkMaxValue(String fieldId) {
		return 0.0;
	}

	public int getMattermarkCount(String fieldId) // this is ne number of non
													// empty fields
	{
		return 0;
	}

	// TODO check the implementation of SurveyManager.SPEEDOMETER_SLOTS
	/*
	 * you can have a static structure that gets boundaries as
	 * SPEEDOMETER_SLOTS. Entries are mattemrak "indicator" fields with the
	 * "MATTERMARK_" prefix.
	 */
	public Map<String, OverallResult.ScoreBoundaries> getMattemrarkSlots() {
		return null;
	}

	public static void main(String[] args) throws Exception {
	}

}
