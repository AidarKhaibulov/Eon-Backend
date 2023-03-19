package rest.eon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@SpringBootApplication
public class EonApplication {
     public static String JWT_SECRET_KEY="";

    public static void main(String[] args) throws IOException {
        try (
                InputStream is1 = EonApplication.class.getResourceAsStream("/secretkey.txt")
        ) {
            assert is1 != null;
            try (BufferedReader reader1 = new BufferedReader(new InputStreamReader(is1))
            )
            {
                JWT_SECRET_KEY=reader1.readLine();
            }
        }
        SpringApplication.run(EonApplication.class, args);

    }
}
