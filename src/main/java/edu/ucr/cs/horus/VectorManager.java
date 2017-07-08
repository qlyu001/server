package edu.ucr.cs.horus;

import com.vividsolutions.jts.geom.Geometry;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * A class that abstracts the processing of a file that contains vector data
 * (e.g., polygons). It makes it possible to search and extract individual
 * geometries (e.g., polgyons) from the file without knowing its format.
 * Currently, the supported formats are:
 * <ul>
 *   <li>A ZIP compressed Shapefile.</li>
 * </ul>
 * Created by Ahmed Eldawy on 4/27/17.
 */
public class VectorManager implements Closeable {
  private final DataStore dataStore;
  private final String type;
  /** The {@link CoordinateReferenceSystem} used by this shapefile */
  private CoordinateReferenceSystem crs;

  /**
   * Constructs a new VectorManager from the given file path.
   * @param filename
   * @throws IOException
   */
  public VectorManager(String filename) throws IOException {
    URL url = new File(filename).toURI().toURL();
    if (url.getFile().toLowerCase().endsWith(".zip")) {
      // A compressed file
      try (ZipFile zipFile = new ZipFile(url.getPath())) {
        Enumeration zipEntries = zipFile.entries();
        while (zipEntries.hasMoreElements()) {
          String internalFilename = ((ZipEntry) zipEntries.nextElement()).getName();
          if (internalFilename.toLowerCase().endsWith(".shp"))
            url = new URL("jar:" + url + "!/" + internalFilename);
        }
      }
    } else if (url.getFile().toLowerCase().endsWith(".shp")) {
      // Do nothing! Use the same file URL as-is
    } else {
      throw new RuntimeException("Unsupported file format "+filename);
    }
    Map<String, Object> map = new HashMap<>();
    map.put(ShapefileDataStoreFactory.URLP.key, url);
    map.put(ShapefileDataStoreFactory.CREATE_SPATIAL_INDEX.key, Boolean.FALSE);

    dataStore = DataStoreFinder.getDataStore(map);
    type = dataStore.getTypeNames()[0];
  }

  /**
   * Search for the record with the given field name and value. Returns a
   * SimpleFeatureCollection for the results. The results might contain zero
   * or more records.
   * @param field Name of the field to search.
   * @param value The value of the field.
   * @return A result set with zero or more results.
   * @throws IOException
   */
  public SimpleFeatureCollection search(String field, String value) throws IOException {
    FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();

    Filter filter = ff.equal(ff.property(field), ff.literal(value), false);

    return dataStore.getFeatureSource(type).getFeatures(filter);
  }

  /**
   * Returns the geometry part of a given record. This function first calls
   * #search(String,String) to match the records and then returns the geometry
   * of the first record in the answer. If the answer is empty, a null is
   * returned.
   * @param field Name of the field to search.
   * @param value The value of the field.
   * @return The geometry of the first matching object or null if no objects match.
   * @throws IOException
   */
  public Geometry getGeom(String field, String value) throws IOException {
    SimpleFeatureCollection searchResult = search(field, value);
    if (searchResult.size() == 0)
      return null;
    SimpleFeatureIterator features = searchResult.features();
    Geometry geom = (Geometry) features.next().getDefaultGeometry();
    features.close();
    return geom;
  }

  public SimpleFeatureCollection getAllFeatures() throws IOException {
    SimpleFeatureSource featureSource = dataStore.getFeatureSource(type);
    this.crs = featureSource.getSchema().getCoordinateReferenceSystem();
    return featureSource.getFeatures();
  }

  public Geometry[] getAllGeom() throws IOException {
    SimpleFeatureCollection features = getAllFeatures();
    Geometry[] allGeom = new Geometry[features.size()];
    SimpleFeatureIterator results = features.features();
    int i = 0;
    while (results.hasNext()) {
      allGeom[i++] = (Geometry) results.next().getDefaultGeometry();
    }
    results.close();
    return allGeom;
  }

  public CoordinateReferenceSystem getCRS() {
    return this.crs;
  }

  @Override
  public void close() throws IOException {
    this.dataStore.dispose();
  }
}
