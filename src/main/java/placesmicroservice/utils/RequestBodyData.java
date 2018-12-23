package placesmicroservice.utils;

import java.awt.geom.Point2D;

public class RequestBodyData {
    private Point2D coord;
    private String type;
    private String cuisine;

    public RequestBodyData(Double lat, Double lon, String type, String cuisine) {
        this.coord = new Point2D.Double(lat, lon);
        this.type = type;
        this.cuisine = (cuisine != null && cuisine.length() > 0) ? cuisine : null;

    }

    public Point2D getCoord() {
        return coord;
    }

    public String getType() {
        return type;
    }

    public String getCuisine() {
        return cuisine;
    }
}