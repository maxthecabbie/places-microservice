package placesmicroservice;

import placesmicroservice.placesfetcher.HttpClient;
import placesmicroservice.placesfetcher.PhotosFetcher;
import placesmicroservice.placesfetcher.PlacePhotosResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PhotosFetcherTest {
    @Autowired
    Environment env;

    @Spy
    private Environment mockEnv;

    @Mock
    private HttpClient mockHttpClient;

    @InjectMocks
    private PhotosFetcher photosFetcher;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(mockEnv.getProperty(any(String.class))).thenReturn(env.getProperty("places.api.key"));
    }

    @Test
    public void testGetPlacePhotos() {
        setupMockHttpClient();

        ConcurrentLinkedQueue<String> mockTaskList = new ConcurrentLinkedQueue<>();
        String[] mockIdsArr = {"1", "2", "3", "4", "5"};
        List<String> mockIdsList = new ArrayList<String>(Arrays.asList(mockIdsArr));
        mockTaskList.addAll(mockIdsList);
        photosFetcher.setTaskList(mockTaskList);

        for (int i = 0; i < mockIdsArr.length; i++) {
            PlacePhotosResult photosResult = photosFetcher.call();

            String expectedPlaceId = mockIdsArr[i];
            int expectedPhotosForPlace = 1;
            String expectedPhotoUrl = "https://maps.googleapis.com/maps/api/place/photo?" +
                    "maxwidth=500&maxheight=500&photoreference=" + "mock_ref_" + expectedPlaceId +
                    "&key=" + env.getProperty("places.api.key");

            assert(photosResult.getPlaceId() == expectedPlaceId);
            assert(photosResult.getPhotos().size() == expectedPhotosForPlace);
            assert(photosResult.getPhotos().get(0).equals(expectedPhotoUrl));
        }
    }

    public void setupMockHttpClient(){
        Mockito.doAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                String url = (String) args[0];

                Map<String, String> params = getUrlParams(url);
                String mockPhotoRef = "mock_ref_" + params.get("placeid");

                String mockRes =
                    "{"+
                        "\"html_attributions\": []," +
                        "\"result\": {" +
                            "\"photos\": [" +
                                "{" +
                                    "\"height\": 5000," +
                                    "\"html_attributions\": []," +
                                    "\"photo_reference\" : " + mockPhotoRef + "," +
                                    "\"width\" : 5000" +
                                "}" +
                            "]" +
                        "}" +
                    "}";
                return mockRes;
            }
        }).when(mockHttpClient).getRequest(any(String.class));
    }

    public Map<String, String> getUrlParams(String url) {
        String[] params = url.split("\\?")[1].split("&");

        Map<String, String> paramsMap = new HashMap<>();

        for (String param : params) {
            String name = param.split("=")[0];
            String value = param.split("=")[1];
            paramsMap.put(name, value);
        }

        return paramsMap;
    }
}
