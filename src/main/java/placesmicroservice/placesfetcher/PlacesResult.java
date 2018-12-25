package placesmicroservice.placesfetcher;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class PlacesResult {
    private TreeMap<String, Place> places;
    private String nextPageToken;
    private String status;

    public PlacesResult() {
        this.places = new TreeMap<>();
    }

    public TreeMap<String, Place> getPlaces() {
        return places;
    }

    public void populateFieldsFromJSON(String respJSON) {
        JsonObject respObject = null;
        Gson gson = new Gson();

        try {
            respObject = gson.fromJson(respJSON, JsonObject.class);
        } catch (Exception e) {
            return;
        }

        JsonElement resultsEle = respObject.get("results");
        JsonElement nextPageTokenEle = respObject.get("next_page_token");
        JsonElement statusEle = respObject.get("status");

        if (resultsEle != null) {
            JsonArray resultsJsonArr = resultsEle.getAsJsonArray();

            for (int i = 0; i < resultsJsonArr.size(); i++) {
                JsonObject placeObj = resultsJsonArr.get(i).getAsJsonObject();
                String placeID = placeObj.get("place_id").getAsString();
                String name = placeObj.get("name").getAsString();
                places.put(placeID, new Place(placeID, name));

            }
        }

        if (nextPageTokenEle != null) {
            nextPageToken = nextPageTokenEle.getAsString();
        }

        if (statusEle != null) {
            status = statusEle.getAsString();
        }
    }

    public void populatePlacesPhotos(HashMap<String, ArrayList<String>> placePhotos) {
        for (Map.Entry<String, ArrayList<String>> entry : placePhotos.entrySet()) {
            Place pl = places.get(entry.getKey());
            pl.setPhotos(entry.getValue());
        }
    }
}
