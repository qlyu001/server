package org.geotools.getName;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.geotools.data.DataStore;
import org.geotools.data.FeatureSource;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.filter.text.cql2.CQLException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;

public class ReadShpFile {
	public void  readshape(String path ,String name) throws IOException, CQLException {
	//public static void main(String[]args) throws IOException{
	JSONArray vectorlist = new JSONArray();
	
	String txtpath="/home/su/Desktop/withothertime/geoweb/src/main/webapp/placelist/"+name+".txt";
	File file = new File(path);
    DataStore dataStore = FileDataStoreFinder.getDataStore(file);
    String typeName = dataStore.getTypeNames()[0];

    FeatureSource<SimpleFeatureType, SimpleFeature> source = dataStore.getFeatureSource(typeName);
    Filter filter = Filter.INCLUDE; // ECQL.toFilter("BBOX(THE_GEOM, 10,20,30,40)")

    FeatureCollection<SimpleFeatureType, SimpleFeature> collection = source.getFeatures(filter);
    
    try (FeatureIterator<SimpleFeature> features = collection.features()) {
        while (features.hasNext()) {
            SimpleFeature feature = features.next();
            System.out.print(feature.getID());
            System.out.print(": ");
        
        
            //CNTRY_NAME:
            Pattern p = Pattern.compile("[:][A-Za-z]*[_]?[A-Za-z]*[,]");//
            Matcher m = p.matcher(feature.getFeatureType().toString());
            int i = 0;
           
            String resultKey = null;
            while (m.find()) {
            	resultKey=m.group();
            	
            	resultKey=resultKey.substring(resultKey.lastIndexOf(":")+1, resultKey.lastIndexOf(","));
            
            	if(resultKey.contentEquals("nom")||resultKey.contentEquals("NAME")||resultKey.contentEquals("name")||resultKey.contentEquals("CNTRY_NAME")){// 
              	JSONObject obj = new JSONObject();//if you don't create a new obj, the name will be same value
              	System.out.println(feature.getID()+":"+feature.getAttribute(resultKey));
        		obj.put(resultKey, feature.getAttribute(resultKey));
        		vectorlist.add(obj);
            	}
            	//resultKey[i]=m.group();
            	//	System.out.println(resultKey[i]);
            		/*
            		Pattern p2 = Pattern.compile("[A-Za-z]*[_]?[A-Za-z]*");
                    Matcher m2 = p2.matcher(m.group().toString());
                    while (m2.find()){
                    	resultKey[j]=m2.group();
                    	System.out.println(resultKey[j]);
                    	
                    	JSONObject obj = new JSONObject();//if you don't create a new obj, the name will be same value
                        
                		obj.put(resultKey[j], feature.getAttribute(2));
                		vectorlist.add(obj);
                		j++;
                		System.out.println(feature.getAttribute(2));
                    }
                */
            	
            	
            	
            	i++;
            }
            
    
        }
    }
 	try (FileWriter file2 = new FileWriter(txtpath)) {
		file2.write(vectorlist.toJSONString());

	}
	}
	
}
