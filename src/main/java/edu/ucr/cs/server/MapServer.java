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
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

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
			} else {
				if (target.equals("/"))
					target = "/index.html";
				tryToLoadStaticResource(target, response);
			}
		} catch (Exception e) {
			e.printStackTrace();
			reportError(response, "Error placing the request", e);
		}
	}
	
	 private void handleAggregateQuery(HttpServletRequest request,
		      HttpServletResponse response) throws ParseException, IOException {
		    try {
		      String west = request.getParameter("min_lon");
		      String east = request.getParameter("max_lon");
		      String south = request.getParameter("min_lat");
		      String north = request.getParameter("max_lat");
		      
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
