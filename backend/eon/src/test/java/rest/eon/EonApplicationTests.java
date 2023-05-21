package rest.eon;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.google.common.net.HttpHeaders;
import rest.eon.auth.AuthenticationRequest;
import rest.eon.auth.JwtService;
import rest.eon.auth.RegisterRequest;
import rest.eon.dto.TaskDto;
import rest.eon.repositories.UserRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static rest.eon.FakeKeyGenerator.generateFakeKey;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestMethodOrder;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc

class Tests {
    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4.2");
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtService jwtService;


    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry dynamicPropertyRegistry) {
        dynamicPropertyRegistry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @BeforeEach
    public void setUp() {
        String fakeKey = generateFakeKey(256);
        jwtService.setKey(fakeKey);
    }

    @Nested
    class AuthenticationTest {

        @Test
        void registerUser() throws Exception {
            RegisterRequest request = RegisterRequest.builder()
                    .email("email@mail.ru")
                    .firstname("ivan")
                    .lastname("ivanov")
                    .nickname("ivan")
                    .password("secretpassword")
                    .build();

            String requestString = objectMapper.writeValueAsString(request);
            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestString))
                    .andExpect(status().isOk());

            boolean isRegistered = userRepository.findByEmail("email@mail.ru").isPresent();
            Assertions.assertTrue(isRegistered);
        }
        @Test
        String authorizeUser() throws Exception {
            AuthenticationRequest request = AuthenticationRequest.builder()
                    .email("email@mail.ru")
                    .password("secretpassword")
                    .build();

            String requestString = objectMapper.writeValueAsString(request);

            MvcResult result=mockMvc.perform(post("/auth/authenticate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestString))
                    .andExpect(status().isOk())
                    .andReturn();

            AuthResponse authResponse = objectMapper.readValue(result.getResponse().getContentAsString(), AuthResponse.class);
            String token = authResponse.getToken();
            Assertions.assertNotNull(token);
            return token;
        }

        static class AuthResponse {
            private String token;

            public String getToken() {
                return token;
            }

        }
    }

    @Nested
    class TaskTest {
        @Test
        void addNewTask() throws Exception {
            //todo: add access to user token
            String token="";
            System.out.println(token);
            TaskDto task = TaskDto.builder()
                    .dateStart("2023-09-29T13:40:00Z")
                    .dateFinish("2023-09-30T16:00:00Z")
                    .title("task")
                    .description("simple desc")
                    .build();
            String requestString = objectMapper.writeValueAsString(task);

            mockMvc.perform(post("/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestString)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").exists());
        }
    }
}