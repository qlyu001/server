package org.geotools.getName;

import java.io.IOException;

import org.geotools.filter.text.cql2.CQLException;
import org.geotools.getName.GetFileNameFunction;
import org.omg.CORBA.PUBLIC_MEMBER;
public class GetFileName
{	
	static String dirPath="/home/qlyu001/workspace/server/";
	public static String vectorfilepath=dirPath+"src/main/resources/webapps/vectorfilelist.txt";
	public static String vectorfoldpath=dirPath+"vectorfile";	
	public static String rasterfilepath=dirPath+"src/main/resources/webapps/rasterfilelist.txt";
	public static String rasterfoldpath=dirPath+"rasterfile";	
    public static void main(String[] args) throws IOException, CQLException
    {	
    	GetFileNameFunction getAndWrite=new GetFileNameFunction();
    	
    	getAndWrite.getAndWriteName(vectorfilepath,vectorfoldpath,false);
    	getAndWrite.getAndWriteName(rasterfilepath,rasterfoldpath,true);
    	

    }
}
