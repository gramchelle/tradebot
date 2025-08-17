package lion.mode.tradebot_backend.config;

import java.net.http.HttpClient;
import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpClientConfig {

    // HttpClient thread-safe olduğu için tek bir instance’ı uygulama boyunca yeniden kullanabiliriz.
    // Yani, her seferinde yeniden oluşturmak yerine, HttpClient bean'ini kullanıyoruz.
    @Bean
    public HttpClient httpClient(){
        return HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    }

}

