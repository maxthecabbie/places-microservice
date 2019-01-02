package placesmicroservice;

import placesmicroservice.placesfetcher.*;
import placesmicroservice.utils.RequestBodyData;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;


import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PlacesFetcherControllerTest {
    private static final int THREAD_COUNT = 20;

    @Autowired
    Environment env;

    @Mock
    private Environment mockEnv;

    @Spy
    private ExecutorCompletionService mockEcs = new ExecutorCompletionService(Executors.newFixedThreadPool(THREAD_COUNT));

    @Spy
    private PhotosFetcher mockPhotosFetcher;

    @Mock
    private HttpClient mockHttpClient;

    @InjectMocks
    private PlacesFetcherController placesController;

    @Before
    public void setUp() {
        when(mockEnv.getProperty(any(String.class))).thenReturn(env.getProperty("places.api.key"));
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetPlaces() throws Exception {
        String mockPlacesData =
                "{" +
                    "\"html_attributions\" : []," +
                    "\"next_page_token\" : \"mock_next_page_token\"," +
                    "\"results\": [" +
                        "{" +
                            "\"name\": \"Mock Venue Name\"," +
                            "\"place_id\": \"mock_place_id\"" +
                        "}" +
                    "]," +
                    "\"status\": \"OK\"" +
                "}";
        when(mockHttpClient.getRequest(any(String.class))).thenReturn(mockPlacesData);

        Double lat = 40.758896;
        Double lon = -73.985130;
        String cuisine = "";
        String nextPageToken = "";
        RequestBodyData reqData = new RequestBodyData(lat, lon, cuisine, nextPageToken);
        placesController.setReqData(reqData);

        String placesData = placesController.getPlaces();
        String expectedPlacesData = mockPlacesData;
        String expectedRequestUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                "location=40.758896,-73.98513&radius=3000&type=restaurant&key=null";

        assert(placesData.equals(expectedPlacesData));
        Mockito.verify(mockHttpClient).getRequest(expectedRequestUrl);
        verify(mockPhotosFetcher, never()).call();
        verify(mockEcs, never()).take();
    }

    @Test
    public void testGetPlacesPhotos() throws Exception {
        setupGetPlacePhotosMockFunc();

        String[] mockIds = {"mock_place_id_1", "mock_place_id_2",
                "mock_place_id_3", "mock_place_id_4", "mock_place_id_5"};
        String mockPlacesData =
                "{" +
                    "\"html_attributions\" : []," +
                    "\"next_page_token\" : \"mock_next_page_token\"," +
                    "\"results\": [" +
                        "{" +
                            "\"name\": \"Mock Venue Name 1\"," +
                            "\"place_id\": \"" + mockIds[0] + "\"" +
                        "}," +
                        "{" +
                            "\"name\": \"Mock Venue Name 2\"," +
                            "\"place_id\": \"" + mockIds[1] + "\"" +
                        "}," +
                        "{" +
                            "\"name\": \"Mock Venue Name 3\"," +
                            "\"place_id\": \"" + mockIds[2] + "\"" +
                        "}," +
                        "{" +
                            "\"name\": \"Mock Venue Name 4\"," +
                            "\"place_id\": \"" + mockIds[3] + "\"" +
                        "}," +
                        "{" +
                            "\"name\": \"Mock Venue Name 5\"," +
                            "\"place_id\": \"" + mockIds[4] + "\"" +
                        "}" +
                    "]," +
                    "\"status\": \"OK\"" +
                "}";
        PlacesResult mockPlacesResult = new PlacesResult();
        mockPlacesResult.populateFieldsFromJson(mockPlacesData);

        ConcurrentLinkedQueue<String> mockTaskList = new ConcurrentLinkedQueue<>();
        List<String> mockIdsList = new ArrayList<>(Arrays.asList(mockIds));
        mockTaskList.addAll(mockIdsList);
        mockPhotosFetcher.setTaskList(mockTaskList);

        HashMap<String, ArrayList<String>> placesPhotos = placesController.getPlacesPhotos(mockPlacesResult);

        int expectedNumPlaces = mockIds.length;
        for (int i = 0; i < expectedNumPlaces; i++) {
            String expectedPlaceId = mockIds[i];
            ArrayList<String> photos = placesPhotos.get(expectedPlaceId);

            for (int j = 0; j < photos.size(); j++) {
                String expectedPhotoRef = expectedPlaceId + "_" + Integer.toString(j);
                String expectedUrl = "https://maps.googleapis.com/maps/api/place/photo?" +
                        "maxwidth=500&maxheight=500&photoreference=" + expectedPhotoRef + "&key=mock_key";
                assert(photos.get(j).equals(expectedUrl));
            }
        }

        verify(mockPhotosFetcher, Mockito.times(expectedNumPlaces)).call();
        verify(mockEcs, Mockito.times(expectedNumPlaces)).take();
    }

    public void setupGetPlacePhotosMockFunc() throws Exception {
        Mockito.doAnswer(new Answer<PlacePhotosResult>() {
            @Override
            public PlacePhotosResult answer(InvocationOnMock invocation) {
                final int NUM_PHOTOS_PER_PLACE = 5;
                Object[] args = invocation.getArguments();
                String placeId = (String) args[0];

                ArrayList<String> photos = new ArrayList<>();
                for (int i = 0; i < NUM_PHOTOS_PER_PLACE; i++) {
                    String mockRef = placeId + "_" + Integer.toString(i);
                    photos.add("https://maps.googleapis.com/maps/api/place/photo?" +
                            "maxwidth=500&maxheight=500&photoreference=" + mockRef + "&key=mock_key");
                }

                return new PlacePhotosResult(placeId, photos);
            }
        }).when(mockPhotosFetcher).getPlacePhotos(any(String.class));
    }
}
