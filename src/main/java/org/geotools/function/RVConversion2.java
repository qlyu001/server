package org.geotools.function;

public class RVConversion2 {
	//Projection of vector to raster
	public static double vectortoraster_x( double number) {
		//Get projected rectangle in raster data. Enlarge the rectangle to integers.
		double width = GetRasterSize.rastersize[1];
		//System.out.println(width);
		return (number + 180) * width / 360;
	}

	public static double vectortoraster_y(double number) {
		double height = GetRasterSize.rastersize[0];
		return (90 + number) * height / 180;
	}
	//Projection of raster to vector
	public static double rastertovector_x(double number) {
		double width = GetRasterSize.rastersize[1];
		return  (number * 360.0 / width) - 180;
	}
	
	public static double rastertovector_y(double number) {
		double height = GetRasterSize.rastersize[0];
		return  ((number * 180.0 / height) - 90);
	}
	
}
