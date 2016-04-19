package si.ijs.ailab.fiimpact.project;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
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
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by flavio on 13/07/2015.
 */

public class ProjectData
{

	private final static Logger logger = LogManager.getLogger(ProjectData.class.getName());

	private String id;
	private Map<String, String> fields = new TreeMap<>();
	private Map<String, String> mattermarkFields = new TreeMap<>();

	private void write(OutputStream os) throws IOException
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
		Element root = doc.createElement("project");
		doc.appendChild(root);
		root.setAttribute("id", id);

    Element idc = doc.createElement("idc");
    root.appendChild(idc);

		for (Map.Entry<String, String> fe : fields.entrySet())
		{
			Element f = doc.createElement("field");
			idc.appendChild(f);
			f.setAttribute("id", fe.getKey());
			f.setAttribute("val", fe.getValue());
		}

		Element mattermark = doc.createElement("mattermark");
		root.appendChild(mattermark);

		for (Map.Entry<String, String> fe : mattermarkFields.entrySet())
		{
			Element f = doc.createElement("field");
			mattermark.appendChild(f);
			f.setAttribute("id", fe.getKey());
			f.setAttribute("val", fe.getValue());
		}

		AIUtils.save(doc, os);

		logger.info("Saved project data for {} with {} fields", id, fields.size());
	}

	public void writeUI(OutputStream os) throws IOException
	{
		JSONObject jsonProject = new JSONObject();
		jsonProject.put("id", id);
		SimpleDateFormat format = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);

		Date curDate = new Date();
		jsonProject.put("timestamp_report_display", format.format(curDate));
		JSONObject jsonFields = new JSONObject();
		jsonProject.put("fields", jsonFields);
		for (Map.Entry<String, String> fe : fields.entrySet())
		{
			JSONObject jsonField = new JSONObject();
			jsonField.put("val", fe.getValue());
			jsonFields.put(fe.getKey(), jsonField);
		}

		for (Map.Entry<String, String> fe : mattermarkFields.entrySet())
		{
			JSONObject jsonField = new JSONObject();
			jsonField.put("val", fe.getValue());
			jsonFields.put(fe.getKey(), jsonField);
		}
		OutputStreamWriter w = new OutputStreamWriter(os, "utf-8");
		jsonProject.write(w);
		w.flush();
		w.close();
		logger.info("Saved project {} with {} fields", id, fields.size());
	}

	public void save(Path root)
	{
		Path p = root.resolve("project-" + id + ".xml");
		try
		{
			write(new FileOutputStream(p.toFile()));
		} catch (IOException e)
		{
			logger.error("Cannot save project {}", p.toString());
		}
	}

	public Map<String, String> getMattermarkFields()
	{
		return mattermarkFields;
	}

	public void read(InputStream is) throws ParserConfigurationException, IOException, SAXException
	{
		fields.clear();
		mattermarkFields.clear();

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(is);
		id = doc.getDocumentElement().getAttribute("id");

    NodeList nl = doc.getElementsByTagName("idc");
    if(nl.getLength() == 1)
    {
      nl = ((Element) nl.item(0)).getElementsByTagName("field");
      for(int i = 0; i < nl.getLength(); i++)
      {
        Element e = (Element) nl.item(i);
        fields.put(e.getAttribute("id"), e.getAttribute("val"));

      }
    }

    nl = doc.getElementsByTagName("mattermark");
    if(nl.getLength() == 1)
    {
      nl = ((Element) nl.item(0)).getElementsByTagName("field");
      for(int i = 0; i < nl.getLength(); i++)
      {
        Element e = (Element) nl.item(i);
        mattermarkFields.put(e.getAttribute("id"), e.getAttribute("val"));

      }
    }

    logger.info("Loaded project {} with {} project fields and {} Mattermark fields.", id, mattermarkFields.size());
	}

	public void addFields(String[] arrFields)
	{
		fields.clear();
		for (String s : arrFields)
		{
			String[] arr = s.split(";");
			if (arr.length == 1)
				logger.error("Empty answer for: {}. Ignore.", arr[0]);
			else
				fields.put(arr[0], arr[1]);
		}

	}

	public void addFields(ArrayList<IOListField> fieldDefinitions, ArrayList<String> fieldValues)
	{
		fields.clear();
		for (int i = 0; i < fieldValues.size(); i++)
		{
			fields.put(fieldDefinitions.get(i).getFieldid(), fieldValues.get(i));
		}

	}

  public void addFieldsMattermark(ArrayList<IOListField> fieldDefinitions, ArrayList<String> fieldValues)
  {
    mattermarkFields.clear();
    for (int i = 0; i < fieldValues.size(); i++)
    {
      mattermarkFields.put(fieldDefinitions.get(i).getFieldid(), fieldValues.get(i));
    }

  }

	public void clearAll()
	{
		fields.clear();
		mattermarkFields.clear();
	}

  public void clearMattermark()
  {
    mattermarkFields.clear();
  }

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}


  public String getValue(String fieldid)
  {
    return fields.get(fieldid);
  }

  public boolean isMattermarkValueSet(String fieldid)
  {
    return mattermarkFields.get(fieldid) != null && !mattermarkFields.get(fieldid).equals("");
  }

  public String getMattermarkValue(String fieldid)
  {
    return mattermarkFields.get(fieldid);
  }

  public int getMattermarkIntValue(String fieldid)
  {
    return AIUtils.parseInteger(mattermarkFields.get(fieldid), 0);
  }

  public static void main(String[] args)
  {
  }

}
