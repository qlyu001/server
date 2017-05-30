package edu.ucr.cs.server;

import java.io.IOException;
import java.util.Vector;

import org.geotools.filter.text.cql2.CQLException;
import org.geotools.function.Decompose;
import org.geotools.function.Getgeom;
import org.geotools.getName.GetFileName;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;



public class geo {
	public static void main(String[] args) throws CQLException, IOException{
		Geometry geometry=Getgeom.getgeom("China", GetFileName.vectorfoldpath+"/countries.shp", "CNTRY_NAME");
		Coordinate[] coorGGG=geometry.getCoordinates();
		System.out.println(geometry.toText());
		Vector<Geometry> polygons=Decompose.get_decompose_array("China", GetFileName.vectorfoldpath+"/countries.shp", "CNTRY_NAME");
		Polygon polygon2=(Polygon) polygons.get(0);
		polygons.size();
//		Geometry polygon2
		Coordinate[] coor=polygon2.getCoordinates();
		for (int i=0;i<coor.length;i++){
			System.out.println(coor[i].x+"/"+coor[i].y);
		}
		
	}
}