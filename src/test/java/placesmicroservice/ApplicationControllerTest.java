package placesmicroservice;

import placesmicroservice.placesfetcher.PlacesFetcherController;
import placesmicroservice.placesfetcher.PlacesResult;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import java.util.ArrayList;
import java.util.HashMap;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ApplicationControllerTest {
    private String jwt, badJwt;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private Environment env;

    @Mock
    private Environment envMock;

    @Mock
    PlacesFetcherController placesControllerMock;

    @InjectMocks
    private ApplicationController reqController;

    @Before
    public void setup() {
        mvc = MockMvcBuilders.standaloneSetup(reqController).build();
        when(envMock.getProperty(any(String.class))).thenReturn(env.getProperty("jwt.secret"));
        Algorithm algorithm = Algorithm.HMAC256(env.getProperty("jwt.secret"));
        jwt = JWT.create().sign(algorithm);
        badJwt = jwt + "bad";
    }

    @Test
    public void testPostReqWithValidJsonBody() throws Exception {
        String mockPlacesData =
                "{" +
                    "\"html_attributions\":[]," +
                    "\"next_page_token\":\"mock_next_page_token\"," +
                    "\"results\": [" +
                        "{" +
                            "\"name\":\"Mock Venue Name\"," +
                            "\"place_id\":\"mock_place_id\"" +
                        "}" +
                    "]," +
                    "\"status\":\"OK\"" +
                "}";
        HashMap<String, ArrayList<String>> mockPlacesPhotos = new HashMap<>();
        ArrayList<String> mockPhotos = new ArrayList<>();
        mockPhotos.add("https://maps.googleapis.com/maps/api/place/photo?" +
                "maxwidth=500&maxheight=500&photoreference=mock_ref&key=mock_key");
        mockPlacesPhotos.put("mock_place_id", mockPhotos);

        when(placesControllerMock.getPlaces()).thenReturn(mockPlacesData);
        when(placesControllerMock.getPlacesPhotos(any(PlacesResult.class))).thenReturn(mockPlacesPhotos);

        String validJson = "{\"lat\": \"40.7831\", \"lon\": 73.9712," +
                "\"cuisine\": \"\"," +
                "\"nextPageToken\": \"\"}";
        MvcResult req = mvc.perform(MockMvcRequestBuilders.post("/")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", jwt)
                .content(validJson))
                .andReturn();
        String jsonResp = req.getResponse().getContentAsString();

        String expectedJsonResp =
                "{" +
                    "\"places\":[" +
                        "{" +
                            "\"placeId\":\"mock_place_id\"," +
                            "\"name\":\"Mock Venue Name\"," +
                            "\"photos\":[" +
                                "\"https://maps.googleapis.com/maps/api/place/photo?" +
                                "maxwidth=500&maxheight=500&photoreference=mock_ref&key=mock_key\"" +
                            "]" +
                        "}" +
                    "]," +
                    "\"nextPageToken\":\"mock_next_page_token\"," +
                    "\"errors\":[]," +
                    "\"status\":\"OK\"" +
                "}";

        assert(jsonResp.equals(expectedJsonResp));
        assert(req.getResponse().getStatus() == HttpStatus.OK.value());
    }

    @Test
    public void testPostReqWithInvalidJsonBody() throws Exception {
        String invalidLatKeyJson = "{\"invalidKey\": \"40.7831\", \"lon\": 73.9712," +
                "\"cuisine\": \"\"," +
                "\"nextPageToken\": \"\"}";
        String invalidLonKeyJson = "{\"lat\": \"40.7831\", \"invalidKey\": 73.9712," +
                "\"cuisine\": \"\"," +
                "\"nextPageToken\": \"\"}";
        String invalidCuisineKeyJson = "{\"lat\": \"40.7831\", \"lon\": 73.9712," +
                "\"invalidKey\": \"\"," +
                "\"nextPageToken\": \"\"}";
        String invalidNextPageTokenJson = "{\"lat\": \"40.7831\", \"lon\": 73.9712," +
                "\"cuisine\": \"\"," +
                "\"invalidKey\": \"\"}";

        MvcResult invalidLatKeyReq = mvc.perform(MockMvcRequestBuilders.post("/")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", jwt)
                .content(invalidLatKeyJson))
                .andReturn();
        MvcResult invalidLonKeyReq = mvc.perform(MockMvcRequestBuilders.post("/")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", jwt)
                .content(invalidLonKeyJson))
                .andReturn();
        MvcResult invalidCuisineKeyReq = mvc.perform(MockMvcRequestBuilders.post("/")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", jwt)
                .content(invalidCuisineKeyJson))
                .andReturn();
        MvcResult invalidNextPageTokenReq = mvc.perform(MockMvcRequestBuilders.post("/")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", jwt)
                .content(invalidNextPageTokenJson))
                .andReturn();

        assert(invalidLatKeyReq.getResponse().getStatus() == HttpStatus.BAD_REQUEST.value());
        assert(invalidLonKeyReq.getResponse().getStatus() == HttpStatus.BAD_REQUEST.value());
        assert(invalidCuisineKeyReq.getResponse().getStatus() == HttpStatus.BAD_REQUEST.value());
        assert(invalidNextPageTokenReq.getResponse().getStatus() == HttpStatus.BAD_REQUEST.value());
        assert(invalidLatKeyReq.getResponse().getContentAsString().equals(""));
        assert(invalidLonKeyReq.getResponse().getContentAsString().equals(""));
        assert(invalidCuisineKeyReq.getResponse().getContentAsString().equals(""));
        assert(invalidNextPageTokenReq.getResponse().getContentAsString().equals(""));
    }

    @Test
    public void testPostReqWithNoJwt() throws Exception {
        String validJson = "{\"lat\": \"40.7831\", \"lon\": 73.9712," +
                "\"cuisine\": \"\"," +
                "\"nextPageToken\": \"\"}";

        MvcResult req = mvc.perform(MockMvcRequestBuilders.post("/")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(validJson))
                .andReturn();

        assert(req.getResponse().getStatus() == HttpStatus.BAD_REQUEST.value());
        assert(req.getResponse().getContentAsString().equals(""));
    }

    @Test
    public void testPostReqWithInvalidJwt() throws Exception {
        String validJson = "{\"lat\": \"40.7831\", \"lon\": 73.9712," +
                "\"cuisine\": \"\"," +
                "\"nextPageToken\": \"\"}";

        MvcResult res = mvc.perform(MockMvcRequestBuilders.post("/")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", badJwt)
                .content(validJson))
                .andReturn();

        assert(res.getResponse().getStatus() == HttpStatus.UNAUTHORIZED.value());
        assert(res.getResponse().getContentAsString().equals(""));
    }

    @Test
    public void testPostReqWithNoBody() throws Exception {
        MvcResult res = mvc.perform(MockMvcRequestBuilders.post("/")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", jwt))
                .andReturn();

        assert(res.getResponse().getStatus() == HttpStatus.BAD_REQUEST.value());
        assert(res.getResponse().getContentAsString().equals(""));
    }

    @Test
    public void testPostReqWithBadPath() throws Exception {
        String validJson = "{\"lat\": \"40.7831\", \"lon\": 73.9712," +
                "\"cuisine\": \"\"," +
                "\"nextPageToken\": \"\"}";

        MvcResult res = mvc.perform(MockMvcRequestBuilders.post("/non-existent-path")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", jwt)
                .content(validJson))
                .andReturn();

        assert(res.getResponse().getStatus() == HttpStatus.NOT_FOUND.value());
        assert(res.getResponse().getContentAsString().equals(""));
    }
}
