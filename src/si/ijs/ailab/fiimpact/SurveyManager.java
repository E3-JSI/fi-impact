package si.ijs.ailab.fiimpact;
import javax.servlet.ServletOutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import si.ijs.ailab.util.AIUtils;


/**
 * Created by flavio on 01/06/2015.
 */

public class SurveyManager
{
  private final Path mapFile;
  private final Path surveyRoot;
  private final Path webappRoot;
  private Map<String, String> externalIDMap = Collections.synchronizedMap(new HashMap<String, String>());

  public Map<String, SurveyData> getSurveys()
  {
    return surveys;
  }

  private Map<String, SurveyData> surveys = Collections.synchronizedMap(new HashMap<String, SurveyData>());

  public Map<String, Map<String, OverallResult>> getResults()
  {
    return results;
  }

  //type (I/S), result ID (innovation etc), overall...
  private Map<String, Map<String, OverallResult>> results = new TreeMap<>();
  private static final Map<String, OverallResult.ScoreBoundaries> SPEEDOMETER_SLOTS = new HashMap<>();
  public static final ArrayList<String> SOCIAL_IMPACT_QUESTIONS = new ArrayList<>();
  public static final String[] QUESTIONNAIRE_TYPE = {"I", "S", "IS"};
  public static final String QUESTIONNAIRE_TYPE_DEFAULT = "I";

  final static Logger logger = LogManager.getLogger(SurveyManager.class.getName());
  private static SurveyManager surveyManager;

  public static JSONObject fiImpactModel;

  /*private static String SPEEDOMETER =
          "\tmin\tlow\tmed\thigh\n" +
                  "INNOVATION\t1.0\t7.6\t14.2\t20.8\n" +
                  //"MARKET\t4\t15\t26\t37\n" +
                  "MARKET\t0.7\t2.3\t3.9\t5.5\n" +
                  "FEASIBILITY\t0\t1.8\t3.6\t5.4\n" +
                  "MARKET_NEEDS_BUSINESS\t0\t4\t7.5\t10\n";*/

  private static String SPEEDOMETER =
          "\tmin\tlow\tmed\thigh\n" +
                  "INNOVATION\t0.0\t1.667\t3.333\t5.000\n" +
                  "MARKET\t0.0\t1.667\t3.333\t5.000\n" +
                  "FEASIBILITY\t0.0\t1.667\t3.333\t5.000\n" +
                  "MARKET_NEEDS_BUSINESS\t0.0\t1.667\t3.333\t5.000\n";


  private static String SOCIAL_IMPACT = "Q6A_1_A\tQ6A_1_B\tQ6A_1_C\tQ6A_1_D\tQ6A_1_E\tQ6A_1_F\tQ6A_1_G\tQ6A_1_H\tQ6A_1_I\tQ6A_1_J\tQ6A_1_K\tQ6B_1_A\tQ6B_1_B\tQ6B_1_C\tQ6B_1_D\tQ6B_1_E\tQ6B_1_F";

  static
  {

    String[] arrSpeedometerRows = SPEEDOMETER.split("\n");
    for(int i = 1; i < arrSpeedometerRows.length; i++)
    {
      String[] arrRow = arrSpeedometerRows[i].split("\t");
      String id = arrRow[0];
      OverallResult.ScoreBoundaries boundaries = new OverallResult.ScoreBoundaries();
      boundaries.min = AIUtils.parseDecimal(arrRow[1], 0.0);
      boundaries.lo_med = AIUtils.parseDecimal(arrRow[2], 0.0);
      boundaries.med_hi = AIUtils.parseDecimal(arrRow[3], 0.0);
      boundaries.max = AIUtils.parseDecimal(arrRow[4], 0.0);
      SPEEDOMETER_SLOTS.put(id, boundaries);
    }
    String[] arrSocialImpact = SOCIAL_IMPACT.split("\t");

    SOCIAL_IMPACT_QUESTIONS.addAll(Arrays.asList(arrSocialImpact));

    for(String s: SOCIAL_IMPACT_QUESTIONS)
    {
      OverallResult.ScoreBoundaries boundaries = new OverallResult.ScoreBoundaries();
      boundaries.min = 0.0;
      boundaries.lo_med = 2.5;
      boundaries.med_hi = 3.5;
      boundaries.max = 5.0;
      SPEEDOMETER_SLOTS.put(s, boundaries);

    }

    Map<String, Double> m5A1_Verticals = SurveyData.SCORES.get("Q5A_1_VERTICALS");
    OverallResult.ScoreBoundaries marketNeedsBusiness = SPEEDOMETER_SLOTS.get("MARKET_NEEDS_BUSINESS");
    for(String s: m5A1_Verticals.keySet())
    {
      SPEEDOMETER_SLOTS.put("MARKET_NEEDS_BUSINESS_"+s, marketNeedsBusiness);
    }

  }


  public static class SurveyDataComparator implements Comparator<SurveyData>
  {
    private final String resultType;

    public SurveyDataComparator(String _resultType)
    {
      resultType = _resultType;
    }
    public int compare(SurveyData sd1, SurveyData sd2)
    {
      Double r1 = sd1.results.get(resultType);
      Double r2 = sd2.results.get(resultType);
      if(r1 == null)
        r1 = 0.0;
      if(r2 == null)
        r2 = 0.0;

      return -r1.compareTo(r2);
    }
  }


  static String readFile(Path file, Charset encoding)
          throws IOException
  {
    byte[] encoded = Files.readAllBytes(file);
    return new String(encoded, encoding);
  }

  //private SurveyManager(String _webappRoot, Map<String, Integer> _slots)
  private SurveyManager(String _webappRoot)
  {
    webappRoot = new File(_webappRoot).toPath();
    mapFile = new File(_webappRoot).toPath().resolve("WEB-INF").resolve("survey-id-list.txt");
    surveyRoot = new File(_webappRoot).toPath().resolve("WEB-INF").resolve("survey");
    logger.debug("Root: {}", _webappRoot);
    //slots = _slots;
    if(Files.notExists(surveyRoot))
    {
      try
      {
        Files.createDirectory(surveyRoot);
        logger.debug("Created dir: {}", surveyRoot.toString());
      }
      catch (IOException e)
      {
        logger.error(e);
      }
    }


    String jsonData;
    Path m = Paths.get(_webappRoot).resolve("js").resolve("fiModelNew.js");
    try
    {
      jsonData = readFile(m, StandardCharsets.UTF_8);
      fiImpactModel = new JSONObject(jsonData);
    }
    catch (IOException e)
    {
      logger.error("Cant load model file {}", m.toString());
    }
    load();
  }

  public static synchronized SurveyManager getSurveyManager(String _webappRoot) //, Map<String, Integer> _slots
  {
    if(surveyManager == null)
    {
      surveyManager = new SurveyManager(_webappRoot);//, _slots);
    }
    return surveyManager;
  }

  public static SurveyManager getSurveyManager()
  {
    return surveyManager;
  }

  private void load()
  {
    logger.info("Load id mappings from: {}", mapFile.toString());
    externalIDMap.clear();
    surveys.clear();
    try
    {
      BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(mapFile.toFile()), "utf-8"));
      String line = br.readLine();
      while(line!=null)
      {
        int ind1 = line.indexOf("\t");
        if(ind1 <= 0)
          logger.error("Error reading line: {}", line);
        else
        {

          String externalId = line.substring(0, ind1);
          String id = line.substring(ind1 + 1);
          SurveyData sd = loadSurvey(id);
          if(sd != null)
          {
            externalIDMap.put(externalId, id);
            surveys.put(id, sd);
            sd.calculateResults();
          }
          else
            logger.error("Survey {}/{} does not exist.", externalId, id);
        }
        line = br.readLine();
      }
      br.close();
      logger.info("calc averages");
      recalcResults();
      logger.info("loaded");
    }
    catch (IOException ioe)
    {
      logger.error("could not read text file "+mapFile.toString());
    }

  }

  private void recalcResults()
  {
    logger.info("Recalc Results");
    //results.clear();
    Map<String, Map<String, OverallResult>> resultsNew = new TreeMap<>();

    for(String type: QUESTIONNAIRE_TYPE)
    {
      Map<String, OverallResult> typeResults = new TreeMap<>();
      resultsNew.put(type, typeResults);
      for(Map.Entry<String, OverallResult.ScoreBoundaries> entry: SPEEDOMETER_SLOTS.entrySet())
      {
        typeResults.put(entry.getKey(), new OverallResult(type, entry.getKey(), entry.getValue()));
      }
    }

    logger.info("Recalc results for {} surveys.", surveys.size());
    synchronized (surveys)
    {
      for (SurveyData sd : surveys.values())
      {
        String sdType = sd.getType();
        for (String type : QUESTIONNAIRE_TYPE)
        {
          if (type.contains(sdType))
          {
            Map<String, OverallResult> typeResults = resultsNew.get(type);

            for (Map.Entry<String, Double> r : sd.results.entrySet())
            {
              OverallResult or = typeResults.get(r.getKey());
              if (or != null)
              {
                or.add(sd);
              }
            }
          }
        }
      }
    }

    for(Map<String, OverallResult> typeResults: resultsNew.values())
      for(OverallResult overallResult: typeResults.values())
      {
        logger.info("Recalc: {}", overallResult.id);
        overallResult.calculate();
      }
    results = resultsNew;
    logger.info("Recalc results done");

  }

  private void saveMap()
  {
    logger.info("Saving id mappings to: {}", mapFile.toString());

    try
    {
      synchronized (externalIDMap)
      {
        OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(mapFile.toFile()), "utf-8");
        for (Map.Entry<String, String> entry : externalIDMap.entrySet())
        {
          w.write(entry.getKey() + "\t" + entry.getValue() + "\n");
        }
        w.close();
      }
    }
    catch(java.io.IOException ioe)
    {
      logger.error("error writing file", ioe);
    }
    logger.info("Saved: {}", mapFile.toFile());

  }

  private SurveyData loadSurvey(String id)
  {

    Path p = surveyRoot.resolve("survey-" + id + ".xml");
    SurveyData sd = new SurveyData();

    try
    {
      sd.read(new FileInputStream(p.toFile()));
    }
    catch (ParserConfigurationException | IOException | SAXException e)
    {
      logger.error("Cannot load survey {}", p.toString());
      sd = null;
    }
    return sd;
  }


  public synchronized void addSurvey(OutputStream outputStream, String[] arrQuestions, String externalId) throws IOException
  {
    String id = externalIDMap.get(externalId);
    SurveyData surveyData;
    if(id == null)
    {
      surveyData = new SurveyData();
      id = java.util.UUID.randomUUID().toString();
      surveyData.setExternalId(externalId);
      surveyData.setId(id);
      externalIDMap.put(externalId, id);
      surveys.put(id, surveyData);
      saveMap();
    }
    else
    {
      surveyData = surveys.get(id);
    }

    surveyData.addQuestions(arrQuestions);
    surveyData.calculateResults();
    surveyData.saveSurvey(surveyRoot);
    recalcResults();
    surveyData.writeStructureXML(outputStream);
  }

  public synchronized void removeSurvey(ServletOutputStream outputStream, String externalId)
  {
    String id = externalIDMap.get(externalId);
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
    Element root = doc.createElement("survey");
    doc.appendChild(root);
    root.setAttribute("external", externalId);
    if(id!=null)
      root.setAttribute("id", id);

    if(id==null)
    {
      root.setTextContent("Survey not foud, can't delete");
      logger.error("Survey not foud, can't delete: {}", externalId);
    }
    else
    {
      Path p = surveyRoot.resolve("survey-" + id + ".xml");
      p.toFile().delete();
      root.setTextContent("Survey removed.");
      logger.info("Survey removed: {}", id);
      externalIDMap.remove(externalId);
      surveys.remove(id);
      saveMap();
      recalcResults();
    }
    AIUtils.save(doc, outputStream);
  }


  public void getJSONSurvey(OutputStream outputStream, String id) throws IOException
  {
    SurveyData surveyData = surveys.get(id);
    if(surveyData == null)
    {
      OutputStreamWriter w = new OutputStreamWriter(outputStream, "utf-8");
      JSONWriter jsonSurvey = new JSONWriter(w);
      jsonSurvey.object().key("id").value(id).key("error").value("Survey not found.").endObject();
      w.flush();
      w.close();
    }
    else
    {
      surveyData.writeUIJSON(outputStream);
    }
  }
  public void getXMLSurvey(OutputStream outputStream, String id) throws IOException
  {
    SurveyData surveyData = surveys.get(id);
    if(surveyData == null)
    {
      OutputStreamWriter w = new OutputStreamWriter(outputStream, "utf-8");
      w.write("Error. Survey doesn't exist");
      w.flush();
      w.close();
    }
    else
    {
      surveyData.writeUIXML(outputStream);
    }
  }

  public JSONObject getAveragesJSON(String type)
  {

    JSONObject averages = new JSONObject();
    averages.put("total", surveys.size());
    averages.put("type", type);
    JSONArray jsonResults = new JSONArray();
    averages.put("results", jsonResults);
    for (OverallResult re : results.get(type).values())
    {
      jsonResults.put(re.toJSON());
    }
    logger.info("Returned averages for {} results", results.get(type).size());
    return averages;
  }

  public void getAverages(String type, OutputStream outputStream) throws IOException
  {
    /*
    OutputStreamWriter w = new OutputStreamWriter(outputStream, "utf-8");
    JSONWriter jsonAverages = new JSONWriter(w);
    jsonAverages.object().key("total").value(surveys.size());
    jsonAverages.key("results").array();
    for (OverallResult re : results.get(type).values())
    {
      re.toJSON(jsonAverages);
    }
    jsonAverages.endArray();
    jsonAverages.endObject();
    w.flush();
    w.close();
    */


    OutputStreamWriter w = new OutputStreamWriter(outputStream, "utf-8");
    getAveragesJSON(type).write(w);
    w.flush();
    w.close();


  }

  public void loadAll(ServletOutputStream outputStream, String fileName) throws IOException
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
    json.object().key("total_before").value(surveys.size());

    while(line!=null)
    {
      String[] lineArr = line.split(";");
      if(lineArr.length != headerArr.length)
      {
        logger.error("Ignore line {}: data length doesn't match ({}/{}).", lineCnt, headerArr.length, lineArr.length);
      }
      else
      {
        String externalId = lineArr[0];
        String id = externalIDMap.get(externalId);
        SurveyData surveyData;
        if(id == null)
        {
          surveyData = new SurveyData();
          id = java.util.UUID.randomUUID().toString();
          surveyData.setExternalId(externalId);
          surveyData.setId(id);
          externalIDMap.put(externalId, id);
          surveys.put(id, surveyData);
          saveMap();
        }
        else
        {
          surveyData = surveys.get(id);
        }
        surveyData.addQuestions(headerArr, lineArr, 1);
        surveyData.calculateResults();
        surveyData.saveSurvey(surveyRoot);
      }
      line = brData.readLine();
      lineCnt++;
    }
    recalcResults();
    logger.info("Added {} surveys, total {}.", lineCnt - 1, surveys.size());
    json.object().key("total_added").value(lineCnt - 1);
    json.object().key("total_after").value(surveys.size());
    json.endArray();
    json.endObject();
    w.flush();
    w.close();

  }

  public void loadAllTest(ServletOutputStream outputStream, String rootDirName) throws IOException
  {
    //Path p = webappRoot.resolve("WEB-INF").resolve(fileName);
    Path rootDir = new File(rootDirName).toPath();
    logger.info("Load data from dir {}", rootDir.toString());

    OutputStreamWriter w = new OutputStreamWriter(outputStream, "utf-8");
    JSONWriter json = new JSONWriter(w);
    json.object().key("total_before").value(surveys.size());
    int totalNewSurveys = 0;

    if(Files.isDirectory(rootDir))
    {
      try(DirectoryStream<Path> stream = Files.newDirectoryStream(rootDir))
      {
        for(Path p : stream)
        {

          String fileName = p.getFileName().toString();
          if (fileName.endsWith(".txt"))
          {
            logger.info("Load data from file {}", p.toString());
            fileName = fileName.substring(0, fileName.length()-4);
            BufferedReader brData = new BufferedReader(new InputStreamReader(new FileInputStream(p.toFile()), "Cp1252"));
            ArrayList<ArrayList<String>> lines = new ArrayList<>();
            String line = brData.readLine();
            StringBuilder sb = null;
            boolean bMulti = false;
            ArrayList<String> lineList = null;
            while (line != null)
            {
              //logger.debug("Line {}", line);
              String[] lineArr = line.split("\t");
              if(!bMulti)
              {
                lineList = new ArrayList<>();
                lines.add(lineList);
              }

              for (String s : lineArr)
              {
                if (bMulti)
                {
                  if (s.endsWith("\"")) //multi-line or multi-tab end
                  {
                    sb.append(" ").append(s.substring(0, s.length() - 1));
                    lineList.add(sb.toString());
                    sb = null;
                    logger.debug("... {}.", s);
                    bMulti = false;
                  }
                  else
                  {
                    sb.append(" ").append(s);
                    logger.debug("... {} ...", s);
                  }
                }
                else
                {
                  if (s.startsWith("\"") && s.endsWith("\""))
                  {
                    lineList.add(s.substring(1, s.length() - 1));

                  }
                  else if (s.startsWith("\"")) //multi-line or multi-tab
                  {
                    sb = new StringBuilder();
                    sb.append(s.substring(1));
                    bMulti = true;
                    logger.debug("Loading multi-line: {}", s);
                  }
                  else
                  {
                    lineList.add(s);
                  }
                }
              }
              line = brData.readLine();
            }

            ArrayList<String> headerList = lines.get(0);
            ArrayList<String> identifiers = new ArrayList<>();
            logger.debug("header: {} items", headerList.size());
            for (int i = 2; i < headerList.size(); i++)
            {
              String externalId = fileName + "_" + headerList.get(i);
              String id = externalIDMap.get(externalId);
              SurveyData surveyData;
              if (id == null)
              {
                surveyData = new SurveyData();
                id = java.util.UUID.randomUUID().toString();
                surveyData.setExternalId(externalId);
                surveyData.setId(id);
                externalIDMap.put(externalId, id);
                surveys.put(id, surveyData);
              }
              else
              {
                surveyData = surveys.get(id);
              }
              surveyData.clear();
              logger.debug("Added: {}/{}", externalId, id);
              identifiers.add(id);
            }

            for (int i = 1; i < lines.size(); i++)
            {
              logger.debug("Extracting answer: {}", i);
              ArrayList<String> answersLine = lines.get(i);
              String questionID = answersLine.get(0);
              logger.debug("Question {}", questionID);

              if (questionID != null && !questionID.equals(""))
              {
                for (int j = 2; j < answersLine.size(); j++)
                {
                  String id = identifiers.get(j - 2);
                  String answer = answersLine.get(j);
                  SurveyData sd = surveys.get(id);
                  logger.debug("{}: {}={}", id, questionID, answer);
                  sd.addQuestion(questionID, answer);
                }
              }
            }
            totalNewSurveys += identifiers.size();
          }
        }
      }
      catch (IOException e)
      {
        logger.error("Error scanning directory", e);
      }
    }


    saveMap();
    synchronized (surveys)
    {
      for (SurveyData surveyData : surveys.values())
      {
        surveyData.calculateResults();
        surveyData.saveSurvey(surveyRoot);
      }
      recalcResults();

      logger.info("Added {} surveys, total {}.", totalNewSurveys, surveys.size());
      json.key("total_added").value(totalNewSurveys);
      json.key("total_after").value(surveys.size());
      json.endObject();
      w.flush();
      w.close();
    }

  }

  public void list(ServletOutputStream outputStream) throws IOException
  {
    OutputStreamWriter w = new OutputStreamWriter(outputStream, "utf-8");
    logger.info("Return {} surveys", surveys.size());
    JSONWriter json = new JSONWriter(w);
    json.object().key("total").value(surveys.size());
    json.key("surveys").array();
    for (SurveyData surveyData: surveys.values())
    {
      json.object();
      json.key("id_external").value(surveyData.getExternalId());
      json.key("id_internal").value(surveyData.getId());

      addQuestionKey(json, "Q1_1", surveyData.questions);
      addQuestionKey(json, "Q1_2", surveyData.questions);
      addQuestionKey(json, "Q1_3", surveyData.questions);
      addQuestionKey(json, "Q1_4", surveyData.questions);

      addResultKey(json, "INNOVATION", surveyData.results);
      addResultKey(json, "MARKET", surveyData.results);
      addResultKey(json, "FEASIBILITY", surveyData.results);
      addResultKey(json, "MARKET_NEEDS_BUSINESS", surveyData.results);

      addResultKey(json, "INNOVATION_GRAPH_PERCENT", surveyData.resultDerivatives);
      addResultKey(json, "MARKET_GRAPH_PERCENT", surveyData.resultDerivatives);
      addResultKey(json, "FEASIBILITY_GRAPH_PERCENT", surveyData.resultDerivatives);
      addResultKey(json, "MARKET_NEEDS_BUSINESS_GRAPH_PERCENT", surveyData.resultDerivatives);

      json.endObject();
    }
    json.endArray();
    json.endObject();
    w.flush();
    w.close();
    logger.info("Returned {} surveys", surveys.size());

  }

  static final char newline = '\n';
  public static void  writeLine(BufferedWriter w, String s) throws IOException
  {
    w.write(s, 0, s.length());
    w.write(newline);
  }

  private String getQuestionSortKey(String s)
  {
    String result = "";
    if (s.contains("_"))
    {
      //this is a hack to sort questions in the export correctly - Q1_1 --> Q1_01, Q1_10 --> Q1_10, Q1_2a --> Q1_02a, ...
      String[] sArr = s.split("\\_");
      if (sArr[1].length() == 2 && !AIUtils.isInteger(sArr[1]))
      {
        sArr[1] = "0" + sArr[1];
      } else if (sArr[1].length() == 1)
      {
        sArr[1] = "0" + sArr[1];
      }
      result = sArr[0] + "_" + sArr[1] + "#" + s;
    }
    else
      result =  s + "#" + s;

    return result;
  }

  private String getResultSortKey(String s)
  {
    String  result =  s + "#" + s;
    return result;
  }


  public void exportTXT(ServletOutputStream outputStream, String exportDir, String type, String groupQuestion, String idList, String questionsList, String resultsList, String resultsDerList) throws IOException
  {
    OutputStreamWriter w = new OutputStreamWriter(outputStream, "utf-8");
    logger.info("Save {} surveys", surveys.size());
    JSONWriter json = new JSONWriter(w);
    json.object().key("total").value(surveys.size());

    SortedSet<String> questionsDef = new TreeSet<>();
    SortedSet<String> resultsDef = new TreeSet<>();
    SortedSet<String> resultsDerDef = new TreeSet<>();
    if(questionsList != null && !questionsList.equals(""))
    {
      String[] arr = questionsList.split(";");
      for(String s: arr)
      {
        questionsDef.add(getQuestionSortKey(s));
      }
    }

    if(resultsList != null && !resultsList.equals(""))
    {
      String[] arr = resultsList.split(";");
      for(String s: arr)
      {
        resultsDef.add(getResultSortKey(s));
      }
    }

    if(resultsDerList != null &&  !resultsDerList.equals(""))
    {
      String[] arr = resultsDerList.split(";");
      for(String s: arr)
      {
        String sk = getResultSortKey(s);
        resultsDerDef.add(sk);
      }
    }

    synchronized (surveys)
    {
      if(questionsList == null || resultsList == null || resultsDerList == null)
      {
        for (SurveyData sd : surveys.values())
        {
          if(questionsList == null)
            for (String s : sd.questions.keySet())
              questionsDef.add(getQuestionSortKey(s));

          if(resultsList == null)
            for (String s : sd.results.keySet())
              resultsDef.add(getResultSortKey(s));

          if(resultsDerList == null)
            for (String s : sd.resultDerivatives.keySet())
            {
              String sk = getResultSortKey(s);
              resultsDerDef.add(sk);
            }
        }
      }
    }

    Map<String, BufferedWriter> exportFiles = new HashMap<>();
    String filename = "export_all_"+type+".txt";
    Path root = new File(exportDir).toPath();
    Path fOut = root.resolve(filename);
    BufferedWriter writerAllTXT = Files.newBufferedWriter(fOut, Charset.forName("UTF-8"), StandardOpenOption.CREATE);
    exportFiles.put("#", writerAllTXT);


    StringBuilder sb = new StringBuilder();
    sb.append("id_external").append("\t").append("id_internal");
    for (String s: questionsDef)
    {
      sb.append("\t").append(s.split("#")[1]);
    }
    for (String s: resultsDef)
    {
      sb.append("\t").append(s.split("#")[1]);
    }
    for (String s: resultsDerDef)
    {
      sb.append("\t").append(s.split("#")[1]);
    }

    String sHeader =  sb.toString();
    writeLine(writerAllTXT, sHeader);

    sb.setLength(0);

    synchronized (surveys)
    {
      for (SurveyData surveyData: surveys.values())
      {
        sb.append(surveyData.getExternalId()).append("\t").append(surveyData.getId());
        for (String s : questionsDef)
        {
          sb.append("\t");
          String answer = surveyData.questions.get(s.split("#")[1]);
          if (answer != null)
          {
            answer = answer.replace("\r\n", ", ");
            answer = answer.replace("\r", ", ");
            answer = answer.replace("\n", ", ");
            sb.append(answer);
          }
        }
        for (String s : resultsDef)
        {
          sb.append("\t");
          Double r = surveyData.results.get(s.split("#")[1]);
          if (r != null)
          {
            sb.append(getDecimalFormatter4().format(r));
          }
        }
        for (String s : resultsDerDef)
        {
          sb.append("\t");
          Double r = surveyData.resultDerivatives.get(s.split("#")[1]);
          if (r != null)
          {
            sb.append(getDecimalFormatter4().format(r));
          }
        }
        writeLine(writerAllTXT, sb.toString());
        if(groupQuestion != null)
        {
          String groupAnswer = surveyData.questions.get(groupQuestion);
          if(groupAnswer == null || groupAnswer.equals(""))
            groupAnswer = "EMPTY";
          BufferedWriter writerGroupTXT = exportFiles.get(groupAnswer);
          if(writerGroupTXT == null)
          {
            filename = "export_" + type + "_"+groupAnswer+ ".txt";
            fOut = root.resolve(filename);
            writerGroupTXT = Files.newBufferedWriter(fOut, Charset.forName("UTF-8"), StandardOpenOption.CREATE);
            exportFiles.put(groupAnswer, writerGroupTXT);
            writeLine(writerGroupTXT, sHeader);
          }
          writeLine(writerGroupTXT, sb.toString());

        }

        sb.setLength(0);
      }
    }
    json.endObject();
    w.flush();
    w.close();
    for(BufferedWriter writer: exportFiles.values())
    {
      writer.flush();
      writer.close();
    }
    logger.info("Saved {} surveys", surveys.size());

  }

  private void addQuestionKey(JSONWriter json, String qID, Map<String, String> questions)
  {
    String val = questions.get(qID);
    if(val != null)
      json.key(qID).value(val);
  }

  private void addResultKey(JSONWriter json, String qID, Map<String, Double> results)
  {
    Double val=results.get(qID);
    if(val != null)
      json.key(qID).value(getDecimalFormatter4().format(val));
  }

  synchronized public void clearAll(ServletOutputStream outputStream) throws IOException
  {
    OutputStreamWriter w = new OutputStreamWriter(outputStream, "utf-8");
    logger.info("Remove all {} surveys", surveys.size());
    JSONWriter json = new JSONWriter(w);
    json.object().key("total").value(surveys.size());
    synchronized (surveys)
    {
      for (String id : surveys.keySet())
      {
        Path p = surveyRoot.resolve("survey-" + id + ".xml");
        p.toFile().delete();
        logger.info("Survey removed: {}", id);
      }
    }
    externalIDMap.clear();
    surveys.clear();
    results = new TreeMap<>();
    saveMap();
    recalcResults();
    json.endObject();
    w.flush();
    w.close();
  }

  public static DecimalFormat getDecimalFormatter4()
  {
    DecimalFormatSymbols custom=new DecimalFormatSymbols();
    custom.setDecimalSeparator('.');
    custom.setGroupingSeparator(',');
    custom.setMinusSign('-');
    return new DecimalFormat("0.0000", custom);
  }

  public static DecimalFormat getDecimalFormatter2()
  {
    DecimalFormatSymbols custom=new DecimalFormatSymbols();
    custom.setDecimalSeparator('.');
    custom.setGroupingSeparator(',');
    custom.setMinusSign('-');
    return new DecimalFormat("0.00", custom);
  }

  public static DecimalFormat getDecimalFormatter0()
  {
    DecimalFormatSymbols custom=new DecimalFormatSymbols();
    custom.setDecimalSeparator('.');
    custom.setGroupingSeparator(',');
    custom.setMinusSign('-');
    return new DecimalFormat("0", custom);
  }

  public static void main(String[] args) throws Exception
  {
    double d = 1.234;
    DecimalFormat df = getDecimalFormatter4();
    System.out.println(df.format(d));
    df = getDecimalFormatter0();
    System.out.println(df.format(d));
  }

}
