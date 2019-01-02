package placesmicroservice;

import placesmicroservice.placesfetcher.PlacesResult;
import placesmicroservice.placesfetcher.Place;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.TreeMap;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PlacesResultTest {
    private PlacesResult placesResult;

    @Before
    public void setup() {
        placesResult = new PlacesResult();
    }

    @Test
    public void testPopulateFieldsFromJson() {
        String[] mockIds = {"mock_place_id_1", "mock_place_id_2"};
        String[] mockNames = {"Mock Venue Name 1", "Mock Venue Name 2"};
        String mockStatus = "OK";
        String mockNextPageToken = "mock_next_page_token";
        String mockPlacesJson =
                "{" +
                    "\"html_attributions\":[]," +
                    "\"next_page_token\":" + mockNextPageToken + "," +
                    "\"results\":[" +
                        "{" +
                            "\"name\":\"" + mockNames[0] + "\"," +
                            "\"place_id\":\"" + mockIds[0] + "\"" +
                        "}," +
                        "{" +
                            "\"name\":\"" + mockNames[1] + "\"," +
                            "\"place_id\":\"" + mockIds[1] + "\"" +
                        "}" +
                    "]," +
                    "\"status\":" + mockStatus +
                "}";

        placesResult.populateFieldsFromJson(mockPlacesJson);
        TreeMap<String, Place> places = placesResult.getPlaces();
        String status = placesResult.getStatus();
        String nextPageToken = placesResult.getNextPageToken();

        int expectedNumPlaces = mockIds.length;
        String expectedStatus = "OK";
        String expectedNextPageToken = "mock_next_page_token";

        for (int i = 0; i < expectedNumPlaces; i++) {
            String expectedId = mockIds[i];
            String expectedName = mockNames[i];
            Place pl = places.get(expectedId);

            assert(pl.getPlaceId().equals(expectedId));
            assert(pl.getName().equals(expectedName));
        }
        assert(places.size() == expectedNumPlaces);
        assert(status.equals(expectedStatus));
        assert(nextPageToken.equals(expectedNextPageToken));
    }

    @Test
    public void testPopulatePlacesPhotos() {
        String[] mockIds = {"mock_place_id_1", "mock_place_id_2"};
        String[] mockNames = {"Mock Venue Name 1", "Mock Venue Name 2"};
        String mockStatus = "OK";
        String mockNextPageToken = "mock_next_page_token";
        String mockPlacesJson =
                "{" +
                    "\"html_attributions\":[]," +
                    "\"next_page_token\":" + mockNextPageToken + "," +
                    "\"results\":[" +
                        "{" +
                            "\"name\":\"" + mockNames[0] + "\"," +
                            "\"place_id\":\"" + mockIds[0] + "\"" +
                        "}," +
                        "{" +
                            "\"name\":\"" + mockNames[1] + "\"," +
                            "\"place_id\":\"" + mockIds[1] + "\"" +
                        "}" +
                    "]," +
                    "\"status\":" + mockStatus +
                "}";

        HashMap<String, ArrayList<String>> mockPlacePhotos = new HashMap<>();

        ArrayList<String> mockPhotos1 = new ArrayList<>();
        ArrayList<String> mockPhotos2 = new ArrayList<>();
        mockPhotos1.add("https://maps.googleapis.com/maps/api/place/photo?" +
                "maxwidth=500&maxheight=500&photoreference=mock_ref1&key=mock_key");
        mockPhotos2.add("https://maps.googleapis.com/maps/api/place/photo?" +
                "maxwidth=500&maxheight=500&photoreference=mock_ref2&key=mock_key");
        mockPlacePhotos.put(mockIds[0], mockPhotos1);
        mockPlacePhotos.put(mockIds[1], mockPhotos2);

        placesResult.populateFieldsFromJson(mockPlacesJson);
        placesResult.populatePlacesPhotos(mockPlacePhotos);
        TreeMap<String, Place> places = placesResult.getPlaces();


        int expectedNumPlaces = mockIds.length;
        ArrayList<ArrayList<String>> mockPhotosList = new ArrayList<>();
        mockPhotosList.add(mockPhotos1);
        mockPhotosList.add(mockPhotos2);

        for (int i = 0; i < expectedNumPlaces; i++) {
            String expectedPlaceId = mockIds[i];
            Place pl = places.get(expectedPlaceId);

            ArrayList<String> photos = pl.getPhotos();
            ArrayList<String> expectedPhotos = mockPhotosList.get(i);

            assert(photos.size() == expectedPhotos.size());
            for (int j = 0; j < photos.size(); j++) {
                assert(photos.get(j).equals(expectedPhotos.get(j)));
            }
        }
    }

    @Test
    public void testPrepareJsonResult() {
        String mockPlacesJson =
                "{" +
                    "\"html_attributions\":[]," +
                    "\"next_page_token\":\"mock_next_page_token\"," +
                    "\"results\":[" +
                        "{" +
                            "\"name\":\"Mock Venue Name 1\"," +
                            "\"place_id\":\"mock_place_id_1\"" +
                        "}," +
                        "{" +
                            "\"name\":\"Mock Venue Name 2\"," +
                            "\"place_id\":\"mock_place_id_2\"" +
                        "}" +
                    "]," +
                    "\"status\":\"OK\"" +
                "}";

        HashMap<String, ArrayList<String>> mockPlacesPhotos = new HashMap<>();
        ArrayList<String> mockPhotos1 = new ArrayList<>();
        ArrayList<String> mockPhotos2 = new ArrayList<>();
        mockPhotos1.add("https://maps.googleapis.com/maps/api/place/photo?" +
                "maxwidth=500&maxheight=500&photoreference=mock_ref1&key=mock_key");
        mockPhotos2.add("https://maps.googleapis.com/maps/api/place/photo?" +
                "maxwidth=500&maxheight=500&photoreference=mock_ref2&key=mock_key");
        mockPlacesPhotos.put("mock_place_id_1", mockPhotos1);
        mockPlacesPhotos.put("mock_place_id_2", mockPhotos2);
        placesResult.populateFieldsFromJson(mockPlacesJson);
        placesResult.populatePlacesPhotos(mockPlacesPhotos);

        String jsonResult = placesResult.prepareJsonResult();
        String expectedJsonResult =
                "{\"places\":[" +
                        "{" +
                            "\"placeId\":\"mock_place_id_1\"," +
                            "\"name\":\"Mock Venue Name 1\"," +
                            "\"photos\":[" +
                                "\"https://maps.googleapis.com/maps/api/place/photo?" +
                                "maxwidth=500&maxheight=500&photoreference=mock_ref1&key=mock_key\"" +
                            "]" +
                        "}," +
                        "{" +
                            "\"placeId\":\"mock_place_id_2\"," +
                            "\"name\":\"Mock Venue Name 2\"," +
                            "\"photos\":[" +
                                "\"https://maps.googleapis.com/maps/api/place/photo?" +
                                "maxwidth=500&maxheight=500&photoreference=mock_ref2&key=mock_key\"" +
                            "]" +
                        "}]," +
                    "\"nextPageToken\":\"mock_next_page_token\"," +
                    "\"errors\":[]," +
                    "\"status\":\"OK\"}";

        assert(jsonResult.equals(expectedJsonResult));
    }

}
