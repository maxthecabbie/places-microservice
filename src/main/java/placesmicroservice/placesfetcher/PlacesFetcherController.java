package placesmicroservice.placesfetcher;

import placesmicroservice.utils.RequestBodyData;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

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

    @Autowired
    private HttpClient httpClient;

    public PlacesFetcherController(ExecutorCompletionService ecs) {
        this.ecs = ecs;
    }

    public void setReqData(RequestBodyData reqData) {
        this.reqData = reqData;
    }

    public String getPlaces() {
        String nearbySearchUrl = buildNearbySearchUrl();
        return httpClient.getRequest(nearbySearchUrl);
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
                    photos.put(res.getPlaceId(), res.getPhotos());
                } else {
                    String errMsg = "Result error: Unable to fetch photos for a place. Result is null";
                    placesResult.addError(errMsg);
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            String errMsg = "Thread error: " + e.getMessage();
            placesResult.addError(errMsg);
        }

        return photos;
    }

    private String buildNearbySearchUrl() {
        Double lat = reqData.getCoord().getX();
        Double lon = reqData.getCoord().getY();
        String cuisine = reqData.getCuisine();
        String nextPageToken = reqData.getNextPageToken();

        String baseUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
        String location = "location=" + String.valueOf(lat) + "," + String.valueOf(lon);
        String radius = "&radius=3000";
        String type = "&type=restaurant";
        String keyword = (cuisine != null && cuisine.length() > 0) ? "&keyword=" + cuisine : "";
        String pageToken = (nextPageToken != null && nextPageToken.length() > 0) ? "&pagetoken=" + nextPageToken : "";
        String key = "&key=" + env.getProperty("places.api.key");
        return baseUrl + location + radius + type + keyword + pageToken + key;
    }

    private ConcurrentLinkedQueue<String> genTaskList(PlacesResult placesResult) {
        ConcurrentLinkedQueue<String> taskList = new ConcurrentLinkedQueue<>();
        for (String key: placesResult.getPlaces().keySet()) {
            taskList.add(key);
        }
        return taskList;
    }
}
