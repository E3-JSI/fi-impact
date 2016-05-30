package si.ijs.ailab.fiimpact.survey;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import org.json.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import si.ijs.ailab.fiimpact.indicators.*;
import si.ijs.ailab.fiimpact.project.*;
import si.ijs.ailab.fiimpact.settings.FIImpactSettings;
import si.ijs.ailab.fiimpact.settings.IOListDefinition;
import si.ijs.ailab.fiimpact.settings.IOListField;
import si.ijs.ailab.util.*;

import javax.servlet.ServletOutputStream;
import javax.xml.parsers.*;
import java.io.*;
import java.nio.file.*;
import java.text.*;
import java.util.*;


/**
 * Created by flavio on 01/06/2015.
 */

public class SurveyManager
{
  private final Map<String, String> externalIDMap = Collections.synchronizedMap(new HashMap<String, String>());

  public Map<String, SurveyData> getSurveys()
  {
    return surveys;
  }

  private final Map<String, SurveyData> surveys = Collections.synchronizedMap(new HashMap<String, SurveyData>());
  private SortedSet<String> accelerators = Collections.synchronizedSortedSet(new TreeSet<String>());
  //private List<String> sortedQuestions = Collections.synchronizedList(new ArrayList<String>());

  Map<String, Map<String, OverallResult>> getResults()
  {
    return results;
  }

  //type (I/S), result ID (innovation etc), overall...
  private Map<String, Map<String, OverallResult>> results = new TreeMap<>();

  private final static Logger logger = LogManager.getLogger(SurveyManager.class.getName());

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



  public SurveyManager()
  {
    load();
  }



  private void load()
  {
    logger.info("Load surveys, id mappings from: {}", FIImpactSettings.getFiImpactSettings().getSurveyMapFile().toString());
    externalIDMap.clear();
    surveys.clear();
    try
    {
      BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(FIImpactSettings.getFiImpactSettings().getSurveyMapFile().toFile()), "utf-8"));
      String line = br.readLine();
      while(line != null)
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
      logger.error("could not read text file " + FIImpactSettings.getFiImpactSettings().getSurveyMapFile().toString());
    }
    logger.info("Loaded {} surveys", surveys.size());

  }

  public void recalcSurveyResults()
  {
    logger.info("Recalc Surveys Results");
    synchronized(surveys)
    {
      for(SurveyData surveyData : surveys.values())
      {
        surveyData.calculateResults();
      }

    }
    logger.info("Recalc Surveys Results done");
  }


  public void recalcResults()
  {
    logger.info("Recalc Results");
    //results.clear();
    Map<String, Map<String, OverallResult>> resultsNew = new TreeMap<>();
    SortedSet<String> acceleratorsNew = Collections.synchronizedSortedSet(new TreeSet<String>());
    //SortedSet<String> sortedQuestionsSet = Collections.synchronizedSortedSet(new TreeSet<String>());

    for(String type : FIImpactSettings.QUESTIONNAIRE_TYPE)
    {
      Map<String, OverallResult> typeResults = new TreeMap<>();
      resultsNew.put(type, typeResults);
      for(Map.Entry<String, OverallResult.ScoreBoundaries> entry : FIImpactSettings.SPEEDOMETER_SLOTS.entrySet())
      {
        typeResults.put(entry.getKey(), new OverallResult(type, entry.getKey(), entry.getValue()));
      }
      //Same loop for mattermark slots  -  ProjectManager.getMattemrarkSlots()
      for(Map.Entry<String, OverallResult.ScoreBoundaries> entry : FIImpactSettings.getFiImpactSettings().getProjectManager().getMattemrarkSlots().entrySet())
      {
        typeResults.put(entry.getKey(), new OverallResult(type, entry.getKey(), entry.getValue()));
      }
    }

    logger.info("Recalc results for {} surveys.", surveys.size());
    IOListDefinition surveyPredefinedFields = FIImpactSettings.getFiImpactSettings().getListDefinition(FIImpactSettings.LIST_SURVEYS);
    synchronized(surveys)
    {
      for(SurveyData sd : surveys.values())
      {
        String sdType = sd.getType();
        for(String type : FIImpactSettings.QUESTIONNAIRE_TYPE)
        {
          if(type.contains(sdType))
          {
            Map<String, OverallResult> typeResults = resultsNew.get(type);

            for(Map.Entry<String, Double> r : sd.results.entrySet())
            {
              OverallResult or = typeResults.get(r.getKey());
              if(or != null)
              {
                or.add(sd);
              }
            }
          }
        }
        String Q1_1 = sd.questions.get("Q1_1");
        if(Q1_1 != null && !Q1_1.equals(""))
          acceleratorsNew.add(Q1_1);

        for(String questionID : sd.questions.keySet())
          if(!surveyPredefinedFields.getFieldsById().containsKey(questionID))
            logger.error("Survey question {} is not predefined. Please update io-list-def.xml", questionID);
      }
    }

    for(Map<String, OverallResult> typeResults : resultsNew.values())
      for(OverallResult overallResult : typeResults.values())
      {
        //logger.info("Recalc: {}", overallResult.getId());
        overallResult.calculate();
      }
    synchronized(surveys)
    {
      results = resultsNew;
      accelerators = acceleratorsNew;
    }
    logger.info("Recalc results done");

  }

  private void saveMap()
  {
    logger.info("Saving id mappings to: {}", FIImpactSettings.getFiImpactSettings().getSurveyMapFile().toString());

    try
    {
      synchronized(externalIDMap)
      {
        OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(FIImpactSettings.getFiImpactSettings().getSurveyMapFile().toFile()), "utf-8");
        for(Map.Entry<String, String> entry : externalIDMap.entrySet())
        {
          w.write(entry.getKey() + "\t" + entry.getValue() + "\n");
        }
        w.close();
      }
    }
    catch (java.io.IOException ioe)
    {
      logger.error("error writing file", ioe);
    }
    logger.info("Saved: {}", FIImpactSettings.getFiImpactSettings().getSurveyMapFile().toFile());

  }

  private SurveyData loadSurvey(String id)
  {

    Path p = FIImpactSettings.getFiImpactSettings().getSurveyRoot().resolve("survey-" + id + ".xml");
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
    surveyData.saveSurvey(FIImpactSettings.getFiImpactSettings().getSurveyRoot());
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
    if(id != null)
      root.setAttribute("id", id);

    if(id == null)
    {
      root.setTextContent("Survey not foud, can't delete");
      logger.error("Survey not foud, can't delete: {}", externalId);
    }
    else
    {
      Path p = FIImpactSettings.getFiImpactSettings().getSurveyRoot().resolve("survey-" + id + ".xml");
      //noinspection ResultOfMethodCallIgnored
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
      for(String s : accelerators)
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
    for(OverallResult re : results.get(type).values())
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
      try (DirectoryStream<Path> stream = Files.newDirectoryStream(rootDir))
      {
        for(Path p : stream)
        {

          String fileName = p.getFileName().toString();
          if(fileName.endsWith(".txt"))
          {
            logger.info("Load data from file {}", p.toString());
            fileName = fileName.substring(0, fileName.length() - 4);
            BufferedReader brData = new BufferedReader(new InputStreamReader(new FileInputStream(p.toFile()), "Cp1252"));
            ArrayList<ArrayList<String>> lines = new ArrayList<>();
            String line = brData.readLine();
            StringBuilder sb = null;
            boolean bMulti = false;
            ArrayList<String> lineList = null;
            while(line != null)
            {
              //logger.debug("Line {}", line);
              String[] lineArr = line.split("\t");
              if(!bMulti)
              {
                lineList = new ArrayList<>();
                lines.add(lineList);
              }

              for(String s : lineArr)
              {
                if(bMulti)
                {
                  if(s.endsWith("\"")) //multi-line or multi-tab end
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
                  if(s.startsWith("\"") && s.endsWith("\""))
                  {
                    lineList.add(s.substring(1, s.length() - 1));

                  }
                  else if(s.startsWith("\"")) //multi-line or multi-tab
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
            for(int i = 2; i < headerList.size(); i++)
            {
              String externalId = fileName + "_" + headerList.get(i);
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
              }
              else
              {
                surveyData = surveys.get(id);
              }
              surveyData.clear();
              logger.debug("Added: {}/{}", externalId, id);
              identifiers.add(id);
            }

            for(int i = 1; i < lines.size(); i++)
            {
              logger.debug("Extracting answer: {}", i);
              ArrayList<String> answersLine = lines.get(i);
              String questionID = answersLine.get(0);
              logger.debug("Question {}", questionID);

              if(questionID != null && !questionID.equals(""))
              {
                for(int j = 2; j < answersLine.size(); j++)
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
    synchronized(surveys)
    {
      for(SurveyData surveyData : surveys.values())
      {
        surveyData.calculateResults();
        surveyData.saveSurvey(FIImpactSettings.getFiImpactSettings().getSurveyRoot());
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
    for(SurveyData surveyData : surveys.values())
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
        ArrayList<IOListField> mattermarkIndicators = FIImpactSettings.getFiImpactSettings().getMattermarkIndicators();
        for(IOListField indicator : mattermarkIndicators)
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

  //filter syntax: q=FIELD;a,b,c
  // q=FIELD;x,y,z
  //Highlights - a list of fields for the highlighting F1;F2;...
  public void listFilter(ServletOutputStream outputStream, String[] filter, String[] highlights, String referenceSurveyInternalID) throws IOException
  {
    OutputStreamWriter w = new OutputStreamWriter(outputStream, "utf-8");
    logger.info("Return {} surveys", surveys.size());
    JSONWriter json = new JSONWriter(w);
    json.object().key("total").value(surveys.size());
    json.key("surveys").array();

    Map<String, List<String>> mapFilter = new TreeMap<>();
    if(filter != null)
    {
      for (String s : filter)
      {
        String[] arr = s.split(";");
        if (arr.length == 1)
          logger.error("Empty filter for: {}. Ignore.", arr[0]);
        if (arr.length > 2)
        {
          int pos = s.indexOf(';');
          String answer = s.substring(pos + 1);
          logger.warn("Filter {} contains more than one semicolon. Ignore: {}", arr[0], answer);

        }
        else
        {
          List<String> values = Arrays.asList(arr[1].split("\\,"));
          mapFilter.put(arr[0], values);
        }
      }
    }

    for(SurveyData surveyData : surveys.values())
    {

      boolean bInclude = true;
      for(Map.Entry<String, List<String>> entryFilter: mapFilter.entrySet())
      {
        String key = entryFilter.getKey();
        List<String> values = entryFilter.getValue();
        String sAnswer = null;
        IOListField ioListField = FIImpactSettings.getFiImpactSettings().getListFieldDefinition(key);
        if(!ioListField.getListId().equals(FIImpactSettings.LIST_SURVEYS))
        {
          ProjectData pd = surveyData.getProject();
          if(pd!=null)
          {
            if(ioListField.getListId().equals(FIImpactSettings.LIST_PROJECTS))
              sAnswer = pd.getValue(key);
            else if(ioListField.getListId().equals(FIImpactSettings.LIST_MATTERMARK))
              sAnswer = pd.getMattermarkValue(key);
          }
        }
        else
          sAnswer = surveyData.questions.get(key);

        if(sAnswer == null || sAnswer.equals(""))
          sAnswer = "EMPTY";
        boolean bOr = false;
        //I assume everything is a multiple choice....
        String[] arrAnswer = sAnswer.split("\\,");
        for(String sSingleAnswer: arrAnswer)
        {
          if(sSingleAnswer == null || sSingleAnswer.equals(""))
            sSingleAnswer = "EMPTY";
          for(String filterValue : values)
          {
            bOr = sSingleAnswer.equals(filterValue);
            if(bOr)
              break;
          }
          if(bOr)
            break;
        }
        bInclude = bOr;
        if(!bInclude)
          break;
      }

      if(bInclude)
      {
        json.object();

        json.key("info").object();
        addQuestionKey(json, "Q1_3", surveyData.questions);
        addQuestionKey(json, "Q1_4", surveyData.questions);
        json.endObject();

        String node_type = "normal";
        ProjectData pd = surveyData.getProject();
        if(pd != null)
        {
          if(pd.getValue("SUCCESS_VIP") != null && pd.getValue("SUCCESS_VIP").equals("X"))
            node_type = "VIP";
          else if(pd.getValue("SUCCESS_IDG") != null && pd.getValue("SUCCESS_IDG").equals("X"))
            node_type = "IDG";
          else if(pd.getValue("SUCCESS_VIP") != null && pd.getValue("SUCCESS_HPI").equals("X"))
            node_type = "HPI";
        }
        if(surveyData.getId().equals(referenceSurveyInternalID))
          node_type = "SELECTED";

        json.key("type").value(node_type);

        json.key("filters").object();

        for(String s: highlights)
        {

          IOListField ioListField = FIImpactSettings.getFiImpactSettings().getListFieldDefinition(s);
          if(!ioListField.getListId().equals(FIImpactSettings.LIST_SURVEYS))
          {
            if(pd != null)
            {
              if(ioListField.getListId().equals(FIImpactSettings.LIST_PROJECTS))
                addQuestionKey(json, s, pd.getFields());
              else if(ioListField.getListId().equals(FIImpactSettings.LIST_MATTERMARK))
                addQuestionKey(json, s, pd.getMattermarkFields());
            }
          }
          else
            addQuestionKey(json, s, surveyData.questions);
        }

        json.endObject();

        json.key("KPI").object();
        addResultKey(json, "INNOVATION", surveyData.results);
        addResultKey(json, "MARKET", surveyData.results);
        addResultKey(json, "FEASIBILITY", surveyData.results);
        addResultKey(json, "MARKET_NEEDS", surveyData.results);
        ArrayList<IOListField> mattermarkIndicators = FIImpactSettings.getFiImpactSettings().getMattermarkIndicators();
        for(IOListField indicator : mattermarkIndicators)
          addResultKey(json, indicator.getFieldid(), surveyData.results);

        json.endObject();
        json.key("KPI_percent").object();

        addResultKey(json, "INNOVATION_GRAPH_PERCENT", surveyData.resultDerivatives);
        addResultKey(json, "MARKET_GRAPH_PERCENT", surveyData.resultDerivatives);
        addResultKey(json, "FEASIBILITY_GRAPH_PERCENT", surveyData.resultDerivatives);
        addResultKey(json, "MARKET_NEEDS_GRAPH_PERCENT", surveyData.resultDerivatives);

        //add results for all mattermark indicators
        for(IOListField indicator : mattermarkIndicators)
          addResultKey(json, indicator.getFieldid()+"_GRAPH_PERCENT", surveyData.resultDerivatives);
        json.endObject();
        json.endObject();
      }
    }
    json.endArray();
    json.endObject();
    w.flush();
    w.close();
    logger.info("Returned {} surveys", surveys.size());

  }


  private boolean isFlagSet(int mask, int flag)
  {
    return (mask & flag) != 0;
  }

  public void exportJson(ServletOutputStream outputStream) throws IOException
  {
    logger.info("Export {} surveys to JSON (for QMiner analysys)", surveys.size());
    OutputStreamWriter writer = new OutputStreamWriter(outputStream, "utf-8");
    JSONWriter json = new JSONWriter(writer);
    json.object();

    IOListDefinition listMattermarkDef = FIImpactSettings.getFiImpactSettings().getListDefinition(FIImpactSettings.LIST_MATTERMARK);
    IOListDefinition listProjectsDef = FIImpactSettings.getFiImpactSettings().getListDefinition(FIImpactSettings.LIST_PROJECTS);
    IOListDefinition listSurveysDef = FIImpactSettings.getFiImpactSettings().getListDefinition(FIImpactSettings.LIST_SURVEYS);

    json.key("settings").array();
    json.object();
    json.key("field").value("id_internal");
    json.key("usage").value("id");
    json.key("type").value("text");
    json.endObject();

    json.object();
    json.key("field").value("full_text");
    json.key("usage").value("feature");
    json.key("type").value("text");
    json.endObject();

    json.object();
    json.key("field").value("node_type");
    json.key("usage").value("node_type");
    json.key("type").value("text");
    json.endObject();

    for(IOListField ioListField : listSurveysDef.getFields())
    {
      if(!(ioListField.getGraph().equals(FIImpactSettings.FIELD_GRAPH_IGNORE) || ioListField.getGraph().equals(FIImpactSettings.FIELD_GRAPH_FEATURE_TEXT)))
      {
        json.object();
        json.key("field").value(ioListField.getFieldid());
        json.key("usage").value(ioListField.getGraph());
        json.key("type").value(ioListField.getType());
        json.endObject();
      }
    }

    for(IOListField ioListField : listMattermarkDef.getFields())
    {
      if(!(ioListField.getGraph().equals(FIImpactSettings.FIELD_GRAPH_IGNORE) || ioListField.getGraph().equals(FIImpactSettings.FIELD_GRAPH_FEATURE_TEXT)))
      {
        json.object();
        json.key("field").value(ioListField.getFieldid());
        json.key("usage").value(ioListField.getGraph());
        json.key("type").value(ioListField.getType());
        json.endObject();
      }
    }

    for(IOListField ioListField : listProjectsDef.getFields())
    {
      if(!(ioListField.getGraph().equals(FIImpactSettings.FIELD_GRAPH_IGNORE) || ioListField.getGraph().equals(FIImpactSettings.FIELD_GRAPH_FEATURE_TEXT)))
      {
        json.object();
        json.key("field").value(ioListField.getFieldid());
        json.key("usage").value(ioListField.getGraph());
        json.key("type").value(ioListField.getType());
        json.endObject();
      }
    }
    json.endArray();

    json.key("surveys").array();
    synchronized(surveys)
    {

      for(SurveyData surveyData : surveys.values())
      {
        json.object();
        json.key("id_internal").value(surveyData.getId());
        StringBuilder fullText = new StringBuilder();

        String node_type = "normal";
        ProjectData pd = surveyData.getProject();
        if(pd != null)
        {
          if(pd.getValue("SUCCESS_VIP") != null && pd.getValue("SUCCESS_VIP").equals("X"))
            node_type = "VIP";
          else if(pd.getValue("SUCCESS_IDG") != null && pd.getValue("SUCCESS_IDG").equals("X"))
            node_type = "IDG";
          else if(pd.getValue("SUCCESS_VIP") != null && pd.getValue("SUCCESS_HPI").equals("X"))
            node_type = "HPI";
        }

        json.key("node_type").value(node_type);


        for(IOListField ioListField : listSurveysDef.getFields())
        {
          if(!ioListField.getGraph().equals(FIImpactSettings.FIELD_GRAPH_IGNORE))
          {
            String answer = surveyData.questions.get(ioListField.getFieldid());
            if(answer != null)
            {
              answer = FIImpactSettings.normaliseCSVString(answer);
              if(ioListField.getGraph().equals(FIImpactSettings.FIELD_GRAPH_FEATURE_TEXT))
                fullText.append(answer).append(" ");
              else
                json.key(ioListField.getFieldid()).value(answer);
            }
          }


        }

        ProjectData projectData = surveyData.getProject();
        for(IOListField ioListField : listMattermarkDef.getFields())
        {
          if(projectData != null)
          {
            if(!ioListField.getGraph().equals(FIImpactSettings.FIELD_GRAPH_IGNORE))
            {
              String answer = projectData.getMattermarkValue(ioListField.getFieldid());
              if(answer != null)
              {
                answer = FIImpactSettings.normaliseCSVString(answer);
                if(ioListField.getGraph().equals(FIImpactSettings.FIELD_GRAPH_FEATURE_TEXT))
                  fullText.append(answer).append(" ");
                else
                  json.key(ioListField.getFieldid()).value(answer);
              }
            }
          }
        }

        for(IOListField ioListField : listProjectsDef.getFields())
        {

          if(projectData != null)
          {
            if(!ioListField.getGraph().equals(FIImpactSettings.FIELD_GRAPH_IGNORE))
            {
              String answer = projectData.getValue(ioListField.getFieldid());
              if(answer != null)
              {
                answer = FIImpactSettings.normaliseCSVString(answer);
                if(ioListField.getGraph().equals(FIImpactSettings.FIELD_GRAPH_FEATURE_TEXT))
                  fullText.append(answer).append(" ");
                else
                  json.key(ioListField.getFieldid()).value(answer);
              }
            }
          }

        }
        json.key("full_text").value(fullText.toString());
        json.endObject();
      }

      json.endArray();
    }
    json.endObject();

    writer.flush();
    writer.close();

    logger.info("Saved {} surveys", surveys.size());
  }

  public void exportText(ServletOutputStream outputStream, String groupQuestion, String groupAnswer, int exportSettings) throws IOException
  {
    OutputStreamWriter writer = new OutputStreamWriter(outputStream, "utf-8");
    logger.info("Export {} surveys", surveys.size());

    logger.info("Export {} surveys", surveys.size());

    String questionsList = null;
    String indicatorsList = null;

    if(isFlagSet(exportSettings, FIImpactSettings.EXPORT_SHORT_LIST) || !isFlagSet(exportSettings, FIImpactSettings.EXPORT_FI_IMPACT_QUESTIONS))
      questionsList = FIImpactSettings.SHORT_QUESTIONS_LIST;

    if(isFlagSet(exportSettings, FIImpactSettings.EXPORT_SHORT_LIST))
      indicatorsList = FIImpactSettings.SHORT_INDICATORS_LIST;

    ArrayList<String> questionsDef = new ArrayList<>();
    SortedSet<String> indicatorsDef = new TreeSet<>();
    SortedSet<String> indicatorsDerivedDef = new TreeSet<>();

    if(questionsList != null)
    {
      String[] arr = questionsList.split(";");
      Collections.addAll(questionsDef, arr);
    }
    else
    {
      if(isFlagSet(exportSettings, FIImpactSettings.EXPORT_FI_IMPACT_QUESTIONS))
        for(IOListField ioListField : FIImpactSettings.getFiImpactSettings().getListDefinition(FIImpactSettings.LIST_SURVEYS).getFields())
          questionsDef.add(ioListField.getFieldid());
    }

    if(indicatorsList != null)
    {
      String[] arr = indicatorsList.split(";");
      Collections.addAll(indicatorsDef, arr);
    }

    IOListDefinition listMattermarkDef = null;
    IOListDefinition listProjectsDef = null;
    if(isFlagSet(exportSettings, FIImpactSettings.EXPORT_MATTERMARK_FIELDS))
    {
      listMattermarkDef = FIImpactSettings.getFiImpactSettings().getListDefinition(FIImpactSettings.LIST_MATTERMARK);
    }

    if(isFlagSet(exportSettings, FIImpactSettings.EXPORT_PROJECT_DATA))
    {
      listProjectsDef = FIImpactSettings.getFiImpactSettings().getListDefinition(FIImpactSettings.LIST_PROJECTS);
    }
    if(isFlagSet(exportSettings, FIImpactSettings.EXPORT_DERIVED_INDICATORS))
    {
      logger.debug("Export derived indicators flag is set");
    }


    synchronized(surveys)
    {
      if(!isFlagSet(exportSettings, FIImpactSettings.EXPORT_SHORT_LIST))
      {
        for(SurveyData sd : surveys.values())
        {

          if(isFlagSet(exportSettings, FIImpactSettings.EXPORT_FI_IMPACT_INDICATORS) || isFlagSet(exportSettings, FIImpactSettings.EXPORT_MATTERMARK_INDICATORS))
          {
            for(String s : sd.results.keySet())
            {
              if(s.contains("MATTERMARK"))
              {
                if(isFlagSet(exportSettings, FIImpactSettings.EXPORT_MATTERMARK_INDICATORS))
                  indicatorsDef.add(s);
              }
              else
              {
                if(isFlagSet(exportSettings, FIImpactSettings.EXPORT_FI_IMPACT_INDICATORS))
                  indicatorsDef.add(s);
              }
            }
          }

          if(isFlagSet(exportSettings, FIImpactSettings.EXPORT_DERIVED_INDICATORS))
          {
            for(String s : sd.resultDerivatives.keySet())
            {
              if(s.contains("MATTERMARK"))
              {
                if(isFlagSet(exportSettings, FIImpactSettings.EXPORT_MATTERMARK_INDICATORS))
                  indicatorsDerivedDef.add(s);
              }
              else
              {
                if(isFlagSet(exportSettings, FIImpactSettings.EXPORT_FI_IMPACT_INDICATORS))
                  indicatorsDerivedDef.add(s);
              }
            }
          }
        }
      }
    }


    StringBuilder sb = new StringBuilder();
    sb.append("id_external").append("\t").append("id_internal");
    logger.debug("Export {} questions", questionsDef.size());
    for(String s : questionsDef)
    {
      sb.append("\t").append(s);
    }

    if(listMattermarkDef != null)
    {
      logger.debug("Export {} Mattermark fields", listMattermarkDef.getFields().size());
      for(IOListField ioListField : listMattermarkDef.getFields())
      {
        sb.append("\t").append(ioListField.getLabel());
      }
    }

    if(listProjectsDef != null)
    {
      logger.debug("Export {} projects fields", listProjectsDef.getFields().size());
      for(IOListField ioListField : listProjectsDef.getFields())
      {
        sb.append("\t").append(ioListField.getLabel());
      }
    }

    logger.debug("Export {} indicators", indicatorsDef.size());
    for(String s : indicatorsDef)
    {
      //!!FF Hack!!!!!
      sb.append("\t").append("INDICATOR_").append(s);
    }

    logger.debug("Export derived {} indicators", indicatorsDerivedDef.size());
    for(String s : indicatorsDerivedDef)
    {
      sb.append("\t").append(s.split("#")[1]);
    }

    String sHeader = sb.toString();
    FIImpactSettings.writeLine(writer, sHeader);

    sb.setLength(0);

    synchronized(surveys)
    {
      for(SurveyData surveyData : surveys.values())
      {
        sb.append(surveyData.getExternalId()).append("\t").append(surveyData.getId());
        for(String s : questionsDef)
        {
          sb.append("\t");
          String answer = surveyData.questions.get(s);
          if(answer != null)
          {
            answer = FIImpactSettings.normaliseCSVString(answer);
            sb.append(answer);
          }
        }

        ProjectData projectData = surveyData.getProject();

        if(listMattermarkDef != null)
        {
          for(IOListField ioListField : listMattermarkDef.getFields())
          {
            sb.append("\t");
            if(projectData != null)
            {
              String answer = projectData.getMattermarkValue(ioListField.getFieldid());
              if(answer != null)
              {
                answer = FIImpactSettings.normaliseCSVString(answer);
                sb.append(answer);
              }
            }
          }
        }

        if(listProjectsDef != null)
        {
          for(IOListField ioListField : listProjectsDef.getFields())
          {
            sb.append("\t");
            if(projectData != null)
            {
              String answer = projectData.getValue(ioListField.getFieldid());
              if(answer != null)
              {
                answer = FIImpactSettings.normaliseCSVString(answer);
                sb.append(answer);
              }
            }
          }
        }

        for(String s : indicatorsDef)
        {
          sb.append("\t");
          Double r = surveyData.results.get(s);
          if(r != null)
          {
            sb.append(FIImpactSettings.getDecimalFormatter4().format(r));
          }
        }
        for(String s : indicatorsDerivedDef)
        {
          sb.append("\t");
          Double r = surveyData.resultDerivatives.get(s.split("#")[1]);
          if(r != null)
          {
            sb.append(FIImpactSettings.getDecimalFormatter4().format(r));
          }
        }
        boolean bInclude = true;
        if(groupQuestion != null)
        {
          String sAnswer = surveyData.questions.get(groupQuestion);
          if(sAnswer == null || sAnswer.equals(""))
            sAnswer = "EMPTY";
          bInclude = sAnswer.equals(groupAnswer);
        }
        if(bInclude)
        {
          FIImpactSettings.writeLine(writer, sb.toString());
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
    Double val = results.get(qID);
    if(val != null)
      json.key(qID).value(FIImpactSettings.getDecimalFormatter4().format(val));
  }

  synchronized public void clearAll(ServletOutputStream outputStream) throws IOException
  {
    OutputStreamWriter w = new OutputStreamWriter(outputStream, "utf-8");
    logger.info("Remove all {} surveys", surveys.size());
    JSONWriter json = new JSONWriter(w);
    json.object().key("total").value(surveys.size());
    synchronized(surveys)
    {
      for(String id : surveys.keySet())
      {
        Path p = FIImpactSettings.getFiImpactSettings().getSurveyRoot().resolve("survey-" + id + ".xml");
        //noinspection ResultOfMethodCallIgnored
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

  public static void main(String[] args) throws Exception
  {
    double d = 1.234;
    DecimalFormat df = FIImpactSettings.getDecimalFormatter4();
    System.out.println(df.format(d));
    df = FIImpactSettings.getDecimalFormatter0();
    System.out.println(df.format(d));
  }

}
