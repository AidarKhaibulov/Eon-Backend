package rest.eon;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import rest.eon.services.impl.UserNotificationService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalTime;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "EON", version = "0.666V", description = "Hello world from Eon api documentation. Have a nice day!"))
public class EonApplication {
    public static String JWT_SECRET_KEY = "";

    public static void main(String[] args) throws IOException {
        try (
                InputStream is1 = EonApplication.class.getResourceAsStream("/secretkey.txt")
        ) {
            assert is1 != null;
            try (BufferedReader reader1 = new BufferedReader(new InputStreamReader(is1))
            ) {
                JWT_SECRET_KEY = reader1.readLine();
            }
        }
        ApplicationContext applicationContext = SpringApplication.run(EonApplication.class, args);

        System.out.println((LocalDate.parse("2023-04-13T19:30:00Z".substring(0,10)).getDayOfWeek()));
        System.out.println(LocalTime.parse("2023-04-09T23:59:00Z".substring(11,16)));
        UserNotificationService service = applicationContext.getBean(UserNotificationService.class);
        service.taskChecking();
    }

}
