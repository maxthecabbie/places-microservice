package placesmicroservice.placesfetcher;

import java.util.ArrayList;

public class PlacePhotosResult {
    private String placeId;
    private ArrayList<String> photos;

    public PlacePhotosResult(String placeId, ArrayList<String> photos) {
        this.placeId = placeId;
        this.photos = photos;
    }

    public String getPlaceId() {
        return placeId;
    }

    public ArrayList<String> getPhotos() {
        return photos;
    }
}
