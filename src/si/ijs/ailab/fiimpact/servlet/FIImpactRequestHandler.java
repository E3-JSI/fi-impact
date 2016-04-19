package si.ijs.ailab.fiimpact.servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import javax.xml.parsers.ParserConfigurationException;

import java.io.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import si.ijs.ailab.fiimpact.pdf.PDFManager;
import si.ijs.ailab.fiimpact.project.ProjectManager;
import si.ijs.ailab.fiimpact.survey.SurveyManager;

/**
 * Created by flavio on 01/06/2015.
 */
public class FIImpactRequestHandler extends HttpServlet
{
  final static Logger logger = LogManager.getLogger(FIImpactRequestHandler.class.getName());
  SurveyManager surveyManager;
  PDFManager pdfManager;

  ProjectManager projectManager;

  @Override
  public void init(ServletConfig config) throws ServletException
  {
    projectManager=ProjectManager.getProjectManager(config.getServletContext().getRealPath("/"));
    surveyManager = SurveyManager.getSurveyManager(config.getServletContext().getRealPath("/"));
    pdfManager = pdfManager.getPDFManager(config.getServletContext().getRealPath("/"));
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {
    String sAction = request.getParameter("action");
    String sId = request.getParameter("id");
    if(sId != null)
      sId = new String(sId.getBytes("iso-8859-1"), "UTF-8");
    String[] arrQuestions = request.getParameterValues("q");

    //http://stackoverflow.com/questions/3029401/java-servlet-and-utf-8-problem
    //http://stackoverflow.com/questions/469874/how-do-i-correctly-decode-unicode-parameters-passed-to-a-servlet/12763865#12763865
    //final String param = new String(request.getParameter("param").getBytes("iso-8859-1"), "UTF-8");

    if(arrQuestions == null)
      arrQuestions = new String[0];
    for(int i = 0; i < arrQuestions.length; i++)
      if(arrQuestions[i] != null)
        arrQuestions[i] = new String(arrQuestions[i].getBytes("iso-8859-1"), "UTF-8");

    logger.info("Received request: action={} for {} with {} questions", sAction, sId, arrQuestions.length);
    if (sAction == null || sAction.equals(""))
      setBadRequest(response, "Parameter 'action' not defined.");
    else if (!(sAction.equals("add") || sAction.equals("remove") || sAction.equals("resultsxml")  ||sAction.equals("resultsnew") || sAction.equals("averages") || sAction.equals("pdf") || sAction.equals("allpdf")))
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
        surveyManager.addSurvey(response.getOutputStream(), arrQuestions, sId);

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
        surveyManager.removeSurvey(response.getOutputStream(), sId);
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
        surveyManager.getJSONSurvey(response.getOutputStream(), sId);
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
      surveyManager.getXMLSurvey(response.getOutputStream(), sId);
    }

    }
    else if(sAction.equals("averages"))
    {
      response.setContentType("application/json");
      response.setCharacterEncoding("utf-8");
      String sType = request.getParameter("type");
      if(sType == null || sType.equals(""))
      {
        sType = SurveyManager.QUESTIONNAIRE_TYPE_DEFAULT;
        logger.warn("averages type not defined. Default to {}", sType);
      }

      surveyManager.getAverages(sType, response.getOutputStream());
    }
  }

  private void setBadRequest(HttpServletResponse response, String message) throws IOException
  {
    response.sendError(HttpServletResponse.SC_BAD_REQUEST, message);
    logger.error(message);
  }

}
