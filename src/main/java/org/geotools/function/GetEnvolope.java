package org.geotools.function;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

public class GetEnvolope {
	public static Envelope getenvelope(Geometry geom) {
		Envelope env = null;
		if (env == null)
			env = geom.getEnvelopeInternal();
		return env;
	}
}
