package si.ijs.ailab.fiimpact;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by flavio on 01/06/2015.
 */
public class FIImpactSecureRequestHandler extends HttpServlet
{
  final static Logger logger = LogManager.getLogger(FIImpactSecureRequestHandler.class.getName());
  //SurveyManager surveyManager;
  String importDir;
  String exportFile;

  @Override
  public void init(ServletConfig config) throws ServletException
  {
    //surveyManager = SurveyManager.getSurveyManager(config.getServletContext().getRealPath("/"));
    //E:\Dropbox\FI-IMPACT\data\FI-IMPACT_Export_20150624
    importDir = config.getServletContext().getInitParameter("import-dir");
    //E:\Dropbox\FI-IMPACT\data\export.txt
    exportFile = config.getServletContext().getInitParameter("export-file");
    logger.info("import-dir={}", importDir);
    logger.info("export-file={}", exportFile);
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

    logger.info("Received request: action={}.", sAction);
    if (sAction == null || sAction.equals(""))
      setBadRequest(response, "Parameter 'action' not defined.");
    else if (!(sAction.equals("load") || sAction.equals("list") || sAction.equals("clear") || sAction.equals("export")))
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
      response.setContentType("application/json");
      response.setCharacterEncoding("utf-8");
      SurveyManager.getSurveyManager().exportTXT(response.getOutputStream(), exportFile);
    }
  }

  private void setBadRequest(HttpServletResponse response, String message) throws IOException
  {
    response.sendError(HttpServletResponse.SC_BAD_REQUEST, message);
    logger.error(message);
  }

}
