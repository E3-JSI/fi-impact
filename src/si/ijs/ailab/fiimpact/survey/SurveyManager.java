package si.ijs.ailab.fiimpact.survey;
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
import si.ijs.ailab.fiimpact.indicators.OverallResult;
import si.ijs.ailab.fiimpact.indicators.OverallResult.ScoreBoundaries;
import si.ijs.ailab.fiimpact.project.ProjectData;
import si.ijs.ailab.fiimpact.project.ProjectManager;
import si.ijs.ailab.util.AIUtils;


/**
 * Created by flavio on 01/06/2015.
 */

public class SurveyManager
{
  private final Path mapFile;
  private final Path surveyRoot;
  private final Map<String, String> externalIDMap = Collections.synchronizedMap(new HashMap<String, String>());

  public Map<String, SurveyData> getSurveys()
  {
    return surveys;
  }

  private final Map<String, SurveyData> surveys = Collections.synchronizedMap(new HashMap<String, SurveyData>());
  private final SortedSet<String> accelerators = Collections.synchronizedSortedSet(new TreeSet<String>());

  Map<String, Map<String, OverallResult>> getResults()
  {
    return results;
  }

  //type (I/S), result ID (innovation etc), overall...
  private Map<String, Map<String, OverallResult>> results = new TreeMap<>();
  private static final Map<String, OverallResult.ScoreBoundaries> SPEEDOMETER_SLOTS = new HashMap<>();
  static final ArrayList<String> SOCIAL_IMPACT_QUESTIONS = new ArrayList<>();
  private static final String[] QUESTIONNAIRE_TYPE;
  public static final String QUESTIONNAIRE_TYPE_DEFAULT = "I";

  private final static Logger logger = LogManager.getLogger(SurveyManager.class.getName());
  private static SurveyManager surveyManager;

  static JSONObject fiImpactModel;

  /*private static String SPEEDOMETER =
          "\tmin\tlow\tmed\thigh\n" +
                  "INNOVATION\t1.0\t7.6\t14.2\t20.8\n" +
                  //"MARKET\t4\t15\t26\t37\n" +
                  "MARKET\t0.7\t2.3\t3.9\t5.5\n" +
                  "FEASIBILITY\t0\t1.8\t3.6\t5.4\n" +
                  "MARKET_NEEDS\t0\t4\t7.5\t10\n";*/

  private static String SPEEDOMETER =
          "\tmin\tlow\tmed\thigh\n" +
                  "INNOVATION\t0.0\t1.667\t3.333\t5.000\n" +
                  "MARKET\t0.0\t1.667\t3.333\t5.000\n" +
                  "FEASIBILITY\t0.0\t1.667\t3.333\t5.000\n" +
                  "MARKET_NEEDS\t0.0\t1.667\t3.333\t5.000\n";


  private static String SOCIAL_IMPACT = "Q6A_1_A\tQ6A_1_B\tQ6A_1_C\tQ6A_1_D\tQ6A_1_E\tQ6A_1_F\tQ6A_1_G\tQ6A_1_H\tQ6A_1_I\tQ6A_1_J\tQ6A_1_K\tQ6B_1_A\tQ6B_1_B\tQ6B_1_C\tQ6B_1_D\tQ6B_1_E\tQ6B_1_F";

  static
  {

    String[] arrSpeedometerRows = SPEEDOMETER.split("\n");
    for(int i = 1; i < arrSpeedometerRows.length; i++)
    {
      String[] arrRow = arrSpeedometerRows[i].split("\t");
      String id = arrRow[0];
      OverallResult.ScoreBoundaries boundaries = new OverallResult.ScoreBoundaries();
      boundaries.setMin(AIUtils.parseDecimal(arrRow[1], 0.0));
      boundaries.setLo_med(AIUtils.parseDecimal(arrRow[2], 0.0));
      boundaries.setMed_hi(AIUtils.parseDecimal(arrRow[3], 0.0));
      boundaries.setMax(AIUtils.parseDecimal(arrRow[4], 0.0));
      SPEEDOMETER_SLOTS.put(id, boundaries);
    }
    String[] arrSocialImpact = SOCIAL_IMPACT.split("\t");

    SOCIAL_IMPACT_QUESTIONS.addAll(Arrays.asList(arrSocialImpact));

    for(String s : SOCIAL_IMPACT_QUESTIONS)
    {
      OverallResult.ScoreBoundaries boundaries = new OverallResult.ScoreBoundaries();
      boundaries.setMin(0.0);
      boundaries.setLo_med(2.5);
      boundaries.setMed_hi(3.5);
      boundaries.setMax(5.0);
      SPEEDOMETER_SLOTS.put(s, boundaries);

    }

    Map<String, Double> m5A1_Verticals = SurveyData.SCORES.get("Q5A_1_VERTICALS");
    OverallResult.ScoreBoundaries marketNeedsBusiness = SPEEDOMETER_SLOTS.get("MARKET_NEEDS");
    for(String s : m5A1_Verticals.keySet())
    {
      SPEEDOMETER_SLOTS.put("MARKET_NEEDS_" + s, marketNeedsBusiness);
    }

    QUESTIONNAIRE_TYPE = new String[]{"I", "S", "IS"};
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


  private static String readFile(Path file, Charset encoding)
          throws IOException
  {
    byte[] encoded = Files.readAllBytes(file);
    return new String(encoded, encoding);
  }
  private ProjectManager projectManager;

  //private SurveyManager(String _webappRoot, Map<String, Integer> _slots)
  private SurveyManager(String _webappRoot)
  {
    projectManager = ProjectManager.getProjectManager();

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
  public void recalcSurveyResults()
  {
    synchronized(surveys)
    {
      for(SurveyData surveyData : surveys.values())
      {
        surveyData.calculateResults();
      }

    }
  }


  public void recalcResults()
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
      //Same loop for mattermark slots  -  ProjectManager.getMattemrarkSlots()
     for(Map.Entry<String, ScoreBoundaries> entry:projectManager.getMattemrarkSlots().entrySet())
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
        String Q1_1 = sd.questions.get("Q1_1");
        if(Q1_1 != null && !Q1_1.equals(""))
          accelerators.add(Q1_1);
      }
    }

    for(Map<String, OverallResult> typeResults: resultsNew.values())
      for(OverallResult overallResult: typeResults.values())
      {
        logger.info("Recalc: {}", overallResult.getId());
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
    surveyData.writeStructure(outputStream, true);
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


  public void getJSONAccelerators(OutputStream outputStream, String userAccelerator) throws IOException
  {
    synchronized(accelerators)
    {
        OutputStreamWriter w = new OutputStreamWriter(outputStream, "utf-8");
        JSONWriter jsonSurvey = new JSONWriter(w);
        jsonSurvey.array();
        for(String s: accelerators)
        {
          if(userAccelerator.equals("") || userAccelerator.equals(s))
            jsonSurvey.value(s);
        }
        jsonSurvey.endArray();
        w.flush();
        w.close();
    }
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

  private JSONObject getAveragesJSON(String type)
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

  public void list(ServletOutputStream outputStream, String groupQuestion, String groupAnswer) throws IOException
  {
    OutputStreamWriter w = new OutputStreamWriter(outputStream, "utf-8");
    logger.info("Return {} surveys", surveys.size());
    JSONWriter json = new JSONWriter(w);
    json.object().key("total").value(surveys.size());
    json.key("surveys").array();
    for (SurveyData surveyData: surveys.values())
    {

      boolean bInclude = groupQuestion == null;
      if(!bInclude)
      {
        String sAnswer = surveyData.questions.get(groupQuestion);
        if(sAnswer == null || sAnswer.equals(""))
          sAnswer = "EMPTY";
        bInclude = sAnswer.equals(groupAnswer);
      }

      if(bInclude)
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
        addResultKey(json, "MARKET_NEEDS", surveyData.results);

        addResultKey(json, "INNOVATION_GRAPH_PERCENT", surveyData.resultDerivatives);
        addResultKey(json, "MARKET_GRAPH_PERCENT", surveyData.resultDerivatives);
        addResultKey(json, "FEASIBILITY_GRAPH_PERCENT", surveyData.resultDerivatives);
        addResultKey(json, "MARKET_NEEDS_GRAPH_PERCENT", surveyData.resultDerivatives);

        //add results for all mattermark indicators
        ProjectManager projectManager = ProjectManager.getProjectManager();
        ArrayList<ProjectManager.IOListField> mattermarkIndicators = projectManager.getMattermarkIndicators();
        for(ProjectManager.IOListField indicator : mattermarkIndicators)
        {
          addResultKey(json, indicator.getFieldid(), surveyData.results);
          addResultKey(json, indicator.getFieldid() + "_GRAPH_PERCENT", surveyData.resultDerivatives);
        }


        json.endObject();
      }
    }
    json.endArray();
    json.endObject();
    w.flush();
    w.close();
    logger.info("Returned {} surveys", surveys.size());

  }

  static final char newline = '\n';
  private static void  writeLine(Writer w, String s) throws IOException
  {
    w.write(s, 0, s.length());
    w.write(newline);
  }

  private String getQuestionSortKey(String s)
  {
    String result;
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


  public static final int EXPORT_SHORT_LIST = 1;
  public static final int EXPORT_FI_IMPACT_QUESTIONS = 2;
  public static final int EXPORT_FI_IMPACT_INDICATORS = 4;
  public static final int EXPORT_MATTERMARK_FIELDS = 8;
  public static final int EXPORT_MATTERMARK_INDICATORS = 16;
  public static final int EXPORT_DERIVED_INDICATORS = 32;
  public static final int EXPORT_PROJECT_DATA = 64;

  static final String  SHORT_QUESTIONS_LIST = "Q1_1;Q1_2;Q1_3;Q1_4;Q1_22";
  static final String  SHORT_INDICATORS_LIST = "FEASIBILITY;INNOVATION;MARKET;MARKET_NEEDS";

  private boolean isFlagSet(int mask, int flag)
  {
    return (mask & flag) != 0;
  }

  private static String normaliseCSVString(String s)
  {
    s = s.replace("\r\n", ", ");
    s = s.replace("\r", ", ");
    s = s.replace("\n", ", ");
    s = s.replace("\t", " ");
    if(s.contains("\""))
    {
      s = s.replace("\"", "\"\"");
      s = "\""+s+"\"";
    }
    return s;
  }
  public void exportTXT(ServletOutputStream outputStream, String groupQuestion, String groupAnswer, int exportSettings) throws IOException
  {
    OutputStreamWriter writer = new OutputStreamWriter(outputStream, "utf-8");
    logger.info("Save {} surveys", surveys.size());

    String questionsList = null;
    String indicatorsList = null;

    if(isFlagSet(exportSettings, EXPORT_SHORT_LIST) || !isFlagSet(exportSettings, EXPORT_FI_IMPACT_QUESTIONS))
      questionsList = SHORT_QUESTIONS_LIST;

    if(isFlagSet(exportSettings, EXPORT_SHORT_LIST))
      indicatorsList = SHORT_INDICATORS_LIST;

    SortedSet<String> questionsDef = new TreeSet<>();
    SortedSet<String> indicatorsDef = new TreeSet<>();
    SortedSet<String> indicatorsDerivedDef = new TreeSet<>();

    if(questionsList != null)
    {
      String[] arr = questionsList.split(";");
      for(String s: arr)
      {
        questionsDef.add(getQuestionSortKey(s));
      }
    }

    if(indicatorsList != null)
    {
      String[] arr = indicatorsList.split(";");
      for(String s: arr)
      {
        indicatorsDef.add(getResultSortKey(s));
      }
    }

    ProjectManager.IOListDefinition listMattermarkDef = null;
    ProjectManager.IOListDefinition listProjectsDef = null;
    if(isFlagSet(exportSettings, EXPORT_MATTERMARK_FIELDS))
    {
      listMattermarkDef = ProjectManager.getProjectManager().getListDefinition(ProjectManager.LIST_MATTERMARK);
    }

    if(isFlagSet(exportSettings, EXPORT_PROJECT_DATA))
    {
      listProjectsDef = ProjectManager.getProjectManager().getListDefinition(ProjectManager.LIST_PROJECTS);
    }
    if(isFlagSet(exportSettings, EXPORT_DERIVED_INDICATORS))
    {
      logger.debug("Export derived indicators flag is set");
    }


    synchronized (surveys)
    {
      if(!isFlagSet(exportSettings, EXPORT_SHORT_LIST))
      {
        for (SurveyData sd : surveys.values())
        {
          if(isFlagSet(exportSettings, EXPORT_FI_IMPACT_QUESTIONS))
            for (String s : sd.questions.keySet())
              questionsDef.add(getQuestionSortKey(s));

          if(isFlagSet(exportSettings, EXPORT_FI_IMPACT_INDICATORS) || isFlagSet(exportSettings, EXPORT_MATTERMARK_INDICATORS))
          {
            for(String s : sd.results.keySet())
            {
              if(s.contains("MATTERMARK"))
              {
                if(isFlagSet(exportSettings, EXPORT_MATTERMARK_INDICATORS))
                  indicatorsDef.add(getResultSortKey(s));
              }
              else
              {
                if(isFlagSet(exportSettings, EXPORT_FI_IMPACT_INDICATORS))
                  indicatorsDef.add(getResultSortKey(s));
              }
            }
          }

          if(isFlagSet(exportSettings, EXPORT_DERIVED_INDICATORS))
          {
            for(String s : sd.resultDerivatives.keySet())
            {
              if(s.contains("MATTERMARK"))
              {
                if(isFlagSet(exportSettings, EXPORT_MATTERMARK_INDICATORS))
                  indicatorsDerivedDef.add(getResultSortKey(s));
              }
              else
              {
                if(isFlagSet(exportSettings, EXPORT_FI_IMPACT_INDICATORS))
                  indicatorsDerivedDef.add(getResultSortKey(s));
              }
            }
          }
        }
      }
    }


    StringBuilder sb = new StringBuilder();
    sb.append("id_external").append("\t").append("id_internal");
    logger.debug("Export {} questions", questionsDef.size());
    for (String s: questionsDef)
    {
      sb.append("\t").append(s.split("#")[1]);
    }

    if(listMattermarkDef != null)
    {
      logger.debug("Export {} Mattermark fields", listMattermarkDef.getFields().size());
      for(ProjectManager.IOListField ioListField : listMattermarkDef.getFields())
      {
        sb.append("\t").append(ioListField.getLabel());
      }
    }

    if(listProjectsDef != null)
    {
      logger.debug("Export {} projects fields", listProjectsDef.getFields().size());
      for(ProjectManager.IOListField ioListField : listProjectsDef.getFields())
      {
        sb.append("\t").append(ioListField.getLabel());
      }
    }

    logger.debug("Export {} indicators", indicatorsDef.size());
    for (String s: indicatorsDef)
    {
      sb.append("\t").append(s.split("#")[1]);
    }

    logger.debug("Export derived {} indicators", indicatorsDerivedDef.size());
    for (String s: indicatorsDerivedDef)
    {
      sb.append("\t").append(s.split("#")[1]);
    }

    String sHeader =  sb.toString();
    writeLine(writer, sHeader);

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
            answer = normaliseCSVString(answer);
            sb.append(answer);
          }
        }

        String Q1_22 = surveyData.questions.get("Q1_22");
        ProjectData projectData = null;
        if(Q1_22 == null)
        {
          logger.warn("No FI accelerator project associated with questionnaire {}/{}", surveyData.getExternalId(), surveyData.getId());
        }
        else
        {
          projectData = ProjectManager.getProjectManager().getProjects().get(Q1_22);
          if(projectData == null)
          {
            logger.warn("No project data for {}", Q1_22);
          }
        }

        if(listMattermarkDef != null)
        {
          for(ProjectManager.IOListField ioListField : listMattermarkDef.getFields())
          {
            sb.append("\t");
            if(projectData != null)
            {
              String answer = projectData.getMattermarkValue(ioListField.getFieldid());
              if(answer != null)
              {
                answer = normaliseCSVString(answer);
                sb.append(answer);
              }
            }
          }
        }

        if(listProjectsDef != null)
        {
          for(ProjectManager.IOListField ioListField : listProjectsDef.getFields())
          {
            sb.append("\t");
            if(projectData != null)
            {
              String answer = projectData.getValue(ioListField.getFieldid());
              if(answer != null)
              {
                answer = normaliseCSVString(answer);
                sb.append(answer);
              }
            }
          }
        }

        for (String s : indicatorsDef)
        {
          sb.append("\t");
          Double r = surveyData.results.get(s.split("#")[1]);
          if (r != null)
          {
            sb.append(getDecimalFormatter4().format(r));
          }
        }
        for (String s : indicatorsDerivedDef)
        {
          sb.append("\t");
          Double r = surveyData.resultDerivatives.get(s.split("#")[1]);
          if (r != null)
          {
            sb.append(getDecimalFormatter4().format(r));
          }
        }
        if(groupQuestion == null)
        {
          writeLine(writer, sb.toString());
        }
        else
        {
          String sAnswer = surveyData.questions.get(groupQuestion);
          if(sAnswer == null || sAnswer.equals(""))
            sAnswer = "EMPTY";
          if(sAnswer.equals(groupAnswer))
            writeLine(writer, sb.toString());
        }
        sb.setLength(0);
      }
    }
    writer.flush();
    writer.close();
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

  static DecimalFormat getDecimalFormatter2()
  {
    DecimalFormatSymbols custom=new DecimalFormatSymbols();
    custom.setDecimalSeparator('.');
    custom.setGroupingSeparator(',');
    custom.setMinusSign('-');
    return new DecimalFormat("0.00", custom);
  }

  static DecimalFormat getDecimalFormatter0()
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
