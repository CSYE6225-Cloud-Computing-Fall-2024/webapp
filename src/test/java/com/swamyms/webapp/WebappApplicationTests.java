package com.swamyms.webapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swamyms.webapp.entity.AddUser;
import com.swamyms.webapp.service.UserService;
import com.swamyms.webapp.validations.UserValidations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WebappApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserService userService;

	@Autowired
	private UserValidations userValidations;

	private String basicAuthHeader;
	@Autowired
	private ObjectMapper objectMapper;

	@BeforeEach
	public void setup() {
		// Prepare Basic Auth header for tests
		String username = "testuser1@example.com";
		String password = "Password123!";
		basicAuthHeader = "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));

//        // Optionally, you can create a user before tests
//        User user = new User();
//        user.setEmail(username);
//        user.setPassword(password);
//        user.setFirstName("Test1");
//        user.setLastName("User1");
//        userService.save(user);
	}

	@Test
	void testCreateUser() throws Exception {
		AddUser newUser = new AddUser();
		newUser.setEmail("TestingWithGradle@example.com");
		newUser.setPassword("TestingWithGradle123!");
		newUser.setFirst_name("TestingWithGradle");
		newUser.setLast_name("TestingWithGradle");

		mockMvc.perform(post("/v1/user")
						.contentType(MediaType.APPLICATION_JSON)
						.content(new ObjectMapper().writeValueAsString(newUser)))
				.andExpect(status().isCreated())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON));
	}

//	@Test
//	void testGetUser() throws Exception {
//		mockMvc.perform(get("/v1/user/self")
//						.header(HttpHeaders.AUTHORIZATION, basicAuthHeader))
//				.andExpect(status().isOk())
//				.andExpect(content().contentType(MediaType.APPLICATION_JSON));
//	}
//
//	@Test
//	void testCreateUser_UserAlreadyExists() throws Exception {
//		AddUser newUser = new AddUser();
//		newUser.setEmail("linux@example.com");
//		newUser.setPassword("Linux123!");
//		newUser.setFirst_name("Linux");
//		newUser.setLast_name("Linux");
//
////        // Mock existing user check
////        User existingUser = new User();
////        existingUser.setEmail(newUser.getEmail());
////        Mockito.when(userService.getUserByEmail(newUser.getEmail())).thenReturn(existingUser);
//
//		// Perform POST request and expect failure due to existing user
//		mockMvc.perform(post("/v1/user")
//						.contentType(MediaType.APPLICATION_JSON)
//						.content(objectMapper.writeValueAsString(newUser)))
//				.andExpect(status().isBadRequest());
////                .andExpect(content().string("User Email Already Exists: linux@example.com"));
//	}
//	@Test
//	void testCreateUser_InvalidEmail() throws Exception {
//		AddUser newUser = new AddUser();
//		newUser.setEmail("invalid-email");
//		newUser.setPassword("Linux123!");
//		newUser.setFirst_name("Linux");
//		newUser.setLast_name("Linux");
//
//		// Perform POST request and expect failure due to invalid email
//		mockMvc.perform(post("/v1/user")
//						.contentType(MediaType.APPLICATION_JSON)
//						.content(objectMapper.writeValueAsString(newUser)))
//				.andExpect(status().isBadRequest());
//	}
//
//	@Test
//	void testCreateUser_InvalidPassword() throws Exception {
//		AddUser newUser = new AddUser();
//		newUser.setEmail("linux@example.com");
//		newUser.setPassword("short");
//		newUser.setFirst_name("Linux");
//		newUser.setLast_name("Linux");
//
//		// Perform POST request and expect failure due to invalid password
//		mockMvc.perform(post("/v1/user")
//						.contentType(MediaType.APPLICATION_JSON)
//						.content(objectMapper.writeValueAsString(newUser)))
//				.andExpect(status().isBadRequest())
//				.andExpect(content().string("Password must be at least 8 characters long, contain uppercase, lowercase, a number, and a special character."));
//	}
//
//	@Test
//	void testCreateUser_MissingFields() throws Exception {
//		AddUser newUser = new AddUser();
//		newUser.setEmail("linux@example.com");
//
//		// Perform POST request with missing fields and expect failure
//		mockMvc.perform(post("/v1/user")
//						.contentType(MediaType.APPLICATION_JSON)
//						.content(objectMapper.writeValueAsString(newUser)))
//				.andExpect(status().isBadRequest());
//	}
//
//	@Test
//	void testGetUser_Unauthorized() throws Exception {
//		// Mock failed authentication
////        Mockito.when(userService.authenticateUser("testuser1@example.com", "Password1123!")).thenReturn(false);
//
//		String username = "testuser1@example.com";
//		String password = "Password1123!";
//		String basicAuthHeaderUnauthorized = "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
//
//		// Perform GET request and expect Unauthorized status
//		mockMvc.perform(get("/v1/user/self")
//						.header(HttpHeaders.AUTHORIZATION, basicAuthHeaderUnauthorized))
//				.andExpect(status().isUnauthorized());
//	}
//
//	@Test
//	void testGetUser_BadRequest_WithParams() throws Exception {
//		// Perform GET request with params, expect bad request due to params being present
//		mockMvc.perform(get("/v1/user/self")
//						.param("someParam", "value")
//						.header(HttpHeaders.AUTHORIZATION, basicAuthHeader))
//				.andExpect(status().isBadRequest());
//	}
//
//	@Test
//	void testGetUser_BadRequest_WithRequestBody() throws Exception {
//		// Perform GET request with a request body, expect bad request due to request body being present
//		mockMvc.perform(get("/v1/user/self")
//						.header(HttpHeaders.AUTHORIZATION, basicAuthHeader)
//						.content("{\"some\":\"value\"}")
//						.contentType(MediaType.APPLICATION_JSON))
//				.andExpect(status().isBadRequest());
//	}
//
//	@Test
//	void testUpdateUser_Successful() throws Exception {
////        // Mock user retrieval and successful update
////        User user = new User();
////        user.setEmail("testuser1@example.com");
////        user.setFirstName("Test");
////        user.setLastName("User");
////        user.setPassword("TestPassword1123!");
////
////        Mockito.when(userService.authenticateUser("testuser1@example.com", "TestPassword1123!")).thenReturn(true);
////        Mockito.when(userService.getUserByEmail("testuser1@example.com")).thenReturn(user);
////        Mockito.when(userService.save(Mockito.any(User.class))).thenReturn(user);
//
//		AddUser updateUser = new AddUser();
//		updateUser.setFirst_name("UpdatedFirstName");
//		updateUser.setLast_name("UpdatedLastName");
//		updateUser.setPassword("Password123!");
//
//		mockMvc.perform(put("/v1/user/self")
//						.header(HttpHeaders.AUTHORIZATION, basicAuthHeader)
//						.contentType(MediaType.APPLICATION_JSON)
//						.content(objectMapper.writeValueAsString(updateUser)))
//				.andExpect(status().isNoContent());
//	}

}
