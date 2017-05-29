package org.geotools.test;

import java.io.File;
import java.io.IOException;

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;

import com.vividsolutions.jts.geom.Geometry;

public class GetGeomTest {

			private static SimpleFeatureSource featureSource;
		public static void main( String[] args) throws IOException{
		    
		    String path="C:/Users/29563/Desktop/vectorfile/regions-20140306-100m.shp";
			File file = new File(path);
			FileDataStore fileDataStore=FileDataStoreFinder.getDataStore(file);
			featureSource=fileDataStore.getFeatureSource();
			
			SimpleFeatureType schema=featureSource.getSchema();
		
			System.out.println(schema.getCoordinateReferenceSystem().toWKT());
		//	simpleFeatureSource.getSchema();
		//	String filterText = "nom = '" + countryname + "'";
			/*
			String filterText = "GMI_CNTRY = '" + countryname + "'";
			Filter filter = CQL.toFilter(filterText);
			SimpleFeatureCollection result = simpleFeatureSource.getFeatures(filter);
			SimpleFeatureIterator iterator = result.features();
			try {

				while (iterator.hasNext()) {
					SimpleFeature simplefeature = iterator.next();
					Geometry geom = (Geometry) simplefeature.getDefaultGeometry();
					return geom;
				}

			} finally {
				// dataStore.dispose();
				iterator.close();
			}
			*/
		}

	}

