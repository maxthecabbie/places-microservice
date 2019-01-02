package placesmicroservice.placesfetcher;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class PlacesResult {
    private TreeMap<String, Place> places;
    private ArrayList<String> errors;
    private String nextPageToken;
    private String status;

    public PlacesResult() {
        places = new TreeMap<>();
        errors = new ArrayList<>();
    }

    public TreeMap<String, Place> getPlaces() {
        return places;
    }

    public ArrayList<String> getErrors() {
        return errors;
    }

    public String getNextPageToken() {
        return nextPageToken;
    }

    public String getStatus() {
        return status;
    }

    public void addError(String errMsg) {
        errors.add(errMsg);
    }

    public void populateFieldsFromJson(String respJson) {
        JsonObject respObject = null;
        Gson gson = new Gson();

        try {
            respObject = gson.fromJson(respJson, JsonObject.class);
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
                String placeId = placeObj.get("place_id").getAsString();
                String name = placeObj.get("name").getAsString();
                places.put(placeId, new Place(placeId, name));

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

    public String prepareJsonResult() {
        Gson gson = new GsonBuilder()
                .disableHtmlEscaping()
                .create();

        ArrayList<Place> placesArrayList = new ArrayList<>();
        for (Map.Entry<String, Place> entry : places.entrySet()) {
            placesArrayList.add(entry.getValue());
        }

        Map<String, Object> map = new HashMap<>();
        map.put("places", placesArrayList);
        map.put("status", status);
        map.put("nextPageToken", nextPageToken);
        map.put("errors", errors);
        return gson.toJson(map);
    }
}
