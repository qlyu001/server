package edu.ucr.cs.horus;

import com.vividsolutions.jts.geom.Geometry;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.*;

/**
 * Processes several queries based on raster and shapefiles.
 * Created by Ahmed Eldawy on 5/28/17.
 */
public class QueryProcessor {

  /**A global variable for the total time for reading and projecting the vector file.*/
  public static long vectorReadingTime;

  /**A global vairable for the total time of opening raster file*/
  public static long rasterOpenTime;

  public QueryProcessor() {
  }

  /**
   * Computes statistics for the entire raster file. Used as a baseline for how fast we can read the raster filer
   * without any geometrical computation.
   * @param rasterFile
   * @return
   * @throws IOException
   */
  public Statistics statsAll(String rasterFile) throws IOException {
    RasterManager raster = new GeoToolsRasterManager(rasterFile);
    Rectangle bounds = new Rectangle(raster.getTileX(), raster.getTileY(), raster.getTileWidth(), raster.getTileHeight());
    int[] values = null;
    int tileColumns = (int) Math.ceil((float)(raster.getWidth()/raster.getTileWidth()));
    int tileRows = (int) Math.ceil((float)(raster.getHeight()/raster.getTileHeight()));
    int numOfTiles = tileColumns * tileRows;

    Statistics stats = new Statistics();
    stats.setNumBands(raster.getNumBands());
    for (int iTile = 0; iTile < numOfTiles; iTile++) {
      int x1 = (iTile % tileColumns) * raster.getTileWidth();
      int y1 = (iTile / tileColumns) * raster.getTileHeight();
      int x2 = Math.min(x1 + raster.getTileWidth(), raster.getWidth());
      int y2 = Math.min(y1 + raster.getTileHeight(), raster.getHeight());

      bounds.x = x1;
      bounds.width = x2 - x1;
      bounds.y = y1;
      bounds.height = y2 - y1;

      raster.loadRaster(bounds);

      values = raster.getPixels(bounds.x, bounds.y, bounds.width, bounds.height, values);
      stats.collect(bounds.x, bounds.y, bounds.width, bounds.height, values);
    }
    return stats;
  }

  public static class Pair<V1, V2> {
    public V1 first;
    public V2 second;

    public Pair(){}
    public Pair(V1 v1, V2 v2) {
      this.first = v1;
      this.second = v2;
    }
  }

  public Pair<SimpleFeature[], Collector[]> stats(String rasterPath, String vectorFile, Clipper clipper) throws IOException {
    try {
      // Read all geometries in the vector file
      long t0 = System.nanoTime();
      VectorManager vectorManager = new VectorManager(vectorFile);
      SimpleFeatureCollection featureCollection = vectorManager.getAllFeatures();;
      ReferencedEnvelope vectorBounds = featureCollection.getBounds();

      SimpleFeature[] features = new SimpleFeature[featureCollection.size()];
      int iFeature = 0;
      SimpleFeatureIterator featureIterator = featureCollection.features();
      while (featureIterator.hasNext())
        features[iFeature++] = featureIterator.next();

      CoordinateReferenceSystem vectorCRS = vectorManager.getCRS();

      featureIterator.close();
      vectorManager.close();
      long t1 = System.nanoTime();
      vectorReadingTime += t1 - t0;

      // Start processing raster files
      File[] rasterFiles;
      File f = new File(rasterPath);
      if (f.isDirectory()) {
        // Read all files in the given directory
        rasterFiles = f.listFiles(new FileFilter() {
          @Override
          public boolean accept(File pathname) {
            String name = pathname.getName();
            int lastDot = name.lastIndexOf('.');
            if (lastDot == -1)
              return false; // A file with no extension
            String ext = name.substring(lastDot + 1).toLowerCase();
            return GeoToolsRasterManager.SupportedExtensions.contains(ext);
          }
        });
      } else {
        rasterFiles = new File[] {f};
      }

      // Process all rasterFiles and combine results from them
      Statistics[] finalResults = new Statistics[features.length];

      DirectPosition2D p1 = null, p2 = null;

      for (File rasterFile : rasterFiles) {
        t0 = System.nanoTime();
        // Open raster file
        RasterManager rasterManager = new GeoToolsRasterManager(rasterFile.getPath());

        // Load the part of the raster file of interest based on the MBR of all shapes in the shapefile
        p1 = rasterManager.v2r(new DirectPosition2D(vectorBounds.getMinX(), vectorBounds.getMinY()), p1);
        p2 = rasterManager.v2r(new DirectPosition2D(vectorBounds.getMaxX(), vectorBounds.getMaxY()), p2);
        Rectangle rasterBlock = new Rectangle((int)Math.min(p1.x, p2.x), (int)Math.min(p1.y, p2.y),
            (int)(Math.max(p1.x, p2.x) - Math.min(p1.x, p2.x)), (int)(Math.max(p1.y, p2.y) - Math.min(p1.y, p2.y)));

        Geometry[] geometries = new Geometry[features.length];
        for (int iGeometry = 0; iGeometry < geometries.length; iGeometry++)
          geometries[iGeometry] = rasterManager.reproject((Geometry) features[iGeometry].getDefaultGeometry(), vectorCRS);

        t1 = System.nanoTime();
        rasterOpenTime += t1 - t0;

        finalResults = clipper.stats(geometries, rasterManager, finalResults);
      }

      return new Pair<>(features, finalResults);

    } catch (TransformException e) {
      throw new RuntimeException("Error processing", e);
    }
  }

}
