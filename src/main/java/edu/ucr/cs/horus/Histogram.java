package edu.ucr.cs.horus;

/**
 * Computes the histogram of a given raster file.
 * Created by aseldawy on 5/19/2017.
 */
public class Histogram implements Collector {
  public long[][] histogram;

  /**A flag that is set when the value of the histogram becomes invalid*/
  private boolean invalid;

  @Override
  public void setNumBands(int n) {
    this.histogram = new long[n][256];
  }

  @Override
  public void collect(int column, int row, int[] value) {
    for (int i = 0; i < value.length; i++)
      histogram[i][value[i]]++;
  }

  @Override
  public void collect(int column, int row, int width, int height, int[] values) {
    int numOfDataElements = width * height * this.histogram.length;
    for (int i = 0; i < numOfDataElements; i++) {
      int j = i % this.histogram.length;
      histogram[j][values[i]]++;
    }
  }

  @Override
  public void accumulate(Collector c) {
    Histogram h = (Histogram) c;
    for (int band = 0; band < histogram.length; band++) {
      for (int value = 0; value < histogram[band].length; value++) {
        this.histogram[band][value] += h.histogram[band][value];
      }
    }
    this.invalid = this.invalid || h.invalid;
  }

  @Override
  public int getNumBands() {
    return this.histogram.length;
  }

  @Override
  public void invalidate() {
    this.invalid = true;
  }

  @Override
  public boolean isValid() {
    return !this.invalid;
  }
}
