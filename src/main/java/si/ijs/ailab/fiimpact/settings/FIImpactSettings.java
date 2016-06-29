package si.ijs.ailab.fiimpact.settings;

import com.opencsv.CSVWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import si.ijs.ailab.fiimpact.indicators.OverallResult;
import si.ijs.ailab.fiimpact.project.ProjectManager;
import si.ijs.ailab.fiimpact.qminer.QMinerManager;
import si.ijs.ailab.fiimpact.survey.SurveyManager;
import si.ijs.ailab.util.AIUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

/**
 * Created by flavio on 25/05/2016.
 */
public class FIImpactSettings
{
  private final static Logger logger = LogManager.getLogger(FIImpactSettings.class.getName());


  public static final Map<String, OverallResult.ScoreBoundaries> SPEEDOMETER_SLOTS = new HashMap<>();
  public static final ArrayList<String> SOCIAL_IMPACT_QUESTIONS = new ArrayList<>();
  public static final String[] QUESTIONNAIRE_TYPE = new String[]{"I", "S", "IS"};
  public static final String QUESTIONNAIRE_TYPE_DEFAULT = "I";
  public static final int EXPORT_SHORT_LIST = 1;
  public static final int EXPORT_FI_IMPACT_QUESTIONS = 2;
  public static final int EXPORT_FI_IMPACT_INDICATORS = 4;
  public static final int EXPORT_MATTERMARK_FIELDS = 8;
  public static final int EXPORT_MATTERMARK_INDICATORS = 16;
  public static final int EXPORT_DERIVED_INDICATORS = 32;
  public static final int EXPORT_PROJECT_DATA = 64;
  private static final char newline = '\n';
  public static final String SHORT_QUESTIONS_LIST = "Q1_1;Q1_2;Q1_3;Q1_4;Q1_22";
  public static final String SHORT_INDICATORS_LIST = "FEASIBILITY;INNOVATION;MARKET;MARKET_NEEDS";
  public static final boolean RANDOM_VARIANCE_PLOT = true;

  private static FIImpactSettings fiImpactSettings;

  private static String SPEEDOMETER =
          "\tmin\tlow\tmed\thigh\n" +
                  "INNOVATION\t0.0\t1.667\t3.333\t5.000\n" +
                  "MARKET\t0.0\t1.667\t3.333\t5.000\n" +
                  "FEASIBILITY\t0.0\t1.667\t3.333\t5.000\n" +
                  "MARKET_NEEDS\t0.0\t1.667\t3.333\t5.000\n";
  private static String SOCIAL_IMPACT = "Q6A_1_A\tQ6A_1_B\tQ6A_1_C\tQ6A_1_D\tQ6A_1_E\tQ6A_1_F\tQ6A_1_G\tQ6A_1_H\tQ6A_1_I\tQ6A_1_J\tQ6A_1_K\tQ6B_1_A\tQ6B_1_B\tQ6B_1_C\tQ6B_1_D\tQ6B_1_E\tQ6B_1_F";


  public enum OutputFormat {XML, JSON}

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

  private static String SCORES_5B1 ="\tA\tB\tC\tD\tE\tF\tG\tH\tI\tJ\n" +
          "A\t0.3888888888888890\t0.1388888888888890\t0.0972222222222222\t0.3472222000000000\t0.0000000000000000\t0.1388889000000000\t0.2083333000000000\t0.8333333000000000\t0.5555556000000000\t0.1388890000000000\n" +
          "B\t0.6944444444444450\t0.0000000000000000\t0.0000000000000000\t0.8333333000000000\t0.8333333000000000\t0.0000000000000000\t0.4305556000000000\t0.0000000000000000\t0.3888889000000000\t0.3472220000000000\n" +
          "C\t0.8333333333333330\t0.6250000000000000\t0.6944444444444450\t0.6944444000000000\t0.5555556000000000\t0.3472222000000000\t0.6944444000000000\t0.6944444000000000\t0.8333333000000000\t0.8333333000000000\n" +
          "D\t0.4861111111111110\t0.8333333333333330\t0.8333333333333330\t0.0416667000000000\t0.6944444000000000\t0.4444444000000000\t0.0000000000000000\t0.5555556000000000\t0.1388889000000000\t0.5555560000000000\n" +
          "E\t0.0000000000000000\t0.2777777777777780\t0.3888888888888890\t0.0000000000000000\t0.2777778000000000\t0.8333333000000000\t0.3611111000000000\t0.4166667000000000\t0.0972222000000000\t0.0000000000000000\n" +
          "F\t0.5555555555555560\t0.6944444444444450\t0.3194444444444440\t0.5277778000000000\t0.1388889000000000\t0.5555556000000000\t0.8333333000000000\t0.7916667000000000\t0.6944444000000000\t0.6944440000000000\n" +
          "G\t0.2083333333333330\t0.4444444444444440\t0.5555555555555560\t0.0972222000000000\t0.4166667000000000\t0.6944444000000000\t0.5555556000000000\t0.2083333000000000\t0.0000000000000000\t0.2500000000000000";

  public static final ArrayList<String> JSON_TRUNCATE_DECIMALS = new ArrayList<>();

  public static final String[] SLOT_INTERPRETATION = {"l", "l", "m", "h", "h"};

  public static final double SPEEDOMETER_R = 100.0;
  public static final double SPEEDOMETER_r = 80.0;

  public static final String SPEEDOMETER_ARC_SVG = "M %s %s A %s %s 0 0 1 %s %s L %s %s A %s %s 0 0 0 %s %s";
  public static final String[] SPEEDOMETER_COLORS = {"#923933", "#F4B900", "#00A54F"};
  public static final String SPEEDOMETER_NEEDLE_SVG = "M %s %s L %s %s L %s %s";

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

    //Weights for calculating scores
    Map<String, Double> m = new HashMap<>();

    m.put("TRL1", 1.0);
    m.put("TRL2", 1.2);
    m.put("TRL3", 1.3);
    m.put("TRL4", 1.4);
    m.put("TRL5", 1.5);
    m.put("TRL6", 1.6);
    m.put("TRL7", 1.7);
    m.put("TRL8", 1.7);
    m.put("TRL9", 1.7);
    SCORES.put("Q2_1", m);

    m = new HashMap<>();
    m.put("A", 1.0);
    m.put("B", 1.2);
    SCORES.put("Q2_2", m);

    m = new HashMap<>();
    m.put("A", 0.75);
    m.put("B", 1.00);
    SCORES.put("Q2_3", m);

    m = new HashMap<>();
    m.put("A", 1.0);
    m.put("B", 1.2);
    SCORES.put("Q2_4", m);

    m = new HashMap<>();
    m.put("A", 1.0);
    m.put("B", 1.2);
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
    m.put("B", 1.5);
    m.put("C", 2.0);
    m.put("D", 2.5);
    m.put("E", 1.0);
    SCORES.put("Q3_5", m);

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


    //5A_1
    String[] arr5a1Rows = SCORES_5A1.split("\n");
    String[] arr5a1Verticals = arr5a1Rows[0].split("\t");

    Map<String, Double> m5A1_Verticals = new HashMap<>();
    Map<String, Double> m5A1_Benefits = new HashMap<>();
    for(int i = 1; i < arr5a1Verticals.length; i++)
      m5A1_Verticals.put(arr5a1Verticals[i], 0.0);

    m = new HashMap<>();
    for(int i = 1; i < arr5a1Rows.length; i++)
    {
      String[] arrRow = arr5a1Rows[i].split("\t");

      m5A1_Benefits.put(arrRow[0], 0.0);

      for(int j = 1; j < arrRow.length; j++)
      {
        String row_column = arrRow[0] + "_" + arr5a1Verticals[j];
        double d = AIUtils.parseDecimal(arrRow[j], 0.0);
        //logger.debug("{}: {}", row_column, d);
        m.put(row_column, d);
      }
    }
    SCORES.put("Q5A_1", m);
    SCORES.put("Q5A_1_BENEFITS", m5A1_Benefits);
    SCORES.put("Q5A_1_VERTICALS", m5A1_Verticals);

    //5B_1
    String[] arr5b1Rows = SCORES_5B1.split("\n");
    String[] arr5b1Verticals = arr5b1Rows[0].split("\t");

    HashMap<String, Double> m5B1_Verticals = new HashMap<>();
    HashMap<String, Double> m5B1_Benefits = new HashMap<>();
    for(int i = 1; i < arr5b1Verticals.length; i++)
      m5B1_Verticals.put(arr5b1Verticals[i], 0.0);

    m = new HashMap<>();
    for(int i = 1; i < arr5b1Rows.length; i++)
    {
      String[] arrRow = arr5b1Rows[i].split("\t");

      m5B1_Benefits.put(arrRow[0], 0.0);

      for(int j = 1; j < arrRow.length; j++)
      {
        String row_column = arrRow[0] + "_" + arr5b1Verticals[j];
        double d = AIUtils.parseDecimal(arrRow[j], 0.0);
        //logger.debug("{}: {}", row_column, d);
        m.put(row_column, d);
      }
    }
    SCORES.put("Q5B_1", m);
    SCORES.put("Q5B_1_BENEFITS", m5B1_Benefits);
    SCORES.put("Q5B_1_VERTICALS", m5B1_Verticals);



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

    m5A1_Verticals = SCORES.get("Q5A_1_VERTICALS");
    OverallResult.ScoreBoundaries marketNeedsBusiness = SPEEDOMETER_SLOTS.get("MARKET_NEEDS");
    for(String s : m5A1_Verticals.keySet())
    {
      SPEEDOMETER_SLOTS.put("MARKET_NEEDS_" + s, marketNeedsBusiness);
    }


  }

  public final static String LIST_PROJECTS = "project-list";
  public final static String LIST_MATTERMARK = "mattermark-export";
  public final static String LIST_MAPPING = "fi-impact-url-mapping";
  public final static String LIST_SURVEYS = "survey";


  public final static String FIELD_TYPE_TEXT = "text";
  public final static String FIELD_TYPE_LOOKUP = "lookup";
  public final static String FIELD_TYPE_CATEGORY = "category";
  public final static String FIELD_TYPE_MULTI = "multi";
  public final static String FIELD_TYPE_INT = "int";
  public final static String FIELD_TYPE_NUM = "num";
  public final static String FIELD_TYPE_IGNORE = "ignore";

  public final static String FIELD_PLOT_IGNORE = "ignore";
  public final static String FIELD_PLOT_SELECTION = "selection";
  public final static String FIELD_PLOT_DISPLAY = "display";
  public final static String FIELD_PLOT_INDICATOR = "indicator";

  public final static String FIELD_GRAPH_DISPLAY = "display";
  public final static String FIELD_GRAPH_FEATURE = "feature";
  public final static String FIELD_GRAPH_FEATURE_SELECTION = "feature-selection";
  public final static String FIELD_GRAPH_FEATURE_TEXT = "feature-text";
  public final static String FIELD_GRAPH_IGNORE = "ignore";
  public final static String FIELD_GRAPH_SELECTION = "selection";


  private final Path projectsList;
  private final Path projectsRoot;
  private Map<String, IOListDefinition> ioDefinitions;
  private Map<String, IOListField> ioAllFields;
  private ArrayList<IOListField> mattermarkIndicators = new ArrayList<>();


  private final Path surveyMapFile;
  private final Path surveyRoot;
  private final SurveyManager surveyManager;
  private final ProjectManager projectManager;
  private final QMinerManager qMinerManager;
  private final JSONObject fiImpactModel;



  private FIImpactSettings(ServletContext servletContext)
  {
    fiImpactSettings = this;

    Path webappRoot = new File(servletContext.getRealPath("/")).toPath();

    surveyMapFile = webappRoot.resolve("WEB-INF").resolve("survey-id-list.txt");
    surveyRoot = webappRoot.resolve("WEB-INF").resolve("survey");
    logger.debug("Root: {}", webappRoot.toString());
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
    Path m = webappRoot.resolve("js").resolve("fiModelNew.js");
    JSONObject fiImpactModelTemp = null;
    try
    {
      jsonData = readFile(m, StandardCharsets.UTF_8);
      fiImpactModelTemp = new JSONObject(jsonData);
    }
    catch (IOException e)
    {
      logger.error("Cant load model file {}", m.toString());
    }

    fiImpactModel = fiImpactModelTemp;
    //logger.info("Model: {}", fiImpactModel.toString());

    if(fiImpactModel == null)
      logger.error("Error loading fiModelNew.js");

    projectsList = webappRoot.resolve("WEB-INF").resolve("projects-id-list.txt");
    projectsRoot = webappRoot.resolve("WEB-INF").resolve("projects");

    if(Files.notExists(projectsRoot))
    {
      try
      {
        Files.createDirectory(projectsRoot);
        logger.debug("Created dir: {}", projectsRoot.toString());
      }
      catch (IOException e)
      {
        logger.error(e);
      }
    }
    ioDefinitions = new HashMap<>();
    ioAllFields = new HashMap<>();
    File listIoDef = webappRoot.resolve("WEB-INF").resolve("lists-io-def.xml").toFile();
    loadIOdef(listIoDef);

    parseSurveyLookups();

    projectManager = new ProjectManager();
    surveyManager = new SurveyManager();
    String url = servletContext.getInitParameter("qMinerUrl");
    logger.info("qMinerUrl: {}", url);
    qMinerManager = new QMinerManager(url);
  }

  public static synchronized void createSettings(ServletContext servletContext)
  {
    if(fiImpactSettings == null)
    {
      new FIImpactSettings(servletContext);
    }
  }

  public static FIImpactSettings getFiImpactSettings()
  {
    return fiImpactSettings;
  }


  private static String readFile(Path file, Charset encoding)
          throws IOException
  {
    byte[] encoded = Files.readAllBytes(file);
    return new String(encoded, encoding);
  }

  public JSONObject getFiImpactModel()
  {
    return fiImpactModel;
  }

  public Path getSurveyMapFile()
  {
    return surveyMapFile;
  }

  public Path getSurveyRoot()
  {
    return surveyRoot;
  }

  public Path getProjectsList()
  {
    return projectsList;
  }

  public Path getProjectsRoot()
  {
    return projectsRoot;
  }

  public SurveyManager getSurveyManager()
  {
    return surveyManager;
  }

  public QMinerManager getQMinerManager()
  {
    return qMinerManager;
  }

  public ProjectManager getProjectManager()
  {
    return projectManager;
  }

  public static void writeLine(Writer w, String s) throws IOException
  {
    w.write(s, 0, s.length());
    w.write(newline);
  }

  public static String normaliseCSVString(String s)
  {
    s = s.replace("\r\n", ", ");
    s = s.replace("\r", ", ");
    s = s.replace("\n", ", ");
    s = s.replace("\t", " ");
    if(s.contains("\""))
    {
      s = s.replace("\"", "\"\"");
      s = "\"" + s + "\"";
    }
    return s;
  }

  public static DecimalFormat getDecimalFormatter4()
  {
    DecimalFormatSymbols custom = new DecimalFormatSymbols();
    custom.setDecimalSeparator('.');
    custom.setGroupingSeparator(',');
    custom.setMinusSign('-');
    return new DecimalFormat("0.0000", custom);
  }

  public static DecimalFormat getDecimalFormatter2()
  {
    DecimalFormatSymbols custom = new DecimalFormatSymbols();
    custom.setDecimalSeparator('.');
    custom.setGroupingSeparator(',');
    custom.setMinusSign('-');
    return new DecimalFormat("0.00", custom);
  }

  public static DecimalFormat getDecimalFormatter0()
  {
    DecimalFormatSymbols custom = new DecimalFormatSymbols();
    custom.setDecimalSeparator('.');
    custom.setGroupingSeparator(',');
    custom.setMinusSign('-');
    return new DecimalFormat("0", custom);
  }

  private void loadIOdef(File listIoDef)
  {

    Document doc;
    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder dBuilder;

    try
    {
      dBuilder = dbFactory.newDocumentBuilder();
      doc = dBuilder.parse(listIoDef);
      doc.getDocumentElement().normalize();

      NodeList nList = doc.getElementsByTagName("list");
      for(int i = 0; i < nList.getLength(); i++)
      {
        String id = null;
        int iStart=0;
        Node nNode = nList.item(i);
        NodeList nodeList = nNode.getChildNodes();
        if(nNode.getNodeType() == Node.ELEMENT_NODE)
        {
          Element eElement = (Element) nNode;
          id = eElement.getAttribute("name");
          if(eElement.hasAttribute("header_row"))
            iStart = AIUtils.parseInteger(eElement.getAttribute("header_row"), 0);
          if(iStart > 0)
            iStart--;
        }

        IOListDefinition ioListDefinition = new IOListDefinition(id, iStart);


        for(int j = 0; j < nodeList.getLength(); j++)
        {
          if(nodeList.item(j).getNodeType() == Node.ELEMENT_NODE)
          {

            Element eElement = (Element) nodeList.item(j);
            IOListField ioListField = new IOListField(id, eElement.getAttribute("column"),
                    eElement.getAttribute("label"), eElement.getAttribute("fieldid"),
                    eElement.getAttribute("usage"), eElement.getAttribute("missing"),
                    eElement.getAttribute("include-record-when"), eElement.getAttribute("transform"),
                    eElement.getAttribute("type"),
                    eElement.getAttribute("plot"),eElement.getAttribute("graph"));
            ioListDefinition.addField(ioListField);
            ioAllFields.put(ioListField.getFieldid(), ioListField);
          }

        }

        ioDefinitions.put(id, ioListDefinition);
      }

      IOListDefinition ioListDefinition = ioDefinitions.get(LIST_MATTERMARK);
      ArrayList<IOListField> listDefinitionMattermark = ioListDefinition.getFields();

      for(IOListField ioListField : listDefinitionMattermark)
      {
        if(ioListField.getUsage().equals("indicator"))
          mattermarkIndicators.add(ioListField);
      }

    }
    catch (SAXException | IOException | ParserConfigurationException e)
    {
      logger.error("Error loading list definition.", e);
    }

  }

  public IOListDefinition getListDefinition(String sList)
  {
    return ioDefinitions.get(sList);
  }

  public IOListField getListFieldDefinition(String id)
  {
    return ioAllFields.get(id);
  }

  public Map<String, IOListField> getAllFields()
  {
    return ioAllFields;
  }

  public ArrayList<IOListField> getMattermarkIndicators()
  {
    return mattermarkIndicators;
  }


  private void parseSurveyLookups()
  {
    logger.info("Parse lookups");
    JSONArray jsonSections = FIImpactSettings.getFiImpactSettings().getFiImpactModel().getJSONArray("sections");
    for(int iSection = 0; iSection < jsonSections.length(); iSection++)
    {
      JSONObject jsonSection = jsonSections.getJSONObject(iSection);
      String sectionID = jsonSection.getString("id");
      JSONArray jsonQuestions = jsonSection.getJSONArray("questions");
      parseQuestions(sectionID, jsonQuestions, null);
    }
    JSONArray jsonAdditionalLookups = FIImpactSettings.getFiImpactSettings().getFiImpactModel().optJSONArray("additional_lookups");
    parseQuestions(jsonAdditionalLookups);

    logger.info("Parse lookups done");
  }


  private void parseQuestions(String sectionID, JSONArray jsonQuestions, String parentQuestion)
  {
    logger.debug("Parse: {}/{}", sectionID, parentQuestion);
    for(int iQuestion = 0; iQuestion < jsonQuestions.length(); iQuestion++)
    {
      JSONObject jsonQuestion = jsonQuestions.getJSONObject(iQuestion);
      String questionID = jsonQuestion.getString("id");
      String fullQuestionID = "Q" + sectionID + "_" + questionID;
      String sDefaultAnswer = jsonQuestion.optString("default", null);
      //logger.debug("Parse question: {}", fullQuestionID);
      boolean bLookupFound = false;
      IOListDefinition ioListDefinition = getListDefinition(LIST_SURVEYS);
      IOListField ioListField;
      if(parentQuestion == null)
        ioListField = ioListDefinition.getFieldsById().get(fullQuestionID);
      else
        ioListField = ioListDefinition.getCalculatedFieldsById().get(parentQuestion);

      if(ioListField != null)
      {
        if(sDefaultAnswer != null)
          ioListField.setDefaultAnswer(sDefaultAnswer);
        //logger.info("Found : {}", ioListField.getFieldid());
        JSONArray jsonLookups = jsonQuestion.optJSONArray("lookup");
        if(parentQuestion != null)
          ioListField.addCalculatedFrom(fullQuestionID);

        if(jsonLookups != null)
        {
          for(int iLookup = 0; iLookup < jsonLookups.length(); iLookup++)
          {
            JSONObject jsonLookup = jsonLookups.getJSONObject(iLookup);
            String key = null;
            String value = null;
            if(jsonLookup.length() == 1)
            {
              key = jsonLookup.keys().next();
              value = jsonLookup.getString(key);
            }
            else if(jsonLookup.length() == 2)
            {
              key = jsonLookup.getString("id");
              value = jsonLookup.getString("label");
            }
            if(key != null)
            {
              if(parentQuestion != null && !value.equals(""))
                key =  questionID + key;
              ioListField.addLookup(key, value);
            }
          }
          bLookupFound = true;
        }
      }

      if(!bLookupFound)
      {
        JSONArray jsonMerge = jsonQuestion.optJSONArray("merge");
        String calculated = jsonQuestion.optString("calculated");
        if(jsonMerge != null && calculated != null && calculated.equals("true"))
        {
          logger.info("Adding calculated filed {}", fullQuestionID);
          ioListField = new IOListField(LIST_SURVEYS, null, jsonQuestion.optString("label"), fullQuestionID,
                  null, null, null, null, "multi", "selection", "selection");
          ioListDefinition.addCalculatedField(ioListField);
          ioAllFields.put(ioListField.getFieldid(), ioListField);
          parseQuestions(sectionID, jsonMerge, fullQuestionID);
        }
      }
    }

  }

  private void parseQuestions(JSONArray jsonLookups)
  {
    if(jsonLookups == null)
    {
      logger.warn("No additional lookups");
      return;
    }
    for(int iQuestion = 0; iQuestion < jsonLookups.length(); iQuestion++)
    {
      JSONObject jsonQuestion = jsonLookups.getJSONObject(iQuestion);
      String questionID = jsonQuestion.getString("id");
      String sDefaultAnswer = jsonQuestion.optString("default", null);
      IOListField ioListField = getAllFields().get(questionID);
      if(ioListField != null)
      {
        if(sDefaultAnswer != null)
          ioListField.setDefaultAnswer(sDefaultAnswer);
        //logger.info("Found : {}", ioListField.getFieldid());
        JSONArray jsonLookup = jsonQuestion.optJSONArray("lookup");
        if(jsonLookup != null)
        {
          for(int iLookup = 0; iLookup < jsonLookup.length(); iLookup++)
          {
            JSONObject jsonLookupEntry = jsonLookup.getJSONObject(iLookup);
            String key = jsonLookupEntry.keys().next();
            String value = jsonLookupEntry.getString(key);
            ioListField.addLookup(key, value);
          }
        }

      }
    }
  }

  public void exportLegendTxt(ServletOutputStream outputStream) throws IOException
  {
    OutputStreamWriter writer = new OutputStreamWriter(outputStream, "utf-8");
    CSVWriter csvWriter = new CSVWriter(writer, '\t');
    String[] header = {"question id", "lookup", "label"};
    csvWriter.writeNext(header);
    IOListDefinition ioListDefinition = getListDefinition(LIST_SURVEYS);

    for(IOListField ioListField: ioListDefinition.getFields())
    {
      csvWriter.writeNext(new String[]{ioListField.getFieldid(), "label", ioListField.getLabel()});
      Map<String, String> lookups = ioListField.getLookup();
      for(Map.Entry<String, String> lookupEntry : lookups.entrySet())
      {
        csvWriter.writeNext(new String[]{ioListField.getFieldid(), lookupEntry.getKey(), lookupEntry.getValue()});
      }
    }
    csvWriter.close();
  }

  private void getListDefinitionJson(JSONWriter json, String listID, String plotFieldType)
  {
    IOListDefinition ioListDefinition = getListDefinition(listID);
    for(IOListField ioListField: ioListDefinition.getFields())
    {
      if (ioListField.getPlot().equals(plotFieldType))
      {
        json.object();
        json.key("field").value(ioListField.getFieldid());
        json.key("label").value(ioListField.getLabel());
        json.key("type").value(ioListField.getType());
        if(ioListField.getLookup().size() > 0)
        {
          json.key("lookup").array();
          for(Map.Entry<String, String> lookupEntry: ioListField.getLookup().entrySet())
            json.object().key(lookupEntry.getKey()).value(lookupEntry.getValue()).endObject();
          json.endArray();
        }
        json.endObject();
      }
    }
    for(IOListField ioListField: ioListDefinition.getCalculatedFieldsById().values())
    {
      if (ioListField.getPlot().equals(plotFieldType))
      {
        json.object();
        json.key("field").value(ioListField.getFieldid());
        json.key("label").value(ioListField.getLabel());
        json.key("type").value(ioListField.getType());
        if(ioListField.getLookup().size() > 0)
        {
          json.key("lookup").array();
          for(Map.Entry<String, String> lookupEntry: ioListField.getLookup().entrySet())
            json.object().key(lookupEntry.getKey()).value(lookupEntry.getValue()).endObject();
          json.endArray();
        }
        json.endObject();
      }
    }

  }

  public void getPlotLegend(ServletOutputStream outputStream) throws IOException
  {
    OutputStreamWriter writer = new OutputStreamWriter(outputStream, "utf-8");
    JSONWriter json = new JSONWriter(writer);
    json.object();

    json.key("selections").array();
    getListDefinitionJson(json, LIST_SURVEYS, FIELD_PLOT_SELECTION);
    getListDefinitionJson(json, LIST_MATTERMARK, FIELD_PLOT_SELECTION);
    getListDefinitionJson(json, LIST_PROJECTS, FIELD_PLOT_SELECTION);
    json.endArray();

    json.key("KPI").array();
    addKPI(json, "INNOVATION", "Innovation");
    addKPI(json, "MARKET", "Market focus");
    addKPI(json, "FEASIBILITY", "Feasibility");
    addKPI(json, "MARKET_NEEDS", "Market Needs");
    addKPI(json, "MATTERMARK_GROWTH", "Mattermark growth");
    json.endArray();

    json.endObject();
    writer.flush();
    writer.close();
  }

  private void addKPI(JSONWriter json, String key, String desc)
  {
    json.object();
    json.key("field").value(key);
    json.key("label").value(desc);
    json.endObject();
  }

}
