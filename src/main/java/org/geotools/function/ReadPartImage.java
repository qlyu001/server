package org.geotools.function;

import java.awt.Rectangle;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridCoverage2DReader;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.geotools.filter.text.cql2.CQLException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;


//Read image and return Raster
public class ReadPartImage {
	
	public static Raster readimage(Raster raster,String image, String countryname,String path,String placeKey,double[]rastercoordinate) throws IOException, CQLException{
		File file = new File(image);
		AbstractGridFormat format = GridFormatFinder.findFormat( file );
		GridCoverage2DReader reader = format.getReader( file );
		GridCoverage2D coverage = (GridCoverage2D) reader.read(null);
		RenderedImage imagedata = coverage.getRenderedImage();
		
		Geometry wholegeom = Getgeom.getgeom(countryname,path,placeKey);
		Envelope env = wholegeom.getEnvelopeInternal();

//		Envelope env1  = coverage.getEnvelope();
//		System.out.println(env1);
//		CoordinateReferenceSystem crs = coverage.getCoordinateReferenceSystem2D();
//		System.out.println(crs);
//		Envelope env = coverage.getEnvelope();
		double proportionx=(rastercoordinate[2]-rastercoordinate[0])/raster.getWidth();
		double proportiony=(rastercoordinate[3]-rastercoordinate[1])/raster.getHeight();
		int x_left = Rounding.getfloor(RVConversionAdaptToAll.vectortoraster_x(env.getMinX(),rastercoordinate[0],proportionx));
		int y_down = Rounding.getcelling(RVConversionAdaptToAll.vectortoraster_y(env.getMinY(),rastercoordinate[1],proportiony));
		int x_right = Rounding.getcelling(RVConversionAdaptToAll.vectortoraster_x(env.getMaxX(),rastercoordinate[0],proportionx));
		int y_up = Rounding.getfloor(RVConversionAdaptToAll.vectortoraster_y(env.getMaxY(),rastercoordinate[1],proportiony));
		
		Rectangle rect =  new Rectangle(x_left,y_up,(x_right-x_left + 1),(y_down-y_up + 1 ));
		Raster raster2 =imagedata.getData(rect);
		

//		System.out.println("x_left " + x_left + " " + y_down + " " + " " + x_right + " "+ y_up);
			

//		int height = raster.getHeight();
//		int width = raster.getWidth(); 
//		System.out.println(height + " " + width);
//		System.out.println(raster.getMinX() + " " +raster.getMinY() );
//		Raster raster =imagedata.getData();
		
/*		Test correctness of reading part raster
 * 		Envelope geomenv = Getgeom.getgeom(countryname).getEnvelopeInternal();
		Geometry geombouns = Getgeom.getgeom(countryname).getEnvelope();
		System.out.println(geomenv);
		int x_left = Rounding.getfloor(RVConversion.vectortoraster_x(geomenv.getMinX()));
		int y_up = Rounding.getfloor(RVConversion.vectortoraster_y(geomenv.getMaxY()));
		int y_down = Rounding.getcelling(RVConversion.vectortoraster_y(geomenv.getMinY()));
		int x_right = Rounding.getcelling(RVConversion.vectortoraster_x(geomenv.getMaxX()));

		 System.out.println("env.getMinX() " + geomenv.getMinX() + " " +
				 geomenv.getMinY() + " " + " " + geomenv.getMaxX() + " " + geomenv.getMaxY());
		 System.out.println("x_left " + x_left + " " + y_down + " " + " " +
		 x_right + " "+ y_up);
		
		for(Geometry polygon :polygons){
			if (!geombouns.contains(polygon.getEnvelope())){
				System.out.println("Outside!" + polygon.getEnvelope());
			};
		}
 * 
 */
		
		
		return raster2;
	}
}
