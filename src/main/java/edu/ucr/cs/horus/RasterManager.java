package edu.ucr.cs.horus;

import com.vividsolutions.jts.geom.Geometry;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.geometry.DirectPosition2D;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import java.awt.*;
import java.awt.image.Raster;
import java.io.IOException;

/**
 * Created by eldawy on 5/26/17.
 */
public interface RasterManager {

  /**
   * Returns the value of a given pixel. This function assumes that the raster has been already loaded and contains
   * this pixel. While this function can automatically check and load the appropriate part of the raster, it could be
   * too much overhead when reading big portions of the raster file. For performance consideration, the developer must
   * first load the appropriate part of the raster in one call before reading all pixels.
   *
   * @param column
   * @param row
   * @param value
   * @return
   */
  public int[] getPixel(int column, int row, int[] value);


  /**
   * Returns all the pixels in the given range
   * @param column
   * @param row
   * @param width
   * @param height
   * @param value
   * @return
   */
  public int[] getPixels(int column, int row, int width, int height, int[] value);

  /**
   * Load part of the underlying raster for the given rectangle. The input rectangle defines the pixel range to be loaded.
   * @param rect
   * @return
   * @throws IOException
   */
  public void loadRaster(Rectangle rect) throws IOException;

  /**
   * The height of the entire raster layer in pixels
   * @return
   */
  public int getHeight();

  /**
   * The width of the entire raster layer in pixels.
   * @return
   */
  public int getWidth();

  /**
   * Number of bands stored in the raster file.
   * @return
   */
  public int getNumBands();

  /**
   * Converts a point in the vector space (double coordinates) to a point in
   * the raster space (integer coordinates of a pixel). It returns the first
   * pixel with a center next to the given coordinates. That is, it finds the
   * nearest point p which has coordinates (p.x > in.x) and (p.y > in.y) where
   * (p.x, p.y) is the vector coordinates of the center of the pixel and
   * (in.x, in.y) is the coordinates of the input point.
   * @param inPoint
   * @param outPoint
   * @return
   */
  public DirectPosition2D v2r(DirectPosition2D inPoint, DirectPosition2D outPoint) throws TransformException;

  /**
   * Converts a point from the raster space (integer coordinates) to the vector
   * space (double coordinates). This function returns the coordinates of the
   * center of the corresponding pixel. In other words, if a pixel maps to a
   * rectangle, this function returns the center of that rectangle. Notice that
   * the geographic coordinate system (projection) is not taken into account.
   * The returned value is in whatever that coordinate system is.
   * @param x
   * @param y
   * @param outPoint
   * @return
   */
  public DirectPosition2D r2v(int x, int y, DirectPosition2D outPoint) throws TransformException;

  public GridCoverage2D getGridCoverage() throws IOException;

  public abstract int getTileWidth();
  public abstract int getTileHeight();
  public abstract int getTileX();
  public abstract int getTileY();
  // TODO add a close function to ensure that the underlying file is closed

  /**
   * Reprojects the given geometry from the given {@link CoordinateReferenceSystem} to the coordinate reference system
   * used by this raster layer. The new geometry is returned.
   * @param geom
   * @param crs
   * @return
   */
  Geometry reproject(Geometry geom, CoordinateReferenceSystem crs) throws TransformException;
}
