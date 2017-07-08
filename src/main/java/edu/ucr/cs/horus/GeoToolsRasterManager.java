package edu.ucr.cs.horus;

import com.vividsolutions.jts.geom.Geometry;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridCoverage2DReader;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.geotools.data.PrjFileReader;
import org.geotools.factory.Hints;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.coverage.grid.GridEnvelope;
import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.datum.PixelInCell;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import javax.media.jai.ImageLayout;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.*;
import java.util.HashSet;
import java.util.Set;

/**
 * A wrapper around GeoTools for reading raster files supported by it.
 *
 * Created by Ahmed Eldawy on 4/27/17.
 */
public class GeoToolsRasterManager implements RasterManager {
  /** A set of all supported extensions in lower case letter withtout the dot */
  public static final Set<String> SupportedExtensions;

  static {
    SupportedExtensions = new HashSet<>();
    SupportedExtensions.add("jpg");
    SupportedExtensions.add("tif");
  }

  private final File dataFile;

  /** Grid information for the file. How the image is laid out on disk.*/
  private final ImageLayout imageLayout;

  /** The {@link CoordinateReferenceSystem} used in this raster */
  private final CoordinateReferenceSystem crs;

  /** Grid to world transformation (aka raster to vector r2v) */
  private MathTransform g2w;

  /** World to grid transformation (aka vector to raster v2r) */
  private MathTransform w2g;

  /**The underlying grid coverage for the raster file*/
  private GridCoverage2D coverage;

  /**Dimensions of the entire raster file regardless of how much of it is currently loaded*/
  private GridEnvelope gridRange;

  /**The rectangle that represents the current raster. If null, the raster represents the entire space*/
  private Rectangle rasterBounds;

  /**The underlying raster layer if a raster layer is currently loaded. Otherwise, <code>null</code>.*/
  private Raster raster;

  /**Number of bands (aka layers) in the raster file*/
  private int numBands;

  /**
   * Constructs a new RasterManager for the given file path. The given file
   * could be one of the following:
   * <ul>
   *   <li>A JPG file associated with a projection (.prj) and a world (.jgw)
   *   files with the same name and path.</li>
   *   <li>A TIF file associated with a projection (.prj) and a world (.tfw)
   *   files with the same name and path.</li>
   * </ul>
   * @param filePath
   * @throws IOException
   */
  public GeoToolsRasterManager(String filePath) throws IOException {
    this.dataFile = new File(filePath);
    if (!dataFile.exists())
      throw new IOException("File "+ dataFile +" not found");
    AbstractGridFormat format = GridFormatFinder.findFormat(dataFile);
    GridCoverage2DReader reader;
    if (format instanceof GeoTiffFormat) {
      //this is a bit hacky but does make more geotiffs work
      Hints hints = new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE);
      reader = format.getReader(dataFile, hints);
    } else {
      reader = format.getReader(dataFile);
    }

    this.crs = reader.getCoordinateReferenceSystem();
    this.imageLayout = reader.getImageLayout();
    this.gridRange = reader.getOriginalGridRange();
    this.coverage = reader.read(null);
    this.numBands = this.coverage.getNumSampleDimensions();

    // Extract world information from the file
    try {
      this.g2w = reader.getOriginalGridToWorld(PixelInCell.CELL_CENTER);
      this.w2g = g2w.inverse();
    } catch (org.opengis.referencing.operation.NoninvertibleTransformException e) {
      e.printStackTrace();
    }
  }

  /**
   * Returns the GridCoverage2D associated with this raster file. This can be used to process raster operations.
   * @return
   * @throws IOException
   */
  @Override
  public GridCoverage2D getGridCoverage() throws IOException {
    return coverage;
  }

  /**
   * Converts a point from the raster space (integer coordinates) to the vector space (double coordinates).
   * This function returns the coordinates of the center of the corresponding pixel. In other words, if a pixel maps
   * to a rectangle, this function returns the center of that rectangle. Notice that the geographic coordinate system
   * (projection) is not taken into account. The returned value is in whatever that coordinate system is.
   * @param x The index of the column of the pixel
   * @param y THe index of the row of the pixel.
   * @return
   */
  @Override
  public DirectPosition2D r2v(int x, int y, DirectPosition2D outPoint) throws TransformException {
    if (outPoint == null)
      outPoint = new DirectPosition2D();
    DirectPosition srcPoint = new DirectPosition2D(x+0.5f, y+0.5f);
    g2w.transform(srcPoint, (DirectPosition) outPoint);
    return outPoint;
  }

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
  @Override
  public DirectPosition2D v2r(DirectPosition2D inPoint, DirectPosition2D outPoint) throws TransformException {
    if (outPoint == null)
      outPoint = new DirectPosition2D();
    w2g.transform(inPoint, outPoint);
    // Convert to raster by rounding the number
    outPoint.x = Math.round(outPoint.x);
    outPoint.y = Math.round(outPoint.y);
    return outPoint;
  }

  /**
   * Returns the value of a single pixel at the given coordinate. If the passed <code>value</code> is null, a new
   * array is automatically created and returned.
   * @param column
   * @param row
   * @param value
   * @return
   */
  @Override
  public int[] getPixel(int column, int row, int[] value) {
    value = this.raster.getPixel(column, row, value);
    return value;
  }

  /***\
   * Returns all values of all pixels in the given block range. All values are returned in one array sorted row-wise.
   * If the raster layer contains more than one band, all the values of each pixel are stored consecutively. For
   * example, if the given range contains four pixels (two columns and two rows) and three bands, the values returned
   * are in this order: <br/>
   * (0, 0, 0), (0, 0, 1), (0, 0, 2),
   * (1, 0, 0), (1, 0, 1), (1, 0, 2),
   * (0, 1, 0), (0, 1, 1), (0, 1, 2),
   * (1, 1, 0), (1, 1, 1), (1, 1, 2) <br/>
   * where a values (x, y, b) means column <code>x</code>, row <code>y</code>, and band <code>b</code>.
   * @param column
   * @param row
   * @param width
   * @param height
   * @param values
   * @return
   */
  public int[] getPixels(int column, int row, int width, int height, int[] values) {
    return this.raster.getPixels(column, row, width, height, values);
  }

  @Override
  public void loadRaster(Rectangle rect) throws IOException {
    if (this.raster == null) {
      // No raster has been previously loaded. We have to load it.
      RenderedImage renderedImage = getGridCoverage().getRenderedImage();
      if (rect == null) {
        this.raster = renderedImage.getData();
        this.rasterBounds = null;
      } else {
        this.raster = renderedImage.getData(rect);
        this.rasterBounds = new Rectangle(rect);
      }
    } else {
      // An existing raster has been previously loaded. Check if it can be used.
      if (this.rasterBounds != null && (rect == null || !this.rasterBounds.contains(rect))) {
        // The existing raster does not cover the raster to be loaded
        RenderedImage renderedImage = getGridCoverage().getRenderedImage();
        this.raster = renderedImage.getData(rect);
        this.rasterBounds = rect == null? null : new Rectangle(rect);
      }
    }
  }

  public int getHeight() {
    return this.gridRange.getHigh(1) - this.gridRange.getLow(1) + 1;
  }

  public int getWidth() {
    return this.gridRange.getHigh(0) - this.gridRange.getLow(0) + 1;
  }

  public int getNumBands() {
    return numBands;
  }

  public int getTileWidth() { return imageLayout.getTileWidth(null); }
  public int getTileHeight() { return imageLayout.getTileHeight(null); }
  public int getTileX() { return imageLayout.getTileGridXOffset(null); }
  public int getTileY() { return imageLayout.getTileGridYOffset(null); }

  @Override
  public Geometry reproject(Geometry geom, CoordinateReferenceSystem geomCRS) throws TransformException {
    try {
      MathTransform mathTransform = CRS.findMathTransform(geomCRS, this.crs, true);
      return JTS.transform(geom, mathTransform);
    } catch (FactoryException e) {
      throw new RuntimeException(e);
    }
  }
}
