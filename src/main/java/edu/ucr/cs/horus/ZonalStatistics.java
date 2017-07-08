package edu.ucr.cs.horus;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import org.apache.commons.cli.*;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.geotools.geometry.DirectPosition2D;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.operation.TransformException;

import java.io.IOException;

/**
 * Created by Ahmed Eldawy on 5/1/17.
 */
public class ZonalStatistics {
  /**
   * Possible methods to run a zonal statistics query.
   */
  enum Method {Naive, QSplit, Scanline, Rasterize};

  public static Options getCommandLineOptions() {
    Options options = new Options();
    Option rinput = new Option("r", "raster", true, "raster file path");
    rinput.setRequired(true);
    options.addOption(rinput);
    Option vinput = new Option("v", "vector", true, "vector file path");
    vinput.setRequired(true);
    options.addOption(vinput);
    Option method = new Option("m", "method", true, "query method to use");
    method.setType(Method.class);
    options.addOption(method);
    Option query = new Option("q", "query", true, "query type to run");
    options.addOption(query);
    Option threshold = new Option("t", "threshold", true, "cost threshold for QSplit method");
    threshold.setType(Integer.class);
    options.addOption(threshold);
    return options;
  }

  public static void main(String[] args) throws IOException, TransformException {
    Options options = getCommandLineOptions();
    CommandLineParser parser = new DefaultParser();
    HelpFormatter formatter = new HelpFormatter();
    CommandLine cmd;

    try {
      cmd = parser.parse(options, args);
    } catch (ParseException e) {
      System.out.println(e.getMessage());
      formatter.printHelp("ZonalStatistics", options);

      System.exit(1);
      return;
    }

    String rasterFileName = cmd.getOptionValue('r');
    String vectorFileName = cmd.getOptionValue('v');
    String query = cmd.getOptionValue('q', "allpolys").toLowerCase();
    int threshold = Integer.parseInt(cmd.getOptionValue('t', "1000"));

    Method method = Method.valueOf(cmd.getOptionValue('m', "Scanline"));
    Clipper clipper;
    switch (method) {
      case Naive: clipper = new NaivePiPClipper(); break;
      case QSplit: clipper = new QSplitPiPClipper(); ((QSplitPiPClipper)clipper).setThreshold(threshold); break;
      case Rasterize: clipper = new RasterizeClipper(); break;
      case Scanline: clipper = new ScanClipper(); break;
      default: throw new RuntimeException("Unsupported method "+method);
    }


    if (query.equals("allpolys")) {
      long t0 = System.nanoTime();
      QueryProcessor.Pair<SimpleFeature[], Collector[]> stats = new QueryProcessor().stats(rasterFileName, vectorFileName, clipper);
      long t1 = System.nanoTime();
      long totalTime = t1 - t0;

      // A JSON mapper to create JSON objects
      ObjectMapper jsonMapper = new ObjectMapper();
      ArrayNode queryResults = formatResults(stats, jsonMapper);
      ObjectNode runtimeStats = formatRunningTime(method, totalTime, jsonMapper);
      ObjectNode response = jsonMapper.createObjectNode();
      response.put("queryResults", queryResults);
      response.put("runtimeStats", runtimeStats);

      System.out.println(jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
    } else if (query.equals("individuals")) {
      // Open raster file
      long t0 = System.nanoTime();
      RasterManager rasterManger = new GeoToolsRasterManager(rasterFileName);
      long t1 = System.nanoTime();
      long rasterOpenTime = t1 - t0;

      // Open vector file and retrieve all geometries
      t0 = System.nanoTime();
      VectorManager vectorManager = new VectorManager(vectorFileName);
      Geometry[] geoms =  vectorManager.getAllGeom();
      for (int iGeom = 0; iGeom < geoms.length; iGeom++) {
        geoms[iGeom] = rasterManger.reproject(geoms[iGeom], vectorManager.getCRS());
      }
      vectorManager.close();
      t1 = System.nanoTime();
      long vectorOpenTime = t1 - t0;

      // Process each polygon
      long[] times = new long[geoms.length];
      int[] pixels = new int[geoms.length];
      int[] segments = new int[geoms.length];
      Statistics[] stats = new Statistics[geoms.length];
      DirectPosition2D corner1 = new DirectPosition2D(), corner2 = new DirectPosition2D();
      DirectPosition2D pt1 = null, pt2 = null;
      for (int iGeom = 0; iGeom < geoms.length; iGeom++) {
        segments[iGeom] = geoms[iGeom].getNumPoints();
        Coordinate[] mbrCoords = geoms[iGeom].getEnvelope().getCoordinates();
        corner1.setLocation(mbrCoords[0].x, mbrCoords[0].y);
        corner2.setLocation(mbrCoords[2].x, mbrCoords[2].y);
        pt1 = rasterManger.v2r(corner1, pt1);
        pt2 = rasterManger.v2r(corner2, pt2);
        pixels[iGeom] = (int) Math.abs(pt2.x - pt1.x) * (int) Math.abs(pt2.y - pt1.y);
        t0 = System.nanoTime();
        stats[iGeom] = clipper.stats(geoms[iGeom], rasterManger, null);
        t1 = System.nanoTime();
        times[iGeom] = t1 - t0;
      }

      ObjectMapper jsonMapper = new ObjectMapper();
      ArrayNode response = jsonMapper.createArrayNode();
      for (int iGeom = 0; iGeom < geoms.length; iGeom++) {
        ArrayNode queryAnswer = formatResultJSON(stats[iGeom], jsonMapper);
        ObjectNode runtimeStats = jsonMapper.createObjectNode();
        runtimeStats.put("runtime", times[iGeom]/1E9);
        runtimeStats.put("pixels", pixels[iGeom]);
        runtimeStats.put("segments", segments[iGeom]);

        ObjectNode objectResult = jsonMapper.createObjectNode();
        objectResult.put("queryAnswer", queryAnswer);
        objectResult.put("runtimeStats", runtimeStats);
        response.add(objectResult);
      }
      System.out.println(jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
    } else {
      throw new RuntimeException("Unknown query '"+query+"'");
    }

  }

  /**
   * Format the running time as a JSON object
   * @param overallTime
   * @param jsonMapper
   * @return
   */
  private static ObjectNode formatRunningTime(Method method, long overallTime, ObjectMapper jsonMapper) {
    ObjectNode runtimeStats = jsonMapper.createObjectNode();
    runtimeStats.put("vectorReadTime", QueryProcessor.vectorReadingTime / 1E9);
    runtimeStats.put("rasterOpenTime", QueryProcessor.rasterOpenTime / 1E9);
    switch (method) {
      case Scanline:
        runtimeStats.put("intersectionTime", ScanClipper.intersectionTime / 1E9);
        runtimeStats.put("computationTime", ScanClipper.processingTime / 1E9);
        break;
      case QSplit:
        runtimeStats.put("splitTime", QSplitPiPClipper.splitTime / 1E9);
        runtimeStats.put("pipTime", NaivePiPClipper.pipTime / 1E9);
        runtimeStats.put("grabTime", NaivePiPClipper.grabTime / 1E9);
        break;
      case Rasterize:
        runtimeStats.put("clippingTime", RasterizeClipper.ClippingTime / 1E9);
        runtimeStats.put("processingTime", RasterizeClipper.ProcessingTime / 1E9);
    }
    runtimeStats.put("totalTime", overallTime / 1E9);
    return runtimeStats;
  }

  /**
   * Format the results as a JSON object
   * @param results
   * @param jsonMapper
   * @return
   */
  private static ArrayNode formatResults(QueryProcessor.Pair<SimpleFeature[], Collector[]> results, ObjectMapper jsonMapper) {
    // A JSON array that contains all results
    ArrayNode queryResults = jsonMapper.createArrayNode();
    for (int i = 0; i < results.first.length; i++) {
      ObjectNode objectAttrs = jsonMapper.createObjectNode();
      for (Property property : results.first[i].getProperties()) {
        objectAttrs.put(property.getName().toString(), property.getValue().toString());
      }

      ArrayNode objectResults = formatResultJSON(results.second[i], jsonMapper);

      ObjectNode entryResult = jsonMapper.createObjectNode();
      entryResult.put("objectAttributes", objectAttrs);
      entryResult.put("queryAnswer", objectResults);
      queryResults.add(entryResult);
    }
    return queryResults;
  }

  /**
   * Format the results of one geometry into JSON.
   * @param result
   * @param jsonMapper
   * @return
   */
  public static ArrayNode formatResultJSON(Collector result, ObjectMapper jsonMapper) {
    ArrayNode bandsResults = jsonMapper.createArrayNode();
    for (int iBand = 0; iBand < result.getNumBands(); iBand++) {
      ObjectNode objectResultsBand = jsonMapper.createObjectNode();
      if (result instanceof Statistics) {
        Statistics objectStatistics = (Statistics) result;
        objectResultsBand.put("sum", objectStatistics.sum[iBand]);
        objectResultsBand.put("count", objectStatistics.count[iBand]);
        objectResultsBand.put("min", objectStatistics.min[iBand]);
        objectResultsBand.put("max", objectStatistics.max[iBand]);
      }
      bandsResults.add(objectResultsBand);
    }
    return bandsResults;
  }
}
