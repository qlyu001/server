package edu.ucr.cs.server;

import java.io.IOException;

import org.geotools.filter.text.cql2.CQLException;
import org.geotools.getName.GetFileName;

import com.vividsolutions.jts.geom.Geometry;

import edu.ucr.cs.horus.VectorManager;

public class callhorus {
	public static void main(String[] args) throws CQLException, IOException{
		 VectorManager vectorManager = new VectorManager(GetFileName.vectorfoldpath+"/countries.shp");
	     Geometry[] geoms =  vectorManager.getAllGeom();
	
	}
		

}





