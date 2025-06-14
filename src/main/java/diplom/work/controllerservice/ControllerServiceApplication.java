package diplom.work.controllerservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ControllerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ControllerServiceApplication.class, args);
    }

}
