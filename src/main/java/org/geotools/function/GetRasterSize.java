package org.geotools.function;
import java.awt.image.Raster;
import java.io.IOException;

//Read size of raster file 
public class GetRasterSize {
	public static int [] rastersize = new int [2] ;
	public static int[] getrastersize(String imagefile) throws IOException {
		
		Raster raster = Readimage.readimage(imagefile);
		int height = raster.getHeight();
		int width = raster.getWidth(); 	
		int minx = raster.getMinX();
		int miny = raster.getMinY();
//		System.out.println("The size of raster data is " + height + " " + width + " " + minx + " " + miny);
//		raster.getPixel(points.elementAt(index).X, points.elementAt(index).Y, iArray);
		rastersize [0]= height;
		rastersize [1] = width;  
		return rastersize;
	}
}
