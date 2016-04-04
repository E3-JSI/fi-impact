package si.ijs.ailab.fiimpact.servlet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import si.ijs.ailab.fiimpact.project.ProjectManager;
import si.ijs.ailab.fiimpact.survey.SurveyManager;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;

import java.io.IOException;

/**
 * Created by flavio on 01/06/2015.
 */
public class FIImpactSecureRequestHandler extends HttpServlet
{
  final static Logger logger = LogManager.getLogger(FIImpactSecureRequestHandler.class.getName());
  String importDir;
  String exportDir;
  ProjectManager projectManager;
	
  @Override
  public void init(ServletConfig config) throws ServletException
  {
    //E:\Dropbox\FI-IMPACT\data\FI-IMPACT_Export_20150624
    importDir = config.getServletContext().getInitParameter("import-dir");
    //E:\Dropbox\FI-IMPACT\data\export.txt
    exportDir = config.getServletContext().getInitParameter("export-dir");
    logger.info("import-dir={}", importDir);
    logger.info("export-dir={}", exportDir);
    

    try {
		projectManager=ProjectManager.getProjectManager(config.getServletContext().getRealPath("/"));
	} catch (ParserConfigurationException | SAXException | IOException e) {
		logger.error(e.getMessage().toString());
	}
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
    //TODO add actions "refresh-projects", "refresh-mattermark"

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
    else if (!(sAction.equals("load") || sAction.equals("list") || sAction.equals("clear") || sAction.equals("export")|| sAction.equals("refresh-projects")||sAction.equals("refresh-mattermark")))
      setBadRequest(response, "Parameter 'action' not valid: "+sAction);
    else if (sAction.equals("load"))
    {
      //id is external
      response.setContentType("application/json");
      response.setCharacterEncoding("utf-8");
      //surveyManager.loadAll(response.getOutputStream(), "list.csv");
      SurveyManager.getSurveyManager().loadAllTest(response.getOutputStream(), importDir);
    }
    else if(sAction.equals("list"))
    {
      response.setContentType("application/json");
      response.setCharacterEncoding("utf-8");
      SurveyManager.getSurveyManager().list(response.getOutputStream());
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
          idList = "id_external";
          questionsList = "Q1_1;Q1_2;Q1_3;Q1_4;Q1_22";
          resultsList = "FEASIBILITY;INNOVATION;MARKET;MARKET_NEEDS";
          resultsDerList = "";
          break;
        }
      }

      response.setContentType("application/json");
      response.setCharacterEncoding("utf-8");
      SurveyManager.getSurveyManager().exportTXT(response.getOutputStream(), exportDir, sType, groupQuestion, idList, questionsList, resultsList, resultsDerList);
    }
    else if(sAction.equals("refresh-projects"))
    {
      projectManager.importProjects(response.getOutputStream(), "import/project-list.csv");   	
    }
    else if(sAction.equals("refresh-mattermark"))
    {
	  projectManager.importProjects(response.getOutputStream(), "import/mattermark-export.csv");
    }
  }

  private void setBadRequest(HttpServletResponse response, String message) throws IOException
  {
    response.sendError(HttpServletResponse.SC_BAD_REQUEST, message);
    logger.error(message);
  }

}
