package edu.ucr.cs.horus;

/**
 * An interface for a process that collects pixel values from the raster layer and accumulates them.
 * Created by Ahmed Eldawy on 5/19/2017.
 */
public interface Collector {

  /**
   * Initializes the collector with the given number of bands.
   * @param n
   */
  void setNumBands(int n);

  /**
   * Collects a value of the given location which has multiple bands.
   * @param column
   * @param row
   * @param value
   */
  void collect(int column, int row, int[] value);

  /**
   * Collects all values in a given block range. The array values is assumed to contain consecutive values for each
   * band in each pixel stored row-wise. In other words, the first N entries in the array <code>values</code> are used
   * where N = width * height * number of bands.
   * @param column
   * @param row
   * @param width
   * @param height
   * @param values
   */
  void collect(int column, int row, int width, int height, int[] values);

  /**
   * Accumulates the value of this collector with another one. Helpful for combining several partial results.
   * @param c
   */
  void accumulate(Collector c);

  /**
   * Return the total number of bands
   * @return
   */
  int getNumBands();

  /**
   * Make the value of this collector invalid to indicate an error in processing
   */
  void invalidate();

  /**Whether the value of the collector is valid or not*/
  boolean isValid();
}
