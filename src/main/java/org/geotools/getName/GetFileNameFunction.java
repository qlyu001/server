package org.geotools.getName;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.geotools.filter.text.cql2.CQLException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class GetFileNameFunction {
    public  String [] getFileName(String path)
    {
        File file = new File(path);
        String [] fileName = file.list();
        return fileName;
    }
    public  void getAllFileName(String path,ArrayList<String> fileName)
    {
        File file = new File(path);
        File [] files = file.listFiles();
        String [] names = file.list();
        if(names != null)
        fileName.addAll(Arrays.asList(names));
        for(File a:files)
        {
            if(a.isDirectory())
            {
                getAllFileName(a.getAbsolutePath(),fileName);
            }
        }
    }
    
    public void getAndWriteName(String txtpath,String foldpath,boolean type) throws IOException, CQLException{//bool false is vector, true is raster
		JSONArray vectorlist = new JSONArray();
		
		
        File vectortxt=new File(txtpath);
        if(!vectortxt.exists()){
        	vectortxt.createNewFile();
        }
        
        String [] vectorFileName = getFileName(foldpath);
        if(type==false){
        	ReadShpFile readShpFile=new ReadShpFile();
        	
	        for(String name:vectorFileName){	
	        	if(name.contains(".shp")){
	        		readShpFile.readshape(foldpath+"/"+name, name);
					JSONObject obj = new JSONObject();//if you don't create a new obj, the name will be same value         		
	        		obj.put("list", name);
	        		vectorlist.add(obj);
	
	        		
	        		
	        	}
	        }
        }
        else {
            for(String name:vectorFileName){	
	        	
					JSONObject obj = new JSONObject();//if you don't create a new obj, the name will be same value         		
	        		obj.put("list", name);
	        		vectorlist.add(obj);

	        }
		}
   
    	try (FileWriter file = new FileWriter(txtpath)) {
			file.write(vectorlist.toJSONString());

		}
    }
}
