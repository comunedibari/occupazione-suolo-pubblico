package it.fincons.osp.utils;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import it.fincons.osp.dto.GeoMultiPointDTO;
import it.fincons.osp.dto.GeoPointDTO;
import it.fincons.osp.exceptions.BusinessException;
import it.fincons.osp.exceptions.ErrorCode;

public class GeometryUtil {
    
    public static final int LATLNG_SRID = 4326; //LatLng
    private static WKTReader wktReader = new WKTReader();
    
    private static Geometry wktToGeometry(String wellKnownText) {
        Geometry geometry = null;
        
        try {
            geometry = wktReader.read(wellKnownText);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return geometry;
    }

    public static Point parseGeoLocation(GeoPointDTO point) {
        Geometry geometry = GeometryUtil.wktToGeometry(String.format("POINT (%s %s)", point.getLon(), point.getLat()));
        Point ret =(Point)geometry;
        ret.setSRID(LATLNG_SRID); 
        return ret;
    }

    public static Polygon parseGeoPolygon(GeoMultiPointDTO polygon) throws IllegalArgumentException{
        if (polygon.getPoints() == null || polygon.getPoints().size() < 3) {
            throw new IllegalArgumentException("Not enought points");
        }
        Coordinate[] ac = geoDTOtoCoordinates(polygon);
        
        Polygon ret;
        
        try {
        	ret = new GeometryFactory().createPolygon(ac);
		} catch (IllegalArgumentException e) {
			throw new BusinessException(ErrorCode.E18,
					"Le coordinate geografiche inserite non sono valide in quanto rappresentano un poligono non chiuso");
		}
        
        ret.setSRID(LATLNG_SRID); 
        return ret;
    }

	public static LineString parseGeoLine(GeoMultiPointDTO line) throws IllegalArgumentException {
        if (line.getPoints() == null || line.getPoints().size() != 2) {
            throw new IllegalArgumentException("Not a line");
        }
        Coordinate[] ac = geoDTOtoCoordinates(line);
        LineString ret = new GeometryFactory().createLineString(ac);
        ret.setSRID(LATLNG_SRID); 
        return ret;
	}
	
    public static Geometry parseGeoBrokenLineOrPolygon(GeoMultiPointDTO brokenLineOrPolygon) throws IllegalArgumentException{
        if (brokenLineOrPolygon.getPoints() == null || brokenLineOrPolygon.getPoints().size() < 3) {
            throw new IllegalArgumentException("Not enought points");
        }
        Coordinate[] ac = geoDTOtoCoordinates(brokenLineOrPolygon);
        
        try {
        	Polygon ret = new GeometryFactory().createPolygon(ac);
        	ret.setSRID(LATLNG_SRID); 
        	return ret;
		} catch (IllegalArgumentException e) {
			// se va in errore vuol dire che non e' un poligono chiuso, ma una linea composta da più punti
			LineString ret = new GeometryFactory().createLineString(ac);
			ret.setSRID(LATLNG_SRID); 
        	return ret;
		}
    }

	public static Geometry parseGeoGeomerty(GeoMultiPointDTO multipoint) throws IllegalArgumentException {
		Geometry ret = null;
		if (multipoint != null) {
			if (multipoint.getPoints() == null) {
				throw new IllegalArgumentException("Empty points");
			}
			if (multipoint.getPoints().size() == 1) {
				ret = parseGeoLocation(multipoint.getPoints().get(0));
			}
			else if (multipoint.getPoints().size() == 2) {
				ret = parseGeoLine(multipoint);
			}
			else {
				ret = parseGeoBrokenLineOrPolygon(multipoint);
			}
		}
		return ret;
	}

	public static MultiPoint parseGeoMultiPoint(GeoMultiPointDTO multiPoint) {

		if (multiPoint == null || multiPoint.getPoints().isEmpty()) {
			return null;
		}

		// conversione oggetti coordinate
		Coordinate[] ac = geoDTOtoCoordinates(multiPoint);

		// in caso le coordinate rappresentino un poligono, controllo che formino un
		// poligono chiuso
		if (multiPoint.getPoints().size() >= 3) {
			try {
				new GeometryFactory().createPolygon(ac);
			} catch (IllegalArgumentException e) {
				throw new BusinessException(ErrorCode.E18,
						"Le coordinate geografiche inserite non sono valide in quanto rappresentano un poligono non chiuso");
			}
		}

		// creazione oggetto MultiPoint
		MultiPoint ret = new GeometryFactory().createMultiPointFromCoords(ac);
		ret.setSRID(LATLNG_SRID);

		return ret;
	}
	
	public static GeoMultiPointDTO parseMultiPointToDto(MultiPoint multiPoint) {

		if (multiPoint == null || multiPoint.getCoordinates().length == 0) {
			return null;
		}

		GeoMultiPointDTO geoMultiPoint = new GeoMultiPointDTO();
		geoMultiPoint.setPoints(coordinatesToGeoDTO(multiPoint.getCoordinates()));

		return geoMultiPoint;
	}

	public static GeoMultiPointDTO parseGeometryToDto(Geometry geometry) {
		GeoMultiPointDTO ret = null;
		
		if (geometry != null) {
			ret = new GeoMultiPointDTO();
			ret.setPoints(coordinatesToGeoDTO(geometry.getCoordinates()));
		}
		return ret;
	}

	private static Coordinate[] geoDTOtoCoordinates(GeoMultiPointDTO dto) {
		Coordinate[] ac = new Coordinate[dto.getPoints().size()];
		for (int i = 0; i < dto.getPoints().size(); i++) {
			ac[i] = new Coordinate(dto.getPoints().get(i).getLon(), dto.getPoints().get(i).getLat());
		}
		return ac;
	}

	private static List<GeoPointDTO> coordinatesToGeoDTO(Coordinate[] coordinates) {
		List<GeoPointDTO> points = new ArrayList<>();
		for (int i = 0; i < coordinates.length; i++) {
			points.add(new GeoPointDTO(coordinates[i].getX(), coordinates[i].getY()));
		}
		return points;
	}

}    
