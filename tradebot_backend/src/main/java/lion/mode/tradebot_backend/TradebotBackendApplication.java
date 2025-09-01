package lion.mode.tradebot_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

//@EnableScheduling
@EnableCaching
@SpringBootApplication
public class TradebotBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(TradebotBackendApplication.class, args);
	}

}
