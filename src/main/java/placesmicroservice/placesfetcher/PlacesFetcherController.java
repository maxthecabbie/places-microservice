package placesmicroservice.placesfetcher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Component
public class PlacesFetcherController {
    private PlacesResult placesResult;
    private Point2D coord;
    private String type;
    private String cuisine;

    @Autowired
    private Environment env;

    public PlacesFetcherController(PlacesResult placesResult) {
        this.placesResult = placesResult;
    }

    public void setCoord(Point2D coord) {
        this.coord = coord;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setCuisine(String cuisine) {
        this.cuisine = cuisine;
    }

    public String getPlaces() {
        HttpURLConnection conn = null;

        try {
            URL url = new URL(buildNearbySearchUrl());
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream (conn.getOutputStream());
            wr.close();

            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder resp = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                resp.append(line);
                resp.append("\r");
            }
            reader.close();

            return resp.toString();

        } catch (Exception e) {
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private String buildNearbySearchUrl() {
        String baseURL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
        String location = "location=" + String.valueOf(coord.getX()) + "," + String.valueOf(coord.getY());
        String radius = "&radius=3000";
        String type = "&type=restaurant";
        String keyword = (cuisine != null && cuisine.length() > 0) ? "&keyword=" + cuisine : "";
        String key = "&key=" + env.getProperty("places.api.key");
        return baseURL + location + radius + type + keyword + key;
    }
}
