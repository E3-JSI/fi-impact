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

public class ProjectData {

	final static Logger logger = LogManager.getLogger(ProjectData.class.getName());

	private String id;
	public Map<String, String> fields = new TreeMap<>();

	public Map<String, String> mattermarkFields = new TreeMap<>();

	private void w(OutputStream os, Map<String, String> tempFields) throws IOException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = null;
		try {
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			logger.error("Can't believe this", e);
		}
		Document doc = db.newDocument();
		Element root = doc.createElement("project");
		doc.appendChild(root);
		root.setAttribute("id", id);

		for (Map.Entry<String, String> fe : tempFields.entrySet()) {
			Element f = doc.createElement("field");
			root.appendChild(f);
			f.setAttribute("id", fe.getKey());
			f.setAttribute("val", fe.getValue());
		}
		AIUtils.save(doc, os);

		logger.info("Saved project data for {} with {} fields", id, tempFields.size());
	}

	public void write(OutputStream os) throws IOException {
		w(os, fields);
	}

	public void writeMatermark(OutputStream os) throws IOException {
		w(os, mattermarkFields);
	}

	private void wUI(OutputStream os, Map<String, String> tempFields) throws IOException {
		JSONObject jsonProject = new JSONObject();
		jsonProject.put("id", id);
		SimpleDateFormat format = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);

		Date curDate = new Date();
		jsonProject.put("timestamp_report_display", format.format(curDate));
		JSONObject jsonFields = new JSONObject();
		jsonProject.put("fields", jsonFields);
		for (Map.Entry<String, String> fe : tempFields.entrySet()) {
			JSONObject jsonField = new JSONObject();
			jsonField.put("val", fe.getValue());
			jsonFields.put(fe.getKey(), jsonField);
		}

		OutputStreamWriter w = new OutputStreamWriter(os, "utf-8");
		jsonProject.write(w);
		w.flush();
		w.close();
		logger.info("Saved project {} with {} fields", id, tempFields.size());
	}

	public void writeUI(OutputStream os) throws IOException {

		wUI(os, fields);
	}

	public void writeUIMatermark(OutputStream os) throws IOException {

		wUI(os, mattermarkFields);
	}

	public void save(Path root) {
		Path p = root.resolve("project-" + id + ".xml");
		try {
			write(new FileOutputStream(p.toFile()));
		} catch (IOException e) {
			logger.error("Cannot save project {}", p.toString());
		}
	}

	public Map<String, String> r(InputStream is, Map<String, String> tempFields)
			throws ParserConfigurationException, IOException, SAXException {
		tempFields.clear();

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(is);
		id = doc.getDocumentElement().getAttribute("id");

		NodeList nl = doc.getElementsByTagName("field");

		for (int i = 0; i < nl.getLength(); i++) {
			Element e = (Element) nl.item(i);
			tempFields.put(e.getAttribute("id"), e.getAttribute("val"));

		}

		logger.info("Loaded project {} with {} fields.", id, tempFields.size());
		return tempFields;
	}

	public void read(InputStream is) throws ParserConfigurationException, IOException, SAXException {
		fields = r(is, fields);
	}

	public void readMattermark(InputStream is) throws ParserConfigurationException, IOException, SAXException {
		mattermarkFields = r(is, mattermarkFields);
	}

	public Map<String, String> addF(String[] arrFields, Map<String, String> tempFields) {
		tempFields.clear();
		for (String s : arrFields) {
			String[] arr = s.split(";");
			if (arr.length == 1)
				logger.error("Empty answer for: {}. Ignore.", arr[0]);
			else
				tempFields.put(arr[0], arr[1]);
		}
		return tempFields;
	}

	public void addFields(String[] arrFields) {
		fields = addF(arrFields, fields);

	}

	public void addFieldsMattermark(String[] arrFields) {
		mattermarkFields = addF(arrFields, mattermarkFields);

	}

	public Map<String, String> addF(String[] headerArr, String[] lineArr, Map<String, String> tempFields) {
		tempFields.clear();
		for (int i = 0; i < headerArr.length; i++) {
			String fieldID = headerArr[i];
			String fieldVal = lineArr[i];
			tempFields.put(fieldID, fieldVal);
		}
		return tempFields;
	}

	public void addFields(String[] headerArr, String[] lineArr) {
		fields = addF(headerArr, lineArr, fields);

	}

	public void addFieldsMattermark(String[] headerArr, String[] lineArr) {
		mattermarkFields = addF(headerArr, lineArr, mattermarkFields);

	}

	public void clear() {
		fields.clear();
	}

	public void clearMattermark() {
		mattermarkFields.clear();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public static void main(String[] args) {
	}

}
