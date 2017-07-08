package edu.ucr.cs.horus;

import com.vividsolutions.jts.geom.*;
import org.geotools.geometry.DirectPosition2D;
import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.operation.TransformException;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

/**
 * A Clipper that implements a quadratic split method. First, It starts with
 * the given polygon and applies the steps below. At any point, it might generate
 * several polygons which are recursively processed in the same way.
 * <ul>
 *   <li>If the record is not a polygon, e.g., empty or a line, it skips it.</li>
 *   <li>If the record is a multipolygon, it decomposes it into simple polygons.
 *   Each polygon is recursively processed in the same manner.</li>
 *   <li>If the record is a simple polygon with less then (threshold) line segments,
 *   it applies the NaivePipClipper algorithm.</li>
 *   <li>If the record is a simple polygon with more than (threshold) line segments,
 *   it splits it into four along the center of its MBR. The four generated polygons
 *   are recursively processed in the same manner.</li>
 * </ul>
 * Created by Ahmed Eldawy on 5/2/17.
 */
public class QSplitPiPClipper extends NaivePiPClipper {
  /**Total time in nano seconds spent in splitting polygons into smaller ones*/
  public static double splitTime;

  protected GeometryFactory geomFactory = new GeometryFactory();

  protected int threshold = 100 * 10 * log2_64(10);

  @Override
  public long countPixels(Geometry geometry, RasterManager raster) {
    return stats(geometry, raster, null).count[0];
  }

  public void decompose(Geometry in, List<Geometry> out) {
    for (int i = 0; i < in.getNumGeometries(); i++) {
      out.add(in.getGeometryN(i));
    }
  }

  public void setThreshold(int t) {
    this.threshold = t;
  }

  public void qsplit(Geometry in, List<Geometry> out) {
    long t0 = System.nanoTime();
    Geometry envelope = in.getEnvelope();
    Coordinate[] coords = envelope.getCoordinates();
    Coordinate[][] corners = new Coordinate[3][3];
    double x1 = Math.min(coords[0].x, coords[2].x);
    double x2 = Math.max(coords[0].x, coords[2].x);
    double y1 = Math.min(coords[0].y, coords[2].y);
    double y2 = Math.max(coords[0].y, coords[2].y);
    double cx = (x1 + x2) / 2;
    double cy = (y1 + y2) / 2;
    corners[0][0] = new Coordinate(x1, y1);
    corners[0][1] = new Coordinate(x1, cy);
    corners[0][2] = new Coordinate(x1, y2);
    corners[1][0] = new Coordinate(cx, y1);
    corners[1][1] = new Coordinate(cx, cy);
    corners[1][2] = new Coordinate(cx, y2);
    corners[2][0] = new Coordinate(x2, y1);
    corners[2][1] = new Coordinate(x2, cy);
    corners[2][2] = new Coordinate(x2, y2);

    Polygon q0 = geomFactory.createPolygon(
        geomFactory.createLinearRing(
            new Coordinate[] { corners[0][0], corners[1][0], corners[1][1], corners[0][1], corners[0][0] }),
        null);
    out.add(q0.intersection(in));
    Polygon q1 = geomFactory.createPolygon(
        geomFactory.createLinearRing(
            new Coordinate[] { corners[0][1], corners[1][1], corners[1][2], corners[0][2], corners[0][1] }),
        null);
    out.add(q1.intersection(in));
    Polygon q2 = geomFactory.createPolygon(
        geomFactory.createLinearRing(
            new Coordinate[] { corners[1][0], corners[2][0], corners[2][1], corners[1][1], corners[1][0] }),
        null);
    out.add(q2.intersection(in));
    Polygon q3 = geomFactory.createPolygon(
        geomFactory.createLinearRing(
            new Coordinate[] { corners[1][1], corners[2][1], corners[2][2], corners[1][2], corners[1][1] }),
        null);
    out.add(q3.intersection(in));
    long t1 = System.nanoTime();
    splitTime += t1 - t0;
  }

  @Override
  protected Collector process(Geometry geometry, RasterManager raster, Collector result) {
    try {
      Stack<Geometry> toProcess = new Stack<Geometry>();
      toProcess.push(geometry);
      DirectPosition2D p1 = new DirectPosition2D(), p2 = new DirectPosition2D();
      while (!toProcess.isEmpty()) {
        Geometry geom = toProcess.pop();
        if (geom.getNumGeometries() > 1) {
          // A multipolygon, decompose it into single polygons
          decompose(geom, toProcess);
        } else if (geom.isEmpty() || geom.getNumPoints() < 4) {
          // Skip an empty geometry, or a non-polygonal geometry (e.g., a line)
        } else if (geom.isRectangle()) {
          // A rectangle does not need to be split any further
          super.process(geom, raster, result);
        } else {
          // Compute the complexity of the polygon
          Coordinate[] mbrCoords = geom.getEnvelope().getCoordinates();
          p1.setLocation(mbrCoords[0].x, mbrCoords[0].y); raster.v2r(p1, p1);
          p2.setLocation(mbrCoords[2].x, mbrCoords[2].y); raster.v2r(p2, p2);
          int a = Math.abs((int) ((p2.x-p1.x) * (p2.y-p1.y)));
          int n = geom.getNumPoints();
          double cost = (double) a * n * log2_64(n);
          if (cost > threshold) {
            // A single complex polygon, split it into smaller ones
            qsplit(geom, toProcess);
          } else {
            // A single and simple polygon, process it using the naive method
            super.process(geom, raster, result);
          }
        }
      }
    } catch (TransformException e) {
      throw new RuntimeException("Error converting vector to raster", e);
    } catch (TopologyException e) {
      result.invalidate();
    }

    return result;
  }

  private static final int[] Tab64 = {
    63,  0, 58,  1, 59, 47, 53,  2,
        60, 39, 48, 27, 54, 33, 42,  3,
        61, 51, 37, 40, 49, 18, 28, 20,
        55, 30, 34, 11, 43, 14, 22,  4,
        62, 57, 46, 52, 38, 26, 32, 41,
        50, 36, 17, 19, 29, 10, 13, 21,
        56, 45, 25, 31, 35, 16,  9, 12,
        44, 24, 15,  8, 23,  7,  6,  5};

  public static int log2_64 (long value) {
    value |= value >> 1;
    value |= value >> 2;
    value |= value >> 4;
    value |= value >> 8;
    value |= value >> 16;
    value |= value >> 32;
    return (int)Tab64[(int)(((long)((value - (value >> 1))*0x07EDD5E59A4E28C2L)) >>> 58)];
  }
}
