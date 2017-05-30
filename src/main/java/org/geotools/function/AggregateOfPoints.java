package org.geotools.function;

import java.awt.image.Raster;
import java.io.IOException;
import java.util.Vector;

import com.vividsolutions.jts.geom.Coordinate;

public class AggregateOfPoints {

	public static double aggregate(Raster raster, Vector<Coordinate> points) throws IOException {
		Vector<Double> valuearray = new Vector<Double>();
	
		float[] rgb = GetRGB.get_rgb_new(raster, points);
		Vector<Float> v = new Vector<Float>();
		for (int i = 0; i < rgb.length; i++) {
			//System.out.println(rgb[i]);
			v.add(rgb[i]);
		}
		valuearray.add(GetAverage.get_average_float(v));

		return GetAverage.get_average_double(valuearray);
	}
}
