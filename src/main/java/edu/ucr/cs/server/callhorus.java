package edu.ucr.cs.server;

import java.io.IOException;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.function.Getgeom;
import org.geotools.getName.GetFileName;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Geometry;

import edu.ucr.cs.horus.Clipper;
import edu.ucr.cs.horus.Collector;
import edu.ucr.cs.horus.GeoToolsRasterManager;
import edu.ucr.cs.horus.QueryProcessor;
import edu.ucr.cs.horus.RasterManager;
import edu.ucr.cs.horus.ScanClipper;
import edu.ucr.cs.horus.Statistics;
import edu.ucr.cs.horus.VectorManager;

public class callhorus {
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
		        //System.out.println(objectResultsBand[sum]);
		      }
		      bandsResults.add(objectResultsBand);
		    }
		    return bandsResults;
		  }
		
	public static void main(String[] args) throws CQLException, IOException{
		 //Geometry geometry=Getgeom.getgeom(name, GetFileName.vectorfoldpath+"/boundaries.shp", "CNTRY_NAME");
		 RasterManager rasterManger = new GeoToolsRasterManager(GetFileName.rasterfoldpath+"/asd.tif");
		 VectorManager vectorManager = new VectorManager(GetFileName.vectorfoldpath+"/boundaries.shp");
	     Geometry geometry =  vectorManager.getGeom("CNTRY_NAME","Japan");
	     //System.out.println(geometry.toText());
	     Clipper clipper = new ScanClipper();
	     QueryProcessor.Pair<SimpleFeature[], Collector[]> stats = new QueryProcessor().stats(GetFileName.rasterfoldpath+"/asd.tif", GetFileName.vectorfoldpath+"/countries.shp", clipper);
	     SimpleFeature[] first = stats.first;
	     Collector[] second = stats.second;
	     ObjectMapper jsonMapper = new ObjectMapper();
	     ArrayNode queryResults = formatResults(stats, jsonMapper);
	     System.out.println(queryResults.size());
	     JsonNode jsonNode = queryResults.get(1).get("queryAnswer").get(0);
	     System.out.println(jsonNode);
	     //float sum = queryResults["sum"];
	     //System.out.println(queryResults.toString());
	     
	      
	   
	}

}





