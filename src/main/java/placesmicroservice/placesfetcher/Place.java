package placesmicroservice.placesfetcher;

import java.util.ArrayList;

public class Place {
    private String placeId;
    private String name;
    private ArrayList<String> photos;

    public Place(String placeId, String name) {
        this.placeId = placeId;
        this.name = name;
        this.photos = new ArrayList<>();
    }

    public String getPlaceId() {
        return placeId;
    }

    public String getName() {
        return name;
    }

    public ArrayList<String> getPhotos() {
        return photos;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhotos(ArrayList<String> photos) {
        this.photos = photos;
    }
}
