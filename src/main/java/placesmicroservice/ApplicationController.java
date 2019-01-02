package placesmicroservice;

import placesmicroservice.placesfetcher.PlacesFetcherController;
import placesmicroservice.placesfetcher.PlacesResult;
import placesmicroservice.utils.RequestBodyData;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.ArrayList;
import java.util.HashMap;

@RestController
public class ApplicationController {
    @Autowired
    private Environment env;

    @Autowired
    private PlacesFetcherController placesController;

    @PostMapping(value = "/")
    public ResponseEntity<String> index(@RequestHeader(value="Authorization") String token,
                                        @RequestBody String reqBodyString) throws Exception{
        try {
            Algorithm algorithm = Algorithm.HMAC256(env.getProperty("jwt.secret"));
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT jwt = verifier.verify(token);
        } catch (JWTVerificationException exception){
            return new ResponseEntity<>(null, null, HttpStatus.UNAUTHORIZED);
        }

        RequestBodyData reqData = parseReqBodyString(reqBodyString);
        if (reqData == null) {
            return new ResponseEntity<>(null, null, HttpStatus.BAD_REQUEST);
        }

        PlacesResult places = fetchPlaces(reqData);
        return sendResponse(places);
    }

    private RequestBodyData parseReqBodyString(String reqBodyString) {
        Gson gson = new Gson();
        Double lat, lon;
        String cuisine, nextPageToken;
        try {
            JsonObject reqBodyJsonObj = gson.fromJson(reqBodyString, JsonObject.class);
            lat = reqBodyJsonObj.get("lat").getAsDouble();
            lon = reqBodyJsonObj.get("lon").getAsDouble();
            cuisine = reqBodyJsonObj.get("cuisine").getAsString();
            nextPageToken = reqBodyJsonObj.get("nextPageToken").getAsString();
        } catch (Exception e) {
            return null;
        }
        return new RequestBodyData(lat, lon, cuisine, nextPageToken);
    }

    private PlacesResult fetchPlaces(RequestBodyData reqData) {
        PlacesResult places = new PlacesResult();

        placesController.setReqData(reqData);
        String placesJson = placesController.getPlaces();
        places.populateFieldsFromJson(placesJson);

        HashMap<String, ArrayList<String>> placesPhotos = placesController.getPlacesPhotos(places);
        places.populatePlacesPhotos(placesPhotos);

        return places;
    }

    private ResponseEntity<String> sendResponse(PlacesResult places) {
        String responseJson = places.prepareJsonResult();
        return new ResponseEntity<>(responseJson, null, HttpStatus.OK);
    }
}
