package placesmicroservice.placesfetcher;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;

public class PlacesResult {
    private ArrayList<Place> places;
    private String nextPageToken;
    private String status;

    public PlacesResult() {
        this.places = new ArrayList();
    }

    public void setResults(String respJSON) {
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
                this.places.add(new Place(placeID, name));

            }
        }

        if (nextPageTokenEle != null) {
            this.nextPageToken = nextPageTokenEle.getAsString();
        }

        if (statusEle != null) {
            this.status = statusEle.getAsString();
        }
    }
}
