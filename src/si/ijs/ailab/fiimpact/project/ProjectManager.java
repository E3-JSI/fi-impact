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
  private final Path projectsList;
  private final Path projectsRoot;
  private final Path webappRoot;

  public Map<String, ProjectData> getProjects()
  {
    return projects;
  }

  private Map<String, ProjectData> projects = Collections.synchronizedMap(new HashMap<String, ProjectData>());

  final static Logger logger = LogManager.getLogger(ProjectManager.class.getName());
  private static ProjectManager projectManager;


  private ProjectManager(String _webappRoot)
  {
    webappRoot = new File(_webappRoot).toPath();
    projectsList = new File(_webappRoot).toPath().resolve("WEB-INF").resolve("projects-id-list.txt");
    projectsRoot = new File(_webappRoot).toPath().resolve("WEB-INF").resolve("projects");
    logger.debug("Root: {}", _webappRoot);
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
    loadProjects();


  }

  public static synchronized ProjectManager getProjectManager(String _webappRoot)
  {
    if(projectManager == null)
    {
      projectManager = new ProjectManager(_webappRoot);//, _slots);
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
      while(line!=null)
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
      logger.error("could not read text file "+projectsList.toString());
    }

  }


  private void saveProjectsList()
  {
    logger.info("Saving id list to: {}", projectsList.toString());

    try
    {
      synchronized (projects)
      {
        OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(projectsList.toFile()), "utf-8");
        for (String id : projects.keySet())
        {
          w.write(id + "\n");
        }
        w.close();
      }
    }
    catch(IOException ioe)
    {
      logger.error("error writing file", ioe);
    }
    logger.info("Saved: {}", projectsList.toFile());

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

  //TODO adapt this import to the definition in the file lists-io-def.xml, <list name="project-list">
  public void importProjects(ServletOutputStream outputStream, String fileName) throws IOException
  {
    Path p = webappRoot.resolve("WEB-INF").resolve(fileName);
    logger.info("Load data from {}", p.toString());
    BufferedReader brData = new BufferedReader(new FileReader(p.toFile()));
    String line = brData.readLine();
    String[] headerArr = line.split(";");
    line = brData.readLine();
    int lineCnt = 1;

    OutputStreamWriter w = new OutputStreamWriter(outputStream, "utf-8");
    JSONWriter json = new JSONWriter(w);
    json.object().key("total_before").value(projects.size());

    while(line!=null)
    {
      String[] lineArr = line.split(";");
      if(lineArr.length != headerArr.length)
      {
        logger.error("Ignore line {}: data length doesn't match ({}/{}).", lineCnt, headerArr.length, lineArr.length);
      }
      else
      {
        //TODO Get id index from the metadata description
        String id = lineArr[0];
        ProjectData pd = projects.get(id);
        if(pd == null)
        {
          pd = new ProjectData();
          pd.setId(id);
          projects.put(id, pd);
          saveProjectsList();
        }
        //TODO "headerArr" should be replaced with the list of field identifiers from the metadata description.
        //You can also replace array structures with something more appropriate, like a map of id/values
        pd.addFields(headerArr, lineArr);
        pd.save(projectsRoot);
      }
      line = brData.readLine();
      lineCnt++;
    }
    logger.info("Added {} projects, total {}.", lineCnt - 1, projects.size());
    json.object().key("total_added").value(lineCnt - 1);
    json.object().key("total_after").value(projects.size());
    json.endArray();
    json.endObject();
    w.flush();
    w.close();

  }

  //TODO same as import projects - import Mattermark data
  public void importMattermark(ServletOutputStream outputStream, String fileName) throws IOException
  {

    //  adapt this import to the definition in the file lists-io-def.xml, <list name="mattermark-export">

    /*
      You have to expand the ProjectData class
        - add a new map to it (like the existing "fields" map).
        - getters/setters, persistence (load/save)
        -add getter for clean-url metadata
     */

    /*
     1. go through all ProjectData instances and clear mattermark information
     2.load the file - use the clean-url usage information to match it with the correct ProjectData instance
      You may create a temporary map, where you have the clean-url as key in order to find the correct project.
      Save each ProjectData insance and the list.
     */
  }

  public void listProjects(ServletOutputStream outputStream) throws IOException
  {
    OutputStreamWriter w = new OutputStreamWriter(outputStream, "utf-8");
    logger.info("Return {} projects", projects.size());
    JSONWriter json = new JSONWriter(w);
    json.object().key("total").value(projects.size());
    json.key("projects").array();
    for (ProjectData pd: projects.values())
    {
      json.object();
      json.key("id").value(pd.getId());
      //TODO Add field keys for the list view.
      //addFieldKey(json, "Q1_1", pd.fields);
      json.endObject();
    }
    json.endArray();
    json.endObject();
    w.flush();
    w.close();
    logger.info("Returned {} projects", projects.size());

  }

  static final char newline = '\n';
  public static void  writeLine(BufferedWriter w, String s) throws IOException
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
      for(String s: arr)
      {
        columnsDef.add(s);
      }
    }

    synchronized(projects)
    {
      if(columnsDef.size() == 0)
      {
        for (ProjectData pd : projects.values())
        {
          for(String s : pd.fields.keySet())
            columnsDef.add(s);
        }
      }
    }

    String filename = "export_all_"+type+".txt";
    Path root = new File(exportDir).toPath();
    Path fOut = root.resolve(filename);
    BufferedWriter writerAllTXT = Files.newBufferedWriter(fOut, Charset.forName("UTF-8"), StandardOpenOption.CREATE);
    StringBuilder sb = new StringBuilder();
    sb.append("id");
    for (String s: columnsDef)
    {
      sb.append("\t").append(s);
    }

    String sHeader =  sb.toString();
    writeLine(writerAllTXT, sHeader);

    sb.setLength(0);

    synchronized (projects)
    {
      for (ProjectData pd: projects.values())
      {
        sb.append(pd.getId());
        for (String s : columnsDef)
        {
          sb.append("\t");
          String val = pd.fields.get(s);
          if (val != null)
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

  //TODO You may need in the list function
  private void addFieldKey(JSONWriter json, String qID, Map<String, String> questions)
  {
    String val = questions.get(qID);
    if(val != null)
      json.key(qID).value(val);
  }

  synchronized public void clearAllProjects(ServletOutputStream outputStream) throws IOException
  {
    OutputStreamWriter w = new OutputStreamWriter(outputStream, "utf-8");
    logger.info("Remove all {} projects", projects.size());
    JSONWriter json = new JSONWriter(w);
    json.object().key("total").value(projects.size());
    synchronized (projects)
    {
      for (String id : projects.keySet())
      {
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

  //TODO return the list of "indicator" field identifiers
  public ArrayList<String> getMattermarkIndicators()
  {
    return null;
  }

  //TODO when loading or importing mattermark data store min/max/n values for "indicator" fields in a data structure
  //and return them here
  public double getMattermarkMinValue(String fieldId)
  {
    return 0.0;
  }

  public double getMattermarkMaxValue(String fieldId)
  {
    return 0.0;
  }

  public int getMattermarkCount(String fieldId) //this is ne number of non empty fields
  {
    return 0;
  }

  //TODO check the implementation of SurveyManager.SPEEDOMETER_SLOTS
  /*you can have a static structure that gets boundaries as SPEEDOMETER_SLOTS. Entries are mattemrak "indicator" fields
  with the "MATTERMARK_" prefix. */
  public Map<String, OverallResult.ScoreBoundaries> getMattemrarkSlots()
  {
    return null;
  }


  public static void main(String[] args) throws Exception
  {
  }




}
