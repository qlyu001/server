package edu.ucr.cs.horus;

import com.vividsolutions.jts.geom.Geometry;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.operation.TransformException;

import java.awt.*;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Ahmed Eldawy on 5/21/2017.
 */
public class Server {


  public Server() {
  }

  public void runHistogramQuery(String rasterFile, String vectorFile) throws IOException {
    try {
      ArrayList<Long> times = new ArrayList<Long>();
      VectorManager vectorManager;
      SimpleFeatureCollection features;
      SimpleFeatureIterator f;
      RasterManager glc;
      // Open raster file
      times.add(System.nanoTime());
      vectorManager = new VectorManager(vectorFile);
      times.add(System.nanoTime());
      features = vectorManager.getAllFeatures();
      times.add(System.nanoTime());
      f = features.features();

      // Open vector file and retrieve results
      times.add(System.nanoTime());
      glc = new GeoToolsRasterManager(rasterFile);
      ReferencedEnvelope vbounds = features.getBounds();
      Point2D.Double p1 = glc.v2r(new DirectPosition2D(vbounds.getMinX(), vbounds.getMinY()), null);
      Point2D.Double p2 = glc.v2r(new DirectPosition2D(vbounds.getMaxX(), vbounds.getMaxY()), null);
      if (p1.x > p2.x) {
        double t = p1.x;
        p1.x = p2.x;
        p2.x = t;
      }
      if (p1.y > p2.y) {
        double t = p1.y;
        p1.y = p2.y;
        p2.y = t;
      }
      glc.loadRaster(new Rectangle((int)p1.x, (int)p1.y, (int)(p2.x - p1.x), (int)(p2.y - p1.y)));

      //Clipper rclipper = new RasterizeClipper();
      //QSplitPiPClipper qclipper100 = new QSplitPiPClipper(); qclipper100.setThreshold(100);
      //QSplitPiPClipper qclipper50 = new QSplitPiPClipper(); qclipper50.setThreshold(50);
      //QSplitPiPClipper qclipper10 = new QSplitPiPClipper(); qclipper10.setThreshold(10);
      Clipper sclipper = new ScanClipper();
      Collector hist = null;
      times.add(System.nanoTime());
      while (f.hasNext()) {
        SimpleFeature obj = f.next();
        Geometry geom = (Geometry) obj.getDefaultGeometry();

        hist = sclipper.stats(geom, glc, null);
        System.out.print(obj.getAttribute("name") + ": [");
        for (long x : ((Statistics)hist).count)
          System.out.print(x+", ");
        System.out.println(']');
      }
      times.add(System.nanoTime());
      System.out.println("Total time " + (times.get(times.size() - 1) - times.get(0)) / 1E9);
      System.out.print("[");
      for (int i = 1; i < times.size(); i++) {
        if (i > 1)
          System.out.print(", ");
        System.out.print((times.get(i) - times.get(i - 1)) / 1E9);
      }
      System.out.println("]");
      f.close();


    } catch (TransformException e) {
      throw new RuntimeException("Error processing", e);
    }
  }
}
