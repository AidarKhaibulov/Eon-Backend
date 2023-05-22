package rest.eon;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application.properties")
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
    @Value("${EON_SECRET_KEY}")
    private String key;
private static String token;
    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry dynamicPropertyRegistry) {
        dynamicPropertyRegistry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @BeforeEach
    public void setUp() {
        jwtService.setKey(key);
    }


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
    void authorizeUser() throws Exception {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("email@mail.ru")
                .password("secretpassword")
                .build();

        String requestString = objectMapper.writeValueAsString(request);

        MvcResult result = mockMvc.perform(post("/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestString))
                .andExpect(status().isOk())
                .andReturn();

        AuthResponse authResponse = objectMapper.readValue(result.getResponse().getContentAsString(), AuthResponse.class);
        token = authResponse.getToken();
        Assertions.assertNotNull(token);

    }

    @Test
    void addNewTask() throws Exception {
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
    @Test
    void getAllTasksWithCorrectDataInterval() throws Exception {
        GetTasksRequest request = new GetTasksRequest(
                "defaultDesc",
                "2023-09-29T00:00:00Z",
                "2023-09-30T00:00:00Z");

        String requestString = objectMapper.writeValueAsString(request);

        mockMvc.perform(get("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestString)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").exists());
    }
    @Test
    void getAllTasksWithWrongDataInterval() throws Exception {
        authorizeUser();
        GetTasksRequest request = new GetTasksRequest(
                "defaultDesc",
                "2023-09-22T10:00:00Z",
                "2023-09-22T15:00:00Z");

        String requestString = objectMapper.writeValueAsString(request);

        mockMvc.perform(get("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestString)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").doesNotExist());
    }
    static class GetTasksRequest {
        private String sortingMethod;
        private String dateStart;
        private String dateFinish;

        public GetTasksRequest(String sortingMethod, String dateStart, String dateFinish) {
            this.sortingMethod = sortingMethod;
            this.dateStart = dateStart;
            this.dateFinish = dateFinish;
        }

        public String getSortingMethod() {
            return sortingMethod;
        }

        public String getDateStart() {
            return dateStart;
        }

        public String getDateFinish() {
            return dateFinish;
        }
    }
    static class AuthResponse {
        private String token;

        public String getToken() {
            return token;
        }

    }

}