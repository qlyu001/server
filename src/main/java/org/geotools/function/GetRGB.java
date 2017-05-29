package org.geotools.function;
import java.awt.image.Raster;
import java.io.IOException;
import java.util.Vector;

import com.vividsolutions.jts.geom.Coordinate;
//public static int count=1;
//Read RGB value
public class GetRGB {
	public static int[] get_rgb(Raster raster, Vector<Coordinate> points) throws IOException {
		int [] rgb = new int [points.size()];
		for (int i = 0; i < points.size(); i++) {
			raster.getPixel((int)points.elementAt(i).x, (int)points.elementAt(i).y, rgb);
//			System.out.println((int)points.elementAt(i).x);
			if(rgb[i]==0){//this count just for test in this case, rgb has different value in different case.
				
			}
		}
		return rgb;
	}
	public static float[] get_rgb_new(Raster raster,Vector<Coordinate>points)throws IOException{
		float [] temp=new float[points.size()];
		int t=0;
		for(int i=0;i<points.size();i++){
			
			temp[t]=raster.getDataBuffer().getElemFloat((int)points.elementAt(i).y*raster.getWidth()+(int)points.elementAt(i).x);
			//temp[t]=raster.getDataBuffer().getElemFloat(16353*40320-1);
			t++;
			}
		
		return temp;
	}
}
