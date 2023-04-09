package rest.eon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import rest.eon.services.impl.UserNotificationService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@SpringBootApplication
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

        UserNotificationService service = applicationContext.getBean(UserNotificationService.class);
        service.taskChecking();
    }

}
