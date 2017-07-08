package edu.ucr.cs.horus;

import com.vividsolutions.jts.geom.Geometry;

import java.io.IOException;

/**
 * An abstract class for the clipping functions between vector and rasters. There
 * are different concrete implementations that are all supposed to returns the same
 * results. The performance might be different based on the algorithm applied
 * by each implementation.
 * Created by Ahmed Eldawy on 5/2/17.
 */
public abstract class Clipper {

  /**
   * Clips the raster file based on the given array of geometries and counts number of pixels
   * inside each geometry. This function assumes that both the polygon and the raster file are in the same coordinate
   * reference system (CRS).
   *
   * @param geometries An array of all the geometries to be processed
   * @param raster the raster file to be processed
   * @return
   */
  public long[] countPixels(Geometry[] geometries, RasterManager raster) throws IOException {
    long[] counts = new long[geometries.length];
    Statistics[] stats = stats(geometries, raster, null);
    for (int i = 0; i < stats.length; i++)
      counts[i] = stats[i].count[0];
    return counts;
  }

  /**
   * Count the number of pixels in the given geometry. It assumes that both the geometry and the raster file
   * are in the same coordinate reference system (CRS).
   * @param geometry The geometry to be processed
   * @param raster The raster file from which pixels are counted
   * @return
   * @throws IOException
   */
  public long countPixels(Geometry geometry, RasterManager raster) throws IOException {
    return countPixels(new Geometry[] {geometry}, raster)[0];
  }

  /**
   * Computes simple {@link Statistics} for all pixels covered by the given array of geometries over the raster file.
   * An object of {@link Statistics} is returned for each geometry in the given array.
   * @param geometries The list of geometries oto be processed.
   * @param raster The raster file from which values are read.
   * @param statistics (Optional) An array of statistics objects to write the results to. This function does not
   *                   initialize any given statistics object. This can be helpful of accumulating over multiple
   *                   geometries or multiple raster files. If the given array is null an array is initialized with
   *                   the size of the geometries object. If one of the statistics objects is null, it is initialized.
   * @return
   */
  public Statistics[] stats(Geometry[] geometries, RasterManager raster, Statistics[] statistics) {
    if (statistics == null)
      statistics = new Statistics[geometries.length];
    for (int i = 0; i < geometries.length; i++) {
      if (statistics[i] == null) {
        statistics[i] = new Statistics();
        statistics[i].setNumBands(raster.getNumBands());
      }
    }
    process(geometries, raster, statistics);
    return statistics;
  }

  /**
   * Similar to {@link #stats(Geometry[], RasterManager, Statistics[])} but works for one geometry only.
   * @param geometry The geometry to be used as a filter
   * @param raster The raster file from which values are read.
   * @param statistics (Optional) Either an initialized statistics object, or <code>null</code>. If <code>null</code>
   *                   is passed, a new object is created and returned. Otherwise, the statistics of the given
   *                   geometry will be accumulated to the given object.
   * @return Either the given {@link Statistics} object if not <code>null</code>; otherwise, a newly initialized and
   * processed {@link Statistics} object.
   */
  public Statistics stats(Geometry geometry, RasterManager raster, Statistics statistics) {
    return stats(new Geometry[] {geometry}, raster, new Statistics[] {statistics})[0];
  }

  /**
   * Computes the histograms for each geometry in the given array of geometries. A {@link Histogram} records the number
   * of occurrences for each pixel value for each band.
   * @param geometries The array of geometries to be processed.
   * @param raster The raster file from which the values are read.
   * @param histograms (Optional) an existing array of {@link Histogram}s to be used. If <code>null</code> is passed,
   *                   a new array is created and initialized.
   * @return Either the passed array of histograms, or a newly created and processed array of histograms.
   */
  public Histogram[] histogram(Geometry[] geometries, RasterManager raster, Histogram[] histograms) {
    if (histograms == null)
      histograms = new Histogram[geometries.length];
    for (int i = 0; i < histograms.length; i++) {
      if (histograms[i] == null) {
        histograms[i] = new Histogram();
        histograms[i].setNumBands(raster.getNumBands());
      }
    }
    process(geometries, raster, histograms);
    return histograms;
  }

  /**
   * Similar to {@link #histogram(Geometry[], RasterManager, Histogram[])} but for one geometry only.
   * @param geometry  The geometry to be processed
   * @param raster The raster file from which values are read.
   * @param histogram (Optional) Either an existing {@link Histogram} object, or <code>null</code>. If <code>null</code>
   *                  is passed, a new object is created and used.
   * @return
   */
  public Histogram histogram(Geometry geometry, RasterManager raster, Histogram histogram) {
    return histogram(new Geometry[] {geometry}, raster, new Histogram[] {histogram})[0];
  }

    /**
     * An abstract method that performs the actual processing. It finds all the pixels inside the given geometry
     * and collects them using the given {@link Collector}.
     * @param geometry
     * @param raster
     * @param result
     * @return
     */
  protected Collector process(Geometry geometry, RasterManager raster, Collector result) {
    return process(new Geometry[] {geometry}, raster, new Collector[] {result})[0];
  }


  /**
   * Processes an array of geometries at once and returns the results for all of them.
   * The default implementation will loop over all the geometries and process them one by one using #process
   * @param geometries
   * @param raster
   * @param results
   * @return
   */
  protected abstract Collector[] process(Geometry geometries[], RasterManager raster, Collector[] results);
}
