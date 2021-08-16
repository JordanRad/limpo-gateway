package limpo.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableEurekaClient
@RestController
public class GatewayApplication {

	@GetMapping("/isAlive")
	public ResponseEntity<String> isAlive(){
		return new ResponseEntity<String>("Gateway connection is alive!", HttpStatus.OK);
	}
	public static void main(String[] args) {
		SpringApplication.run(GatewayApplication.class, args);
	}

}
