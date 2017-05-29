package org.geotools.function;
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
import org.opengis.filter.Filter;

import com.vividsolutions.jts.geom.Geometry;

// Read shapefile. Return geom of country
public class Getgeom {

	public static Geometry getgeom(String countryname,String path,String placeKey) throws IOException, CQLException {
		File file = new File(path);
		FileDataStore dataStore = FileDataStoreFinder.getDataStore(file);
		String typeName = dataStore.getTypeNames()[0];
		SimpleFeatureSource simpleFeatureSource = dataStore.getFeatureSource(typeName);
	//	simpleFeatureSource.getSchema();
	//	String filterText = "nom = '" + countryname + "'";
		String filterText = placeKey+ " = '" + countryname + "'";
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
		return null;
	}

}
