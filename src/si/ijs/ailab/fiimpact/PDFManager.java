package si.ijs.ailab.fiimpact;

import org.apache.fop.apps.FOPException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.MimeConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import si.ijs.ailab.util.AIUtils;

import javax.servlet.ServletOutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

/**
 * Created by flavio on 15/09/2015.
 */
public class PDFManager
{

  private static PDFManager pdfManager = null;
  private Path webappRoot;
  final static Logger logger = LogManager.getLogger(PDFManager.class.getName());
  private final Path fopDefRoot;
  private final Path pdfOutRoot;
  FopFactory fopFactory;

  public static synchronized PDFManager getPDFManager(String _webappRoot)
  {
    if(pdfManager == null)
    {
      pdfManager = new PDFManager(_webappRoot);
    }
    return pdfManager;
  }

  public static PDFManager getPDFManager()
  {
    return pdfManager;
  }

  private PDFManager(String _webappRoot)
  {
    webappRoot = new File(_webappRoot).toPath();
    logger.debug("Root: {}", _webappRoot);
    pdfOutRoot = webappRoot.resolve("pdf");
    fopDefRoot = webappRoot.resolve("WEB-INF").resolve("fop");

    if (Files.notExists(pdfOutRoot))
    {
      try
      {
        Files.createDirectory(pdfOutRoot);
        logger.debug("Created dir: {}", pdfOutRoot.toString());
      } catch (IOException e)
      {
        logger.error(e);
      }
    }

    try
    {
      // Step 1: Construct a FopFactory by specifying a reference to the configuration file
      // (reuse if you plan to render multiple documents!)
      fopFactory = FopFactory.newInstance(fopDefRoot.resolve("fop.xconf").toFile());

    }
    catch (SAXException | IOException e)
    {
      logger.error("Error creating FOP factory", e);

    }
  }

  private synchronized boolean createPDFInternal(String id) throws IOException
  {

    // Step 2: Set up output stream.
    // Note: Using BufferedOutputStream for performance reasons (helpful with FileOutputStreams).
    OutputStream out = null;
    boolean bStatus = false;
    logger.info("Create PDF for {}", id);

    try
    {

      out = new BufferedOutputStream(new FileOutputStream(pdfOutRoot.resolve("survey-" + id + ".pdf").toFile()));

      // Step 3: Construct fop with desired output format
      Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, out);

      // Step 4: Setup JAXP using identity transformer
      TransformerFactory factory = TransformerFactory.newInstance();
      Transformer transformer = factory.newTransformer(new StreamSource(fopDefRoot.resolve("report.xsl").toFile()));

      // Set the value of a <param> in the stylesheet
      transformer.setParameter("versionParam", "2.0");

      // Step 5: Setup input and output for XSLT transformation
      // Setup input stream
      ByteArrayOutputStream osSurvey = new ByteArrayOutputStream();
      SurveyManager.getSurveyManager().getXMLSurvey(osSurvey, id);
      InputStream inputStream = new ByteArrayInputStream(osSurvey.toByteArray());

      Source src = new StreamSource(inputStream);

      // Resulting SAX events (the generated FO) must be piped through to FOP
      Result res = new SAXResult(fop.getDefaultHandler());

      // Step 6: Start XSLT transformation and FOP processing
      transformer.transform(src, res);
      bStatus = true;

    }
    catch (FileNotFoundException | FOPException | TransformerException e)
    {
      logger.error("Error creating pdf for " + id, e);

    }
    finally
    {
      try
      {
        out.close();
      } catch (IOException e)
      {
        logger.error("Error creating pdf for " + id, e);
      }
    }
    logger.info("PDF {} created: ", id, bStatus);
    return bStatus;
  }

  public void  createPDF(OutputStream outputStream, String id) throws IOException
  {

    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db = null;
    try
    {
      db = dbf.newDocumentBuilder();
    } catch (ParserConfigurationException e)
    {
      logger.error("Can't believe this", e);
    }
    Document doc = db.newDocument();
    Element root = doc.createElement("status");
    doc.appendChild(root);
    root.setAttribute("external-id", id);
    boolean bStatus = createPDFInternal(id);

    if(bStatus)
      root.setAttribute("status", "OK");
    else
      root.setAttribute("status", "ERR");
    AIUtils.save(doc, outputStream);
  }

  synchronized public void createPDFAll(ServletOutputStream outputStream) throws IOException
  {
    Map<String, SurveyData> surveys = SurveyManager.getSurveyManager().getSurveys();
    int cntOK = 0;
    int cntERR = 0;
    logger.info("Create all PDFs");
    synchronized (surveys)
    {
      for (String id: surveys.keySet())
      {
        if(createPDFInternal(id))
          cntOK++;
        else
          cntERR++;

      }
    }
    logger.info("Create all PDFs - done");

    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db = null;
    try
    {
      db = dbf.newDocumentBuilder();
    } catch (ParserConfigurationException e)
    {
      logger.error("Can't believe this", e);
    }
    Document doc = db.newDocument();
    Element root = doc.createElement("status");
    doc.appendChild(root);
    root.setAttribute("ok", Integer.toString(cntOK));
    root.setAttribute("err", Integer.toString(cntERR));
    AIUtils.save(doc, outputStream);
  }
}
