package placesmicroservice;
import placesmicroservice.placesfetcher.PlacesFetcherController;
import placesmicroservice.placesfetcher.PlacesResult;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {
    @Bean
    public PlacesResult placesResult() {
        return new PlacesResult();
    }

    @Bean
    public PlacesFetcherController placesFetcherController(PlacesResult placesResult) {
        return new PlacesFetcherController(placesResult);
    }
}
