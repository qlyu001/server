package edu.ucr.cs.horus;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.Point;
import org.geotools.geometry.DirectPosition2D;
import org.opengis.referencing.operation.TransformException;

import java.awt.*;
import java.awt.geom.Point2D;
import java.io.IOException;

/**
 * A naive clipper that uses point-in-polygon queries to test whether each pixel
 * is in the polygon or not. It works as follows.
 * <ol>
 *   <li>Compute the MBR of the query polygon using Geometry#getEnvelope</li>
 *   <li>Map the corners of the rectangle to the raster space to obtain a
 *   rectangle in the raster space.</li>
 *   <li>If the input polygon is a rectangle, return all points in the mapped
 *   rectangle in the raster space</li>
 *   <li>If the input polygon is not a rectangle, scan all the pixels in the
 *   mapped rectangle. For each pixel, map its center to the vector space and
 *   test it against the polygon. If it is contained in the polygon, we return
 *   it; otherwise, we skip it.</li>
 * </ol>
 * Created by Ahmed Eldawy on 5/2/17.
 */
public class NaivePiPClipper extends Clipper {

  /**Total time in nano seconds for running point-in-polygon queries*/
  public static double pipTime;

  /**Total time in nano seconds for directly reading a rectangular block of the raster file without pip tests*/
  public static long grabTime;

  @Override
  protected Collector process(Geometry geometry, RasterManager raster, Collector result) {
    try {
      long t0 = System.nanoTime();
      Coordinate[] mbrCoords = geometry.getEnvelope().getCoordinates();
      Point2D.Double p1 = raster.v2r(new DirectPosition2D(mbrCoords[0].x, mbrCoords[0].y), null);
      Point2D.Double p2 = raster.v2r(new DirectPosition2D(mbrCoords[2].x, mbrCoords[2].y), null);
      if (p1.x > p2.x) {
        double t = p1.x;
        p1.x = p2.x;
        p2.x = t;
      }
      if (p1.x < 0)
        p1.x = 0;
      if (p2.x > raster.getWidth())
        p2.x = raster.getWidth();
      if (p1.y > p2.y) {
        double t = p1.y;
        p1.y = p2.y;
        p2.y = t;
      }

      if (p1.y < 0)
        p1.y = 0;
      if (p2.y > raster.getHeight())
        p2.y = raster.getHeight();

      if (p1.y >= p2.y || p1.x >= p2.x)
        return result;

      try {
        raster.loadRaster(new Rectangle((int)p1.x, (int)p1.y, (int)(p2.x - p1.x), (int)(p2.y - p1.y)));
      } catch (IOException e) {
        throw new RuntimeException("Error reading raster file", e);
      }

      int[] value = null;
      if (geometry.isRectangle()) {
        // No need to test point-in-polygon queries. Just return all the points.
        for (int x = (int) p1.x; x < p2.x; x++) {
          for (int y = (int) p1.y; y < p2.y; y++) {
            value = raster.getPixel(x, y, value);
            result.collect(x, y, value);
          }
        }
        long t1 = System.nanoTime();
        grabTime += t1 - t0;
      } else {
        GeometryFactory geomFactory = new GeometryFactory();
        Coordinate coord = new Coordinate();

        DirectPosition2D p = new DirectPosition2D();
        for (int i = (int) p1.x; i < p2.x; i++) {
          for (int j = (int) p1.y; j < p2.y; j++) {
            p = raster.r2v(i, j, p);
            coord.setOrdinate(0, p.getX());
            coord.setOrdinate(1, p.getY());
            Point pt = geomFactory.createPoint(coord);
            if (geometry.contains(pt)) {
              value = raster.getPixel(i, j, value);
              result.collect(i, j, value);
            }
          }
        }
        long t1 = System.nanoTime();
        pipTime += t1 - t0;
      }
    } catch (TransformException e) {
      throw new RuntimeException("Error processing file", e);
    }
    return result;
  }

  @Override
  protected Collector[] process(Geometry[] geoms, RasterManager raster, Collector[] results) {
    for (int iGeom = 0; iGeom < geoms.length; iGeom++) {
      results[iGeom] = this.process(geoms[iGeom], raster, results[iGeom]);
    }
    return results;
  }
}
