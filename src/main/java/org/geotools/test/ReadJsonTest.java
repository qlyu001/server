package org.geotools.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONValue;


public class ReadJsonTest {
	
		  
		 public static void main(String[]args) {
		  String listPath = "C:/Users/29563/OneDrive/文档/geoweb/src/main/webapp/placelist/countries.shp.txt";
	      
		   File file = new File(listPath);
		      Scanner scanner = null;
		      StringBuilder buffer = new StringBuilder();
		      try {
		          scanner = new Scanner(file, "utf-8");
		          while (scanner.hasNextLine()) {
		              buffer.append(scanner.nextLine());
		          }

		      } catch (FileNotFoundException e) {
		          // TODO Auto-generated catch block  

		      } finally {
		          if (scanner != null) {
		              scanner.close();
		          }
		      }
		      Object obj=JSONValue.parse(buffer.toString());
		      JSONArray jsonArray=(JSONArray)obj;
		    
		      String fromJson=jsonArray.get(1).toString();
		      fromJson= fromJson.substring(fromJson.indexOf("\"")+1, fromJson.indexOf(":")-1);
		       
		      System.out.println(fromJson);
	     
		  }
	

}
