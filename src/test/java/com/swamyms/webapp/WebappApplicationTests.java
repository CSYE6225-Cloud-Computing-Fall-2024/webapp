package com.swamyms.webapp;

import com.swamyms.webapp.config.MetricsConfig;
import com.swamyms.webapp.config.SecurityConfig;
import com.swamyms.webapp.controllers.UserRestController;
import com.swamyms.webapp.entity.AddUser;
import com.swamyms.webapp.entity.User;
import com.swamyms.webapp.entity.VerifyUser;
import com.swamyms.webapp.service.UserService;
import com.swamyms.webapp.service.VerifyUserService;
import com.swamyms.webapp.validations.UserValidations;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Base64;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;



@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = SecurityConfig.class)
class WebappApplicationTests {

	private UserRestController userRestController;
	@Mock
	private UserService userService;
	@Mock
	private VerifyUserService verifyUserService;
	@Mock
	private UserValidations userValidations;
	//    private User testUser;
	private AddUser newUser;

	@Mock
	private MetricsConfig metricsConfig;

	@Mock
	private MeterRegistry meterRegistry;
	private User savedUser;


	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
//        // Set up a test user
//        testUser = new User();
//        testUser.setEmail("1@mail.com");
//        testUser.setFirstName("Pmaile");
//        testUser.setLastName("Jogi");
//        testUser.setPassword("ffffff@1A");
		// Set up AddUser object (request body)
		newUser = new AddUser();
		newUser.setEmail("newuser@example.com");
		newUser.setFirst_name("dsgfghds");
		newUser.setLast_name("Doe");
		newUser.setPassword("Password123!");
		// Set up User object (response body)
		savedUser = new User();
		savedUser.setId("1L");
		savedUser.setEmail(newUser.getEmail());
		savedUser.setFirstName(newUser.getFirst_name());
		savedUser.setLastName(newUser.getLast_name());
		savedUser.setPassword(newUser.getPassword());

		// Create a real SimpleMeterRegistry instead of a mock
		SimpleMeterRegistry simpleMeterRegistry = new SimpleMeterRegistry();

		// Create a real Timer
		Timer timer = Timer.builder("api.user.calls")
				.description("Time taken for user check API calls")
				.register(simpleMeterRegistry);

		// Mock the MeterRegistry
		meterRegistry = mock(MeterRegistry.class);
		when(meterRegistry.timer(anyString())).thenReturn(timer);

		// Mock the MeterRegistry.Config
		MeterRegistry.Config config = mock(MeterRegistry.Config.class);
		when(meterRegistry.config()).thenReturn(config);
		when(config.clock()).thenReturn(Clock.SYSTEM);

		// Create the UserRestController instance
		userRestController = new UserRestController(userService, userValidations, meterRegistry, verifyUserService);


	}


//		userRestController = new UserRestController(userService, userValidations, mockMeterRegistry);
	//Successful get request
//	@Test
//	void getUserSuccessfull() throws JsonProcessingException {
//		// Arrange
//		HashMap<String, String> params = new HashMap<>();
//		HttpHeaders headers = new HttpHeaders();
//		// Set the authorization header
//		String auth = "Basic " + Base64.getEncoder().encodeToString("1@mail.com:ffffff@1A".getBytes());
//		headers.set("Authorization", auth);
//		// Mock the userService.authenticateUser method
//		when(userService.authenticateUser("1@mail.com", "ffffff@1A")).thenReturn(true);
//		// Act
//		ResponseEntity<?> response = userRestController.getUser(params, headers, null);
//		// Assert
//		assertEquals(200, response.getStatusCodeValue());
//	}
	@Test
	void getUser_withParams_shouldReturnBadRequest() {
		// Arrange
		HashMap<String, String> params = new HashMap<>();
		params.put("testParam", "testValue");
		HttpHeaders headers = new HttpHeaders();
		String requestBody = null;
		// Act
		ResponseEntity<?> response = userRestController.getUser(params, headers, requestBody);
		// Assert
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}
	@Test
	void getUser_withRequestBody_shouldReturnBadRequest() {
		// Arrange
		HashMap<String, String> params = new HashMap<>();
		HttpHeaders headers = new HttpHeaders();
		String requestBody = "testBody";
		// Act
		ResponseEntity<?> response = userRestController.getUser(params, headers, requestBody);
		// Assert
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}
	@Test
	void getUser_withParamsAndRequestBody_shouldReturnBadRequest() {
		// Arrange
		HashMap<String, String> params = new HashMap<>();
		params.put("testParam", "testValue");
		HttpHeaders headers = new HttpHeaders();
		String requestBody = "testBody";
		// Act
		ResponseEntity<?> response = userRestController.getUser(params, headers, requestBody);
		// Assert
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}
	//401 get request
	@Test
	void getUserWithUnauthorized() {
		// Arrange
		HashMap<String, String> params = new HashMap<>();
		HttpHeaders headers = new HttpHeaders();
		// Set the authorization header
		String auth = "Basic " + Base64.getEncoder().encodeToString("1@mail.com:fff11fff@1A".getBytes());
		headers.set("Authorization", auth);
		// Mock the userService.authenticateUser method
		when(userService.authenticateUser("1@mail.com", "ffffff@1A")).thenReturn(true);
		// Mock the verifyUserService.getByName method to return an unverified user
		VerifyUser unverifiedUser = new VerifyUser();
		unverifiedUser.setVerified(true);
		when(verifyUserService.getByName("1@mail.com")).thenReturn(unverifiedUser);

//		when(verifyUserService.getByName("1@mail.com")).thenReturn();
		// Act
		ResponseEntity<?> response = userRestController.getUser(params, headers, null);
		// Assert
		assertEquals(401, response.getStatusCodeValue());
	}
	@Test
	void getUser_withOnlyUsername_shouldReturnBadRequest() {
		// Arrange
		HashMap<String, String> params = new HashMap<>();
		HttpHeaders headers = new HttpHeaders();
		// Set up basic authentication header with only username
		String originalInput = "username:";
		String encodedString = Base64.getEncoder().encodeToString(originalInput.getBytes());
		headers.add("authorization", "Basic " + encodedString);
		String requestBody = null;
		// Act
		ResponseEntity<?> response = userRestController.getUser(params, headers, requestBody);
		// Assert
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}
	@Test
	void getUser_withOnlyPassword_shouldReturnBadRequest() {
		// Arrange
		HashMap<String, String> params = new HashMap<>();
		HttpHeaders headers = new HttpHeaders();
		// Set up basic authentication header with only password
		String originalInput = ":password";
		String encodedString = Base64.getEncoder().encodeToString(originalInput.getBytes());
		headers.add("authorization", "Basic " + encodedString);
		String requestBody = null;
		// Act
		ResponseEntity<?> response = userRestController.getUser(params, headers, requestBody);
		// Assert
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}
	@Test
	void getUser_withNoCredentials_shouldReturnBadRequest() {
		// Arrange
		HashMap<String, String> params = new HashMap<>();
		HttpHeaders headers = new HttpHeaders();
		// Set up basic authentication header with no credentials
		String originalInput = ":";
		String encodedString = Base64.getEncoder().encodeToString(originalInput.getBytes());
		headers.add("authorization", "Basic " + encodedString);
		String requestBody = null;
		// Act
		ResponseEntity<?> response = userRestController.getUser(params, headers, requestBody);
		// Assert
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}
	@Test
	void createUser_ParamsPresent_ReturnsBadRequest() {
		// Arrange
		HashMap<String, String> params = new HashMap<>();
		params.put("key", "value");  // Adding parameter to simulate params being present
		HttpHeaders headers = new HttpHeaders();
		String userBody = "{\"username\": \"testuser\", \"password\": \"testpass\"}";
		// Act
		ResponseEntity<Object> response = userRestController.createUser(params, headers, userBody);
		// Assert
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals(CacheControl.noCache().getHeaderValue(), response.getHeaders().getCacheControl());
	}
	@Test
	void createUser_BodyNull_ReturnsBadRequest() {
		// Arrange
		HashMap<String, String> params = new HashMap<>();  // No parameters
		HttpHeaders headers = new HttpHeaders();
		String userBody = null;  // User body is null
		// Act
		ResponseEntity<Object> response = userRestController.createUser(params, headers, userBody);
		// Assert
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals(CacheControl.noCache().getHeaderValue(), response.getHeaders().getCacheControl());
	}
	@Test
	void createUser_CredentialsPresent_ReturnsBadRequest() {
		// Arrange
		HashMap<String, String> params = new HashMap<>();
		HttpHeaders headers = new HttpHeaders();
		String auth = "Basic " + Base64.getEncoder().encodeToString("username:password".getBytes());
		headers.set("Authorization", auth);
		// Mock the getCreds method to return credentials
		UserRestController spyController = spy(userRestController);
		doReturn(new String[]{"username", "password"}).when(spyController).getCreds(headers);
		// Act
		ResponseEntity<Object> response = spyController.createUser(params, headers, "validUserBody");
		// Assert
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals(CacheControl.noCache().getHeaderValue(), response.getHeaders().getCacheControl());
	}
	@Test
	void createUser_MissingPropertiesInRequestBody_ShouldReturnBadRequest() throws Exception {
		// Arrange
		HashMap<String, String> param = new HashMap<>();
		HttpHeaders headers = new HttpHeaders();
		String userBody = "{ \"first_name\": \"John\", \"last_name\": \"Doe\", \"email\": \"\", \"password\": \"\" }"; // Missing required properties
		// Act
		ResponseEntity<Object> response = userRestController.createUser(param, headers, userBody);
		// Assert
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}
	@Test
	void createUser_NullPropertiesInRequestBody_ShouldReturnBadRequest() throws Exception {
		// Arrange
		HashMap<String, String> param = new HashMap<>();
		HttpHeaders headers = new HttpHeaders();
		String userBody = "{ \"first_name\": null, \"last_name\": \"Doe\", \"email\": \"john.doe@example.com\", \"password\": \"password123\" }"; // first_name is null
		// Act
		ResponseEntity<Object> response = userRestController.createUser(param, headers, userBody);
		// Assert
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}
	@Test
	void createUser_EmptyPropertiesInRequestBody_ShouldReturnBadRequest() throws Exception {
		// Arrange
		HashMap<String, String> param = new HashMap<>();
		HttpHeaders headers = new HttpHeaders();
		String userBody = "{ \"first_name\": \"\", \"last_name\": \"Doe\", \"email\": \"john.doe@example.com\", \"password\": \"password123\" }"; // first_name is empty
		// Act
		ResponseEntity<Object> response = userRestController.createUser(param, headers, userBody);
		// Assert
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}
	@Test
	void createUser_ContainsRestrictedFields_ShouldReturnBadRequest() throws Exception {
		// Arrange
		HashMap<String, String> param = new HashMap<>();
		HttpHeaders headers = new HttpHeaders();
		// Test case where 'id' is present
		String userBodyWithId = "{ \"id\": 1, \"first_name\": \"John\", \"last_name\": \"Doe\", \"email\": \"john.doe@example.com\", \"password\": \"password123\" }";
		// Act
		ResponseEntity<Object> responseWithId = userRestController.createUser(param, headers, userBodyWithId);
		// Assert
		assertEquals(HttpStatus.BAD_REQUEST, responseWithId.getStatusCode());
		// Test case where 'accountCreated' is present
		String userBodyWithAccountCreated = "{ \"first_name\": \"John\", \"last_name\": \"Doe\", \"email\": \"john.doe@example.com\", \"password\": \"password123\", \"accountCreated\": \"2024-10-02T10:15:30\" }";
		// Act
		ResponseEntity<Object> responseWithAccountCreated = userRestController.createUser(param, headers, userBodyWithAccountCreated);
		// Assert
		assertEquals(HttpStatus.BAD_REQUEST, responseWithAccountCreated.getStatusCode());
		// Test case where 'accountUpdated' is present
		String userBodyWithAccountUpdated = "{ \"first_name\": \"John\", \"last_name\": \"Doe\", \"email\": \"john.doe@example.com\", \"password\": \"password123\", \"accountUpdated\": \"2024-10-02T10:15:30\" }";
		// Act
		ResponseEntity<Object> responseWithAccountUpdated = userRestController.createUser(param, headers, userBodyWithAccountUpdated);
		// Assert
		assertEquals(HttpStatus.BAD_REQUEST, responseWithAccountUpdated.getStatusCode());
	}
	@Test
	void createUser_InvalidEmail_ShouldReturnBadRequest() throws Exception {
		// Arrange
		HashMap<String, String> param = new HashMap<>();
		HttpHeaders headers = new HttpHeaders();
		// Mock invalid email
		String userBodyWithInvalidEmail = "{ \"first_name\": \"John\", \"last_name\": \"Doe\", \"email\": \"invalid-email\", \"password\": \"ValidPassword1!\" }";
		// Mock the validation method
		when(userValidations.validateEmail("invalid-email")).thenReturn(false);
		// Act
		ResponseEntity<Object> response = userRestController.createUser(param, headers, userBodyWithInvalidEmail);
		// Assert
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}
	@Test
	void createUser_InvalidPassword_ShouldReturnBadRequestWithMessage() throws Exception {
		// Arrange
		HashMap<String, String> param = new HashMap<>();
		HttpHeaders headers = new HttpHeaders();
		// Mock valid email
		String userBodyWithInvalidPassword = "{ \"first_name\": \"John\", \"last_name\": \"Doe\", \"email\": \"john.doe@example.com\", \"password\": \"short\" }";
		// Mock the validation methods
		when(userValidations.validateEmail("john.doe@example.com")).thenReturn(true);
		when(userValidations.isValidPassword("short")).thenReturn(false);
		// Act
		ResponseEntity<Object> response = userRestController.createUser(param, headers, userBodyWithInvalidPassword);
		// Assert
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals("Password must be at least 8 characters long, contain uppercase, lowercase, a number, and a special character.", response.getBody());
	}
//	@Test
//	void createUser_ValidEmailAndPassword_ShouldProceed() throws Exception {
//		// Arrange
//		HashMap<String, String> param = new HashMap<>();
//		HttpHeaders headers = new HttpHeaders();
//		// Mock valid email and password
//		String userBody = "{ \"first_name\": \"John\", \"last_name\": \"Doe\", \"email\": \"john.doe@example.com\", \"password\": \"ValidPassword1!\" }";
//		// Mock the validation methods
//		when(userValidations.validateEmail("john.doe@example.com")).thenReturn(true);
//		when(userValidations.isValidPassword("ValidPassword1!")).thenReturn(true);
//		// Act
//		ResponseEntity<Object> response = userRestController.createUser(param, headers, userBody);
//		// Assert
//		assertEquals(HttpStatus.CREATED, response.getStatusCode());
//	}
	//
//    @Test
//    void createUser_Success() throws Exception {
//        // Arrange
//        HashMap<String, String> params = new HashMap<>();
//        HttpHeaders headers = new HttpHeaders();
//        String requestBody = new ObjectMapper().writeValueAsString(newUser);
//
//        // Mock user validation and user service methods
//        when(userValidations.validateEmail(newUser.getEmail())).thenReturn(true);
//        when(userValidations.isValidPassword(newUser.getPassword())).thenReturn(true);
////    when(userService.getUserByEmail(newUser.getEmail())).thenReturn(null);
//        when(userService.save(any(User.class))).thenReturn(savedUser);
//
//        // Act
//        ResponseEntity<Object> response = userRestController.createUser(params, headers, requestBody);
//
//        // Assert
//        assertEquals(201, response.getStatusCodeValue());
//    }
//    @Test
//    void createUser_failure() throws Exception {
//        // Arrange
//        HashMap<String, String> params = new HashMap<>();
//        HttpHeaders headers = new HttpHeaders();
//        String requestBody = new ObjectMapper().writeValueAsString(newUser);
//
//        // Mock user validation and user service methods
//        when(userValidations.validateEmail(newUser.getEmail())).thenReturn(true);
//        when(userValidations.isValidPassword(newUser.getPassword())).thenReturn(true);
////    when(userService.getUserByEmail(newUser.getEmail())).thenReturn(null);
//        when(userService.save(any(User.class))).thenReturn(savedUser);
//
//        // Act
//        ResponseEntity<Object> response = userRestController.createUser(params, headers, requestBody);
//
//        // Assert
//        assertEquals(400, response.getStatusCodeValue());
//    }
//
//    @Test
//    void postUserWithSlash() {
//    }
//
//    @Test
//    void updateUser() {
//    }
//
//    @Test
//    void getCreds() {
//    }
//
//    @Test
//    void configureMapper() {
//    }
	@Test
	void updateUser_WithParams_ShouldReturnBadRequest() {
		// Arrange
		HashMap<String, String> params = new HashMap<>();
		params.put("id", "1"); // Simulating the presence of parameters
		HttpHeaders headers = new HttpHeaders();
		String requestBody = "{\"first_name\":\"John\",\"last_name\":\"Doe\"}"; // Example request body
		// Act
		ResponseEntity<Object> response = userRestController.updateUser(params, headers, requestBody);
		// Assert
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}
	@Test
	void updateUser_EmptyRequestBody_ShouldReturnBadRequest() {
		// Arrange
		HashMap<String, String> params = new HashMap<>();
		HttpHeaders headers = new HttpHeaders();
		String requestBody = null; // Simulating the absence of a request body
		// Act
		ResponseEntity<Object> response = userRestController.updateUser(params, headers, requestBody);
		// Assert
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}
	@Test
	void updateUser_NoCredentials_ShouldReturnBadRequest() {
		// Arrange
		HashMap<String, String> params = new HashMap<>(); // No parameters
		HttpHeaders headers = new HttpHeaders(); // No headers
		String requestBody = "{\"first_name\":\"John\",\"last_name\":\"Doe\"}"; // Example valid request body
		// Act
		ResponseEntity<Object> response = userRestController.updateUser(params, headers, requestBody);
		// Assert
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}
	@Test
	void updateUser_OnlyUsername_ShouldReturnBadRequest() {
		// Arrange
		HashMap<String, String> params = new HashMap<>(); // No parameters
		HttpHeaders headers = new HttpHeaders();
		// Simulating only username in headers
		String auth = "Basic " + Base64.getEncoder().encodeToString("username:".getBytes());
		headers.set("Authorization", auth);
		String requestBody = "{\"first_name\":\"John\",\"last_name\":\"Doe\"}"; // Example valid request body
		// Act
		ResponseEntity<Object> response = userRestController.updateUser(params, headers, requestBody);
		// Assert
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}
	@Test
	void updateUser_OnlyPassword_ShouldReturnBadRequest() {
		// Arrange
		HashMap<String, String> params = new HashMap<>(); // No parameters
		HttpHeaders headers = new HttpHeaders();
		// Simulating only password in headers
		String auth = "Basic " + Base64.getEncoder().encodeToString(":password".getBytes());
		headers.set("Authorization", auth);
		String requestBody = "{\"first_name\":\"John\",\"last_name\":\"Doe\"}"; // Example valid request body
		// Act
		ResponseEntity<Object> response = userRestController.updateUser(params, headers, requestBody);
		// Assert
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}
	@Test
	void updateUser_EmptyUsernameAndPassword_ShouldReturnBadRequest() {
		// Arrange
		HashMap<String, String> params = new HashMap<>(); // No parameters
		HttpHeaders headers = new HttpHeaders();
		// Simulating empty username and password
		String auth = "Basic " + Base64.getEncoder().encodeToString(":".getBytes());
		headers.set("Authorization", auth);
		String requestBody = "{\"first_name\":\"John\",\"last_name\":\"Doe\"}"; // Example valid request body
		// Act
		ResponseEntity<Object> response = userRestController.updateUser(params, headers, requestBody);
		// Assert
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}
	@Test
	void updateUser_InvalidCredentials_ShouldReturnUnauthorized() {
		// Arrange
		HashMap<String, String> params = new HashMap<>(); // No parameters
		HttpHeaders headers = new HttpHeaders();
		// Simulating invalid credentials
		String auth = "Basic " + Base64.getEncoder().encodeToString("invalid@mail.com:wrongpassword".getBytes());
		headers.set("Authorization", auth);
		String requestBody = "{\"first_name\":\"John\",\"last_name\":\"Doe\"}"; // Example valid request body
		// Mock the userService.authenticateUser method to return false
		when(userService.authenticateUser("invalid@mail.com", "wrongpassword")).thenReturn(false);

		// Mock the verifyUserService.getByName method to return an unverified user
		VerifyUser unverifiedUser = new VerifyUser();
		unverifiedUser.setVerified(true);
		when(verifyUserService.getByName("invalid@mail.com")).thenReturn(unverifiedUser);

		// Act
		ResponseEntity<Object> response = userRestController.updateUser(params, headers, requestBody);
		// Assert
		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
		assertEquals("Unauthorized Access", response.getBody());
	}
	@Test
	void updateUser_MissingAllRequiredFields_ShouldReturnBadRequest() {
		// Arrange
		HashMap<String, String> params = new HashMap<>(); // No parameters
		HttpHeaders headers = new HttpHeaders();
		// Valid user credentials
		String auth = "Basic " + Base64.getEncoder().encodeToString("valid@mail.com:correctpassword".getBytes());
		headers.set("Authorization", auth);
		// Creating user body with no required fields
		String requestBody = "{}"; // Empty JSON body
		// Mock the userService.authenticateUser method to return true
		when(userService.authenticateUser("valid@mail.com", "correctpassword")).thenReturn(true);

		// Mock the verifyUserService.getByName method to return an unverified user
		VerifyUser unverifiedUser = new VerifyUser();
		unverifiedUser.setVerified(true);
		when(verifyUserService.getByName("valid@mail.com")).thenReturn(unverifiedUser);

		// Act
		ResponseEntity<Object> response = userRestController.updateUser(params, headers, requestBody);
		// Assert
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}
	@Test
	void updateUser_WithIdField_ShouldReturnBadRequest() {
		// Arrange
		HashMap<String, String> params = new HashMap<>(); // No parameters
		HttpHeaders headers = new HttpHeaders();
		// Valid user credentials
		String auth = "Basic " + Base64.getEncoder().encodeToString("valid@mail.com:correctpassword".getBytes());
		headers.set("Authorization", auth);
		// Creating user body with prohibited 'id' field
		String requestBody = "{\"id\":1,\"first_name\":\"John\",\"last_name\":\"Doe\",\"password\":\"ValidPassword1!\"}"; // Contains 'id'
		// Mock the userService.authenticateUser method to return true
		when(userService.authenticateUser("valid@mail.com", "correctpassword")).thenReturn(true);
		// Mock the verifyUserService.getByName method to return an unverified user
		VerifyUser unverifiedUser = new VerifyUser();
		unverifiedUser.setVerified(true);
		when(verifyUserService.getByName("valid@mail.com")).thenReturn(unverifiedUser);

		// Act
		ResponseEntity<Object> response = userRestController.updateUser(params, headers, requestBody);
		// Assert
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}
	@Test
	void updateUser_WithEmailField_ShouldReturnBadRequest() {
		// Arrange
		HashMap<String, String> params = new HashMap<>(); // No parameters
		HttpHeaders headers = new HttpHeaders();
		// Valid user credentials
		String auth = "Basic " + Base64.getEncoder().encodeToString("valid@mail.com:correctpassword".getBytes());
		headers.set("Authorization", auth);

		// Mock the verifyUserService.getByName method to return an unverified user
		VerifyUser unverifiedUser = new VerifyUser();
		unverifiedUser.setVerified(true);
		when(verifyUserService.getByName("valid@mail.com")).thenReturn(unverifiedUser);

		// Creating user body with prohibited 'email' field
		String requestBody = "{\"email\":\"invalid@mail.com\",\"first_name\":\"John\",\"last_name\":\"Doe\",\"password\":\"ValidPassword1!\"}"; // Contains 'email'
		// Mock the userService.authenticateUser method to return true
		when(userService.authenticateUser("valid@mail.com", "correctpassword")).thenReturn(true);
		// Act
		ResponseEntity<Object> response = userRestController.updateUser(params, headers, requestBody);
		// Assert
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}
	@Test
	void updateUser_WithAccountCreatedField_ShouldReturnBadRequest() {
		// Arrange
		HashMap<String, String> params = new HashMap<>(); // No parameters
		HttpHeaders headers = new HttpHeaders();
		// Valid user credentials
		String auth = "Basic " + Base64.getEncoder().encodeToString("valid@mail.com:correctpassword".getBytes());
		headers.set("Authorization", auth);
		// Creating user body with prohibited 'accountCreated' field
		String requestBody = "{\"accountCreated\":\"2024-10-01T10:00:00Z\",\"first_name\":\"John\",\"last_name\":\"Doe\",\"password\":\"ValidPassword1!\"}"; // Contains 'accountCreated'
		// Mock the userService.authenticateUser method to return true
		when(userService.authenticateUser("valid@mail.com", "correctpassword")).thenReturn(true);

		// Mock the verifyUserService.getByName method to return an unverified user
		VerifyUser unverifiedUser = new VerifyUser();
		unverifiedUser.setVerified(true);
		when(verifyUserService.getByName("valid@mail.com")).thenReturn(unverifiedUser);

		// Act
		ResponseEntity<Object> response = userRestController.updateUser(params, headers, requestBody);
		// Assert
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}
	@Test
	void updateUser_WithAccountUpdatedField_ShouldReturnBadRequest() {
		// Arrange
		HashMap<String, String> params = new HashMap<>(); // No parameters
		HttpHeaders headers = new HttpHeaders();
		// Valid user credentials
		String auth = "Basic " + Base64.getEncoder().encodeToString("valid@mail.com:correctpassword".getBytes());
		headers.set("Authorization", auth);
		// Creating user body with prohibited 'accountUpdated' field
		String requestBody = "{\"accountUpdated\":\"2024-10-01T10:00:00Z\",\"first_name\":\"John\",\"last_name\":\"Doe\",\"password\":\"ValidPassword1!\"}"; // Contains 'accountUpdated'
		// Mock the userService.authenticateUser method to return true
		when(userService.authenticateUser("valid@mail.com", "correctpassword")).thenReturn(true);

		// Mock the verifyUserService.getByName method to return an unverified user
		VerifyUser unverifiedUser = new VerifyUser();
		unverifiedUser.setVerified(true);
		when(verifyUserService.getByName("valid@mail.com")).thenReturn(unverifiedUser);
		// Act
		ResponseEntity<Object> response = userRestController.updateUser(params, headers, requestBody);
		// Assert
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}
	@Test
	void updateUser_WithInvalidPassword_ShouldReturnBadRequest() {
		// Arrange
		HashMap<String, String> params = new HashMap<>(); // No parameters
		HttpHeaders headers = new HttpHeaders();
		// Valid user credentials
		String auth = "Basic " + Base64.getEncoder().encodeToString("valid@mail.com:correctpassword".getBytes());
		headers.set("Authorization", auth);
		// Creating user body with an invalid password (e.g., too short, no special character)
		String requestBody = "{\"first_name\":\"John\",\"last_name\":\"Doe\",\"password\":\"short\"}"; // Invalid password
		// Mock the userService.authenticateUser method to return true
		when(userService.authenticateUser("valid@mail.com", "correctpassword")).thenReturn(true);
		// Mock the password validation method to return false for invalid password
		when(userValidations.isValidPassword("short")).thenReturn(false);

		// Mock the verifyUserService.getByName method to return an unverified user
		VerifyUser unverifiedUser = new VerifyUser();
		unverifiedUser.setVerified(true);
		when(verifyUserService.getByName("valid@mail.com")).thenReturn(unverifiedUser);

		// Act
		ResponseEntity<Object> response = userRestController.updateUser(params, headers, requestBody);
		// Assert
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals("Password must be at least 8 characters long, contain uppercase, lowercase, a number, and a special character.", response.getBody());
	}
	//    @Test
//    void updateUser_SuccessfulUpdate_ShouldReturnNoContent() {
//        // Arrange
//        HashMap<String, String> params = new HashMap<>(); // No parameters
//        HttpHeaders headers = new HttpHeaders();
//
//        // Valid user credentials
//        String auth = "Basic " + Base64.getEncoder().encodeToString("1@mail.com:ffffff@1A".getBytes());
//        headers.set("Authorization", auth);
//
//        // Creating a valid user body
//        String requestBody = "{\"first_name\":\"John\",\"last_name\":\"Doe\",\"password\":\"NewValidPass1!\"}";
//
//        // Mock the userService.authenticateUser method to return true
//        when(userService.authenticateUser("1@mail.com", "ffffff@1A")).thenReturn(true);
//
//
//        // Mock user retrieval
////        User existingUser = new User();
////        existingUser.setEmail("valid@mail.com");
////        existingUser.setFirstName("OldName");
////        existingUser.setLastName("OldLastName");
////        existingUser.setPassword("OldPassword");
////
////        when(userService.getUserByEmail("valid@mail.com")).thenReturn(existingUser);
////
////        // Mock the userService.save method to return the updated user
////        when(userService.save(existingUser)).thenReturn(existingUser);
//
//        // Act
//        ResponseEntity<Object> response = userRestController.updateUser(params, headers, requestBody);
//
//        // Assert
//        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
//    }
	@Test
	void handleDeleteUserStatus_ShouldReturnMethodNotAllowed() {
		// Act
		ResponseEntity<String> response = userRestController.handleDeleteUserStatus();
		// Assert
		assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
		assertNotNull(response.getHeaders());
		assertEquals("no-cache, no-store, must-revalidate", response.getHeaders().getFirst(HttpHeaders.CACHE_CONTROL));
		assertEquals("no-cache", response.getHeaders().getFirst("Pragma"));
		assertNull(response.getBody());  // Since there is no body returned
	}
	@Test
	void handlePatchUserStatus_ShouldReturnMethodNotAllowed() {
		// Act
		ResponseEntity<String> response = userRestController.handlePatchUserStatus();
		// Assert
		assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
		assertNotNull(response.getHeaders());
		assertEquals("no-cache, no-store, must-revalidate", response.getHeaders().getFirst(HttpHeaders.CACHE_CONTROL));
		assertEquals("no-cache", response.getHeaders().getFirst("Pragma"));
		assertNull(response.getBody());  // Since there is no body returned
	}
	@Test
	void handleHeadUserStatus_ShouldReturnMethodNotAllowed() {
		// Act
		ResponseEntity<String> response = userRestController.handleHeadUserStatus();
		// Assert
		assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
		assertNotNull(response.getHeaders());
		assertEquals("no-cache, no-store, must-revalidate", response.getHeaders().getFirst(HttpHeaders.CACHE_CONTROL));
		assertEquals("no-cache", response.getHeaders().getFirst("Pragma"));
		assertNull(response.getBody());  // Since there is no body returned
	}
	@Test
	void handleOptionsUserStatus_ShouldReturnMethodNotAllowed() {
		// Act
		ResponseEntity<String> response = userRestController.handleOptionsUserStatus();
		// Assert
		assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
		assertNotNull(response.getHeaders());
		assertEquals("no-cache, no-store, must-revalidate", response.getHeaders().getFirst(HttpHeaders.CACHE_CONTROL));
		assertEquals("no-cache", response.getHeaders().getFirst("Pragma"));
		assertNull(response.getBody());  // Since there is no body returned
	}
}
