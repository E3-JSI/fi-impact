package si.ijs.ailab.fiimpact.qminer;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONWriter;
import si.ijs.ailab.fiimpact.settings.FIImpactSettings;
import si.ijs.ailab.util.AIUtils;

import java.io.*;
import java.util.*;

/**
 * Created by flavio on 07/06/2016.
 */
public class QMinerManager extends TimerTask
{

  public static final String ACTION_START_UPDATE_JOB = "q-start-job";
  public static final String ACTION_STOP_UPDATE_JOB = "q-stop-job";
  public static final String ACTION_POST_DATASET = "q-post-dataset";
  public static final String ACTION_GET_STATUS = "q-get-status";
  public static final String ACTION_GET_GRAPH = "q-get-graph";


  private final String QMINER_ENDPOINT_POST_DATASET = "post_data";
  private final String QMINER_ENDPOINT_CALC_DATASET = "main_graph_async";
  private final String QMINER_ENDPOINT_STATUS = "status";
  private final String QMINER_ENDPOINT_GRAPH = "custom_graph";
  private final String QMINER_ENDPOINT_POST_GET_GRAPH = "custom_graph_full_record";

  private final static Logger logger = LogManager.getLogger(QMinerManager.class.getName());
  private final int HTTP_TIMEOUT = 60 * 1000; // one minute timeout

  private String qMinerRootURL;

  private Timer timer;
  private boolean bRun = false;

  public QMinerManager(String _url)
  {
    qMinerRootURL = _url;
  }
  /**
   * Return all contents of an input stream. Like python's .read().
   */
  private byte[] readAll(InputStream stream)
  {

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];
    int len;
    try {
      while ((len = stream.read(buffer)) > -1 ) {
        baos.write(buffer, 0, len);
      }
      baos.flush();
    } catch (Exception e) {
      return null;
    }
    return baos.toByteArray();
  }

  private HttpClient createHttpClient()
  {
    HttpClient httpClient = new DefaultHttpClient();
    HttpParams httpParams = httpClient.getParams();
    HttpConnectionParams.setConnectionTimeout(httpParams, HTTP_TIMEOUT);
    HttpConnectionParams.setSoTimeout(httpParams, HTTP_TIMEOUT);
    return httpClient;
  }


  private String postDataset()
  {
    String status = null;
    HttpClient httpClient = null;
    try
    {
      logger.info("Prepare qMiner call");

      httpClient = createHttpClient();

      ByteArrayOutputStream out = new ByteArrayOutputStream();
      FIImpactSettings.getFiImpactSettings().getSurveyManager().exportJson(out);
      logger.debug("Prepare post entity");
      ByteArrayEntity ent = new ByteArrayEntity(out.toByteArray());
      ent.setContentEncoding(new BasicHeader("Content-Type", "application/json"));
      String sUrl = qMinerRootURL + QMINER_ENDPOINT_POST_DATASET;
      HttpPost post = new HttpPost(sUrl);
      post.setEntity(ent);
      logger.debug("About to call qMiner: {}", sUrl);
      HttpResponse response = httpClient.execute(post);
      logger.debug("Service returned {}", response.getStatusLine().getStatusCode());
      if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
      {
        logger.info("QMiner post status OK: {}", response.getStatusLine());
        EntityUtils.consumeQuietly(response.getEntity());
        logger.debug("cleared response entity.");
      }
      else
      {
        status = "Error executing service: " + response.getStatusLine();
        logger.error("Error executing service: {}", response.getStatusLine());
        EntityUtils.consumeQuietly(response.getEntity());
        logger.debug("cleared response entity.");
      }
      //no errors, start calculating graph
      if(status == null)
      {

        logger.debug("Prepare get");
        sUrl = qMinerRootURL + QMINER_ENDPOINT_CALC_DATASET;
        HttpGet get = new HttpGet(sUrl);
        logger.debug("About to call qMiner: {}", sUrl);
        response = httpClient.execute(get);
        logger.debug("Service returned {}", response.getStatusLine().getStatusCode());
        if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
        {
          logger.info("QMiner post status OK: {}", response.getStatusLine());
          EntityUtils.consumeQuietly(response.getEntity());
          logger.debug("cleared response entity.");
        }
        else
        {
          status = "Error executing service: " + response.getStatusLine();
          logger.error("Error executing service: {}", response.getStatusLine());
          EntityUtils.consumeQuietly(response.getEntity());
          logger.debug("cleared response entity.");
        }

      }
    }
    catch (IOException e)
    {
      status = "Error calling QMiner service: " + e.getLocalizedMessage();
      logger.error("Error calling QMiner service", e);
    }
    finally
    {
      if(httpClient != null)
        httpClient.getConnectionManager().shutdown();
    }
    logger.debug("Done.");
    return status;

  }

  private String getGraphForFullRecord(OutputStream os, String id)
  {
    String status = null;
    HttpClient httpClient = null;
    try
    {
      logger.info("Prepare qMiner get graph for full record");

      httpClient = createHttpClient();

      ByteArrayOutputStream out = new ByteArrayOutputStream();
      FIImpactSettings.getFiImpactSettings().getSurveyManager().exportQMinerJsonSurvey(out, id);
      logger.debug("Prepare post entity");
      ByteArrayEntity ent = new ByteArrayEntity(out.toByteArray());
      ent.setContentEncoding(new BasicHeader("Content-Type", "application/json"));
      String sUrl = qMinerRootURL + QMINER_ENDPOINT_POST_GET_GRAPH;
      HttpPost post = new HttpPost(sUrl);
      post.setEntity(ent);
      logger.debug("About to call qMiner: {}", sUrl);
      HttpResponse response = httpClient.execute(post);
      logger.debug("Service returned {}", response.getStatusLine().getStatusCode());
      if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
      {
        logger.info("QMiner post status OK: {}", response.getStatusLine());
        HttpEntity entity = response.getEntity();
        if(entity != null)
        {
          //parse result....
          byte[] responseData = readAll(entity.getContent());
          AIUtils.streamCopy(new ByteArrayInputStream(responseData), os);
        }
      }
      else
      {
        status = "Error executing service: " + response.getStatusLine();
        logger.error("Error executing service: {}", response.getStatusLine());
        EntityUtils.consumeQuietly(response.getEntity());
        logger.debug("cleared response entity.");
      }
    }
    catch (IOException e)
    {
      status = "Error calling QMiner service: " + e.getLocalizedMessage();
      logger.error("Error calling QMiner service", e);
    }
    finally
    {
      if(httpClient != null)
        httpClient.getConnectionManager().shutdown();
    }
    logger.debug("Done.");
    return status;

  }

  private String getStatus(OutputStream os)
  {
    String status = null;
    HttpClient httpClient = null;
    try
    {
      logger.info("Prepare qMiner get status call");

      httpClient = createHttpClient();

      /*ByteArrayOutputStream out = new ByteArrayOutputStream();
      ByteArrayEntity ent = new ByteArrayEntity(out.toByteArray());
      ent.setContentEncoding(new BasicHeader("Content-Type", "text/xml"));*/
      String sUrl = qMinerRootURL + QMINER_ENDPOINT_STATUS;
      HttpGet get = new HttpGet(sUrl);
      //post.setEntity(ent);
      logger.debug("About to call qMiner: {}", sUrl);
      HttpResponse response = httpClient.execute(get);
      logger.debug("Service returned {}", response.getStatusLine().getStatusCode());
      if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
      {
        logger.info("QMiner post status OK: {}", response.getStatusLine());
        HttpEntity entity = response.getEntity();
        if(entity != null)
        {
          //parse result....
          byte[] responseData = readAll(entity.getContent());
          AIUtils.streamCopy(new ByteArrayInputStream(responseData), os);
        }
      }
      else
      {
        status = "Error executing service: " + response.getStatusLine();
        logger.error("Error executing service: {}", response.getStatusLine());
        EntityUtils.consumeQuietly(response.getEntity());
        logger.debug("cleared response entity.");
      }
    }
    catch (IOException e)
    {
      status = "Error calling QMiner service: " + e.getLocalizedMessage();
      logger.error("Error calling QMiner service", e);
    }
    finally
    {
      if(httpClient != null)
        httpClient.getConnectionManager().shutdown();
    }
    logger.debug("Done.");
    return status;

  }

  private String getGraph(OutputStream os, String id)
  {
    String status = null;
    HttpClient httpClient = null;
    try
    {
      logger.info("Prepare qMiner get graph call");

      httpClient = createHttpClient();

      String sUrl = qMinerRootURL + QMINER_ENDPOINT_GRAPH + "/"+ id;
      HttpGet get = new HttpGet(sUrl);
      logger.debug("About to call qMiner: {}", sUrl);
      HttpResponse response = httpClient.execute(get);
      logger.debug("Service returned {}", response.getStatusLine().getStatusCode());
      if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
      {
        logger.info("QMiner get graph status OK: {}", response.getStatusLine());
        HttpEntity entity = response.getEntity();
        if(entity != null)
        {
          //parse result....
          byte[] responseData = readAll(entity.getContent());
          AIUtils.streamCopy(new ByteArrayInputStream(responseData), os);
        }
      }
      else
      {
        status = "Error executing service: " + response.getStatusLine();
        logger.error("Error executing service: {}", response.getStatusLine());
        EntityUtils.consumeQuietly(response.getEntity());
        logger.debug("cleared response entity.");
      }
    }
    catch (IOException e)
    {
      status = "Error calling QMiner service: " + e.getLocalizedMessage();
      logger.error("Error calling QMiner service", e);
    }
    finally
    {
      if(httpClient != null)
        httpClient.getConnectionManager().shutdown();
    }
    logger.debug("Done.");
    return status;


  }

  @Override
  public void run()
  {
    if(bRun)
    {
      postDataset();
    }
  }

  public void actionStartJob(OutputStream outputStream) throws IOException
  {
    OutputStreamWriter w = new OutputStreamWriter(outputStream, "utf-8");
    JSONWriter json = new JSONWriter(w);
    json.object();
    json.key("action").value(ACTION_START_UPDATE_JOB);
    String status = startJob();
    if(status == null)
    {
      json.key("success").value("true");
    }
    else
    {
      json.key("success").value("false");
      json.key("error").value(status);
    }
    json.endObject();
    w.flush();
    w.close();

  }

  public void actionStopJob(OutputStream outputStream) throws IOException
  {
    OutputStreamWriter w = new OutputStreamWriter(outputStream, "utf-8");
    JSONWriter json = new JSONWriter(w);
    json.object();
    json.key("action").value(ACTION_STOP_UPDATE_JOB);
    String status = stopJob();
    if(status == null)
    {
      json.key("success").value("true");
    }
    else
    {
      json.key("success").value("false");
      json.key("error").value(status);
    }
    json.endObject();
    w.flush();
    w.close();
  }

  public void actionPostDataset(OutputStream outputStream) throws IOException
  {
    OutputStreamWriter w = new OutputStreamWriter(outputStream, "utf-8");
    JSONWriter json = new JSONWriter(w);
    json.object();
    json.key("action").value(ACTION_POST_DATASET);
    //String status = ;
    String status = postDataset();

    if(status == null)
    {
      json.key("success").value("true");
    }
    else
    {
      json.key("success").value("false");
      json.key("error").value(status);
    }
    json.endObject();
    w.flush();
    w.close();
  }

  public void actionGetStatus(OutputStream outputStream) throws IOException
  {
    String status = getStatus(outputStream);

    if(status != null)
    {
      OutputStreamWriter w = new OutputStreamWriter(outputStream, "utf-8");
      JSONWriter json = new JSONWriter(w);
      json.object();
      json.key("action").value(ACTION_GET_STATUS);
      json.key("success").value("false");
      json.key("error").value(status);
      json.endObject();
      w.flush();
      w.close();
    }
  }

  public void actionGetGraph(OutputStream outputStream, String internalSurveyId) throws IOException
  {
    String status = getGraph(outputStream, internalSurveyId);

    if(status != null)
    {
      logger.warn("Project with id {} does not exist in qMiner. About to post the full record.", internalSurveyId);
      status = getGraphForFullRecord(outputStream, internalSurveyId);
      if(status != null)
      {
        logger.error("Error calling get graph for {}: {}", internalSurveyId, status);
        OutputStreamWriter w = new OutputStreamWriter(outputStream, "utf-8");
        JSONWriter json = new JSONWriter(w);
        json.object();
        json.key("action").value(ACTION_GET_GRAPH);
        json.key("success").value("false");
        json.key("error").value(status);
        json.endObject();
        w.flush();
        w.close();
      }
    }
  }

  private synchronized String startJob()
  {
    String status = null;
    if(bRun)
    {
      logger.warn("Job already running.");
      status = "Job already running.";
    }
    else
    {
      logger.info("Start QMiner job.");
      timer = new Timer("QMinerManager");
      Calendar cStart = Calendar.getInstance();
      //tomorrow at 1 AM
      cStart.add(Calendar.DAY_OF_MONTH, 1);
      cStart.set(Calendar.HOUR_OF_DAY, 1);
      cStart.set(Calendar.MINUTE, 0);
      cStart.set(Calendar.SECOND, 0);
      cStart.set(Calendar.MILLISECOND, 0);
      timer.scheduleAtFixedRate(this, cStart.getTime(), 24L * 60L * 60L * 1000L); //24h*60min*60sec*1000msec
      logger.info("Next scheduled at {}", AIUtils.getYMDHMDateFormat().format(cStart.getTime()));
      bRun = true;
      logger.info("Started.");
    }
    return status;
  }

  private synchronized String stopJob()
  {
    String status = null;
    if(bRun)
    {
      logger.info("Stop QMiner job.");
      bRun = false;
      timer.cancel();
      timer = null;
      logger.info("Stop QMiner job - done.");
    }
    else
    {
      logger.warn("Job already stopped.");
      status = "Job already stopped.";
    }
    return status;
  }

}
