package org.geotools.function;
public class RVConversionAdaptToAll {
	
	
	public static double vectortoraster_x( double number,double oringinCoord,double proportion ) {
		//Get projected rectangle in raster data. Enlarge the rectangle to integers.
		return (number-oringinCoord)/proportion;
	}

	public static double vectortoraster_y(double number,double oringinCoord,double proportion) {
		return (number -oringinCoord)  / proportion;
	}
	//Projection of raster to vector
	public static double rastertovector_x(double number,double oringinCoord,double proportion) {
			return  (number * proportion) + oringinCoord;
	}
	
	public static double rastertovector_y(double number,double oringinCoord,double proportion) {
			return  (number * proportion) + oringinCoord;
	}
}
