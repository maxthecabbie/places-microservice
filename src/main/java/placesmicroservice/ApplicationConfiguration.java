package placesmicroservice;

import placesmicroservice.placesfetcher.PlacesFetcherController;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;

@Configuration
public class ApplicationConfiguration {
    private static final int THREAD_COUNT = 20;

    @Bean
    public ExecutorCompletionService ecs() {
        return new ExecutorCompletionService(Executors.newFixedThreadPool(THREAD_COUNT));
    }

    @Bean
    public PlacesFetcherController placesFetcherController(ExecutorCompletionService ecs) {
        return new PlacesFetcherController(ecs);
    }
}
