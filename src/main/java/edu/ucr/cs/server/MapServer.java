package edu.ucr.cs.server;


import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.ajax.json.JSONObject;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.geotools.function.Getgeom;
import org.geotools.getName.GetFileName;


import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.simplify.TopologyPreservingSimplifier;



public class MapServer extends AbstractHandler {
	private static final Log LOG = LogFactory.getLog(MapServer.class);

	private static void startServer() throws Exception {
		int port = 8888;

		Server server = new Server(port);
		server.setHandler(new MapServer());
		server.start();
		server.join();
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		// Bypass cross-site scripting (XSS)
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.addHeader("Access-Control-Allow-Credentials", "true");
		((Request) request).setHandled(true);

		try {
			LOG.info("Received request: '"+request.getRequestURL()+"'");
			if (target.endsWith("/aggregate_query.cgi")) {
				// TODO handle request
				LOG.info("Received query request: "+target);
				handleAggregateQuery(request, response);
			}
			else if(target.endsWith("/name_query.cgi")){
				// TODO handle request
				LOG.info("Received query request: "+target);
				handleNameQuery(request, response);
				
			}
			else if(target.endsWith("/data_query.cgi")){
				// TODO handle request
				LOG.info("Received query request: "+target);
				handleNameQuery(request, response);
				
			}
			else if(target.endsWith("/first_query.cgi")){
				// TODO handle request
				LOG.info("Received query request: "+target);
				handleFirstQuery(request,response);
				
			}
			else {
				if (target.equals("/"))
					target = "/index.html";
				tryToLoadStaticResource(target, response);
			}
		} catch (Exception e) {
			e.printStackTrace();
			reportError(response, "Error placing the request", e);
		}
	}
	
	 private void handleNameQuery(HttpServletRequest request,
		      HttpServletResponse response) throws ParseException, IOException {
		    try {
		       String name = request.getParameter("chooseName");
		       String aNorth = request.getParameter("aNorth");
		       String aEast = request.getParameter("aEast");
		       String aSouth = request.getParameter("aSouth");
		       String aWest = request.getParameter("aWest");
		       String rWidth = request.getParameter("rWidth");
		       String rHeight = request.getParameter("rHeight");
		       String countResponse = request.getParameter("countResponse");
		       String firstLoad = request.getParameter("firstLoad");
		       System.out.println(countResponse);
		       //parse the number
		       float North = Float.parseFloat(aNorth);
		       float East = Float.parseFloat(aEast);
		       float South = Float.parseFloat(aSouth);
		       float West = Float.parseFloat(aWest);
		       float Width = Float.parseFloat(rWidth);
		       float Height = Float.parseFloat(rHeight);
		       float number = Float.parseFloat("10.0");
		       float calculateWidth = (East - West)/number;
		       float calculateHeight = (North - South)/number;
		       float pixelWidth =  (East - West)/Width;
		       float pixelHeight = (North - South)/Height;
		       float minimum = Math.min(pixelWidth,pixelHeight);
		       //minimum *= 64;
		       
		        /*
  google.maps.event.addListener(map, 'click', function(event) {
    // Move the selection rectangle in the clicked location
    lat = event.latLng.lat();
    lng = event.latLng.lng();
  
    var ne = map.getBounds().getNorthEast();
    var sw = map.getBounds().getSouthWest();
    var width = (ne.lng() - sw.lng()) / 10.0;
    var height = (ne.lat() - sw.lat()) / 10.0;
    bounds = new google.maps.LatLngBounds(
      new google.maps.LatLng(lat - height, lng - width),
      new google.maps.LatLng(lat + height, lng + width));
    
    //MoveRectangle(bounds);
  });
	aNorth  =   map.getBounds().getNorthEast().lat();   
    aEast   =   map.getBounds().getNorthEast().lng();
    aSouth  =   map.getBounds().getSouthWest().lat();   
    aWest   =   map.getBounds().getSouthWest().lng(); 

  */
		       System.out.println("country name "+ name);
		       System.out.println("minimum value " + minimum);
		       System.out.println(North);
		       //get the polygon
		       //Geometry geometry=Getgeom.getgeom(name, GetFileName.vectorfoldpath+"/boundaries.shp", "NAME");
		       //Geometry geometry=Getgeom.getgeom("China", GetFileName.vectorfoldpath+"/boundaries.shp", "CNTRY_NAME");
		      

		       long startTime = System.currentTimeMillis();

		       Geometry geometry=Getgeom.getgeom(name, GetFileName.vectorfoldpath+"/boundaries.shp", "CNTRY_NAME");
		      
			   long endTime = System.currentTimeMillis();
			   long duration=endTime-startTime;
			   
			   //this function will return double 
			   System.out.println("Area of the geometry" + geometry.getArea());
			   System.out.println("time it take to read the polygon from the shapefile" + duration);
		     
		       //x-coordinate is the longitude and the y-coordinate is the latitude.
			   //this part create a rectangle and than intersect it with the whole geometry and then simplify it 
			   PrintWriter writer = response.getWriter();
		       JSONObject json = new JSONObject(); 
		       
		       System.out.println(firstLoad);
			   if(firstLoad.equals("true")){
				   
			       Geometry firstSimple = TopologyPreservingSimplifier.simplify(geometry, 0.1);
			       Envelope envelopForBound = firstSimple.getEnvelopeInternal() ;
			       System.out.println("get the area for the envelop "+envelopForBound);
			       
			       json.put("MinX", envelopForBound.getMinX());
			       json.put("MaxX", envelopForBound.getMaxX());
			       json.put("MinY", envelopForBound.getMinY());
			       json.put("MaxY", envelopForBound.getMaxY());
			       json.put("geometry",firstSimple);
			       
			   }else{
				   //this is for the case that the country is not first load
				   long startTime1 = System.currentTimeMillis();
			       GeometryFactory geomFact =  new GeometryFactory();
			       Coordinate[] coordinates = new Coordinate[5];
			       coordinates[0] = new Coordinate(West - calculateWidth, South - calculateHeight);
			       coordinates[1] = new Coordinate(West - calculateWidth, North + calculateHeight);
			       coordinates[2] = new Coordinate(East + calculateWidth, North + calculateHeight);
			       coordinates[3] = new Coordinate(East + calculateWidth, South - calculateHeight);
			       coordinates[4] = new Coordinate(West - calculateWidth, South - calculateHeight);
			       LinearRing lr = geomFact.createLinearRing(coordinates);
			       Polygon rectangleBound = geomFact.createPolygon(lr, new LinearRing[]{}); 
			       // System.out.println("Area of the square "+rectangleBound.getArea());
			       Geometry intersection = rectangleBound.intersection(geometry);
			       System.out.println("Area of the intersection "+intersection.getArea());
			       Geometry simple = TopologyPreservingSimplifier.simplify(intersection, minimum);
			       long endTime1 = System.currentTimeMillis();
			       long duration1=endTime1-startTime1;
			       System.out.println("time to simplify the polygon" + duration1);
			       System.out.println("Area of the simply geometry "+simple.getArea());
			       if(simple.isEmpty()){
			    	   json.put("geometry",0);
			    	   //writer.print("\"points\": "+0);
			    	   //writer.print("\"geometry\": "+0+',');
			    	   //writer.write('0');
			       }else{
			    	   json.put("geometry",simple);
			    	   //writer.print("\"points\": "+simple.toText());
			    	   //writer.print("\"geometry\": "+simple.toText()+',');
			    	   //writer.write(simple.toText());
			       }
			   }
		      
		     
		       //double Minx = 
		       
		       //write the final result to the font end
		      
		       //json.put("boundO",envelopForBound.toText());
		       /*
		       Coordinate[] coor=envelopForBound.getCoordinates();
			   for (int i=0;i<coor.length;i++){
					//System.out.println(coor[i].x+"/"+coor[i].y);
					json.put("corx"+i,coor[i].x);
					json.put("cory"+i,coor[i].y);
			   }*/
		       /*
		       writer.print("{");
			      writer.print("\"results\":{");
		          writer.print("\"min\": "+342+',');
		          writer.print("\"max\": "+500+',');
		          writer.print("\"count\": "+10+',');
		          writer.print("\"sum\": "+1000);
			      writer.print("},");
			      writer.print("\"stats\":{");
			      writer.print("\"totaltime\":"+(t2-t1)+',');
			      writer.print("\"num-of-temporal-partitions\":"+5+',');
			      writer.print("\"num-of-trees\":"+20);
			      writer.print("}");
			      writer.print("}");*/
			      
			   //writer.print("{");
			  // writer.print("\"geometry\":{");
		     
		       //write.write(simple.toText());
		       /*
		       writer.print("},");
		       writer.print("{");
		       writer.print("\"countReceive\": "+ countResponse);
		       writer.print("}");*/
		       json.put("countReceive", countResponse);
		       //System.out.println(json.toString());
		       writer.write(json.toString());
			   writer.flush();
		       writer.close();
		  
		    } catch (Exception e) {
			      response.setContentType("text/plain;charset=utf-8");
			      PrintWriter writer = response.getWriter();
			      e.printStackTrace(writer);
			      writer.close();
			      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			    }
		   
			  }
	 private void handleFirstQuery(HttpServletRequest request,
		      HttpServletResponse response) throws ParseException, IOException {
		    try {
		    	 String name = request.getParameter("chooseName");
			    
			       Geometry geometry=Getgeom.getgeom(name, GetFileName.vectorfoldpath+"/boundaries.shp", "CNTRY_NAME");
			     
				   PrintWriter writer = response.getWriter();
			       JSONObject json = new JSONObject();
			       
			       json.put("geometry", geometry);
			      
			       Envelope envelopForBound = geometry.getEnvelopeInternal();
			       System.out.println("get the area for the envelop "+envelopForBound);
			       System.out.println("this is first query");
			       
			       json.put("Minx", envelopForBound.getMinX());
			       json.put("MaxX", envelopForBound.getMaxX());
			       json.put("MinY", envelopForBound.getMinY());
			       json.put("MaxY", envelopForBound.getMaxY());
			  
			       writer.write(json.toString());
				   writer.flush();
			       writer.close();
		  
		    } catch (Exception e) {
			      response.setContentType("text/plain;charset=utf-8");
			      PrintWriter writer = response.getWriter();
			      e.printStackTrace(writer);
			      writer.close();
			      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			    }
		   
			  }
	 /*
	 private void handleDataQuery(HttpServletRequest request,
		      HttpServletResponse response) throws ParseException, IOException {
		    try {
		      String name = request.getParameter("chooseName");
		       System.out.println(name);
		     
				
		  
		    } catch (Exception e) {
			      response.setContentType("text/plain;charset=utf-8");
			      PrintWriter writer = response.getWriter();
			      e.printStackTrace(writer);
			      writer.close();
			      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			    }
		   
			  }*/
	private void handleAggregateQuery(HttpServletRequest request,
		      HttpServletResponse response) throws ParseException, IOException {
		    try {
		      String west = request.getParameter("min_lon");
		      String east = request.getParameter("max_lon");
		      String south = request.getParameter("min_lat");
		      String north = request.getParameter("max_lat");
		      String name = request.getParameter("query_name");
		      String[] startDateParts = request.getParameter("fromDate").split("/");
		      String startDate = startDateParts[2] + '.' + startDateParts[0] + '.' + startDateParts[1];
		      String[] endDateParts = request.getParameter("toDate").split("/");
		      String endDate = endDateParts[2] + '.' + endDateParts[0] + '.' + endDateParts[1];
		      LOG.info("Date range "+startDate+", "+endDate);
		      
		      
		      // Create the query parameters
		      
		      long t1 = System.currentTimeMillis();
		      Thread.sleep(100); // As if I'm doing some processing
		      long t2 = System.currentTimeMillis();
		      // Report the answer and time
		      response.setContentType("application/json;charset=utf-8");
		      PrintWriter writer = response.getWriter();
		      writer.print("{");
		      writer.print("\"results\":{");
	          writer.print("\"min\": "+342+',');
	          writer.print("\"max\": "+500+',');
	          writer.print("\"count\": "+10+',');
	          writer.print("\"sum\": "+1000);
		      writer.print("},");
		      writer.print("\"stats\":{");
		      writer.print("\"totaltime\":"+(t2-t1)+',');
		      writer.print("\"num-of-temporal-partitions\":"+5+',');
		      writer.print("\"num-of-trees\":"+20);
		      writer.print("}");
		      writer.print("}");
		      writer.close();
		      response.setStatus(HttpServletResponse.SC_OK);
		    } catch (Exception e) {
		      response.setContentType("text/plain;charset=utf-8");
		      PrintWriter writer = response.getWriter();
		      e.printStackTrace(writer);
		      writer.close();
		      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		    }
		  }
	/**
	 * Tries to load the given resource name from class path if it exists.
	 * Used to serve static files such as HTML pages, images and JavaScript files.
	 * @param target
	 * @param response
	 * @throws IOException
	 */
	private void tryToLoadStaticResource(String target,
			HttpServletResponse response) throws IOException {
		LOG.info("Loading resource "+target);
		// Try to load this resource as a static page
		InputStream resource =
				getClass().getResourceAsStream("/webapps/static/shahedfrontend"+target);
		if (resource == null) {
			reportError(response, "Cannot load resource '"+target+"'", null);
			return;
		}
		byte[] buffer = new byte[1024*1024];
		ServletOutputStream outResponse = response.getOutputStream();
		int size;
		while ((size = resource.read(buffer)) != -1) {
			outResponse.write(buffer, 0, size);
		}
		resource.close();
		outResponse.close();
		response.setStatus(HttpServletResponse.SC_OK);
		if (target.endsWith(".js")) {
			response.setContentType("application/javascript");
		} else if (target.endsWith(".css")) {
			response.setContentType("text/css");
		} else {
			response.setContentType(URLConnection.guessContentTypeFromName(target));
		}
		final DateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss ZZZ");
		final long year = 1000L * 60 * 60 * 24 * 365;
		// Expires in a year
		response.addHeader("Expires", format.format(new Date().getTime() + year));
	}



	private void reportError(HttpServletResponse response, String msg,
			Exception e)
					throws IOException {
		if (e != null)
			e.printStackTrace();
		LOG.error(msg);
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		response.getWriter().println("{\"message\": '"+msg+"',");
		if (e != null) {
			response.getWriter().println("\"error\": '"+e.getMessage()+"',");
			response.getWriter().println("\"stacktrace\": [");
			for (StackTraceElement trc : e.getStackTrace()) {
				response.getWriter().println("'"+trc.toString()+"',");
			}
			response.getWriter().println("]");
		}
		response.getWriter().println("}");
	}


	public static void main(String[] args) throws Exception {
		startServer();
	}

}
