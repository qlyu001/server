package org.geotools.function;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridCoverage2DReader;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

//Read image and return Raster
public class Readimage {
	public static Raster readimage(String image) throws IOException{
		File file = new File(image);
		AbstractGridFormat format = GridFormatFinder.findFormat( file );
		GridCoverage2DReader reader = format.getReader( file );
		GridCoverage2D coverage = (GridCoverage2D) reader.read(null);
		CoordinateReferenceSystem crs = coverage.getCoordinateReferenceSystem2D();
//		System.out.println(crs);
		org.opengis.geometry.Envelope env = coverage.getEnvelope();
		RenderedImage imagedata = coverage.getRenderedImage();
		Raster raster = imagedata.getData();
		return raster;
	}
}
