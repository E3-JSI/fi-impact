package si.ijs.ailab.fiimpact.servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import si.ijs.ailab.fiimpact.pdf.PDFManager;
import si.ijs.ailab.fiimpact.qminer.QMinerManager;
import si.ijs.ailab.fiimpact.settings.FIImpactSettings;

/**
 * Created by flavio on 01/06/2015.
 */
public class FIImpactRequestHandler extends HttpServlet
{
  private final static Logger logger = LogManager.getLogger(FIImpactRequestHandler.class.getName());
  private PDFManager pdfManager;


  @Override
  public void init(ServletConfig config) throws ServletException
  {
    FIImpactSettings.createSettings(config.getServletContext());
    pdfManager = PDFManager.getPDFManager(config.getServletContext().getRealPath("/"));
  }

  private static Set<String> ALLOWED_ACTIONS;
  static
  {
    ALLOWED_ACTIONS = new HashSet<>();
    ALLOWED_ACTIONS.add("add");
    ALLOWED_ACTIONS.add("remove");
    ALLOWED_ACTIONS.add("resultsxml");
    ALLOWED_ACTIONS.add("resultsnew");
    ALLOWED_ACTIONS.add("averages");
    ALLOWED_ACTIONS.add("pdf");
    ALLOWED_ACTIONS.add("allpdf");
    ALLOWED_ACTIONS.add("plot");
    ALLOWED_ACTIONS.add("plot-legend");
    ALLOWED_ACTIONS.add("profile");
    ALLOWED_ACTIONS.add(QMinerManager.ACTION_GET_GRAPH_TEXT_FEATURE);
    ALLOWED_ACTIONS.add(QMinerManager.ACTION_GET_GRAPH_TEXT_FEATURE);

  }
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {
    String sAction = request.getParameter("action");
    String sId = request.getParameter("id");
    if(sId != null)
      sId = new String(sId.getBytes("iso-8859-1"), "UTF-8");

    //http://stackoverflow.com/questions/3029401/java-servlet-and-utf-8-problem
    //http://stackoverflow.com/questions/469874/how-do-i-correctly-decode-unicode-parameters-passed-to-a-servlet/12763865#12763865
    //final String param = new String(request.getParameter("param").getBytes("iso-8859-1"), "UTF-8");
    String[] arrQuestions = request.getParameterValues("q");
    if(arrQuestions == null)
      arrQuestions = new String[0];
    for(int i = 0; i < arrQuestions.length; i++)
      if(arrQuestions[i] != null)
        arrQuestions[i] = new String(arrQuestions[i].getBytes("iso-8859-1"), "UTF-8");

    logger.info("Received request: action={} for {} with {} questions", sAction, sId, arrQuestions.length);
    if (sAction == null || sAction.equals(""))
      setBadRequest(response, "Parameter 'action' not defined.");
    else if (!ALLOWED_ACTIONS.contains(sAction))
      setBadRequest(response, "Parameter 'action' not valid: "+sAction);
    else if (sAction.equals("add"))
    {
      if (sId == null || sId.equals(""))
        setBadRequest(response, "Parameter 'id' not defined.");
      else
      {
        //id is external
        response.setContentType("application/xml");
        response.setCharacterEncoding("utf-8");
        FIImpactSettings.getFiImpactSettings().getSurveyManager().addSurvey(response.getOutputStream(), arrQuestions, sId);

      }
    }
    else if (sAction.equals("pdf"))
    {
      if (sId == null || sId.equals(""))
        setBadRequest(response, "Parameter 'id' not defined.");
      else
      {
        //id is internal
        response.setContentType("application/xml");
        response.setCharacterEncoding("utf-8");
        pdfManager.createPDF(response.getOutputStream(), sId);

      }
    }
    else if (sAction.equals("allpdf"))
    {
        response.setContentType("application/xml");
        response.setCharacterEncoding("utf-8");
        pdfManager.createPDFAll(response.getOutputStream());

    }
    else if(sAction.equals("remove"))
    {
      if (sId == null || sId.equals(""))
        setBadRequest(response, "Parameter 'id' not defined.");
      else
      {
        //id is external
        response.setContentType("application/xml");
        response.setCharacterEncoding("utf-8");
        FIImpactSettings.getFiImpactSettings().getSurveyManager().removeSurvey(response.getOutputStream(), sId);
      }
    }
    else if(sAction.equals("resultsnew"))
    {
      if (sId == null || sId.equals(""))
        setBadRequest(response, "Parameter 'id' not defined.");
      else
      {
        //id is internal
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        FIImpactSettings.getFiImpactSettings().getSurveyManager().getJSONSurvey(response.getOutputStream(), sId);
      }

    }
    else if(sAction.equals("profile"))
    {
      if (sId == null || sId.equals(""))
        setBadRequest(response, "Parameter 'id' not defined.");
      else
      {
        //id is internal
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        FIImpactSettings.getFiImpactSettings().getSurveyManager().getJSONProjectProfile(response.getOutputStream(), sId);
      }

    }
    else if(sAction.equals("resultsxml"))
    {
      if (sId == null || sId.equals(""))
        setBadRequest(response, "Parameter 'id' not defined.");
      else
      {
      //id is internal
      response.setContentType("application/xml");
      response.setCharacterEncoding("utf-8");
        FIImpactSettings.getFiImpactSettings().getSurveyManager().getXMLSurvey(response.getOutputStream(), sId);
    }

    }
    else if(sAction.equals("averages"))
    {
      response.setContentType("application/json");
      response.setCharacterEncoding("utf-8");
      String sType = request.getParameter("type");
      if(sType == null || sType.equals(""))
      {
        sType = FIImpactSettings.QUESTIONNAIRE_TYPE_DEFAULT;
        logger.warn("averages type not defined. Default to {}", sType);
      }

      FIImpactSettings.getFiImpactSettings().getSurveyManager().getAverages(sType, response.getOutputStream());
    }
    else if(sAction.equals("plot"))
    {
      response.setContentType("application/json");
      response.setCharacterEncoding("utf-8");

      FIImpactSettings.getFiImpactSettings().getSurveyManager().getPlotJSON(response.getOutputStream(), sId);
    }
    else if(sAction.equals(QMinerManager.ACTION_GET_GRAPH_TEXT_FEATURE) || ALLOWED_ACTIONS.add(QMinerManager.ACTION_GET_GRAPH_ALL_FEATURES))
    {
      response.setContentType("application/json");
      response.setCharacterEncoding("utf-8");
      FIImpactSettings.getFiImpactSettings().getQMinerManager().actionGetGraph(response.getOutputStream(), sId, sAction);
    }
    else if (sAction.equals("plot-legend"))
    {
      response.setContentType("application/json");
      response.setCharacterEncoding("utf-8");
      FIImpactSettings.getFiImpactSettings().getPlotLegend(response.getOutputStream());
    }

  }

  private void setBadRequest(HttpServletResponse response, String message) throws IOException
  {
    response.sendError(HttpServletResponse.SC_BAD_REQUEST, message);
    logger.error(message);
  }

}
