package si.ijs.ailab.fiimpact.tools;

import com.opencsv.CSVReader;



import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by flavio on 15/05/2016.
 */
public class CheckConsistency
{

  static final int idIndexGlobal = 1;
  static final int nameIndexGlobal = 5;//project name: 6;

  static final int idIndexSFC = 78;
  static final int urlIndexSFC = 5;

  static final int urlIndexMattermark = 5;

  static final int idIndexJSI = 28;
  static final int nameIndexJSI = 6;//Project name: 7;

  static final int idIndexA16 = 0;
  static final int nameIndexA16 = 9;//Project name: 5;

  public static void main(String[] args) throws IOException
  {
    //detectMissing();
    checkNames();
  }

  static Set<String> removeNames = new TreeSet<>();
  {
    removeNames.add("Self-employed".toLowerCase());
    removeNames.add("Group of professionals".toLowerCase());
    removeNames.add("Startup".toLowerCase());
    removeNames.add("Entrepreneur".toLowerCase());
    removeNames.add("-Group of Individuals-".toLowerCase());
    removeNames.add("Group of Individuals".toLowerCase());
    removeNames.add("Group of Entrepreneurs".toLowerCase());
    removeNames.add("0".toLowerCase());

  }
  public static String cleanName(String s)
  {
    if(s==null)
      return "";
    s = s.trim();
    s= s.toLowerCase();
    if(removeNames.contains(s))
      return "";
    int index = s.indexOf(' ');
    if(index > 1)
    {
        s = s.substring(0, index);
    }

    index = s.indexOf('.');
    if(index > 1)
    {
      s = s.substring(0, index);
    }

    return s;
  }
  public static void checkNames() throws IOException
  {
    Path root = new File("E:\\Dropbox\\JSI_projects_current\\FI-IMPACT\\Assesment Tool Imports\\20160515").toPath();
    Path pIDCGlobal = root.resolve("02_05_16 IDC Global Database v28.csv");
    Path pOglivy = root.resolve("A16.csv");
    Path pSurveys = root.resolve("fi-impact-export.txt");

    //projects
    CSVReader reader = new CSVReader(new FileReader(pIDCGlobal.toFile()), ';', '"');
    List<String[]> csvLines = reader.readAll();
    Map<String, String> mapIDC = new TreeMap<>();
    for(int i = 3; i < csvLines.size(); i++)
    {
      mapIDC.put(csvLines.get(i)[idIndexGlobal], csvLines.get(i)[nameIndexGlobal]);
    }


    //surveys
    reader = new CSVReader(new FileReader(pSurveys.toFile()), '\t', '"');
    csvLines = reader.readAll();
    Map<String, String> mapJSI = new TreeMap<>();
    for(int i = 1; i < csvLines.size(); i++)
    {
      mapJSI.put(csvLines.get(i)[idIndexJSI], csvLines.get(i)[nameIndexJSI]);
    }

    //Oglivy
    reader = new CSVReader(new FileReader(pOglivy.toFile()), ';', '"');
    csvLines = reader.readAll();
    Map<String, String> mapOglivy = new TreeMap<>();
    for(int i = 1; i < csvLines.size(); i++)
    {
      if(!csvLines.get(i)[idIndexA16].equals(""))
      {
        mapOglivy.put(csvLines.get(i)[idIndexA16], csvLines.get(i)[nameIndexA16]);
      }
    }


    //Start checkinh
    System.out.println("Identifier\tJSI\tIDC\tSME_INFO_A16");
    StringBuilder sb = new StringBuilder();
    System.out.println(sb.toString());
    sb.setLength(0);
    //System.out.println("***IDC*****************");
    for(Map.Entry<String, String> idcEntry: mapIDC.entrySet())
    {
      String id = idcEntry.getKey();
      String nameRaw = idcEntry.getValue();
      String nameJSIRaw = mapJSI.get(id);
      String nameA16Raw = mapOglivy.get(id);

      String name = cleanName(idcEntry.getValue());
      String nameJSI = cleanName(mapJSI.get(id));
      String nameA16 = cleanName(mapOglivy.get(id));
      boolean mismatch = (!nameJSI.equals("") && !nameJSI.equals(name)) || (!nameA16.equals("") && !nameA16.equals(name));
      mapJSI.remove(id);
      mapOglivy.remove(id);
      if(mismatch)
      {
        sb.append(id).append("\t").append(nameJSIRaw).append("\t").append(nameRaw).append("\t").append(nameA16Raw);
        System.out.println(sb.toString());
        sb.setLength(0);
      }
    }
    for(Map.Entry<String, String> jsiEntry: mapJSI.entrySet())
    {
      String id = jsiEntry.getKey();
      String nameRaw = "";
      String nameJSIRaw = mapJSI.get(id);
      String nameA16Raw = mapOglivy.get(id);

      String name = "";
      String nameJSI = cleanName(mapJSI.get(id));
      String nameA16 = cleanName(mapOglivy.get(id));

      boolean mismatch = (!nameA16.equals("") && !nameA16.equals(nameJSI));
      if(mismatch)
      {
        sb.append(id).append("\t").append(nameJSIRaw).append("\t").append(nameRaw).append("\t").append(nameA16Raw);
        System.out.println(sb.toString());
        sb.setLength(0);
      }
    }

  }
  public static void detectMissing() throws IOException
  {
    Path root = new File("E:\\Dropbox\\JSI_projects_current\\FI-IMPACT\\Assesment Tool Imports\\20160515").toPath();
    Path pIDCGlobal = root.resolve("02_05_16 IDC Global Database v28.csv");
    Path pSFC = root.resolve("Mapping.csv");
    Path pOglivy = root.resolve("A16.csv");
    Path pSurveys = root.resolve("fi-impact-export.txt");
    Path pMattermark = root.resolve("MattermarkExport05-15-2016.csv");


    //projects
    CSVReader reader = new CSVReader(new FileReader(pIDCGlobal.toFile()), ';', '"');
    List<String[]> csvLines = reader.readAll();
    Map<String, String> mapIDC = new TreeMap<>();
    for(int i = 3; i < csvLines.size(); i++)
    {
      mapIDC.put(csvLines.get(i)[idIndexGlobal], csvLines.get(i)[nameIndexGlobal]);
    }

    //mappings (SFC)
    reader = new CSVReader(new FileReader(pSFC.toFile()), ';', '"');
    csvLines = reader.readAll();
    Map<String, String> idUrlMap = new TreeMap<>();
    Map<String, String> urlIdMap = new TreeMap<>();
    for(int i = 1; i < csvLines.size(); i++)
    {
      if(!(csvLines.get(i)[idIndexSFC]==null || csvLines.get(i)[urlIndexSFC]==null || csvLines.get(i)[idIndexSFC].isEmpty() || csvLines.get(i)[urlIndexSFC].isEmpty()))
      {
        idUrlMap.put(csvLines.get(i)[idIndexSFC], csvLines.get(i)[urlIndexSFC]);
        urlIdMap.put(csvLines.get(i)[urlIndexSFC], csvLines.get(i)[idIndexSFC]);
      }
      else
      {
        boolean error = true;
      }
    }

    //surveys
    reader = new CSVReader(new FileReader(pSurveys.toFile()), '\t', '"');
    csvLines = reader.readAll();
    Map<String, String> mapJSI = new TreeMap<>();
    for(int i = 1; i < csvLines.size(); i++)
    {
      mapJSI.put(csvLines.get(i)[idIndexJSI], csvLines.get(i)[nameIndexJSI]);
    }

    //Oglivy
    reader = new CSVReader(new FileReader(pOglivy.toFile()), ';', '"');
    csvLines = reader.readAll();
    Map<String, String> mapOglivy = new TreeMap<>();
    for(int i = 1; i < csvLines.size(); i++)
    {
      if(!csvLines.get(i)[idIndexA16].equals(""))
      {
        mapOglivy.put(csvLines.get(i)[idIndexA16], csvLines.get(i)[nameIndexA16]);
      }
    }

    //Mattermark
    reader = new CSVReader(new FileReader(pMattermark.toFile()), ',', '"');
    csvLines = reader.readAll();
    TreeSet<String> setMattermark = new TreeSet<>();
    for(int i = 1; i < csvLines.size(); i++)
    {
      setMattermark.add(csvLines.get(i)[urlIndexMattermark]);
    }

    //Start checkinh
    System.out.println("Identifier\tURL\tJSI\tIDC\tSME_INFO_A16\tSME_INFO_SFC\tMattermark");
    StringBuilder sb = new StringBuilder();
    sb.append("totals\t\t").append(mapJSI.size()).append("\t").append(mapIDC.size()).append("\t").append(mapOglivy.size()).append("\t").append(urlIdMap.size()).append("\t").append(setMattermark.size());
    System.out.println(sb.toString());
    sb.setLength(0);
    //System.out.println("***IDC*****************");
    for(String idcID: mapIDC.keySet())
    {
      String url = idUrlMap.get(idcID);
      boolean existsIDC = true;
      boolean existsJSI = mapJSI.containsKey(idcID);
      boolean existsOglivy = mapOglivy.containsKey(idcID);
      boolean existsSFC = url!=null && !url.equals("");
      boolean existsMattermark = existsSFC && setMattermark.contains(url);
      if(!existsSFC || !existsMattermark || !existsOglivy)
      {
        sb.append(idcID).append("\t").append(url).append("\t").append(existsJSI).append("\t").append(existsIDC).append("\t").append(existsOglivy).append("\t").append(existsSFC).append("\t").append(existsMattermark);
        System.out.println(sb.toString());
        sb.setLength(0);
      }
    }
    //System.out.println("***MAP*****************");

    for(Map.Entry<String, String> idUrl: idUrlMap.entrySet())
    {
      String id = idUrl.getKey();
      String url = idUrl.getValue();
      boolean existsIDC = mapIDC.containsKey(id);
      boolean existsSFC = true;
      boolean existsOglivy = mapOglivy.containsKey(id);
      if(!existsIDC)
      {
        boolean existsJSI = mapJSI.containsKey(id);
        boolean existsMattermark = existsSFC && setMattermark.contains(url);
        if(!existsIDC || !existsMattermark || !existsOglivy)
        {
          sb.append(id).append("\t").append(url).append("\t").append(existsJSI).append("\t").append(existsIDC).append("\t").append(existsOglivy).append("\t").append(existsSFC).append("\t").append(existsMattermark);
          System.out.println(sb.toString());
          sb.setLength(0);
        }
      }
    }

    //System.out.println("***JSI*****************");
    for(String id: mapJSI.keySet())
    {
      String url = idUrlMap.get(id);
      boolean existsJSI = true;
      boolean existsIDC = mapIDC.containsKey(id);
      boolean existsSFC = url!=null && !url.equals("");
      boolean existsMattermark = existsSFC && setMattermark.contains(url);
      boolean existsOglivy = mapOglivy.containsKey(id);
      if(!existsIDC && !existsSFC)
      {
        if(!existsIDC || !existsSFC|| !existsMattermark || !existsOglivy)
        {
          sb.append(id).append("\t").append(url).append("\t").append(existsJSI).append("\t").append(existsIDC).append("\t").append(existsOglivy).append("\t").append(existsSFC).append("\t").append(existsMattermark);
          System.out.println(sb.toString());
          sb.setLength(0);
        }
      }
    }

    //System.out.println("***Mattermark*****************");
    for(String url: setMattermark)
    {
      String id = urlIdMap.get(url);

      boolean existsJSI = id!=null && mapJSI.containsKey(id);
      boolean existsIDC = id!=null && mapIDC.containsKey(id);
      boolean existsSFC = url!=null && !url.equals("");
      boolean existsMattermark = true;
      boolean existsOglivy = id!=null && mapOglivy.containsKey(id);
      if(!existsIDC && !existsSFC )
      {
        if(!existsIDC || !existsSFC|| !existsOglivy)
        {
          sb.append(id).append("\t").append(url).append("\t").append(existsJSI).append("\t").append(existsIDC).append("\t").append(existsOglivy).append("\t").append(existsSFC).append("\t").append(existsMattermark);
          System.out.println(sb.toString());
          sb.setLength(0);
        }
      }
    }
    //System.out.println("***Oglivy*****************");
    for(String id: mapOglivy.keySet())
    {
      String url = idUrlMap.get(id);
      boolean existsJSI = mapJSI.containsKey(id);
      boolean existsIDC = mapIDC.containsKey(id);
      boolean existsSFC = url!=null && !url.equals("");
      boolean existsMattermark = existsSFC && setMattermark.contains(url);
      boolean existsOglivy = true;
      if(!existsIDC && !existsSFC && !existsMattermark)
      {
        if(!existsIDC || !existsSFC || !existsMattermark)
        {
          sb.append(id).append("\t").append(url).append("\t").append(existsJSI).append("\t").append(existsIDC).append("\t").append(existsOglivy).append("\t").append(existsSFC).append("\t").append(existsMattermark);
          System.out.println(sb.toString());
          sb.setLength(0);
        }
      }
    }
    System.out.println("********************");

  }
}