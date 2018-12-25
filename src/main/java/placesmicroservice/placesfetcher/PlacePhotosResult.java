package placesmicroservice.placesfetcher;

import java.util.ArrayList;

public class PlacePhotosResult {
    private String placeID;
    private ArrayList<String> photos;

    public PlacePhotosResult(String placeID, ArrayList<String> photos) {
        this.placeID = placeID;
        this.photos = photos;
    }

    public String getPlaceID() {
        return placeID;
    }

    public ArrayList<String> getPhotos() {
        return photos;
    }
}
