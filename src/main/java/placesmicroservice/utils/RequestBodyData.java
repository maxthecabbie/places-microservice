package placesmicroservice.utils;

import java.awt.geom.Point2D;

public class RequestBodyData {
    private Point2D coord;
    private String cuisine;
    private String nextPageToken;

    public RequestBodyData(Double lat, Double lon, String cuisine, String nextPageToken) {
        this.coord = new Point2D.Double(lat, lon);
        this.cuisine = (cuisine != null && cuisine.length() > 0) ? cuisine : null;
        this.nextPageToken = (nextPageToken != null && nextPageToken.length() > 0) ? nextPageToken : null;
    }

    public Point2D getCoord() {
        return coord;
    }

    public String getCuisine() {
        return cuisine;
    }

    public String getNextPageToken() {
        return nextPageToken;
    }
}