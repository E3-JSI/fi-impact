package si.ijs.ailab.fiimpact.project;

import javax.servlet.ServletOutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.charset.Charset;
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




public class ProjectManager
{
  static class IOListDefinition
  {
    private String id;
    private ArrayList<IOListField> fields;
    private IOListField usageID;
    private IOListField usageCleanUrl;

    IOListDefinition(String id)
    {
      this.id = id;
      fields = new ArrayList<>();
    }

    void addField(IOListField ioListField)
    {
      fields.add(ioListField);
      if(ioListField.getUsage() != null)
      {
        switch(ioListField.getUsage())
        {
          case "id": usageID = ioListField;
            break;
          case "clean-url": usageCleanUrl = ioListField;
            break;
        }
      }
    }

    public ArrayList<IOListField> getFields()
    {
      return fields;
    }

    public String toString()
    {
      StringBuilder str = new StringBuilder();
      for(IOListField ioListField : fields)
        str.append("\n").append(ioListField);
      return str.toString();
    }

    public String getId()
    {
      return id;
    }

    public IOListField getUsageID()
    {
      return usageID;
    }

    IOListField getUsageCleanUrl()
    {
      return usageCleanUrl;
    }
  }

  public static class  IOListField
  {
    private String column;
    private String label;
    private String fieldid;
    private String usage;
    private String missing;
    private String include_record_when;
    private String transform;

    IOListField(String column, String label, String fieldid, String usage, String missing,
                String include_record_when, String transform)
    {
      this.column = column;
      this.label = label;
      this.fieldid = fieldid;
      this.usage = usage;
      this.missing = missing;
      this.include_record_when = include_record_when;
      this.transform = transform;
    }

    public String getTransform()
    {
      return transform;
    }

    public String getColumn()
    {
      return column;
    }

    public String getFieldid()
    {
      return fieldid;
    }

    public String getInclude_record_when()
    {
      return include_record_when;
    }

    public String getLabel()
    {
      return label;
    }

    public String getMissing()
    {
      return missing;
    }

    public String getUsage()
    {
      return usage;
    }

    public boolean isTransformLog()
    {
      return getTransform() != null && getTransform().equals("log");
    }
  }

  private final Path projectsList;
  private final Path projectsRoot;
  private final Path webappRoot;
  private Map<String, IOListDefinition> ioDefinitions;
  ArrayList<IOListField> mattermarkIndicators = new ArrayList<>();
  private Map<String, MattermarkIndicatorInfo> mattermarkIndicatorInfoMap = new HashMap<>();
  private Map<String, OverallResult.ScoreBoundaries> mattermarkSpeedometerSlots = new HashMap<>();



  public Map<String, ProjectData> getProjects()
  {
    return projects;
  }

  private final Map<String, ProjectData> projects = Collections.synchronizedMap(new HashMap<String, ProjectData>());

  private final static Logger logger = LogManager.getLogger(ProjectManager.class.getName());
  private static ProjectManager projectManager;

  public static class MattermarkIndicatorInfo
  {
    public IOListField getIoListField()
    {
      return ioListField;
    }

    IOListField ioListField;
    double min;
    double max;
    int count = 0;

    public MattermarkIndicatorInfo(IOListField ioListField)
    {
      this.ioListField = ioListField;
    }

    public String getId()
    {
      return ioListField.getFieldid();
    }

    public double getMin()
    {
      return min;
    }

    public double getMax()
    {
      return max;
    }

    public int getCount()
    {
      return count;
    }
  }


  private ProjectManager(Path _webappRoot)
  {
    webappRoot = _webappRoot;
    projectsList = webappRoot.resolve("WEB-INF").resolve("projects-id-list.txt");
    projectsRoot = webappRoot.resolve("WEB-INF").resolve("projects");
    logger.info("Root: {}", _webappRoot);

    if(Files.notExists(projectsRoot))
    {
      try
      {
        Files.createDirectory(projectsRoot);
        logger.debug("Created dir: {}", projectsRoot.toString());
      }
      catch (IOException e)
      {
        logger.error(e);
      }
    }
    ioDefinitions = new HashMap<>();
    File listIoDef = webappRoot.resolve("WEB-INF").resolve("lists-io-def.xml").toFile();
    loadIOdef(listIoDef);
    loadProjects();
  }

  private void loadIOdef(File listIoDef)
  {

    Document doc;
    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder dBuilder;

    try
    {
      dBuilder = dbFactory.newDocumentBuilder();
      doc = dBuilder.parse(listIoDef);
      doc.getDocumentElement().normalize();

      NodeList nList = doc.getElementsByTagName("list");
      for(int i = 0; i < nList.getLength(); i++)
      {
        String id = null;
        Node nNode = nList.item(i);
        NodeList nodeList = nNode.getChildNodes();
        if(nNode.getNodeType() == Node.ELEMENT_NODE)
        {
          Element eElement = (Element) nNode;
          id = eElement.getAttribute("name");
        }
        IOListDefinition ioListDefinition = new IOListDefinition(id);

        for(int j = 0; j < nodeList.getLength(); j++)
        {
          if(nodeList.item(j).getNodeType() == Node.ELEMENT_NODE)
          {

            Element eElement = (Element) nodeList.item(j);
            IOListField ioListField = new IOListField(eElement.getAttribute("column"),
                    eElement.getAttribute("label"), eElement.getAttribute("fieldid"),
                    eElement.getAttribute("usage"), eElement.getAttribute("missing"),
                    eElement.getAttribute("include-record-when"), eElement.getAttribute("transform"));
            ioListDefinition.addField(ioListField);
          }

        }

        ioDefinitions.put(id, ioListDefinition);
      }

      IOListDefinition ioListDefinition = ioDefinitions.get("mattermark-export");
      ArrayList<IOListField> listDefinitionMattermark = ioListDefinition.getFields();

      for(IOListField ioListField : listDefinitionMattermark)
      {
        if(ioListField.getUsage().equals("indicator"))
          mattermarkIndicators.add(ioListField);
      }

    }
    catch (SAXException | IOException | ParserConfigurationException e)
    {
      logger.error("Error loading list definition.", e);
    }

  }

  public static synchronized ProjectManager getProjectManager(Path _webappRoot)
  {
    if(projectManager == null)
    {
      projectManager = new ProjectManager(_webappRoot);
    }
    return projectManager;
  }

  public static ProjectManager getProjectManager()
  {
    return projectManager;
  }

  private void loadProjects()
  {
    logger.info("Load id list from: {}", projectsList.toString());
    projects.clear();
    try
    {
      BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(projectsList.toFile()), "utf-8"));
      String line = br.readLine();
      while(line != null)
      {
        ProjectData pd = loadProject(line);
        if(pd != null)
        {
          projects.put(line, pd);
        }
        else
          logger.error("Project {} does not exist.", line);
        line = br.readLine();
      }
      br.close();
      logger.info("loaded");
    }
    catch (IOException ioe)
    {
      logger.error("could not read text file " + projectsList.toString());
    }
    recalcMattermarkIndicatorsInfo();

  }


  private void saveProjectsList()
  {
    logger.info("Saving id list to: {}", projectsList.toString());

    try
    {
      synchronized(projects)
      {
        OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(projectsList.toFile()), "utf-8");
        for(String id : projects.keySet())
        {
          w.write(id + "\n");
        }
        w.close();
      }
      logger.info("Saved: {}", projectsList.toFile());
    }
    catch (IOException ioe)
    {
      logger.error("error writing file", ioe);
    }

  }

  private ProjectData loadProject(String id)
  {

    Path p = projectsRoot.resolve("project-" + id + ".xml");
    ProjectData pd = new ProjectData();

    try
    {
      pd.read(new FileInputStream(p.toFile()));
    }
    catch (ParserConfigurationException | IOException | SAXException e)
    {
      logger.error("Cannot load project data {}", p.toString());
      pd = null;
    }
    return pd;
  }

/*
  public synchronized void addProject(OutputStream outputStream, String[] arrFields, String id) throws IOException
  {
    ProjectData pd = projects.get(id);
    if(pd == null)
    {
      pd = new ProjectData();
      pd.setId(id);
      projects.put(id, pd);
      saveProjectsList();
    }

    pd.addFields(arrFields);
    pd.save(projectsRoot);
    pd.write(outputStream);
  }

  public synchronized void removeProject(ServletOutputStream outputStream, String id)
  {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db = null;
    try
    {
      db = dbf.newDocumentBuilder();
    }
    catch (ParserConfigurationException e)
    {
      logger.error("Can't believe this", e);
    }
    Document doc = db.newDocument();
    Element root = doc.createElement("project");
    doc.appendChild(root);
    root.setAttribute("id", id);
    ProjectData pd = projects.get(id);

    if(pd==null)
    {
      root.setTextContent("Project not found, can't delete");
      logger.error("Project not found, can't delete: {}", id);
    }
    else
    {
      Path p = projectsRoot.resolve("project-" + id + ".xml");
      p.toFile().delete();
      root.setTextContent("Project removed.");
      logger.info("Project removed: {}", id);
      projects.remove(id);
      saveProjectsList();
    }
    AIUtils.save(doc, outputStream);
  }

*/

  public void getJSONProject(OutputStream outputStream, String id) throws IOException
  {
    ProjectData pd = projects.get(id);
    if(pd == null)
    {
      OutputStreamWriter w = new OutputStreamWriter(outputStream, "utf-8");
      JSONWriter jsonProject = new JSONWriter(w);
      jsonProject.object().key("id").value(id).key("error").value("Project not found.").endObject();
      w.flush();
      w.close();
    }
    else
    {
      pd.writeUI(outputStream);
    }
  }

  private static final int ADD_STATUS_NEW = 0;
  private static final int ADD_STATUS_UPDATE = 1;
  private static final int ADD_STATUS_SKIP = 2;

  //returns the status defined above
  private int addProject(int idIndex, ArrayList<IOListField> orderListDefinition, ArrayList<String> fields)
  {
    int ret = ADD_STATUS_UPDATE;
    boolean skipProject = false;// false=save project;true=skip project
    // clean missing, and include record if yes
    for(int i = 0; i < fields.size(); i++)
    {
      IOListField ioListField = orderListDefinition.get(i);
      if(ioListField.getMissing().length() != 0)
      {
        for(String missing : ioListField.getMissing().split(";"))
          if(missing.equals(fields.get(i)))
          {
            fields.set(i, "");
            break;
          }
      }

      if(ioListField.getInclude_record_when().length() != 0)
        if(!ioListField.getInclude_record_when().toLowerCase().equals(fields.get(i).toLowerCase()))
        {
          skipProject = true;
          break;
        }
    }


    if(skipProject)
    {
      ret = ADD_STATUS_SKIP;
    }
    else
    {
      String id = fields.get(idIndex);
      ProjectData pd = projects.get(id);
      if(pd == null)
      {
        ret = ADD_STATUS_NEW;
        pd = new ProjectData();
        pd.setId(id);
        projects.put(id, pd);
        saveProjectsList();
      }

      pd.addFields(orderListDefinition, fields);
      pd.save(projectsRoot);
    }
    return ret;
  }

  private String cleanURL(String rawURL)
  {
    if(rawURL.length() > 0)
      if(rawURL.lastIndexOf("/") == rawURL.length() - 1)
        rawURL = rawURL.substring(0, rawURL.length() - 1);

    return rawURL.replaceFirst("^(https://www.|http://www.|http://|https://|www.|)", "");
  }


  // import according to the definition in the file lists-io-def.xml, <list name="project-list">
  public void importProjects(ServletOutputStream outputStream, Path p) throws IOException
  {
    projects.clear();
    logger.info("Load data from {}", p.toString());

    OutputStreamWriter w = new OutputStreamWriter(outputStream, "utf-8");
    JSONWriter json = new JSONWriter(w);
    JSONWriter array = json.array();
    array.object().key("total_before").value(projects.size()).endObject();

    IOListDefinition ioListDefinition = ioDefinitions.get("project-list");
    ArrayList<IOListField> ioListDefinitionFields = ioListDefinition.getFields();

    String data = AIUtils.readFile(p);
    int pos = 0;
    String state = "OUT";
    ArrayList<String> fields = new ArrayList<>();
    StringBuilder buffer = new StringBuilder();
    ArrayList<IOListField> importOrderListDefinitionFields = null;
    int[] projectCounters = new int[3];

    int idIndex = -1;
    while(pos < data.length())
    {
      String c = data.substring(pos, pos + 1);
      switch(state)
      {
        case "OUT":
          switch(c)
          {
            case "\n":
              fields.add(buffer.toString().trim());
              buffer.setLength(0);

              if(importOrderListDefinitionFields == null)
              {
                // mapping header and definition
                importOrderListDefinitionFields = new ArrayList<>();
                //TODO it may be better to have a map of ioListDefinitionFields
                for(int i = 0; i < fields.size(); i++)
                {
                  String columnName = fields.get(i);
                  for(IOListField ioListField : ioListDefinitionFields)
                  {
                    if(columnName.equals(ioListField.getColumn()))
                    {
                      importOrderListDefinitionFields.add(ioListField);
                      if(ioListField.getUsage().equals("id"))
                        idIndex = i;
                      break;
                    }
                  }
                }


              }
              else
              {
                projectCounters[addProject(idIndex,importOrderListDefinitionFields, fields)]++;
              }
              fields.clear();
              break;
            case ",":
              fields.add(buffer.toString().trim());
              buffer.setLength(0);
              break;
            case "\"":
              buffer.setLength(0);
              state = "IN_STRING";
            default:
              buffer.append(c);
              break;
          }
          break;
        case "IN_STRING":
          switch(c)
          {
            case "\"":
              state = "OUT";
              break;
            default:
              buffer.append(c);
              break;
          }
          break;
      }

      pos++;
    }

    if(buffer.length() > 0)
      fields.add(buffer.toString().trim());

    if(fields.size() > 0)
    {
      projectCounters[addProject(idIndex,importOrderListDefinitionFields, fields)]++;
      fields.clear();
    }

    logger.info("Projects: added={}; updated={}; total={}.", projectCounters[ADD_STATUS_NEW], projectCounters[ADD_STATUS_UPDATE], projects.size());
    logger.info("Skipped {} projects", projectCounters[ADD_STATUS_SKIP]);
    array.object().key("total_added").value(projectCounters[ADD_STATUS_NEW]).endObject();
    array.object().key("total_updated").value(projectCounters[ADD_STATUS_UPDATE]).endObject();
    array.object().key("total_skipped").value(projectCounters[ADD_STATUS_SKIP]).endObject();
    array.object().key("total_after").value(projects.size()).endObject();

    array.endArray();

    recalcMattermarkIndicatorsInfo();

    w.flush();
    w.close();

  }

  private boolean addProjectMattermark(Map<String, ProjectData> projectDataByURL, ArrayList<IOListField> importOrderListDefinitionFields, ArrayList<String> fields)
  {

    String cleanProjectUrlFromMattermark = null;
    boolean ret = false;
    // clean missing
    for(int i = 0; i < fields.size(); i++)
    {
      IOListField ioListField = importOrderListDefinitionFields.get(i);
      if(ioListField.getMissing().length() != 0)
      {
        for(String missing : ioListField.getMissing().split(";"))
          if(missing.equals(fields.get(i)))
          {
            fields.set(i, "");
            break;
          }
      }


      if(ioListField.getUsage().length() != 0)
        if(ioListField.getUsage().equals("clean-url"))
        {
          cleanProjectUrlFromMattermark = cleanURL(fields.get(i));
        }

    }

    if(cleanProjectUrlFromMattermark != null)
    {
      ProjectData projectData = projectDataByURL.get(cleanProjectUrlFromMattermark);
      if(projectData != null)
      {
        ret = true;
        projectData.addFieldsMattermark(importOrderListDefinitionFields, fields);
        projectData.save(projectsRoot);
      }
    }
    return ret;
  }

  //import Mattermark data
  public void importMattermark(ServletOutputStream outputStream, Path p) throws IOException
  {
    logger.info("Load data from {}", p.toString());

    OutputStreamWriter w = new OutputStreamWriter(outputStream, "utf-8");
    JSONWriter json = new JSONWriter(w);
    JSONWriter array = json.array();
    array.object().key("total_before").value(projects.size()).endObject();

    IOListDefinition ioListDefinition = ioDefinitions.get("mattermark-export");
    ArrayList<IOListField> ioListDefinitionFields = ioListDefinition.getFields();

    String data = AIUtils.readFile(p);
    int pos = 0;
    String state = "OUT";
    ArrayList<String> fields = new ArrayList<>();
    StringBuilder buffer = new StringBuilder();
    ArrayList<IOListField> importOrderListDefinitionFields;
    int mattermarkCounter = 0;// number addded mattermark projects

		// 1. go through all ProjectData instances and clear mattermark information
    for(String key : projects.keySet())
    {
      ProjectData projectData = projects.get(key);
      projectData.getMattermarkFields().clear();
    }

		/*
		 * 2.load the file - use the clean-url usage information to match it
		 * with the correct ProjectData instance You may create a temporary map,
		 * where you have the clean-url as key in order to find the correct
		 * project. Save each ProjectData instance and the list.
		 */
    // clean-url
    IOListDefinition  ioListDefinitionProjects = ioDefinitions.get("project-list");
    Map<String, ProjectData> projectDataByURL = new HashMap<>();
    IOListField projectURLField = ioListDefinitionProjects.getUsageCleanUrl();

    for(ProjectData projectData: projects.values())
    {
      String projectURL = projectData.getValue(projectURLField.getFieldid());
      if(projectURL != null)
      {
        projectDataByURL.put(cleanURL(projectURL), projectData);
      }
    }

    importOrderListDefinitionFields = null;
    while(pos < data.length())
    {

      String c = data.substring(pos, pos + 1);
      switch(state)
      {
        case "OUT":
          switch(c)
          {
            case "\n":
              fields.add(buffer.toString().trim());
              buffer.setLength(0);

              if(importOrderListDefinitionFields == null)
              {
                // mapping header and definition
                importOrderListDefinitionFields = new ArrayList<>();
                //TODO it may be better to have a map of ioListDefinitionFields
                for(String attributes : fields)
                {
                  for(IOListField ioListField : ioListDefinitionFields)
                  {
                    if(attributes.equals(ioListField.getColumn()))
                    {
                      importOrderListDefinitionFields.add(ioListField);
                      break;
                    }
                  }
                }
              }
              else
              {
                if(addProjectMattermark(projectDataByURL, importOrderListDefinitionFields, fields))
                  mattermarkCounter++;
              }
              fields.clear();

              break;
            case ",":
              fields.add(buffer.toString().trim());
              buffer.setLength(0);

              break;
            case "\"":
              buffer.setLength(0);
              state = "IN_STRING";

            default:
              buffer.append(c);
              break;

          }

          break;
        case "IN_STRING":
          switch(c)
          {
            case "\"":
              state = "OUT";
              break;

            default:
              buffer.append(c);
              break;
          }

          break;
      }

      pos++;
    }

    if(buffer.length() > 0)
      fields.add(buffer.toString().trim());

    if(fields.size() > 0)
    {
      if(addProjectMattermark(projectDataByURL, importOrderListDefinitionFields, fields))
        mattermarkCounter++;
      fields.clear();
    }

    logger.info("Added mattermark {} projects, total {}.", mattermarkCounter, projects.size());
    array.object().key("added mattermark").value(mattermarkCounter).endObject();
    array.object().key("total_after").value(projects.size()).endObject();
    array.endArray();

    recalcMattermarkIndicatorsInfo();
    w.flush();
    w.close();

  }

  public void listProjects(ServletOutputStream outputStream) throws IOException
  {
    OutputStreamWriter w = new OutputStreamWriter(outputStream, "utf-8");
    logger.info("Return {} projects", projects.size());
    JSONWriter json = new JSONWriter(w);
    json.object().key("total").value(projects.size());
    json.key("projects").array();
    for(ProjectData pd : projects.values())
    {
      json.object();
      json.key("id").value(pd.getId());
      //TODO Add field keys for the list view.
      addFieldKey(json, "P02", pd.getValue("P02"));
      addFieldKey(json, "P04", pd.getValue("P04"));
      addFieldKey(json, "P05", pd.getValue("P05"));
      addFieldKey(json, "P06", pd.getValue("P06"));
      json.endObject();
    }
    json.endArray();
    json.endObject();
    w.flush();
    w.close();
    logger.info("Returned {} projects", projects.size());
  }

  private static final char newline = '\n';

  private static void writeLine(BufferedWriter w, String s) throws IOException
  {
    w.write(s, 0, s.length());
    w.write(newline);
  }

  public void exportProjects(ServletOutputStream outputStream, String exportDir, String type, String fieldsList) throws IOException
  {
    //TODO export to file (TXT/CSV)
    OutputStreamWriter w = new OutputStreamWriter(outputStream, "utf-8");
    logger.info("Save {} projects", projects.size());
    JSONWriter json = new JSONWriter(w);
    json.object().key("total").value(projects.size());

    SortedSet<String> columnsDef = new TreeSet<>();
    if(fieldsList != null)
    {
      String[] arr = fieldsList.split(";");
      Collections.addAll(columnsDef, arr);
    }

    synchronized(projects)
    {
      if(columnsDef.size() == 0)
      {
        IOListDefinition ioListDefinition = ioDefinitions.get("project-list");
        ArrayList<IOListField> ioListDefinitionFields = ioListDefinition.getFields();
        for(IOListField ioListField : ioListDefinitionFields)
            columnsDef.add(ioListField.getFieldid());
      }
    }

    String filename = "export_all_" + type + ".txt";
    Path root = new File(exportDir).toPath();
    Path fOut = root.resolve(filename);
    BufferedWriter writerAllTXT = Files.newBufferedWriter(fOut, Charset.forName("UTF-8"), StandardOpenOption.CREATE);
    StringBuilder sb = new StringBuilder();
    sb.append("id");
    for(String s : columnsDef)
    {
      sb.append("\t").append(s);
    }

    String sHeader = sb.toString();
    writeLine(writerAllTXT, sHeader);

    sb.setLength(0);

    synchronized(projects)
    {
      for(ProjectData pd : projects.values())
      {
        sb.append(pd.getId());
        for(String s : columnsDef)
        {
          sb.append("\t");
          String val = pd.getValue(s);
          if(val != null)
          {
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

  private void addFieldKey(JSONWriter json, String qID, String val)
  {
    if(val != null)
      json.key(qID).value(val);
  }

  synchronized public void clearAllProjects(ServletOutputStream outputStream) throws IOException
  {
    OutputStreamWriter w = new OutputStreamWriter(outputStream, "utf-8");
    logger.info("Remove all {} projects", projects.size());
    JSONWriter json = new JSONWriter(w);
    json.object().key("total").value(projects.size());
    synchronized(projects)
    {
      for(String id : projects.keySet())
      {
        Path p = projectsRoot.resolve("project-" + id + ".xml");
        p.toFile().delete();
        logger.info("Project removed: {}", id);
      }
    }
    projects.clear();
    saveProjectsList();
    recalcMattermarkIndicatorsInfo();
    json.endObject();
    w.flush();
    w.close();
  }

  // return the list of "indicator" field identifiers
  public ArrayList<IOListField> getMattermarkIndicators()
  {
    return mattermarkIndicators;
  }



  private void recalcMattermarkIndicatorsInfo()
  {
    mattermarkIndicatorInfoMap.clear();
    for(IOListField ioListField: mattermarkIndicators)
      mattermarkIndicatorInfoMap.put(ioListField.getFieldid(), new MattermarkIndicatorInfo(ioListField));

    for(ProjectData pd: projects.values())
    {
      for(Map.Entry<String, MattermarkIndicatorInfo> miEntry: mattermarkIndicatorInfoMap.entrySet())
      {
        if (pd.isMattermarkValueSet(miEntry.getKey()))
        {
          MattermarkIndicatorInfo indicatorInfo = miEntry.getValue();
          int mattermarkValue = pd.getMattermarkIntValue(miEntry.getKey());
          if(indicatorInfo.count == 0)
          {
            indicatorInfo.max = mattermarkValue;
            indicatorInfo.min = mattermarkValue;
          }
          else
          {
            if(mattermarkValue < indicatorInfo.min)
              indicatorInfo.min = mattermarkValue;
            if(mattermarkValue > indicatorInfo.min)
              indicatorInfo.max = mattermarkValue;
          }
          indicatorInfo.count++;
        }
      }
    }


    for(Map.Entry<String, MattermarkIndicatorInfo> miEntry: mattermarkIndicatorInfoMap.entrySet())
    {
      MattermarkIndicatorInfo indicatorInfo = miEntry.getValue();
      if(indicatorInfo.ioListField.isTransformLog())
      {
        indicatorInfo.max = Math.signum(indicatorInfo.max)*Math.log(Math.abs(indicatorInfo.max)+1.0);
        indicatorInfo.min = Math.signum(indicatorInfo.min)*Math.log(Math.abs(indicatorInfo.min)+1.0);
      }
    }


    mattermarkSpeedometerSlots.clear();
    for(MattermarkIndicatorInfo mattermarkIndicatorInfo: mattermarkIndicatorInfoMap.values())
    {
      OverallResult.ScoreBoundaries boundaries = new OverallResult.ScoreBoundaries();
      boundaries.setMin(0.0);
      boundaries.setLo_med(1.667);
      boundaries.setMed_hi(3.333);
      boundaries.setMax(5.000);
      mattermarkSpeedometerSlots.put("MATTERMARK_"+mattermarkIndicatorInfo.getId(), boundaries);
    }


  }

  public MattermarkIndicatorInfo getMattermarkIndicatorInfo(String fieldId)
  {
    return mattermarkIndicatorInfoMap.get(fieldId);
  }


	// Entries are mattemrak "indicator" fields with the "MATTERMARK_" prefix.
  public Map<String, OverallResult.ScoreBoundaries> getMattemrarkSlots()
  {
    return mattermarkSpeedometerSlots;
  }

  public static void main(String[] args) throws Exception
  {
  }


}
