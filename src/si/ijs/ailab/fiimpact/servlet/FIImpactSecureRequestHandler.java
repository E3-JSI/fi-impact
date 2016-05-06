package si.ijs.ailab.fiimpact.servlet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.json.JSONWriter;
import si.ijs.ailab.fiimpact.project.ProjectManager;
import si.ijs.ailab.fiimpact.survey.SurveyManager;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import si.ijs.ailab.util.AIUtils;


/**
 * Created by flavio on 01/06/2015.
 */
public class FIImpactSecureRequestHandler extends HttpServlet
{
  private final static Logger logger = LogManager.getLogger(FIImpactSecureRequestHandler.class.getName());
  private int MAX_REQUEST_SIZE = 10 * 1024 * 1024;
  private int MAX_TEMP_MEM_SIZE = 5 * 1024 * 1024;


  private String importDir;
  private String exportDir;

  private Path uploadDir;
  private Path tempUploadDir;

  private Path webappRoot;

  private ProjectManager projectManager;
	
  @Override
  public void init(ServletConfig config) throws ServletException
  {
    //E:\Dropbox\FI-IMPACT\data\FI-IMPACT_Export_20150624
    importDir = config.getServletContext().getInitParameter("import-dir");
    //E:\Dropbox\FI-IMPACT\data\export.txt
    exportDir = config.getServletContext().getInitParameter("export-dir");
    logger.info("import-dir={}", importDir);
    logger.info("export-dir={}", exportDir);

    String sUploadDir = config.getServletContext().getInitParameter("upload-dir");
    if(sUploadDir != null)
      uploadDir = (new File (sUploadDir)).toPath();
    logger.info("upload-dir={}", uploadDir.toString());
    try
    {
      if(Files.notExists(uploadDir))
        Files.createDirectory(uploadDir);
    }
    catch (IOException e)
    {
      logger.error("Cannot create upload directory");
    }

    String sTempUploadDir = config.getServletContext().getInitParameter("temp-upload-dir");
    if(sTempUploadDir != null)
      tempUploadDir = (new File(sTempUploadDir)).toPath();
    logger.info("temp-upload-dir={}", tempUploadDir.toString());
    try
    {
      if(Files.notExists(tempUploadDir))
        Files.createDirectory(tempUploadDir);
    }
    catch (IOException e)
    {
      logger.error("Cannot create temp upload directory");
    }


    webappRoot = new File (config.getServletContext().getRealPath("/")).toPath();
    projectManager=ProjectManager.getProjectManager(webappRoot);
  }


  private static final Map<String, String> ACTION_ROLES = new HashMap<>();

  {
    ACTION_ROLES.put("load", "fiimpact-upload");
    ACTION_ROLES.put("list", "fiimpact");
    ACTION_ROLES.put("clear", "fiimpact-upload");
    ACTION_ROLES.put("export", "fiimpact-export");
    ACTION_ROLES.put("refresh-projects", "fiimpact-upload");
    ACTION_ROLES.put("refresh-mattermark", "fiimpact-upload");
    ACTION_ROLES.put("user-profile", "fiimpact");
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {

    /**
     action:
      load
      list
     clear
     */
    String sAction = request.getParameter("action");
    //TODO later - export to csv - specification still in progress
    /*
      Adapt the exportTXT method to take into account addtional parameters and also to be able to export to either stream or file.
      action=csv
      response.setContentType("application/json"); - should be set to csv or text - check how should this be done.
      export to CSV parameters
      assesment=self/impact/all
      master=survey/project/merge
      data=survey;project;mattermark;survey-additional;project-aditional;mattermark-additional;

      scope=all,accelerator.

      scope has to be checked aginst the role of the current logged in user.
      we should have roles:
        accelerator (sees only accelerator filtered data),
        fiimpact (sees everything),
        fiware (sees all projects, but limited number of columns)

     */

   
    logger.info("Received request: action={}.", sAction);
    if (sAction == null || sAction.equals(""))
      setBadRequest(response, "Parameter 'action' not defined.");
    else if (!(sAction.equals("load") || sAction.equals("list") || sAction.equals("clear") || sAction.equals("export")|| sAction.equals("refresh-projects")||sAction.equals("refresh-mattermark") || sAction.equals("user-profile")))
      setBadRequest(response, "Parameter 'action' not valid: "+sAction);
    else if(!request.isUserInRole(ACTION_ROLES.get(sAction)))
    {
      setBadRequest(response, "User not authorised for "+sAction);
    }
    else if (sAction.equals("user-profile"))
    {
      response.setContentType("application/json");
      response.setCharacterEncoding("utf-8");
      OutputStreamWriter w = new OutputStreamWriter(response.getOutputStream(), "utf-8");
      JSONWriter json = new JSONWriter(w);
      json.object().key("user").value(request.getUserPrincipal().getName());
      json.key("admin").value(request.isUserInRole("fiimpact"));
      json.key("upload").value(request.isUserInRole("fiimpact-upload"));
      json.key("export").value(request.isUserInRole("fiimpact-export"));
      json.endObject();
      w.flush();
      w.close();
    }
    else if(sAction.equals("list"))
    {
      response.setContentType("application/json");
      response.setCharacterEncoding("utf-8");
      SurveyManager.getSurveyManager().list(response.getOutputStream());
    }
    else if (sAction.equals("load"))
    {
      //id is external
      response.setContentType("application/json");
      response.setCharacterEncoding("utf-8");
      //surveyManager.loadAll(response.getOutputStream(), "list.csv");
      SurveyManager.getSurveyManager().loadAllTest(response.getOutputStream(), importDir);
    }
    else if(sAction.equals("refresh-projects"))
    {
      Path p = webappRoot.resolve("WEB-INF").resolve("import/project-list.csv");
      projectManager.importProjects(response.getOutputStream(), p);
    }
    else if(sAction.equals("refresh-mattermark"))
    {
      Path p = webappRoot.resolve("WEB-INF").resolve("import/mattermark-export.csv");
      projectManager.importMattermark(response.getOutputStream(), p);
    }
    else if(sAction.equals("clear"))
    {
      response.setContentType("application/json");
      response.setCharacterEncoding("utf-8");
      SurveyManager.getSurveyManager().clearAll(response.getOutputStream());

    }
    else if (sAction.equals("export"))
    {
      String sType = request.getParameter("type");
      if(sType != null)
        sType = new String(sType.getBytes("iso-8859-1"), "UTF-8");

      if(sType == null || sType.equals(""))
        sType = "full";

      String groupQuestion = null;
      String groupAnswer = null;
      String idList = null;
      String questionsList = null;
      String resultsList = null;
      String resultsDerList = null;
      switch (sType)
      {
        case "full":
        {

          break;
        }
        case "accelerator":
        {
          groupQuestion = "Q1_1";
          questionsList = "Q1_1;Q1_2;Q1_3;Q1_4;Q1_22";
          resultsList = "FEASIBILITY;INNOVATION;MARKET;MARKET_NEEDS";
          resultsDerList = "";
          break;
        }
      }

      response.setContentType("application/x-unknown");
      response.setCharacterEncoding("utf-8");
      response.setHeader( "Content-Disposition", "filename=\"fi-impact-export.txt\"" );
      SurveyManager.getSurveyManager().exportTXT(response.getOutputStream(), groupQuestion, groupAnswer, questionsList, resultsList, resultsDerList, true, true);
    }
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, java.io.IOException
  {
    // Check that we have a file upload request
    boolean isMultipart = ServletFileUpload.isMultipartContent(request);
    if(!isMultipart)
    {
      setBadRequest(response, "Not a file upload request!");
    }
    else if(!(request.isUserInRole("fiimpact") && request.isUserInRole("fiimpact-upload")))
    {
      setBadRequest(response, "User not authorised for uploading");
    }
    else
    {
      response.setContentType("application/json");
      response.setCharacterEncoding("utf-8");
      // maximum size that will be stored in memory and temp location for larger uploads
      DiskFileItemFactory factory = new DiskFileItemFactory(MAX_TEMP_MEM_SIZE, tempUploadDir.toFile());

      // Create a new file upload handler
      ServletFileUpload upload = new ServletFileUpload(factory);

      // Set overall request size constraint
      upload.setSizeMax(MAX_REQUEST_SIZE);

      try
      {
        //action: upload-mattermark
        String sAction=null;
        FileItem postedFile = null;
        boolean bError = false;
        // Parse the request to get file items.
        List<FileItem> fileItems =  upload.parseRequest(request);
        // Process the uploaded file items
        Iterator<FileItem> i = fileItems.iterator();

        while(i.hasNext() && !bError)
        {
          FileItem fi = i.next();
          if(fi.isFormField())
          {
            String name = fi.getFieldName();
            String value = fi.getString();

            if(name.equals("action"))
            {
              sAction = value;
              logger.info("Received POST action={}.", sAction);
              if(sAction == null)
                setBadRequest(response, "Parameter 'action' not defined.");
              else if(!(sAction.equals("upload-mattermark")))
              {
                setBadRequest(response, "Parameter 'action' not valid: " + sAction);
                bError = true;
              }
            }
          }
          else
          {
            if (postedFile != null)
            {
              setBadRequest(response, "Multiple posted files!");
              bError = true;

            }
            else
            {
              postedFile = fi;

            }

          }
        }
        if(postedFile == null)
        {
          bError = true;
          logger.error("No file sent.");
        }
        if(sAction == null)
        {
          bError = true;
          logger.error("No action set.");
        }
        if(!bError)
        {
          String fieldName = postedFile.getFieldName();
          String fileName = postedFile.getName();
          String contentType = postedFile.getContentType();
          boolean isInMemory = postedFile.isInMemory();
          long sizeInBytes = postedFile.getSize();

          String sNameMattermark = "mattermark_"+ AIUtils.getTimestampDateFormat().format(new Date())+".csv";
          Path mattermarkFile = uploadDir.resolve(sNameMattermark);
          logger.info("Uploading Mattermark to: {}", mattermarkFile.toString());
          postedFile.write(mattermarkFile.toFile());
          logger.info("Importing Mattermark");
          projectManager.importMattermark(response.getOutputStream(), mattermarkFile);
          logger.info("Importing Mattermark - done");
        }
      }
      catch (Exception e)
      {
        logger.error("Error uploading file", e);
      }
    }
  }

  private void setBadRequest(HttpServletResponse response, String message) throws IOException
  {
    response.sendError(HttpServletResponse.SC_BAD_REQUEST, message);
    logger.error(message);
  }

}
