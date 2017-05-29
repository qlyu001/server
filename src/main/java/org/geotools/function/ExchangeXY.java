package org.geotools.function;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;


public class ExchangeXY {
	public  Geometry exchangexy(Geometry polygon) {
		GeometryFactory geomFactory = new GeometryFactory();
		
		
		Coordinate[] coords = polygon.getCoordinates();
		double temp;
		for(int i=0;i<coords.length;i++){
			temp=coords[i].y;
			coords[i].y=coords[i].x;
			coords[i].x=temp;
		}
	
		Geometry new_polygon=geomFactory.createPolygon(coords);
		return new_polygon;
}
}