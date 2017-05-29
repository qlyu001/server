package org.geotools.function;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridCoverage2DReader;
import org.geotools.coverage.grid.io.GridFormatFinder;

public class GetCRSCodeAndOringinCoord {//return  coordinate of rater file and crs code .
	public double[] getCRSCodeAndOringinCoord(String rasterPath) throws IOException  {
        File file=new File(rasterPath);
 
        
        AbstractGridFormat format = GridFormatFinder.findFormat(file);
        GridCoverage2DReader reader = format.getReader( file );
       // System.out.println(reader.getFormat());
        String crsCode=reader.getCoordinateReferenceSystem().toString();
        ByteArrayInputStream is=new ByteArrayInputStream(crsCode.getBytes());
        BufferedReader br=new BufferedReader(new InputStreamReader(is));
        String resultCrs = null;
       // System.out.println(crsCode);
        //if you care the time ,you can try other method like RandomAccessFile.In this method, it will read the string two times.
        int countCrsLine=0;
        while(br.readLine()!=null){
        	countCrsLine++;
        }
        ByteArrayInputStream is2=new ByteArrayInputStream(crsCode.getBytes());
        BufferedReader br2=new BufferedReader(new InputStreamReader(is2));
        for(int i=0;i<countCrsLine;i++){
        	
        	resultCrs=br2.readLine();
        	
        }

        resultCrs=resultCrs.substring(resultCrs.indexOf(",\"")+2,resultCrs.length()-"\"]]".length());
        
      	
        Pattern p = Pattern.compile("[-]?\\d{1,}[.]\\d{0,}");//match number like xxx.xxxxx
        String origincoord =reader.getOriginalEnvelope().toString();
        Matcher m = p.matcher(origincoord);
        int i = 0;
        String[] originCoordinate={"","","","",""};
        while (m.find()) {
        	originCoordinate[i]=m.group();
            i++;
        }
        originCoordinate[4]=resultCrs;
        double[] originCoord = new double[5];
        for(int j=0;j<originCoordinate.length;j++){
        	originCoord[j]=Double.parseDouble(originCoordinate[j]);
        	//System.out.println(originCoord[j]);
        }
    
        return originCoord;
      //	String origincoordX1=origincoord.substring(origincoord.indexOf("[(")+2,origincoord.length()-",".length());
        //System.out.println(originCoordinate[1]);
	}
}
