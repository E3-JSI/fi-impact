package si.ijs.ailab.fiimpact.servlet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import si.ijs.ailab.fiimpact.qminer.QMinerManager;
import si.ijs.ailab.fiimpact.settings.FIImpactSettings;
import si.ijs.ailab.fiimpact.users.UserInfo;
import si.ijs.ailab.fiimpact.users.UsersManager;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
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

  private Path uploadDir;
  private Path tempUploadDir;

  private Path webappRoot;

  private UsersManager usersManager;

  @Override
  public void init(ServletConfig config) throws ServletException
  {

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
    usersManager=UsersManager.getUsersManager(webappRoot);
  }


  private static final Map<String, String> GET_ACTION_ROLES = new HashMap<>();
  private static final Map<String, String> POST_ACTION_ROLES = new HashMap<>();

  {
    GET_ACTION_ROLES.put("list", "admin");
    GET_ACTION_ROLES.put("clear", "upload");
    GET_ACTION_ROLES.put("export", "export");
    GET_ACTION_ROLES.put("export-json", "upload-extended");

    GET_ACTION_ROLES.put(QMinerManager.ACTION_START_UPDATE_JOB, "upload-extended");
    GET_ACTION_ROLES.put(QMinerManager.ACTION_STOP_UPDATE_JOB, "upload-extended");
    GET_ACTION_ROLES.put(QMinerManager.ACTION_POST_DATASET, "upload-extended");
    GET_ACTION_ROLES.put(QMinerManager.ACTION_GET_STATUS, "upload-extended");

    GET_ACTION_ROLES.put("user-profile", "admin");
    GET_ACTION_ROLES.put("accelerators", "admin");
    GET_ACTION_ROLES.put("legend", "export");

    GET_ACTION_ROLES.put("roles", "user-management");

    GET_ACTION_ROLES.put("user-list", "user-management");
    GET_ACTION_ROLES.put("user-get", "user-management");

    POST_ACTION_ROLES.put("upload-mattermark", "upload");
    POST_ACTION_ROLES.put("upload-mapping", "upload-extended");
    POST_ACTION_ROLES.put("upload-projects", "upload-extended");

    //user management
    POST_ACTION_ROLES.put("user-create", "user-management");
    POST_ACTION_ROLES.put("user-delete", "user-management");
    POST_ACTION_ROLES.put("user-edit", "user-management");
    POST_ACTION_ROLES.put("user-my-password", "admin");

  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {

    //action: see GET_ACTION_ROLES
    String sAction = request.getParameter("action");

    String userName = request.getUserPrincipal().getName();
    UserInfo userInfo = usersManager.getUserInfo(userName);

    logger.info("Received request: action={}.", sAction);
    if (sAction == null || sAction.equals(""))
      setBadRequest(response, "Parameter 'action' not defined.");
    else if (!GET_ACTION_ROLES.containsKey(sAction))
      setBadRequest(response, "Parameter 'action' not valid: "+sAction);
    else if(!userInfo.getAccessRights().contains(GET_ACTION_ROLES.get(sAction)))
    {
      setBadRequest(response, "User not authorised for "+sAction);
    }
    else if (sAction.equals("user-profile"))
    {
      response.setContentType("application/json");
      response.setCharacterEncoding("utf-8");
      userInfo.getProfile(response.getOutputStream(), "user-profile");
    }
    else if (sAction.equals("user-list"))
    {
      response.setContentType("application/json");
      response.setCharacterEncoding("utf-8");
      usersManager.getUsersList(response.getOutputStream(), userInfo);
    }
    else if (sAction.equals("user-get"))
    {
      response.setContentType("application/json");
      response.setCharacterEncoding("utf-8");
      String sUserNameParam = request.getParameter("user");
      if(sUserNameParam != null)
        sUserNameParam = new String(sUserNameParam.getBytes("iso-8859-1"), "UTF-8");

      usersManager.getUserProfile(response.getOutputStream(), sUserNameParam, userInfo);

    }
    else if (sAction.equals("roles"))
    {
      response.setContentType("application/json");
      response.setCharacterEncoding("utf-8");
      usersManager.getRoles(response.getOutputStream(), userInfo);
    }
    else if(sAction.equals("list"))
    {
      response.setContentType("application/json");
      response.setCharacterEncoding("utf-8");

      String groupQuestion = null;
      String groupAnswer = null;


      if(!userInfo.getAccelerator().equals(""))
      {
        groupQuestion = "Q1_1";
        groupAnswer = userInfo.getAccelerator();

      }
      FIImpactSettings.getFiImpactSettings().getSurveyManager().list(response.getOutputStream(), groupQuestion, groupAnswer);
    }
    else if(sAction.equals("clear"))
    {
      response.setContentType("application/json");
      response.setCharacterEncoding("utf-8");
      FIImpactSettings.getFiImpactSettings().getSurveyManager().clearAll(response.getOutputStream());

    }
    else if(sAction.equals("accelerators"))
    {
      response.setContentType("application/json");
      response.setCharacterEncoding("utf-8");
      FIImpactSettings.getFiImpactSettings().getSurveyManager().getJSONAccelerators(response.getOutputStream(), userInfo.getAccelerator());
    }
    else if (sAction.equals("export"))
    {
      //Export to csv - specification still in progress

      String sType = request.getParameter("type");
      if(sType != null)
        sType = new String(sType.getBytes("iso-8859-1"), "UTF-8");

      if(sType == null || sType.equals(""))
        sType = "full";

      String groupQuestion = null;
      String groupAnswer = null;

      int exportAction = 0;

      switch (sType)
      {
        case "full":
        {
          exportAction = FIImpactSettings.EXPORT_FI_IMPACT_QUESTIONS | FIImpactSettings.EXPORT_FI_IMPACT_INDICATORS |
                  FIImpactSettings.EXPORT_MATTERMARK_FIELDS | FIImpactSettings.EXPORT_MATTERMARK_INDICATORS |
                  FIImpactSettings.EXPORT_DERIVED_INDICATORS | FIImpactSettings.EXPORT_PROJECT_DATA;

          break;
        }
        case "full-no-derived":
        {
          exportAction = FIImpactSettings.EXPORT_FI_IMPACT_QUESTIONS | FIImpactSettings.EXPORT_FI_IMPACT_INDICATORS |
                  FIImpactSettings.EXPORT_MATTERMARK_FIELDS | FIImpactSettings.EXPORT_MATTERMARK_INDICATORS |
                  FIImpactSettings.EXPORT_PROJECT_DATA;
          break;
        }
        case "short":
        {
          exportAction = FIImpactSettings.EXPORT_SHORT_LIST;
          break;
        }
        case "accelerator":
        {
          groupQuestion = "Q1_1";
          exportAction = FIImpactSettings.EXPORT_SHORT_LIST;
          groupAnswer = request.getParameter("id");
          if(groupAnswer != null)
            groupAnswer = new String(groupAnswer.getBytes("iso-8859-1"), "UTF-8");

          break;
        }
      }

      //in case user has the accelerator role set, override the group filter
      if(!userInfo.getAccelerator().equals(""))
      {
        groupQuestion = "Q1_1";
        groupAnswer = userInfo.getAccelerator();
      }

      response.setContentType("application/x-unknown");
      response.setCharacterEncoding("utf-8");
      response.setHeader( "Content-Disposition", "filename=\"fi-impact-export.txt\"" );
      FIImpactSettings.getFiImpactSettings().getSurveyManager().exportText(response.getOutputStream(), groupQuestion, groupAnswer, exportAction);

    }
    else if (sAction.equals("export-json"))
    {
      response.setContentType("application/x-unknown");
      response.setCharacterEncoding("utf-8");
      response.setHeader( "Content-Disposition", "filename=\"fi-impact-export.json\"" );
      FIImpactSettings.getFiImpactSettings().getSurveyManager().exportJson(response.getOutputStream());

    }
    else if (sAction.equals(QMinerManager.ACTION_START_UPDATE_JOB))
    {
      FIImpactSettings.getFiImpactSettings().getQMinerManager().actionStartJob(response.getOutputStream());
    }
    else if (sAction.equals(QMinerManager.ACTION_STOP_UPDATE_JOB))
    {
      FIImpactSettings.getFiImpactSettings().getQMinerManager().actionStopJob(response.getOutputStream());
    }
    else if (sAction.equals(QMinerManager.ACTION_POST_DATASET))
    {
      FIImpactSettings.getFiImpactSettings().getQMinerManager().actionPostDataset(response.getOutputStream());
    }
    else if (sAction.equals(QMinerManager.ACTION_GET_STATUS))
    {
      FIImpactSettings.getFiImpactSettings().getQMinerManager().actionGetStatus(response.getOutputStream());
    }
    else if (sAction.equals("legend"))
    {
      //Export legend to csv
      response.setContentType("application/x-unknown");
      response.setCharacterEncoding("utf-8");
      response.setHeader( "Content-Disposition", "filename=\"fi-impact-legend.txt\"" );

      FIImpactSettings.getFiImpactSettings().exportLegendTxt(response.getOutputStream());
    }
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, java.io.IOException
  {
    // Check that we have a file upload request
    boolean isMultipart = ServletFileUpload.isMultipartContent(request);
    String userName = request.getUserPrincipal().getName();
    UserInfo userInfo = usersManager.getUserInfo(userName);

    if(!isMultipart)
    {
      String sAction = request.getParameter("action");
      logger.info("Received POST request: action={}.", sAction);
      if (sAction == null || sAction.equals(""))
        setBadRequest(response, "Parameter 'action' not defined.");
      else if (!POST_ACTION_ROLES.containsKey(sAction))
        setBadRequest(response, "Post parameter 'action' not valid: "+sAction);
      else if(!userInfo.getAccessRights().contains(POST_ACTION_ROLES.get(sAction)))
      {
        setBadRequest(response, "User not authorised for "+sAction);
      }
      else if (sAction.equals("user-create"))
      {
        String user = request.getParameter("user");
        String password = request.getParameter("password");
        String accelerator = request.getParameter("accelerator");
        String description = request.getParameter("description");
        if(user==null || user.equals("") || password == null || password.equals("") || description==null || description.equals(""))
        {
          setBadRequest(response, "Plase provide user/password/description parameters for "+sAction);
        }
        else
          usersManager.addUser(response.getOutputStream(), user, password, accelerator, description, userInfo);
      }
      else if (sAction.equals("user-delete"))
      {
        String user = request.getParameter("user");
        if(user==null || user.equals(""))
        {
          setBadRequest(response, "Plase provide user parameter for "+sAction);
        }
        else
          usersManager.deleteUser(response.getOutputStream(), user, userInfo);
      }
      else if (sAction.equals("user-edit"))
      {
        String user = request.getParameter("user");
        String password = request.getParameter("password");
        String accelerator = request.getParameter("accelerator");
        String[] roles = request.getParameterValues("role");

        if(user==null || user.equals(""))
        {
          setBadRequest(response, "Plase provide user for "+sAction);
        }
        else
          usersManager.editUser(response.getOutputStream(), user, password, accelerator, roles, userInfo);
      }
      else if (sAction.equals("user-my-password"))
      {
        String oldPassword = request.getParameter("password-old");
        String newPassword = request.getParameter("password-new");
        if(oldPassword == null || oldPassword.equals("") || newPassword == null || newPassword.equals(""))
        {
          setBadRequest(response, "Plase provide user/password-old/password-new parameters for "+sAction);
        }
        else
          usersManager.changeMyPassword(response.getOutputStream(), userInfo, oldPassword, newPassword );
      }

    }
    else if(!(userInfo.getAccessRights().contains("admin") && userInfo.getAccessRights().contains("upload")))
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
              {
                setBadRequest(response, "Parameter 'action' not defined.");
                bError = true;
              }
              else if(!POST_ACTION_ROLES.containsKey(sAction))
              {
                setBadRequest(response, "Parameter 'action' not valid: " + sAction);
                bError = true;
              }
              else if(!userInfo.getAccessRights().contains(POST_ACTION_ROLES.get(sAction)))
              {
                setBadRequest(response, "User not authorised: " + sAction);
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

          String sFileName = sAction+"_"+ AIUtils.getTimestampDateFormat().format(new Date())+".csv";
          Path pFile = uploadDir.resolve(sFileName);
          logger.info("Uploading {}  to: {}", sAction, pFile.toString());
          postedFile.write(pFile.toFile());
          logger.info("Importing {}", sAction);
          switch(sAction)
          {
            case "upload-mattermark":
              FIImpactSettings.getFiImpactSettings().getProjectManager().importMattermark(response.getOutputStream(), pFile);
              break;
            case "upload-mapping":
              FIImpactSettings.getFiImpactSettings().getProjectManager().importMappings(response.getOutputStream(), pFile);
            break;
            case "upload-projects":
              FIImpactSettings.getFiImpactSettings().getProjectManager().importProjects(response.getOutputStream(), pFile);
            break;
          }

          logger.info("Importing {} - done", sAction);
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
