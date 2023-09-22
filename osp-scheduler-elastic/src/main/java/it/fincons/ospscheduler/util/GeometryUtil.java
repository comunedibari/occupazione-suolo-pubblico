package it.fincons.ospscheduler.util;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTWriter;

public class GeometryUtil {

    private static WKTWriter wKTWriter = new WKTWriter();

    public static String geometryToWKTFormat(Geometry geometry) {
        String output = null;

        if(geometry!=null){
            output = wKTWriter.writeFormatted(geometry);
        }

        return output;
    }

}    
