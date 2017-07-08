package edu.ucr.cs.horus;

import com.vividsolutions.jts.geom.Geometry;
import org.geotools.coverage.processing.CoverageProcessor;
import org.opengis.coverage.Coverage;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import javax.sound.sampled.Clip;
import java.awt.*;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.io.IOException;

/**
 * Performs clipped aggregation query by rasterizing the polygon and matching
 * it with the raster file.
 * Created by Ahmed Eldawy on 4/27/17.
 */
public class RasterizeClipper extends Clipper {

  /**Total time for rasterizing the polygons*/
  public static long ClippingTime;
  /**Total time for processing the resulting layer*/
  public static long ProcessingTime;

  public RasterizeClipper(){
  }

  @Override
  protected Collector process(Geometry geometry, RasterManager raster, Collector result) {
    if (!geometry.isValid()) {
      result.invalidate();
      return result;
    }
    try {
      long t0 = System.nanoTime();
      Coverage coverage = null;
      coverage = raster.getGridCoverage();
      CoverageProcessor processor = new CoverageProcessor();
      ParameterValueGroup params = processor.getOperation("CoverageCrop")
          .getParameters();
      params.parameter("Source").setValue(coverage);
      params.parameter("ROI").setValue(geometry);
      params.parameter("ForceMosaic").setValue(true);
      Coverage clippedCoverage = processor.doOperation(params);
      long t1 = System.nanoTime();
      ClippingTime += t1 - t0;

      t0 = System.nanoTime();
      RenderableImage img = clippedCoverage.getRenderableImage(0, 1);
      Raster rimg = img.createDefaultRendering().getData();
      int[] value = null;
      for (int x = rimg.getMinX(); x < rimg.getMinX() + rimg.getWidth(); x++)
        for (int y = rimg.getMinY(); y < rimg.getMinY() + rimg.getHeight(); y++) {
          value = rimg.getPixel(x, y, value);
          result.collect(x, y, value);
        }
      t1 = System.nanoTime();
      ProcessingTime += t1 - t0;
      return result;
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (Exception e) {
      e.printStackTrace();
      result.invalidate();
      return result;
    }
  }

  @Override
  protected Collector[] process(Geometry[] geometries, RasterManager raster, Collector[] results) {
    for (int iGeom = 0; iGeom < geometries.length; iGeom++) {
      results[iGeom] = this.process(geometries[iGeom], raster, results[iGeom]);
    }
    return results;
  }
}
