package placesmicroservice.placesfetcher;

import java.util.ArrayList;

public class Place {
    private String placeID;
    private String name;
    private ArrayList<String> photos;

    public Place(String placeID, String name) {
        this.placeID = placeID;
        this.name = name;
        this.photos = new ArrayList<String>();
    }
}
