package org.geotools.test;

import java.io.File;

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.function.Getgeom;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;

import com.vividsolutions.jts.geom.Geometry;

public class vectorfilecodeTest {
	public static void main(String[] args){
		File file = new File("");
		FileDataStore dataStore = FileDataStoreFinder.getDataStore(file);
		String typeName = dataStore.getTypeNames()[0];
		SimpleFeatureSource simpleFeatureSource = dataStore.getFeatureSource(typeName);
	//	simpleFeatureSource.getSchema();
	//	String filterText = "nom = '" + countryname + "'";
		SimpleFeatureCollection result = simpleFeatureSource.getFeatures(filter);
		SimpleFeatureIterator iterator = result.features();
		try {

			while (iterator.hasNext()) {
				SimpleFeature simplefeature = iterator.next();
				Geometry geom = (Geometry) simplefeature.getDefaultGeometry();
				
			}

		} finally {
			// dataStore.dispose();
			iterator.close();
		}
	
	}
	}
}
