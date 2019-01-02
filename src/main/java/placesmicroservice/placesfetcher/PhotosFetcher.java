package placesmicroservice.placesfetcher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class PhotosFetcher implements Callable {
    private ConcurrentLinkedQueue<String> taskList;

    @Autowired
    private Environment env;

    @Autowired
    private HttpClient httpClient;

    public PhotosFetcher() {
        this.taskList = new ConcurrentLinkedQueue<>();
    }

    public void setTaskList(ConcurrentLinkedQueue<String> taskList) {
        this.taskList = taskList;
    }

    public PlacePhotosResult getPlacePhotos(String placeId) {
        String placeDetailsUrl = buildPlaceDetailsUrl(placeId);
        String placeDetailsRes = httpClient.getRequest(placeDetailsUrl);
        if (placeDetailsRes != null) {
            ArrayList<String> photos = getPhotosFromResp(placeDetailsRes);
            if (photos != null) {
                return new PlacePhotosResult(placeId, photos);
            }
        }
        return null;
    }

    private ArrayList<String> getPhotosFromResp(String resp) {
        Gson gson = new Gson();
        ArrayList<String> photos = new ArrayList<>();
        JsonArray photoRefs = null;

        try {
            photoRefs = gson.fromJson(resp, JsonObject.class)
                    .get("result").getAsJsonObject()
                    .get("photos").getAsJsonArray();
        } catch (Exception e) {
            return null;
        }

        if (photoRefs != null) {
            for (int i = 0; i < photoRefs.size(); i++) {
                String photoRef = photoRefs.get(i).getAsJsonObject()
                        .get("photo_reference").getAsString();
                photos.add(buildPhotosUrl(photoRef));
            }
        }
        return photos;
    }

    private String buildPlaceDetailsUrl(String placeId) {
        String baseUrl = "https://maps.googleapis.com/maps/api/place/details/json?";
        String id = "placeid=" + placeId;
        String fields = "&fields=photo";
        String key = "&key=" + env.getProperty("places.api.key");
        return baseUrl + id + fields + key;
    }

    private String buildPhotosUrl(String photoRef) {
        String baseUrl = "https://maps.googleapis.com/maps/api/place/photo?";
        String dimensions = "maxwidth=500&maxheight=500";
        String ref = "&photoreference=" + photoRef;
        String key = "&key=" + env.getProperty("places.api.key");
        return baseUrl + dimensions + ref + key;
    }

    public PlacePhotosResult call() {
        if (taskList.size() > 0) {
            String placeId = taskList.remove();
            return getPlacePhotos(placeId);
        }
        return null;
    }
}
