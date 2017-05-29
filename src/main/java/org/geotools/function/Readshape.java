package org.geotools.function;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;

import com.vividsolutions.jts.geom.Geometry;

public class Readshape {
	public static Vector <String> Readshape(String path) throws IOException, CQLException {
		File file = new File(path);
    DataStore dataStore = FileDataStoreFinder.getDataStore(file);
    String typeName = dataStore.getTypeNames()[0];

    FeatureSource<SimpleFeatureType, SimpleFeature> source = dataStore
            .getFeatureSource(typeName);
    Filter filter = Filter.INCLUDE; // ECQL.toFilter("BBOX(THE_GEOM, 10,20,30,40)")

    FeatureCollection<SimpleFeatureType, SimpleFeature> collection = source.getFeatures(filter);
    try (FeatureIterator<SimpleFeature> features = collection.features()) {
        while (features.hasNext()) {
            SimpleFeature feature = features.next();
            System.out.print(feature.getID());
            System.out.print(": ");
            System.out.println(feature.getAttribute(5));
        }
    }
	return null;
	}
}
