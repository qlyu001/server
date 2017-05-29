package org.geotools.function;

public class RVConversion {
	//Projection of vector to raster
	public static double vectortoraster_x( double number) {
		//Get projected rectangle in raster data. Enlarge the rectangle to integers.
		return (number-199185)/30;
	}

	public static double vectortoraster_y(double number) {
		return (number -4820685)  / 30;
	}
	//Projection of raster to vector
	public static double rastertovector_x(double number) {
			return  (number * 30) + 199185;
	}
	
	public static double rastertovector_y(double number) {
			return  (number * 30) + 4820685;
	}
	
}
