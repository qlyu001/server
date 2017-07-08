package edu.ucr.cs.horus;

/**
 * A class that stores simple statistics to be computed on raster files. More specifically, it contains the following
 * four values:
 * <ul>
 *   <li>Sum: The sum of all values</li>
 *   <li>Count: The number of elements</li>
 *   <li>Min: The minimum value</li>
 *   <li>Max: The maximum value</li>
 * </ul>
 * Created by Ahmed Eldawy on 5/16/2017.
 */
public class Statistics implements Collector {
  /**The summation of all values represented by this object*/
  public long[] sum;

  /**The number of elements represented by this object*/
  public long[] count;

  /**The minimum value represented by this object*/
  public int[] max;

  /**The maximum value represented by this object*/
  public int[] min;

  /**A flag that is set when the value becomes invalid*/
  private boolean invalid;

  @Override
  public String toString() {
    return String.format("{sum: %d, count: %d, max: %d, min: %d}", sum[0], count[0], max[0], min[0]);
  }

  @Override
  public void setNumBands(int n) {
    this.sum = new long[n];
    this.min = new int[n];
    this.max = new int[n];
    this.count = new long[n];
  }

  @Override
  public void collect(int column, int row, int[] value) {
    for (int i = 0; i < value.length; i++) {
      if (value[i] < min[i])
        min[i] = value[i];
      if (value[i] > max[i])
        max[i] = value[i];
      sum[i] += value[i];
      count[i]++;
    }
  }

  @Override
  public void collect(int column, int row, int width, int height, int[] values) {
    int numOfDataValues = min.length * width * height;
    for (int i = 0; i < numOfDataValues; i++) {
      int j = i % this.sum.length;
      if (values[i] < min[j])
        min[j] = values[i];
      if (values[i] > max[j])
        max[j] = values[i];
      sum[j] += values[i];
    }
    for (int j = 0; j < this.count.length; j++)
      count[j] += width * height;
  }

  @Override
  public void accumulate(Collector c) {
    Statistics s = (Statistics) c;
    for (int i = 0; i < sum.length; i++) {
      this.sum[i] += s.sum[i];
      this.count[i] += s.count[i];
      if (s.max[i] > this.max[i])
        this.max[i] = s.max[i];
      if (s.min[i] < this.min[i])
        this.min[i] = s.min[i];
    }
    this.invalid = this.invalid || s.invalid;
  }

  @Override
  public int getNumBands() {
    return sum.length;
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
