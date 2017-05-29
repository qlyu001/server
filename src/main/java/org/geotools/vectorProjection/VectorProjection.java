package org.geotools.vectorProjection;

import java.io.File;
import java.io.IOException;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

public class VectorProjection {
	private SimpleFeatureSource featureSource;
	public  Geometry vectorProjection(Geometry geom, String path,int crscode) throws IOException{
	    File file=new File(path);
	    FileDataStore fileDataStore=FileDataStoreFinder.getDataStore(file);
	    featureSource=fileDataStore.getFeatureSource();
	    SimpleFeatureType schema=featureSource.getSchema();
		CoordinateReferenceSystem shpSource,target;
		try {
			/*
			
			*/
			shpSource = schema.getCoordinateReferenceSystem();// There is a problem if we use this to get object. Coordinate x y is  exchanged, so I write ExchangeXY.jave to change it again.
			//-->it will take extra time
		
			target = (CoordinateReferenceSystem) CRS.decode("EPSG:"+crscode);
			MathTransform transform2 = CRS.findMathTransform(shpSource,  target);
			Geometry polyAfter =  JTS.transform(geom, transform2);
			//System.out.println( "after"+polyAfter );
			return polyAfter;
		} catch (NoSuchAuthorityCodeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
      catch (MismatchedDimensionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}
	
}