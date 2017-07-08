package edu.ucr.cs.horus;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.Polygon;
import org.geotools.geometry.DirectPosition2D;
import org.opengis.referencing.operation.TransformException;

import java.awt.*;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Applies a scan-line technique to slip rasters based on polygons. It runs
 * as follows:
 * <ol>
 *   <li>Compute the minimum and maximum Y coordinates of the polgyon</li>
 *   <li>Project ymin and ymax to the raster space to find the indexes of
 *   the first and last scan lines that correspond to the query polygon.</li>
 *   <li>Project the center of each scan line from ymin to ymax to the
 *   vector space to compute a y-coordinate.</li>
 *   <li>Break down the polygon into line segments.</li>
 *   <li>Map each line segment to one or more scan lines based on their Y
 *   coordinates</li>
 *   <li>For each scan line, compute all the intersections between the line
 *   and all polygon segments.</li>
 *   <li>Order intersections in each scan line by their X coordinates. Assume
 *   the order is x<sub>0</sub>, x<sub>1</sub>, ..., x<sub>n</sub></li>
 *   <li>Map each x coordinate back to the raster space to find the corresponding
 *   pixels.</li>
 *   <li>Process all pixels between two X coordinates x<sub>2i</sub> and
 *   x_{2i+1}</li>
 * </ol>
 * Created by Ahmed Eldawy on 5/2/17.
 */
public class ScanClipper extends Clipper {

  /**A global variable that stores the total time for computing intersections*/
  public static long processingTime;

  /**A global variable that stores the total time for reading data from raster and processing it*/
  public static long intersectionTime;

  private void computeIntersections(Geometry geom, int minRow, int maxRow, double[] scanLinesY,
                                    ArrayList<Integer>[] intersections, RasterManager rasterFile, int geomIndexMask) throws TransformException {
    if (geom instanceof GeometryCollection) {
      // A geometry collection is decomposed into simple geometries and each one is processed recursively.
      GeometryCollection geomC = (GeometryCollection) geom;
      for (int iGeom = 0; iGeom < geomC.getNumGeometries(); iGeom++)
        computeIntersections(geomC.getGeometryN(iGeom), minRow, maxRow, scanLinesY, intersections, rasterFile, geomIndexMask);
    } else if (geom instanceof Polygon) {
      // A polygon is processed by computing intersections for each linear ring
      Polygon poly = (Polygon) geom;
      computeIntersections(poly.getExteriorRing(), minRow, maxRow, scanLinesY, intersections, rasterFile, geomIndexMask);
      for (int i = 0; i < poly.getNumInteriorRing(); i++)
        computeIntersections(poly.getInteriorRingN(i), minRow, maxRow, scanLinesY, intersections, rasterFile, geomIndexMask);
    } else if (geom instanceof LinearRing) {
      LinearRing line = (LinearRing) geom;
      DirectPosition2D ptemp = new DirectPosition2D();
      Coordinate p1 = line.getCoordinateN(line.getNumPoints() - 1);
      ptemp.setLocation(p1.x, p1.y);
      Point2D.Double pt1 = rasterFile.v2r(ptemp, null);
      Point2D.Double pixel = null;
      for (int iPoint = 1; iPoint < line.getNumPoints(); iPoint++) {
        Coordinate p2 = line.getCoordinateN(iPoint);

        double dx = p2.x - p1.x;
        double dy = p2.y - p1.y;

        ptemp.setLocation(p2.x, p2.y);
        Point2D.Double pt2 = rasterFile.v2r(ptemp, null);

        int row1 = (int) pt1.y;
        int row2 = (int) pt2.y;
        if (row1 > row2) {
          int temp = row1;
          row1 = row2;
          row2 = temp;
        }
        if (row1 < 0)
          row1 = 0;
        if (row2 > rasterFile.getHeight())
          row2 = rasterFile.getHeight();
        for (int row = row1; row < row2; row++) {
          // Find the intersection of the line segment (p1, p2) and the straight line (y = scanLinesY[row])
          double xIntersection = p2.x - dx * (p2.y - scanLinesY[row - minRow]) / dy;
          // Find the corresponding column in the raster file
          ptemp.setLocation(xIntersection, scanLinesY[row - minRow]);
          pixel = rasterFile.v2r(ptemp, null);
          // Cannot support negative indexes with bit mask.
          if (pixel.x < 0)
            pixel.x = 0;
          if (pixel.x > rasterFile.getWidth())
            pixel.x = rasterFile.getWidth();
          intersections[row - minRow].add((int)pixel.x | geomIndexMask);
        }
        // Advance to the next point in the linestring
        p1 = p2;
        pt1 = pt2;
      }
    } else if (geom.isEmpty()) {
      // Nothing to do
    } else {
      throw new RuntimeException("Cannot process geometries of type "+geom.getGeometryType());
    }
  }

  @Override
  protected Collector[] process(Geometry[] geoms, RasterManager raster, Collector[] results) {
    int bitShift = computeNumberOfSignificantBits(raster.getWidth());
    try {
      long t0 = System.nanoTime();
      // Compute the MBR of all geometries to determine the range of scan lines to be processed in the raster file
      Geometry mbr = geoms[0].getEnvelope();
      for (int i = 1; i < geoms.length; i++)
        mbr = mbr.union(geoms[i].getEnvelope()).getEnvelope();
      Coordinate[] mbrCoords = mbr.getCoordinates();
      DirectPosition2D p1 = raster.v2r(new DirectPosition2D(mbrCoords[0].x, mbrCoords[0].y), null);
      DirectPosition2D p2 = raster.v2r(new DirectPosition2D(mbrCoords[2].x, mbrCoords[2].y), null);
      int rasterMinRow = (int) Math.min(p1.y, p2.y); if (rasterMinRow < 0) rasterMinRow = 0;
      int rasterMaxRow = (int) Math.max(p1.y, p2.y); if (rasterMaxRow > raster.getHeight()) rasterMaxRow = raster.getHeight();
      int rasterMinCol = (int) Math.min(p1.x, p2.x); if (rasterMinCol < 0) rasterMinCol = 0;
      int rasterMaxCol = (int) Math.max(p1.x, p2.x); if (rasterMaxCol > raster.getWidth()) rasterMaxCol = raster.getWidth();

      if (rasterMinRow >= rasterMaxRow)
        return results;

      // scanLinesY is an array of y coordinates of the centers of scan lines to be processed
      double[] scanLinesY = new double[rasterMaxRow - rasterMinRow];

      // A 2D array of all pixel intersections for each scan line. First index is for the scan line, second index
      // is a combination of the geometry index in the array and the column in the raster file in the form
      // (geomIndex # column) where # is the concatenation operator. This allows us to sort all values lexicographically
      // geometry index and column number
      ArrayList<Integer>[] intersections = new ArrayList[scanLinesY.length];

      DirectPosition2D convertedPoint = null;
      for (int iRow = rasterMinRow; iRow < rasterMaxRow; iRow++) {
        intersections[iRow - rasterMinRow] = new ArrayList<Integer>();
        convertedPoint = raster.r2v(rasterMinCol, iRow, convertedPoint);
        scanLinesY[iRow - rasterMinRow] = convertedPoint.getY();
      }

      // Start filling in the intersections array for each geometry
      for (int i = 0; i < geoms.length; i++) {
        Geometry geom = geoms[i];
        // Compute the MBR of the polygon to determine the corresponding range of pixels in the raster dataset
        mbrCoords = geom.getEnvelope().getCoordinates();
        p1 = raster.v2r(new DirectPosition2D(mbrCoords[0].x, mbrCoords[0].y), p1);
        p2 = raster.v2r(new DirectPosition2D(mbrCoords[2].x, mbrCoords[2].y), p2);

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
        if (p1.x < 0)
          p1.x = 0;
        if (p2.x > raster.getWidth())
          p2.x = raster.getWidth();
        if (p1.y < 0)
          p1.y = 0;
        if (p2.y > raster.getHeight())
          p2.y = raster.getHeight();

        // If the geometry is completely outside the boundaries of the raster file, skip it
        if (p1.y >= p2.y || p1.x >= p2.x)
          continue;

        // Compute the intersections
        int geomIndexMask = i << bitShift;

        computeIntersections(geom, rasterMinRow, rasterMaxRow, scanLinesY, intersections, raster, geomIndexMask);
      }
      // Sort all the computed intersections lexicographically by (geometry index, intersection column)
      for (int iRow = 0; iRow < intersections.length; iRow++)
        Collections.sort(intersections[iRow]);

      long t1 = System.nanoTime();
      intersectionTime += t1 - t0;

      // Compute the results
      int columnBitMask = (~0) >>> (32 - bitShift);
      // Initialize the values array with the maximum possible size
      int[] values = new int[raster.getTileWidth() * raster.getTileHeight() * raster.getNumBands()];

      // For efficiency, read the results tile by tile
      int gridMinCol = (int)Math.floor((float)(rasterMinCol - raster.getTileX()) / raster.getTileWidth());
      int gridMaxCol = (int)Math.ceil((float)(rasterMaxCol - raster.getTileX()) / raster.getTileWidth());
      int gridMinRow = (int)Math.floor((float)(rasterMinRow - raster.getTileY()) / raster.getTileHeight());
      int gridMaxRow = (int)Math.ceil((float)(rasterMaxRow - raster.getTileY()) / raster.getTileHeight());
      int numTiles = (gridMaxCol - gridMinCol) * (gridMaxRow - gridMinRow);

      Rectangle bounds = new Rectangle();
      for (int iTile = 0; iTile < numTiles; iTile++) {

        // Find the correct part of the raster file to read
        int tileCol = iTile % (gridMaxCol - gridMinCol) + gridMinCol;
        int tileRow = iTile / (gridMaxCol - gridMinCol) + gridMinRow;
        int x1 = tileCol == gridMinCol ? rasterMinCol : (tileCol * raster.getTileWidth() + raster.getTileX());
        int x2 = (tileCol + 1) == gridMaxCol ? rasterMaxCol : x1 + raster.getTileWidth();
        int y1 = tileRow == gridMinRow ? rasterMinRow : (tileRow * raster.getTileHeight() + raster.getTileY());
        int y2 = (tileRow + 1) == gridMaxRow ? rasterMaxRow : y1 + raster.getTileHeight();
        bounds.setBounds(x1, y1, x2-x1, y2-y1);
        raster.loadRaster(bounds);

        // Locate the corresponding parts of the intersections array to determine which pixels to read
        for (int intersectionRow = y1; intersectionRow < y2; intersectionRow++) {
          int intersectionRowIndex = intersectionRow - rasterMinRow;

          if (intersectionRowIndex >= 0 && intersectionRowIndex < intersections.length) {
            for (int j = 0; j < intersections[intersectionRowIndex].size(); j += 2) {
              int intersectionCol1 = intersections[intersectionRowIndex].get(j) & columnBitMask;
              if (intersectionCol1 < x1)
                intersectionCol1 = x1;
              int intersectionCol2 = intersections[intersectionRowIndex].get(j + 1) & columnBitMask;
              if (intersectionCol2 > x2)
                intersectionCol2 = x2;
              int geomIndex = intersections[intersectionRowIndex].get(j) >> bitShift;
              if (intersectionCol1 < intersectionCol2) {
                values = raster.getPixels(intersectionCol1, intersectionRow, intersectionCol2 - intersectionCol1, 1, values);
                results[geomIndex].collect(intersectionCol1, intersectionRow, intersectionCol2 - intersectionCol1, 1, values);
              }
            }
          }

        }
      }
      long t2 = System.nanoTime();
      processingTime += t2 - t1;


      return results;
    } catch (TransformException e) {
      throw new RuntimeException("Error processing file", e);
    } catch (IOException e) {
      throw new RuntimeException("Error loading raster", e);
    }
  }

  /**
   * Commputes the total number of significant bits in the given value.
   * @param v
   * @return
   */
  public static int computeNumberOfSignificantBits(int v) {
    int numBits = 0;
    if (v >= (1 << 16)) { numBits += 16; v >>= 16;}
    if (v >= (1 <<  8)) { numBits += 8; v >>= 8;}
    if (v >= (1 <<  4)) { numBits += 4; v >>= 4;}
    if (v >= (1 <<  2)) { numBits += 2; v >>= 2;}
    if (v >= (1 <<  1)) { numBits += 1; v >>= 1;}
    numBits++;
    return numBits;
  }
}
