package si.ijs.ailab.fiimpact.project;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import org.json.JSONWriter;

public class test {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub



		BufferedReader brData = new BufferedReader(new InputStreamReader(new FileInputStream("C:\\Users\\Matej\\Desktop\\Tomcat7\\webapps\\fi-impact\\WEB-INF\\import\\project-list.csv"), "utf-8"));
		String line = brData.readLine();
		String[] headerArr = line.split(",");
		line = brData.readLine();
		int lineCnt = 1;

		//OutputStreamWriter w = new OutputStreamWriter(outputStream, "utf-8");
	//	JSONWriter json = new JSONWriter(w);
		//json.object().key("total_before").value(projects.size());
		int numSkipProj=0;
		int lengthProj=0;

		int countLine=0;
		while (line != null) {
			countLine++;		
			String[] lineArr = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
			//for(String s:lineArr)
		//		System.out.print(s+", ");	
		//	System.out.println("");
			line=brData.readLine();
		}
		System.out.print(countLine);
		
	}

}
