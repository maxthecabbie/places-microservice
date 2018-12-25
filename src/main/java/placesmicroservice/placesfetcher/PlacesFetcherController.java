package placesmicroservice.placesfetcher;

import placesmicroservice.utils.RequestBodyData;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;

@Component
public class PlacesFetcherController {
    private ExecutorCompletionService ecs;
    private RequestBodyData reqData;

    @Autowired
    private Environment env;

    @Autowired
    private PhotosFetcher photosFetcher;

    public PlacesFetcherController(ExecutorCompletionService ecs) {
        this.ecs = ecs;
    }

    public void setReqData(RequestBodyData reqData) {
        this.reqData = reqData;
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

    public HashMap<String, ArrayList<String>> getPlacesPhotos(PlacesResult placesResult) {
        HashMap<String, ArrayList<String>> photos = new HashMap<>();
        int numRequests = placesResult.getPlaces().size();
        photosFetcher.setTaskList(genTaskList(placesResult));

        for (int i = 0; i < numRequests; i++) {
            Callable<PlacePhotosResult> task = photosFetcher;
            ecs.submit(task);
        }

        try {
            for (int i = 0; i < numRequests; i++) {
                PlacePhotosResult res = (PlacePhotosResult) ecs.take().get();
                if (res != null) {
                    photos.put(res.getPlaceID(), res.getPhotos());
                } else {
                    // TODO: error handling
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            // TODO: error handling
        }

        return photos;
    }

    private String buildNearbySearchUrl() {
        Double lat = reqData.getCoord().getX();
        Double lon = reqData.getCoord().getY();
        String cuisine = reqData.getCuisine();
        String nextPageToken = reqData.getNextPageToken();

        String baseURL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
        String location = "location=" + String.valueOf(lat) + "," + String.valueOf(lon);
        String radius = "&radius=3000";
        String type = "&type=restaurant";
        String keyword = (cuisine != null && cuisine.length() > 0) ? "&keyword=" + cuisine : "";
        String pageToken = (nextPageToken != null && nextPageToken.length() > 0) ? "&pagetoken=" + nextPageToken : "";
        String key = "&key=" + env.getProperty("places.api.key");
        return baseURL + location + radius + type + keyword + pageToken + key;
    }

    private ConcurrentLinkedQueue<String> genTaskList(PlacesResult placesResult) {
        ConcurrentLinkedQueue<String> taskList = new ConcurrentLinkedQueue<>();
        for (String key: placesResult.getPlaces().keySet()) {
            taskList.add(key);
        }
        return taskList;
    }
}
