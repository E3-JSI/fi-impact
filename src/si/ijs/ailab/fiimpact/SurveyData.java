package si.ijs.ailab.fiimpact;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import si.ijs.ailab.util.AIStructures;
import si.ijs.ailab.util.AIUtils;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by flavio on 13/07/2015.
 */

class SurveyData
{

  final static Logger logger = LogManager.getLogger(SurveyData.class.getName());

  private String externalId;
  private String id;
  public Map<String, String> questions = new TreeMap<>();
  public Map<String, Double> results = new TreeMap<>();
  public Map<String, Double> resultDerivatives = new TreeMap<>();

  public enum OutputFormat {XML, JSON}

  public enum OutputType {UI, STRUCTURE}

  public static final Map<String, Map<String, Double>> SCORES = new TreeMap<>();

  private static String SCORES_5A1 =

          "\tA\tB\tC\tD\tE\tG\tH\tI\tJ\tQ\tL\tM\tN\tO\tP\tK\n"+
                  "A\t1.429343664\t1.179898431\t0.961603011\t1.390286064\t1.236054022\t1.227642276\t1.425778823\t0.978890118\t1.008393285\t0.812877979\t1.525322579\t1.201478265\t0.968197149\t0.623188406\t1.181697254\t1.230551934\n"+
                  "B\t0.818947206\t1.13593529\t1.421750283\t1.122820964\t0.883734586\t0.825203252\t0.995621959\t0.999631268\t1.20263789\t1.11170402\t0.780053751\t1.511603866\t1.666666667\t1.223188406\t0.731368514\t1.277488049\n"+
                  "C\t0.760135345\t0.215284544\t0.269490943\t0.400665196\t0.33568213\t0.203252033\t0.255752153\t0.719625737\t0.706235012\t1.538598364\t0.214677399\t1.201478265\t1.052350103\t0.271014493\t0.281039774\t0.479574098\n"+
                  "D\t1.666666667\t0.560243865\t1.666666667\t1.4883566\t0.883734586\t0.532520325\t0.926796861\t0.470731932\t1.569544365\t0.898256848\t1.139838702\t1.666666667\t0.850383014\t1.223188406\t1.147056582\t1.177748805\n"+
                  "E\t0.407342111\t0.509440874\t0.912491707\t1.078243448\t0.570561754\t0.825203252\t1.219303528\t0.58480826\t0.835731415\t1.069014586\t1.204086015\t0.581227064\t0.564262971\t0.81884058\t0.869931204\t0.874619731\n"+
                  "F\t0.733638835\t0.462032158\t0.200033435\t0.213439626\t0.413975338\t0.166666667\t0.58267137\t0.916666667\t0.166666667\t1.197082889\t0.227526861\t1.201478265\t0.168744088\t0.166666667\t0.177117758\t0.166666667\n"+
                  "G\t0.705914914\t1.666666667\t0.931266295\t1.46161009\t0.883734586\t1.666666667\t1.494603921\t1.528530605\t1.213429257\t0.556741373\t1.22978494\t1.666666667\t0.665246515\t1.066666667\t1.666666667\t1.385049978\n"+
                  "H\t0.714415877\t0.504798325\t0.924592425\t1.666666667\t1.666666667\t1.117886179\t1.666666667\t1.134448746\t1.666666667\t1.666666667\t1.666666667\t1.511603866\t0.841967719\t1.666666667\t1.250978599\t1.666666667\n"+
                  "I\t0.166666667\t0.166666667\t0.175292104\t0.166666667\t0.166666667\t0.857009654\t0.166666667\t1.666666667\t1.202654077\t0.166666667\t0.166666667\t0.166666667\t0.166666667\t0.262668297\t0.166666667\t0.179060979\n"+
                  "J\t0.316821663\t0.217659798\t0.166666667\t1.111081921\t0.265609708\t0.384075203\t0.747959145\t0.166666667\t0.774004796\t0.542547136\t0.723117782\t0.413332816\t0.168541489\t0.771010145\t0.618535185\t0.341067579\n"+
                  "K\t0.729747903\t0.713021647\t0.218419291\t1.126336793\t0.869162263\t0.529593496\t1.200075516\t0.944791667\t1.069296523\t0.771207755\t1.549248278\t1.35771179\t0.849352077\t0.715301449\t0.925640333\t1.24679248";

  private static ArrayList<String> JSON_TRUNCATE_DECIMALS = new ArrayList<>();

  private static final String[] SLOT_INTERPRETATION = {"l", "l", "m", "h", "h"};


  static
  {
    //Questions listed in this list will be truncated for UI output
    JSON_TRUNCATE_DECIMALS.add("Q1_13");
    JSON_TRUNCATE_DECIMALS.add("Q1_16");
    JSON_TRUNCATE_DECIMALS.add("Q1_7");
    JSON_TRUNCATE_DECIMALS.add("Q1_8");
    JSON_TRUNCATE_DECIMALS.add("Q1_9");
    JSON_TRUNCATE_DECIMALS.add("Q3_2a");
    JSON_TRUNCATE_DECIMALS.add("Q3_2b");
    JSON_TRUNCATE_DECIMALS.add("Q3_2c");
    JSON_TRUNCATE_DECIMALS.add("Q4_3");
    JSON_TRUNCATE_DECIMALS.add("Q4_3a");
    JSON_TRUNCATE_DECIMALS.add("Q4_3b");
    JSON_TRUNCATE_DECIMALS.add("Q4_3c");
    JSON_TRUNCATE_DECIMALS.add("Q4_3d");
    JSON_TRUNCATE_DECIMALS.add("Q4_6");

    //Wiights for calculating scores
    Map<String, Double> m = new HashMap<>();

    m.put("TRL1", 1.0);
    m.put("TRL2", 1.3);
    m.put("TRL3", 1.9);
    m.put("TRL4", 2.6);
    m.put("TRL5", 3.5);
    m.put("TRL6", 4.6);
    m.put("TRL7", 5.9);
    m.put("TRL8", 4.6);
    m.put("TRL9", 3.5);
    SCORES.put("Q2_1", m);

    m = new HashMap<>();
    m.put("A", 0.90);
    m.put("B", 1.10);
    SCORES.put("Q2_2", m);

    m = new HashMap<>();
    m.put("A", 0.75);
    m.put("B", 1.50);
    SCORES.put("Q2_3", m);

    m = new HashMap<>();
    m.put("A", 1.00);
    m.put("B", 2.50);
    SCORES.put("Q2_4", m);

    m = new HashMap<>();
    m.put("A", 0.70);
    m.put("B", 1.50);
    SCORES.put("Q2_5", m);

    //SECTION 3 - MARKET
    m = new HashMap<>();
    m.put("A", 2.0);
    m.put("B", 1.0);
    m.put("C", 0.8);
    SCORES.put("Q2_2_A_3_7_W1", m);

    m = new HashMap<>();
    m.put("A", 0.0);
    m.put("B", 1.0);
    m.put("C", 1.2);
    SCORES.put("Q2_2_A_3_7_W2", m);

    m = new HashMap<>();
    m.put("A", 2.0);
    m.put("B", 1.5);
    m.put("C", 1.0);
    SCORES.put("Q2_2_B_3_7_W1", m);

    m = new HashMap<>();
    m.put("A", 0.0);
    m.put("B", 0.5);
    m.put("C", 1.0);
    SCORES.put("Q2_2_B_3_7_W2", m);


    m = new HashMap<>();
    m.put("A", 1.0);
    m.put("B", 3.0);
    m.put("C", 5.0);
    SCORES.put("Q3_8", m);

    m = new HashMap<>();
    m.put("A", 1.0);
    m.put("B", 3.0);
    m.put("C", 5.0);
    SCORES.put("Q3_9", m);

    m = new HashMap<>();
    m.put("A", 1.0);
    m.put("B", 3.0);
    m.put("C", 5.0);
    SCORES.put("Q3_10", m);

    m = new HashMap<>();
    m.put("A", 1.0);
    m.put("B", 3.0);
    m.put("C", 5.0);
    SCORES.put("Q3_11", m);

    //SECTION 4 - FEASIBILITY
    m = new HashMap<>();
    m.put("A", 1.0);
    m.put("B", 3.0);
    m.put("C", 5.0);
    SCORES.put("Q4_1", m);

    m = new HashMap<>();
    m.put("A", 1.0);
    m.put("B", 3.0);
    m.put("C", 5.0);
    SCORES.put("Q4_2", m);

    m = new HashMap<>();
    m.put("A", 1.0);
    m.put("B", 3.0);
    m.put("C", 5.0);
    SCORES.put("Q4_4", m);

    m = new HashMap<>();
    m.put("A", 1.0);
    m.put("B", 3.0);
    m.put("C", 5.0);
    SCORES.put("Q4_5", m);

    m = new HashMap<>();
    m.put("A", 2.0);
    m.put("B", 1.2);
    m.put("C", 1.0);
    SCORES.put("Q2_2_A_3_7_W3", m);

    m = new HashMap<>();
    m.put("A", 0.0);
    m.put("B", 0.8);
    m.put("C", 1.0);
    SCORES.put("Q2_2_A_3_7_W4", m);

    m = new HashMap<>();
    m.put("A", 2.0);
    m.put("B", 1.8);
    m.put("C", 1.1);
    SCORES.put("Q2_2_B_3_7_W3", m);

    m = new HashMap<>();
    m.put("A", 0.0);
    m.put("B", 0.2);
    m.put("C", 0.8);
    SCORES.put("Q2_2_B_3_7_W4", m);

    String[] arr5aRows = SCORES_5A1.split("\n");
    String[] arrVerticals = arr5aRows[0].split("\t");

    HashMap<String, Double> m5A1_Verticals = new HashMap<>();
    HashMap<String, Double> m5A1_Benefits = new HashMap<>();
    for(int i = 1; i < arrVerticals.length; i++)
      m5A1_Verticals.put(arrVerticals[i], 0.0);

    m = new HashMap<>();
    for(int i = 1; i < arr5aRows.length; i++)
    {
      String[] arrRow = arr5aRows[i].split("\t");

      m5A1_Benefits.put(arrRow[0], 0.0);

      for(int j = 1; j < arrRow.length; j++)
      {
        String row_column = arrRow[0] + "_" + arrVerticals[j];
        double d = AIUtils.parseDecimal(arrRow[j], 0.0);
        //logger.debug("{}: {}", row_column, d);
        m.put(row_column, d);
      }
    }
    SCORES.put("Q5A_1", m);
    SCORES.put("Q5A_1_BENEFITS", m5A1_Benefits);
    SCORES.put("Q5A_1_VERTICALS", m5A1_Verticals);
  }


  private static double SPEEDOMETER_R = 100.0;
  private static double SPEEDOMETER_r = 80.0;

  private static String SPEEDOMETER_ARC_SVG = "M %s %s A %s %s 0 0 1 %s %s L %s %s A %s %s 0 0 0 %s %s";
  private static String[] SPEEDOMETER_COLORS = {"#923933", "#F4B900", "#00A54F"};
  private static String SPEEDOMETER_NEEDLE_SVG = "M %s %s L %s %s L %s %s";

  private String getSVGArc(double dR, double dr, double boundaryLo, double boundaryHi)
  {

    double dR0x = -dR*Math.cos(Math.PI*boundaryLo);
    double dR0y = -dR*Math.sin(Math.PI *boundaryLo);

    double dR1x = -dR*Math.cos(Math.PI * boundaryHi);
    double dR1y = -dR*Math.sin(Math.PI * boundaryHi);

    double dr0x = -dr*Math.cos(Math.PI * boundaryLo);
    double dr0y = -dr*Math.sin(Math.PI * boundaryLo);

    double dr1x = -dr*Math.cos(Math.PI * boundaryHi);
    double dr1y = -dr*Math.sin(Math.PI * boundaryHi);


    //"M %s %s A %s %s 0 0 1 %s %s L %s %s A %s %s 0 0 0 %s %s"
    String svg = String.format(SPEEDOMETER_ARC_SVG,
            SurveyManager.getDecimalFormatter2().format(dR0x),SurveyManager.getDecimalFormatter2().format(dR0y),
            SurveyManager.getDecimalFormatter2().format(dR),SurveyManager.getDecimalFormatter2().format(dR),
            SurveyManager.getDecimalFormatter2().format(dR1x),SurveyManager.getDecimalFormatter2().format(dR1y),
            SurveyManager.getDecimalFormatter2().format(dr1x),SurveyManager.getDecimalFormatter2().format(dr1y),
            SurveyManager.getDecimalFormatter2().format(dr),SurveyManager.getDecimalFormatter2().format(dr),
            SurveyManager.getDecimalFormatter2().format(dr0x),SurveyManager.getDecimalFormatter2().format(dr0y));

    return svg;

  }

  private void getAverageSVG(double averagePercent, JSONObject jsonSpeedometerSVG)
  {
    double R = SPEEDOMETER_R;
    double len = 15.0;
    double r = 3;

    double average_x = -R*Math.cos(Math.PI*averagePercent);
    double average_y = -R*Math.sin(Math.PI * averagePercent);

    double avg_rad = Math.PI * averagePercent;
    double xl = average_x - r*Math.cos(avg_rad+Math.PI/2.0);
    double yl = average_y - r*Math.sin(avg_rad + Math.PI / 2.0);

    double xt = average_x + len*Math.cos(avg_rad);
    double yt = average_y + len*Math.sin(avg_rad);

    double xr = average_x - r*Math.cos(avg_rad-Math.PI/2.0);
    double yr = average_y - r*Math.sin(avg_rad - Math.PI / 2.0);

    //"M %s %s L %s %s L %s %s";
    String svg = String.format(SPEEDOMETER_NEEDLE_SVG,
            SurveyManager.getDecimalFormatter2().format(xl),SurveyManager.getDecimalFormatter2().format(yl),
            SurveyManager.getDecimalFormatter2().format(xt),SurveyManager.getDecimalFormatter2().format(yt),
            SurveyManager.getDecimalFormatter2().format(xr),SurveyManager.getDecimalFormatter2().format(yr));

    jsonSpeedometerSVG.put("average", svg);
    jsonSpeedometerSVG.put("average_x", SurveyManager.getDecimalFormatter2().format(average_x));
    jsonSpeedometerSVG.put("average_y", SurveyManager.getDecimalFormatter2().format(average_y));

  }

  private String getResultSVG(double resultPercent)
  {
    double len = 85.0;
    double r = 8.0;


    double res_rad = Math.PI * resultPercent;
    double xl = - r*Math.cos(res_rad-Math.PI/2.0);
    double yl = - r*Math.sin(res_rad-Math.PI/2.0);

    double xt = -len*Math.cos(res_rad);
    double yt = -len*Math.sin(res_rad);

    double xr = -r*Math.cos(res_rad+Math.PI/2.0);
    double yr = -r*Math.sin(res_rad+Math.PI/2.0);

    //"M %s %s L %s %s L %s %s";
    String svg = String.format(SPEEDOMETER_NEEDLE_SVG,
            SurveyManager.getDecimalFormatter2().format(xl),SurveyManager.getDecimalFormatter2().format(yl),
            SurveyManager.getDecimalFormatter2().format(xt),SurveyManager.getDecimalFormatter2().format(yt),
            SurveyManager.getDecimalFormatter2().format(xr),SurveyManager.getDecimalFormatter2().format(yr));

    return svg;
  }

  private String getSegmentSVG(double r, int i, int total, double dPercentResult, JSONObject segment)
  {
    double di = i;
    double dtotal = total;

    double x = r + r*dPercentResult*Math.cos(di*2*Math.PI/dtotal+Math.PI/2.0);
    double y = r - r*dPercentResult*Math.sin(di * 2 * Math.PI / dtotal + Math.PI / 2.0);

    String sx = SurveyManager.getDecimalFormatter2().format(x);
    String sy = SurveyManager.getDecimalFormatter2().format(y);
    String svg = sx+","+sy;
    segment.put("x", sx);
    segment.put("y", sy);
    return svg;

  }


  private JSONObject createResultAndSpeedometer(String sectionName, String sectionLabel, String sectionLabel2, JSONObject radarOverviewOut, int overviewSectionsCnt, int overviewSectionsTotal)
  {

    String type = getType();
    if (type.equals("S"))
      type = "IS";

    JSONObject jsonResult = new JSONObject();
    JSONObject jsonOverviewPoint = new JSONObject();
    JSONArray jsonOverviewPoints = radarOverviewOut.getJSONArray("points");
    jsonOverviewPoints.put(jsonOverviewPoint);
    String overviewOutLineAverage = radarOverviewOut.getString("line_average");
    String overviewOutLineResult = radarOverviewOut.getString("line_result");



    jsonOverviewPoint.put("id", sectionName);
    jsonOverviewPoint.put("label", sectionLabel2);


    logger.debug("create result for {}/{}", type, sectionName);

    Map<String, Map<String, OverallResult>> allResults = SurveyManager.getSurveyManager().getResults();
    Map<String, OverallResult> typeResults = allResults.get(type);

    OverallResult or = typeResults.get(sectionName);
    String averageSlot = SLOT_INTERPRETATION[or.getAverageSlot()];
    logger.debug("Calculating interpretation for {}", sectionName);
    Double dSlot = resultDerivatives.get(sectionName + "_GRAPH_SLOT");
    if(dSlot == null)
      dSlot = 0.0;

    int iMySlot = dSlot.intValue();
    logger.debug("My slot: {}", iMySlot);
    String mySlot = SLOT_INTERPRETATION[iMySlot];
    logger.debug("My slot interpretation: {}", mySlot);
    String key = mySlot + averageSlot;
    String response = SurveyManager.fiImpactModel.getJSONObject("interpretation").getString(key);

    Double dResult = results.get(sectionName);
    double R = 250.0;
    double dPercentResult = 0.0;
    if(dResult != null)
    {
      dPercentResult = or.getSpeedometerPercent(dResult);
      jsonResult.put("result_percent", SurveyManager.getDecimalFormatter2().format(dPercentResult));
      jsonOverviewPoint.put("result_percent", SurveyManager.getDecimalFormatter2().format(dPercentResult));
    }
    JSONObject xy = new JSONObject();
    jsonOverviewPoint.put("result_coord", xy);
    String svg = getSegmentSVG(R, overviewSectionsCnt, overviewSectionsTotal, dPercentResult, xy);
    if(overviewOutLineResult.length() > 0)
      overviewOutLineResult+=" ";
    overviewOutLineResult+=svg;
    radarOverviewOut.put("line_result", overviewOutLineResult);


    jsonResult.put("speedometer_lm", SurveyManager.getDecimalFormatter2().format(or.getSpeedometerPercentLM()));
    jsonResult.put("speedometer_mh", SurveyManager.getDecimalFormatter2().format(or.getSpeedometerPercentMH()));

    jsonResult.put("average_percent", SurveyManager.getDecimalFormatter2().format(or.getSpeedometerPercent(or.average)));
    jsonOverviewPoint.put("average_percent", SurveyManager.getDecimalFormatter2().format(or.getSpeedometerPercent(or.average)));
    jsonResult.put("speedometer_histogram", or.toJSONHistogram());


    xy = new JSONObject();
    jsonOverviewPoint.put("avg_coord", xy);
    svg = getSegmentSVG(R, overviewSectionsCnt, overviewSectionsTotal, or.getSpeedometerPercent(or.average), xy);
    if(overviewOutLineAverage.length() > 0)
      overviewOutLineAverage+=" ";
    overviewOutLineAverage+=svg;
    radarOverviewOut.put("line_average", overviewOutLineAverage);

    JSONObject jsonSpeedometerSVG = new JSONObject();
    jsonResult.put("speedometer_svg", jsonSpeedometerSVG);
    JSONArray jsonSpeedometerSegmentsSVG = new JSONArray();
    jsonSpeedometerSVG.put("segments", jsonSpeedometerSegmentsSVG);

    ArrayList<Double> speedometerBoundaries = new ArrayList<>();
    speedometerBoundaries.add(0.0);
    speedometerBoundaries.add(or.getSpeedometerPercentLM());
    speedometerBoundaries.add(or.getSpeedometerPercentMH());
    speedometerBoundaries.add(1.0);

    for(int i = 0; i < speedometerBoundaries.size()-1; i++)
    {
      JSONObject jsonSpeedometerSegment = new JSONObject();
      jsonSpeedometerSegmentsSVG.put(jsonSpeedometerSegment);
      jsonSpeedometerSegment.put("color", SPEEDOMETER_COLORS[i]);

      svg = getSVGArc(SPEEDOMETER_R, SPEEDOMETER_r, speedometerBoundaries.get(i), speedometerBoundaries.get(i + 1));
      jsonSpeedometerSegment.put("arc", svg);


      AIStructures.AIInteger cnt = or.graph.graphValues.get(i + 1);

      double total = or.n;
      double segmentN = 0.0;

      if(cnt != null)
        segmentN = (double)cnt.val;

      double segmentPercent = 0.0;
      if(total > 0.0)
        segmentPercent = segmentN/total;

      double dr = 12.0;
      double dR = SPEEDOMETER_R*0.65*(segmentPercent)+dr;
      //logger.debug("R= {}, r={}, percent={}, n={}, N={}", dR, dr, segmentPercent, segmentN, total);

      svg = getSVGArc(dR, dr, speedometerBoundaries.get(i), speedometerBoundaries.get(i+1));

      jsonSpeedometerSegment.put("histogram", svg);

    }

    double averagePercent = or.getSpeedometerPercent(or.average);
    getAverageSVG(averagePercent, jsonSpeedometerSVG);
    svg = getResultSVG(dPercentResult);
    jsonSpeedometerSVG.put("result", svg);

    //"Your ranking for %s based on the data submitted is currently %s.
    // In this section you scored better than %s% of the %s (total) projects and proposals that have answered this survey.";
    String scoreInWords = SurveyManager.fiImpactModel.getString("score_in_words");
    Double dPercent = resultDerivatives.get(sectionName + "_" + type + "_R");
    if(dPercent == null)
      dPercent = 0.0;
    dPercent = 100.0 - dPercent;

    scoreInWords = String.format(scoreInWords,
            sectionLabel, SurveyManager.fiImpactModel.getJSONObject("ranking").getString(Integer.toString(dSlot.intValue())),
            Integer.toString(dPercent.intValue()), Integer.toString(or.n));
     response = String.format(response, sectionLabel, sectionLabel);
     jsonResult.put("interpretation", scoreInWords + " " + response);

    return jsonResult;
  }



  private void write(OutputStream os, OutputFormat outputFormat, OutputType outputType, boolean writeResults) throws IOException
  {
    if(outputType == OutputType.STRUCTURE)
    {
      if (outputFormat == OutputFormat.XML)
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
        Element root = doc.createElement("survey");
        doc.appendChild(root);
        root.setAttribute("external", externalId);
        root.setAttribute("id", id);

        for (Map.Entry<String, String> qe : questions.entrySet())
        {
          Element q = doc.createElement("q");
          root.appendChild(q);
          q.setAttribute("id", qe.getKey());
          q.setAttribute("answer", qe.getValue());
        }

        if (writeResults)
        {
          for (Map.Entry<String, Double> re : results.entrySet())
          {
            Element r = doc.createElement("result");
            root.appendChild(r);
            r.setAttribute("id", re.getKey());
            r.setAttribute("score", SurveyManager.getDecimalFormatter4().format(re.getValue()));
          }
          for (Map.Entry<String, Double> re : resultDerivatives.entrySet())
          {
            Element r = doc.createElement("result");
            root.appendChild(r);
            r.setAttribute("id", re.getKey());
            r.setAttribute("score", SurveyManager.getDecimalFormatter0().format(re.getValue()));
          }

        }

        AIUtils.save(doc, os);
      }
      else
      {
        OutputStreamWriter w = new OutputStreamWriter(os, "utf-8");
        JSONWriter jsonSurvey = new JSONWriter(w);
        jsonSurvey.object().key("id").value(id).key("external_id").value(externalId);

        jsonSurvey.key("questions").array();
        for (Map.Entry<String, String> qe : questions.entrySet())
        {
          String id = qe.getKey();
          String val = qe.getValue();
          if (JSON_TRUNCATE_DECIMALS.contains(id))
          {
            int i = val.indexOf('.');
            if (i != -1)
              val = val.substring(0, i);
          }

          jsonSurvey.array().value(id).value(val).endArray();
        }
        jsonSurvey.endArray();

        if (writeResults)
        {
          jsonSurvey.key("results").array();
          for (Map.Entry<String, Double> re : results.entrySet())
          {
            jsonSurvey.array().value(re.getKey()).value(SurveyManager.getDecimalFormatter4().format(re.getValue())).endArray();
          }
          for (Map.Entry<String, Double> re : resultDerivatives.entrySet())
          {
            jsonSurvey.array().value(re.getKey()).value(SurveyManager.getDecimalFormatter4().format(re.getValue())).endArray();
          }
          jsonSurvey.endArray();
        }
        jsonSurvey.endObject();
        w.flush();
        w.close();
      }
    }
    else
    {
      JSONObject jsonSurvey = new JSONObject();
      jsonSurvey.put("id", id);
      jsonSurvey.put("external_id", externalId);
      JSONObject jsonSectionsOut = new JSONObject();
      jsonSurvey.put("sections", jsonSectionsOut);
      JSONObject radarOverviewOut = new JSONObject();
      radarOverviewOut.put("line_average", "");
      radarOverviewOut.put("line_result", "");
      radarOverviewOut.put("points", new JSONArray());

      jsonSurvey.put("overview", radarOverviewOut);

      JSONArray modelSections = SurveyManager.fiImpactModel.getJSONArray("sections");

      int overviewSectionsTotal = 0;
      for(int i = 0; i < modelSections.length(); i++)
      {
        JSONObject section = modelSections.getJSONObject(i);
        String sectionName = section.optString("name", null);
        if (sectionName != null)
          overviewSectionsTotal++;
      }
      int overviewSectionsCnt = 0;
      for(int i = 0; i < modelSections.length(); i++)
      {
        JSONObject section = modelSections.getJSONObject(i);
        JSONObject sectionOut = new JSONObject();
        jsonSectionsOut.put("S_" + section.getString("id"), sectionOut);
        JSONObject answersOut = new JSONObject();
        sectionOut.put("answers", answersOut);
        JSONArray questionsDef = section.getJSONArray("questions");
        String sQuestionNameRoot = "Q"+section.getString("id")+"_";
        String sectionName = section.optString("name", null);
        String sectionLabel = section.optString("label", null);
        String sectionLabel2 = section.optString("label_graph", null);

        if(sectionName != null)
        {
          sectionOut.put("result", createResultAndSpeedometer(sectionName, sectionLabel, sectionLabel2, radarOverviewOut, overviewSectionsCnt, overviewSectionsTotal));
          overviewSectionsCnt++;
        }

        logger.info("section {}", sQuestionNameRoot);
        for(int j  = 0; j < questionsDef.length(); j++)
        {
          //logger.info("question seq: {}", j);
          JSONObject questionDef = questionsDef.getJSONObject(j);
          JSONArray lookupDef = questionDef.optJSONArray("lookup");
          JSONArray mergeDef = questionDef.optJSONArray("merge");
          JSONArray multipleDef = questionDef.optJSONArray("multiple_fields");
          String sList = questionDef.optString("list", "false");
          String sLabel = questionDef.optString("label", null);
          JSONArray answersListDef = questionDef.optJSONArray("answers_list");
          String customDef = questionDef.optString("custom", null);
          String defaultAnswer = questionDef.optString("default", null);
          String postfixChar = questionDef.optString("postfix", null);

          StringBuilder sbAnswer = new StringBuilder();
          if(customDef != null)
          {
            String s = questions.get(sQuestionNameRoot + questionDef.getString("id"));
            if(s!=null)
            {
              s = s.trim();
              if(!s.equals(""))
              {
                if(s.contains("A")) //"A" : "My City or Region"
                {
                  if (sbAnswer.length() != 0)
                    sbAnswer.append(", ");
                  String city = questions.get(getLookup(lookupDef, "A"));
                  if(city != null && !city.equals(""))
                    sbAnswer.append(city);
                  else
                    sbAnswer.append("My City or Region");
                }
                if(s.contains("B")) //"B" : "My country"
                {
                  if (sbAnswer.length() != 0)
                    sbAnswer.append(", ");
                  String country = questions.get(getLookup(lookupDef, "B"));
                  if(country != null && !country.equals(""))
                    sbAnswer.append(country);
                  else
                    sbAnswer.append("My country");

                }
                if(s.contains("C")) //"C" : "Multiple Countries"
                {
                  String countryList = questions.get(getLookup(lookupDef, "C"));
                  if (sbAnswer.length() != 0)
                    sbAnswer.append(", ");
                  sbAnswer.append("Multiple Countries");
                  boolean bFirstcouny = true;
                  if(countryList!=null)
                  {
                    sbAnswer.append(" (");
                    String[] sArr = countryList.split(",");
                    for(String sSegment: sArr)
                    {
                      sSegment = sSegment.trim();
                      if(!sSegment.equals(""))
                      {
                        if (bFirstcouny)
                        {
                          sbAnswer.append(sSegment);
                          bFirstcouny = false;
                        }
                        else
                          sbAnswer.append(", ").append(sSegment);
                      }
                    }
                    sbAnswer.append(")");
                  }

                }
                if(s.contains("D")) //"D" : "Global"
                {
                  String sSegment = getLookup(lookupDef, "D");
                  if (sbAnswer.length() != 0)
                    sbAnswer.append(", ");
                  sbAnswer.append(sSegment);

                }
                if(s.contains("E")) //"E" : "Other"
                {
                  String sSegment = getLookup(lookupDef, "E");
                  if (sbAnswer.length() != 0)
                    sbAnswer.append(", ");
                  sbAnswer.append(sSegment);

                }
              }
            }
          }
          else if(mergeDef != null)
          {
            for (int k = 0; k < mergeDef.length(); k++)
            {
              JSONObject mergeObjectDef = mergeDef.getJSONObject(k);
              String sValue = questions.get(sQuestionNameRoot + mergeObjectDef.getString("id"));
              if (sValue != null)
              {
                lookupDef = mergeObjectDef.optJSONArray("lookup");
                if (lookupDef != null)
                {
                  sValue = getLookup(lookupDef, sValue);
                }
                else
                  sValue = truncateDecimals(sQuestionNameRoot + mergeObjectDef.getString("id"), sValue);

                if(!sValue.equals(""))
                {
                  if (sbAnswer.length() != 0)
                    sbAnswer.append(", ").append(sValue);
                  else
                    sbAnswer.append(sValue);
                  if(postfixChar!=null)
                    sbAnswer.append(postfixChar);
                }
              }
            }
          }
          else if(answersListDef != null)
          {

            JSONArray answer = new JSONArray();
            answersOut.put(sQuestionNameRoot + questionDef.getString("id"), answer);
            JSONObject segmentAnswer = new JSONObject();
            answer.put(segmentAnswer);
            JSONArray subSegmentAnswers = new JSONArray();
            segmentAnswer.put("answers", subSegmentAnswers);
            for (int k = 0; k < answersListDef.length(); k++)
            {
              JSONObject def = answersListDef.getJSONObject(k);

              String sValue = questions.get(sQuestionNameRoot + def.getString("id"));
              if (sValue != null)
              {

                String sSubSegmentList = def.optString("list", "false");
                sValue = sValue.trim();
                if(!sSubSegmentList.equals("true"))
                  sValue = truncateDecimals(sQuestionNameRoot + def.getString("id"), sValue);

                if (!sValue.equals(""))
                {
                  JSONObject subSegmentAnswer = new JSONObject();
                  subSegmentAnswers.put(subSegmentAnswer);
                  subSegmentAnswer.put("id", sQuestionNameRoot + def.getString("id"));
                  subSegmentAnswer.put("label", def.getString("label"));
                  if (sSubSegmentList.equals("true"))
                  {
                    String[] sArr = sValue.split(",");
                    for(String sSegment: sArr)
                    {
                      sSegment = sSegment.trim();
                      if(!sSegment.equals(""))
                      {
                        if (sbAnswer.length() != 0)
                          sbAnswer.append(", ").append(sSegment);
                        else
                          sbAnswer.append(sSegment);
                        if(postfixChar!=null)
                          sbAnswer.append(postfixChar);

                      }
                    }
                  }
                  else
                  {
                    sbAnswer.append(sValue);
                    if(postfixChar!=null)
                      sbAnswer.append(postfixChar);
                  }
                  subSegmentAnswer.put("value", sbAnswer.toString());
                  sbAnswer.setLength(0);
                }
              }
            }
          }
          else if(lookupDef != null)
          {
            String s = questions.get(sQuestionNameRoot + questionDef.getString("id"));
            boolean bLinks = questionDef.optBoolean("links", false);
            if(s==null || s.equals(""))
              if(defaultAnswer != null)
                s = defaultAnswer;
            if(s!=null)
            {
              String[] sArr = s.split(",");
              if(bLinks)
              {
                JSONArray answersArr = new JSONArray();
                answersOut.put(sQuestionNameRoot + questionDef.getString("id"), answersArr);
                for (String sSegmentID : sArr)
                {
                  sSegmentID = sSegmentID.trim();
                  String sSegment = getLookup(lookupDef, sSegmentID);
                  if (!sSegment.equals(""))
                  {

                    JSONObject answer = new JSONObject();
                    answersArr.put(answer);
                    //answer.put("id", );
                    answer.put("label", sSegmentID);
                    if(!sSegmentID.equals(sSegment))
                      answer.put("link", sSegment);
                  }
                }

              }
              else
              {
                for (String sSegmentID : sArr)
                {
                  sSegmentID = sSegmentID.trim();
                  String sSegment = getLookup(lookupDef, sSegmentID);
                  if (!sSegment.equals(""))
                  {
                    if (sbAnswer.length() != 0)
                      sbAnswer.append(", ").append(sSegment);
                    else
                      sbAnswer.append(sSegment);
                  }
                }
              }
            }
          }
          else if(sList.equals("true"))
          {
            String s = questions.get(sQuestionNameRoot + questionDef.getString("id"));
            if(s==null || s.equals(""))
              if(defaultAnswer != null)
                s = defaultAnswer;

            if(s!=null)
            {
              String[] sArr = s.split(",");
              for(String sSegment: sArr)
              {
                sSegment = sSegment.trim();
                if(!sSegment.equals(""))
                {
                  if (sbAnswer.length() != 0)
                    sbAnswer.append(", ").append(sSegment);
                  else
                    sbAnswer.append(sSegment);
                  if(postfixChar!=null)
                    sbAnswer.append(postfixChar);
                }
              }
            }

          }
          else if(multipleDef != null)
          {
            JSONArray multiplyBy = questionDef.optJSONArray("multiply");
            String ui_specific = questionDef.optString("ui_specific", "");
            boolean doStars = ui_specific.equals("stars");

            if(multiplyBy != null && multiplyBy.length() > 0)
            {
              boolean topList = questionDef.optBoolean("top_list", false);
              JSONArray answer = new JSONArray();
              answersOut.put(sQuestionNameRoot + questionDef.getString("id"), answer);
              String resultID = questionDef.optString("result", null);
              for(int k = 0; k < multiplyBy.length(); k++)
              {
                JSONObject multiplyQuestion = multiplyBy.getJSONObject(k);
                String mSectionID = multiplyQuestion.getString("section");
                String mQuestionID = multiplyQuestion.getString("question");
                logger.debug("Find {}, {}", mSectionID, mQuestionID);
                JSONObject mQuestionDef = findQuestionDef(modelSections, mSectionID, mQuestionID);
                JSONArray mLookupDef = mQuestionDef.optJSONArray("lookup");
                if(mLookupDef != null)
                {
                  String smQuestionNameRoot = "Q"+mSectionID+"_";
                  String s = questions.get(smQuestionNameRoot + mQuestionID);
                  logger.debug("User answer: {}", s);
                  if(s!=null)
                  {
                    String[] sArr = s.split(",");
                    logger.debug("Answers: {}", sArr.length);
                    for(String sSegmentId: sArr)
                    {
                      sSegmentId = sSegmentId.trim();
                      String sSegment = getLookup(mLookupDef, sSegmentId);
                      if(!sSegment.equals("") && !sSegment.equals(sSegmentId))
                      {
                        JSONObject segmentAnswer = new JSONObject();
                        answer.put(segmentAnswer);
                        segmentAnswer.put("label", sSegment);
                        if(resultID != null)
                        {
                          Double res = results.get(resultID + "_" + sSegmentId);
                          if(res == null)
                            res = 0.0;
                          segmentAnswer.put("result", SurveyManager.getDecimalFormatter2().format(res));
                        }


                        JSONArray subSegmentAnswers = new JSONArray();
                        segmentAnswer.put("answers", subSegmentAnswers);
                        for(int l = 0; l < multipleDef.length(); l++)
                        {
                          JSONObject def = multipleDef.getJSONObject(l);
                          String key = def.getString("id");
                          String sValue = questions.get(sQuestionNameRoot + questionDef.getString("id") + "_" + key);
                          JSONObject subSegmentAnswer = new JSONObject();
                          subSegmentAnswers.put(subSegmentAnswer);
                          subSegmentAnswer.put("id", key);
                          subSegmentAnswer.put("label", def.getString("label"));
                          if (sValue != null)
                          {
                            subSegmentAnswer.put("value", sValue);
                            if(doStars)
                            {
                              int score = AIUtils.parseInteger(sValue, 0);
                              JSONArray jesusChristSuperstar = new JSONArray();
                              subSegmentAnswer.put("star", jesusChristSuperstar);
                              for (int iStar = 0; iStar < score; iStar++)
                              {
                                jesusChristSuperstar.put(new JSONObject());
                              }
                            }
                          }
                          else
                          {
                            subSegmentAnswer.put("value", "0");
                          }
                        }

                        if(topList)
                        {
                          logger.debug("adding top list for {}", sSegmentId);
                          JSONArray subSegmentTop = new JSONArray();
                          segmentAnswer.put("top_list", subSegmentTop);



                          JSONArray jsonTopListDef = SurveyManager.fiImpactModel.getJSONObject("marketNeedsTop5").optJSONArray(sSegmentId);
                          if(jsonTopListDef != null)
                          {
                            for(int l = 0; l < jsonTopListDef.length(); l++)
                            {
                              String topEntryId = jsonTopListDef.getString(l);
                              String topEntryLabel = getLookup(multipleDef, topEntryId);
                              JSONObject topEntry = new JSONObject();
                              topEntry.put("id", topEntryId);
                              topEntry.put("label", topEntryLabel);
                              subSegmentTop.put(topEntry);
                            }
                          }

                        }


                      }
                    }
                  }
                }
              }
            }
            else
            {
              JSONObject segmentAnswer = new JSONObject();
              JSONArray subSegmentAnswers = new JSONArray();
              segmentAnswer.put("answers", subSegmentAnswers);
              answersOut.put(sQuestionNameRoot + questionDef.getString("id"), segmentAnswer);

              String lineAverage = "";
              String lineResult = "";


              for(int l = 0; l < multipleDef.length(); l++)
              {
                JSONObject def = multipleDef.getJSONObject(l);
                String key = def.getString("id");
                JSONObject subSegmentAnswer = new JSONObject();
                subSegmentAnswers.put(subSegmentAnswer);
                subSegmentAnswer.put("id", key);
                subSegmentAnswer.put("label", def.getString("label"));
                String type = getType();
                if (type.equals("S"))
                  type = "IS";
                OverallResult or = SurveyManager.getSurveyManager().getResults().get(type).get(sQuestionNameRoot + questionDef.getString("id") + "_" + key);
                if(or!=null)
                {
                  Double dResult = results.get(sQuestionNameRoot + questionDef.getString("id") + "_" + key);
                  double dPercentResult = 0.0;
                  if(dResult != null)
                  {
                    dPercentResult = or.getSpeedometerPercent(dResult);
                  }

                  double dPercentAverage = or.getSpeedometerPercent(or.average);

                  subSegmentAnswer.put("result_percent", SurveyManager.getDecimalFormatter2().format(dPercentResult));
                  subSegmentAnswer.put("average_percent", SurveyManager.getDecimalFormatter2().format(or.getSpeedometerPercent(or.average)));

                  double R = 200;

                  JSONObject xy = new JSONObject();
                  subSegmentAnswer.put("result_coord", xy);
                  String svg = getSegmentSVG(R, l, multipleDef.length(), dPercentResult, xy);
                  if(lineResult.length() > 0)
                    lineResult+=" ";
                  lineResult+=svg;


                  xy = new JSONObject();
                  subSegmentAnswer.put("avg_coord", xy);
                  svg = getSegmentSVG(R, l, multipleDef.length(), dPercentAverage, xy);
                  if(lineAverage.length() > 0)
                    lineAverage+=" ";
                  lineAverage+=svg;
                }

              }
              segmentAnswer.put("line_average", lineAverage);
              segmentAnswer.put("line_result", lineResult);

            }
          }
          else
          {
            String sValue = questions.get(sQuestionNameRoot + questionDef.getString("id"));
            if(sValue==null || sValue.equals(""))
              if(defaultAnswer != null)
                sValue = defaultAnswer;

            if(sValue != null)
            {
              sbAnswer.append(sValue);
              if(postfixChar!=null)
                sbAnswer.append(postfixChar);
            }
          }

          if(sbAnswer.length() != 0)
          {
            String sAnswer = truncateDecimals(sQuestionNameRoot + questionDef.getString("id"), sbAnswer.toString());
            if(!sAnswer.equals(""))
            {
              JSONObject answer = new JSONObject();
              answersOut.put(sQuestionNameRoot + questionDef.getString("id"), answer);
              answer.put("value", sAnswer);
              if (sLabel != null)
                answer.put("label", sLabel);
            }
          }

        }
      }



      if(outputFormat == OutputFormat.JSON)
      {
        OutputStreamWriter w = new OutputStreamWriter(os, "utf-8");
        jsonSurvey.write(w);
        w.flush();
        w.close();
      }
      else
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
        Element root = doc.createElement("survey");
        doc.appendChild(root);
        jsonToXML(jsonSurvey, doc, root);
        AIUtils.save(doc, os);

      }
    }
    logger.info("Saved survey {}/{} with {} questions and {} results", externalId, id, questions.size(), results.size());
  }


  private String truncateDecimals(String id, String sAnswer)
  {
    if (JSON_TRUNCATE_DECIMALS.contains(id))
    {
      int index = sAnswer.indexOf('.');
      if (index != -1)
        sAnswer =  sAnswer.substring(0, index);
    }
    if(sAnswer.equals("0"))
      sAnswer = "";
    return sAnswer;

  }

  private String getLookup(JSONArray lookupDef, String sValue)
  {
    String ret = sValue;
    for(int i = 0; i < lookupDef.length(); i++)
    {
      JSONObject json = lookupDef.getJSONObject(i);
      if(json.has(sValue))
      {
        return json.getString(sValue);
      }
      else if(json.has("id"))
      {
        String id = json.getString("id");
        if(id.equals(sValue))
          return json.optString("label", sValue);
      }
    }

    return sValue;
  }

  private JSONObject findQuestionDef(JSONArray modelSections, String mSectionID, String mQuestionID)
  {

    for(int i = 0; i < modelSections.length(); i++)
    {
      JSONObject section = modelSections.getJSONObject(i);
      String sectionId = section.getString("id");
      if(sectionId.equals(mSectionID))
      {
        JSONArray questions = section.getJSONArray("questions");
        for(int j = 0; j < questions.length(); j++)
        {
          JSONObject question = questions.getJSONObject(j);

          if((question.getString("id")).equals(mQuestionID))
            return question;
        }
      }
    }
    return null;
  }


  private void addJsonObject(String id, JSONObject json, Document doc, Element element)
  {
    logger.debug("Adding xml from json: {}", id);
    Element subElement = doc.createElement(AIUtils.XMLEscape(id));
    element.appendChild(subElement);
    jsonToXML(json, doc, subElement);

  }

  private void addJsonValue(String id, String value, Document doc, Element element)
  {
    logger.debug("Adding xml from json: {}={}", id, value);
    Element subElement = doc.createElement(AIUtils.XMLEscape(id));
    element.appendChild(subElement);
    subElement.setTextContent(AIUtils.XMLEscape(value));

  }

  private void addJsonArray(String id, JSONArray jsonArray, Document doc, Element element)
  {
    logger.debug("Adding xml from json: {}", id);
    Element subElement = doc.createElement("list_"+AIUtils.XMLEscape(id));
    element.appendChild(subElement);
    jsonArrayToXML(id, jsonArray, doc, subElement);

  }


  private void jsonArrayToXML(String id, JSONArray json, Document doc, Element element)
  {
    for(int i = 0; i < json.length(); i++)
    {
      Object obj = json.get(i);
      if(obj instanceof JSONObject)
      {
        addJsonObject(id, (JSONObject)obj, doc, element);
      }
      else if(obj instanceof JSONArray)
      {
        addJsonArray(id, (JSONArray) obj, doc, element);
      }
      else
      {
        addJsonValue(id, obj.toString(), doc, element);
      }

    }
  }

  private void jsonToXML(JSONObject json, Document doc, Element element)
  {
    Iterator<String> iter = json.keys();
    while(iter.hasNext())
    {
      String id= iter.next();
      Object obj = json.get(id);
      if(obj instanceof JSONObject)
      {
        addJsonObject(id, (JSONObject)obj, doc, element);
      }
      else if(obj instanceof JSONArray)
      {
        addJsonArray(id, (JSONArray) obj, doc, element);
      }
      else
      {
        addJsonValue(id, obj.toString(), doc, element);
      }
    }
  }

  public void saveSurvey(Path root)
  {
    Path p = root.resolve("survey-" + id + ".xml");
    try
    {
      write(new FileOutputStream(p.toFile()), OutputFormat.XML, OutputType.STRUCTURE, false);
    } catch (IOException e)
    {
      logger.error("Cannot save survey {}", p.toString());
    }
  }

  public void writeStructureXML(OutputStream os) throws IOException
  {
    write(os, OutputFormat.XML, OutputType.STRUCTURE, true);
  }

  public void writeUIXML(OutputStream os , JSONObject averages) throws IOException
  {
    write(os, OutputFormat.XML, OutputType.UI, true);
  }

  public void writeUIJSON(OutputStream os, JSONObject averages) throws IOException
  {
    write(os, OutputFormat.JSON, OutputType.UI, true);
  }

  public void writeStructureJSON(OutputStream os) throws IOException
  {
    write(os, OutputFormat.JSON, OutputType.STRUCTURE, true);
  }

  public void read(InputStream is) throws ParserConfigurationException, IOException, SAXException
  {
    questions.clear();
    results.clear();
    resultDerivatives.clear();

    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db = dbf.newDocumentBuilder();
    //Document doc = db.parse(new FileInputStream(f));
    Document doc = db.parse(is);
    externalId = doc.getDocumentElement().getAttribute("external");
    id = doc.getDocumentElement().getAttribute("id");

    NodeList nl = doc.getElementsByTagName("q");

    for (int i = 0; i < nl.getLength(); i++)
    {
      Element e = (Element) nl.item(i);
      questions.put(e.getAttribute("id"), e.getAttribute("answer"));

    }

    /*nl = doc.getElementsByTagName("result");

    for(int i = 0; i < nl.getLength(); i++)
    {
      Element e = (Element) nl.item(i);
      try
      {
        double r = getDecimalFormatter4().parse(e.getAttribute("score")).doubleValue();
        results.put(e.getAttribute("id"), r);
      }
      catch (ParseException e1)
      {
        results.put(e.getAttribute("id"), 0.0);
      }
    }
    */
    logger.info("Loaded survey {}/{} with {} questions results", externalId, id, questions.size());
  }


  public void calculateResults()
  {
    results.clear();
    resultDerivatives.clear();
    logger.debug("Calculate results for {}", id);
    //------------INNOVATION--------------------
    String Q2_1 = questions.get("Q2_1");
    String Q2_2 = questions.get("Q2_2");
    String Q2_3 = questions.get("Q2_3");
    String Q2_4 = questions.get("Q2_4");
    String Q2_5 = questions.get("Q2_5");

    logger.debug("Q2_X: {}, {}, {}, {}, {}", Q2_1, Q2_2, Q2_3, Q2_4, Q2_5);

    if (Q2_1 != null && Q2_2 != null && Q2_3 != null && Q2_4 != null && Q2_5 != null)
    {
      Double A2_1 = SCORES.get("Q2_1").get(Q2_1);
      Double A2_2 = SCORES.get("Q2_2").get(Q2_2);
      Double A2_3 = SCORES.get("Q2_3").get(Q2_3);
      Double A2_4 = SCORES.get("Q2_4").get(Q2_4);
      Double A2_5 = SCORES.get("Q2_5").get(Q2_5);

      logger.debug("A2_X: {}, {}, {}, {}, {}", A2_1, A2_2, A2_3, A2_4, A2_5);

      if (A2_1 != null && A2_2 != null && A2_3 != null && A2_4 != null && A2_5 != null)
      {
        Double r = (A2_1 + A2_4) * A2_2 * A2_3 * A2_5;
        logger.debug("R2: {}", r);
        results.put("INNOVATION", r);
      }

    }


    //------------MARKET--------------------

    //questionnaire v1 has all market sectors in 3_3.
    //questionnaire v2 has main market sector in 3_3a and "other" sectors in 3_3a.
    //in both cases we simply sum the number of choices.

    String Q3_3 = questions.get("Q3_3");
    String Q3_3a = questions.get("Q3_3a");

    String Q3_4 = questions.get("Q3_4");
    String Q3_5 = questions.get("Q3_5");
    String Q3_5_LIST = questions.get("Q3_5c");

    String Q3_7 = questions.get("Q3_7");
    String Q3_8 = questions.get("Q3_8");
    String Q3_9 = questions.get("Q3_9");
    String Q3_10 = questions.get("Q3_10");
    String Q3_11 = questions.get("Q3_11");


    logger.debug("Q3_X: {}, {}, {}, {}, {}, {}, {}, {}, {}", Q3_3, Q3_3a, Q3_4, Q3_5, Q3_7, Q3_8, Q3_9, Q3_10, Q3_11);

    if ((Q3_3 != null || Q3_3a != null) && Q3_4 != null && Q3_5 != null && Q3_7 != null && Q3_8 != null && Q3_9 != null && Q3_10 != null && Q3_11 != null && Q2_2 != null)
    {
      int Q3_3_length = 0;
      if(Q3_3 != null)
      {
        String[] arr = Q3_3.split(",");
        Q3_3_length = arr.length;
      }
      if(Q3_3a != null)
      {
        Q3_3_length+=Q3_3a.length();
      }

      Double A3_3 = (1.0 * Q3_3_length) / 4.0;

      String[] arr = Q3_4.split(",");
      Double A3_4 = (1.0 * arr.length) / 4.0;

      Double A3_5 = 0.0;
      List<String> listQ3_5 = Arrays.asList(Q3_5.split(","));
        /*
          '@Q3_5 = case 'answer3_5'
            when 'A' then 0.50 --city/region
            when 'B' then 0.75 --my country
            when 'C' then 5.00 --global
            when 'D' then count('answer3_5list')*0.75 end --multiple select country

         */
      if (listQ3_5.contains("A"))
        A3_5 += 0.5;

      if (listQ3_5.contains("B"))
        A3_5 += 0.75;

      if (listQ3_5.contains("C"))
      {
        if (Q3_5_LIST != null)
        {
          arr = Q3_5_LIST.split(",");
          A3_5 += 0.75 * arr.length;
        }
      }

      if (listQ3_5.contains("D"))
        A3_5 += 5.0;

      //Market weights from Q2_2 and Q3_7
      //"Q2_2_A_3_7_W1"
      Double W1 = null;
      Double W2 = null;

      String keyQ2_2 = "Q2_2_" + Q2_2 + "_3_7_";
      Map<String, Double> m = SCORES.get(keyQ2_2 + "W1");

      if (m != null)
      {
        W1 = m.get(Q3_7);
      }

      m = SCORES.get(keyQ2_2 + "W2");

      if (m != null)
      {
        W2 = m.get(Q3_7);
      }

      Double A3_8 = SCORES.get("Q3_8").get(Q3_8);
      Double A3_9 = SCORES.get("Q3_9").get(Q3_9);
      Double A3_10 = SCORES.get("Q3_10").get(Q3_10);
      Double A3_11 = SCORES.get("Q3_11").get(Q3_11);

      if (A3_5 != null && A3_8 != null && A3_9 != null && A3_10 != null && A3_11 != null && W1 != null && W2 != null)
      {
        logger.debug("A3_X: 3: {}, 4: {}, 5: {}, 8: {}, 9: {}, 10: {}, 11: {}, W1: {}, W2: {}", A3_3, A3_4, A3_5, A3_8, A3_9, A3_10, A3_11, W1, W2);

        /*
          --calc result --
          @R3 = @W1*(@Q3_8+@Q3_9) + @W2*(@Q3_10 +@Q3_11 +@Q3_3 +Q3_4 +@Q3_5)"
        */
        Double r = W1 * (A3_8 + A3_9) + W2 * (A3_10 + A3_11 + A3_3 + A3_4 + A3_5);
        logger.debug("R3: {}", r);
        results.put("MARKET", r);
      }

    }

    //------------FEASIBILITY--------------------


    String Q4_1 = questions.get("Q4_1");
    String Q4_2 = questions.get("Q4_2");
    String Q4_4 = questions.get("Q4_4");
    String Q4_5 = questions.get("Q4_5");
    String Q4_6 = questions.get("Q4_6");


    logger.debug("Q4_X: {}, {}, {}, {}, {}", Q4_1, Q4_2, Q4_4, Q4_5, Q4_6);

    if (Q4_1 != null && Q4_2 != null && Q4_4 != null && Q4_5 != null && Q4_6 != null && Q2_2 != null && Q3_7 != null)
    {

      //Market weights from Q2_2 and Q3_7
      //"Q2_2_A_3_7_W3",W4
      Double W3 = null;
      Double W4 = null;

      String keyQ2_2 = "Q2_2_" + Q2_2 + "_3_7_";
      Map<String, Double> m = SCORES.get(keyQ2_2 + "W3");

      if (m != null)
      {
        W3 = m.get(Q3_7);
      }

      m = SCORES.get(keyQ2_2 + "W4");

      if (m != null)
      {
        W4 = m.get(Q3_7);
      }

      Double A4_1 = SCORES.get("Q4_1").get(Q4_1);
      Double A4_2 = SCORES.get("Q4_2").get(Q4_2);
      Double A4_4 = SCORES.get("Q4_4").get(Q4_4);
      Double A4_5 = SCORES.get("Q4_5").get(Q4_5);
      Double A4_6 = AIUtils.parseDecimal(Q4_6, 0.0);

      if (A4_1 != null && A4_2 != null && A4_4 != null && A4_5 != null && W3 != null && W4 != null)
      {
        /*Flavio - added additional check for 4.6 ("What is the % required capital you already have ?".
          Should not be > 100%
          */
        if(A4_6 > 100.0)
        {
          logger.warn("Q4_6: {} > 100%. Default to 100.", A4_6);
          A4_6 = 100.0;
        }

        /*
          --calc result --
          @R3 = (@W1*(@Q4_1 * (1-'answer4_6'))   + @W2*(@Q4_2 +@Q4_4 +@Q4_5)/3 )/2
          @R3 = (@W1*(@Q4_1 * 'answer4_6' / 100) + @W2*(@Q4_2 +@Q4_4 +@Q4_5)/3 )/2
        */
        A4_6 = A4_6 / 100.0;
        logger.debug("A4_X: 1: {}, 2: {}, 4: {}, 5: {}, 6: {}, W3: {}, W4: {}", A4_1, A4_2, A4_4, A4_5, A4_6, W3, W4);

        //Double r = (W3 * (A4_1 * A4_6 / 100.0) + W4 * (A4_2 + A4_4 + A4_5) / 3) / 2;
        Double r = (W3 * (A4_1 + A4_6 / 100.0 * 5.0) + W4 * (A4_2 + A4_4 + A4_5) / 3) / 2;

        logger.debug("R4: {}", r);
        results.put("FEASIBILITY", r);
      }

    }

    //------------5A Market needs - Business and Public sector (B2B/B2G)--------------------


    //String Q3_3 = questions.get("Q3_3");
    HashMap<String, Integer> Q5A1_list = new HashMap<>();

    Map<String, Double> m5A1_Benefits = SCORES.get("Q5A_1_BENEFITS");
    Map<String, Double> m5A1_Verticals = SCORES.get("Q5A_1_VERTICALS");

    Map<String, Double> m5A1_weights = SCORES.get("Q5A_1");

    for (String s : m5A1_Benefits.keySet())
    {
      String Q5 = questions.get("Q5A_1_" + s);
      if (Q5 != null && !Q5.equals(""))
        Q5A1_list.put(s, AIUtils.parseInteger(Q5, 0));
    }

    logger.debug("Q3_3: {} Q3_3a: {} Q5A1 list size {}", Q3_3, Q3_3a, Q5A1_list.size());

    if ((Q3_3 != null || Q3_3a != null)&& Q5A1_list.size() > 0)
    {

      ArrayList<String> Q3_3_allSelections = new ArrayList<>();
      if(Q3_3a != null)
        Q3_3_allSelections.add(Q3_3a);

      if(Q3_3 != null)
      {
        String[] arrQ3_3 = Q3_3.split(",");
        Collections.addAll(Q3_3_allSelections, arrQ3_3);
      }
      if (Q3_3_allSelections.size() > 0)
      {
        //double r = 0.0;
        //int verticals = 0;
        double maxVerticalScore = 0.0;
        for (String vertical : Q3_3_allSelections)
        {
          if (m5A1_Verticals.containsKey(vertical))
          {
            //verticals++;
            double verticalScore = 0.0;
            for (Map.Entry<String, Integer> rowScore : Q5A1_list.entrySet())
            {
              Double weight = m5A1_weights.get(rowScore.getKey() + "_" + vertical);
              if (weight != null)
                verticalScore += weight * rowScore.getValue();
            }

            logger.debug("Market needs - Business {}: {}", vertical, verticalScore);
            results.put("MARKET_NEEDS_BUSINESS_"+vertical, verticalScore);

            if(Q3_3a == null)
            {
              if (verticalScore > maxVerticalScore)
                maxVerticalScore = verticalScore;
            }
            else if(vertical.equals(Q3_3a))
            {
              maxVerticalScore = verticalScore;
            }
          }
        }
        //if (verticals > 0)
          //r = r / verticals;

        logger.debug("Market needs - Business: {}", maxVerticalScore);
        results.put("MARKET_NEEDS_BUSINESS", maxVerticalScore);
      }
    }


    //------------SOCIAL IMPACT 6A, 6B--------------------
    logger.debug("Calc social impact");

    for(String qID: SurveyManager.SOCIAL_IMPACT_QUESTIONS)
    {
      String qA = questions.get(qID);
      //logger.debug("{}: {}", qID, qA);
      if (qA != null)
      {
        //we just copy the given score
        Double r = AIUtils.parseDecimal(qA, 0.0);
        //logger.debug("{}: {}", qID, r);
        results.put(qID, r);
      }
    }
    logger.debug("Calc social impact done.");

  }

  public void clear()
  {
    questions.clear();
    results.clear();
    resultDerivatives.clear();
  }

  public void addQuestions(String[] arrQuestions)
  {
    questions.clear();
    results.clear();
    resultDerivatives.clear();
    for (String s : arrQuestions)
    {
      String[] arr = s.split(";");
      if (arr.length == 1)
        logger.error("Empty answer for: {}. Ignore.", arr[0]);
      if (arr.length > 2)
      {
        int pos = s.indexOf(';');
        String answer = s.substring(pos + 1);
        logger.warn("Question {} contains more than one semicolon: {}", arr[0], answer);
        questions.put(arr[0], answer);
      } else
        questions.put(arr[0], arr[1]);
    }
  }

  public void addQuestion(String id, String answer)
  {
    questions.put(id, answer);
  }

  public void addQuestions(String[] headerArr, String[] lineArr, int startIndex)
  {
    questions.clear();
    results.clear();
    resultDerivatives.clear();
    for (int i = startIndex; i < headerArr.length; i++)
    {
      String qID = headerArr[i];
      String qAnswer = lineArr[i];
      questions.put(qID, qAnswer);
    }

  }

  public String getExternalId()
  {
    return externalId;
  }

  public String getId()
  {
    return id;
  }

  public void setExternalId(String externalId)
  {
    this.externalId = externalId;
  }

  public void setId(String id)
  {
    this.id = id;
  }

  public String getType()
  {
    String s = questions.get("Q0_1");
    if(s==null || s.equals(""))
      return SurveyManager.QUESTIONNAIRE_TYPE_DEFAULT;
    else
      return s;
  }

}
