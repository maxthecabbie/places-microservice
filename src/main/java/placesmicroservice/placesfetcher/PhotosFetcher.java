package placesmicroservice.placesfetcher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class PhotosFetcher implements Callable {
    private ConcurrentLinkedQueue<String> taskList;

    @Autowired
    private Environment env;

    public PhotosFetcher() {
        this.taskList = new ConcurrentLinkedQueue<>();
    }

    public void setTaskList(ConcurrentLinkedQueue<String> taskList) {
        this.taskList = taskList;
    }

    private PlacePhotosResult getPlacePhotos(String placeID) {
        HttpURLConnection conn = null;

        try {
            URL url = new URL(buildPlaceDetailsURL(placeID));
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

            ArrayList<String> photos = getPhotosFromResp(resp.toString());
            return new PlacePhotosResult(placeID, photos);

        } catch (Exception e) {
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
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
                photos.add(buildPhotosURL(photoRef));
            }
        }
        return photos;
    }

    private String buildPlaceDetailsURL(String placeID) {
        String baseURL = "https://maps.googleapis.com/maps/api/place/details/json?";
        String id = "placeid=" + placeID;
        String fields = "&fields=photo";
        String key = "&key=" + env.getProperty("places.api.key");
        return baseURL + id + fields + key;
    }

    private String buildPhotosURL(String photoRef) {
        String baseURL = "https://maps.googleapis.com/maps/api/place/photo?";
        String dimensions = "maxwidth=500&maxheight=500";
        String ref = "&photoreference=" + photoRef;
        String key = "&key=" + env.getProperty("places.api.key");
        return baseURL + dimensions + ref + key;
    }

    public PlacePhotosResult call() {
        if (taskList.size() > 0) {
            String placeID = taskList.remove();
            return getPlacePhotos(placeID);
        }
        return null;
    }
}
