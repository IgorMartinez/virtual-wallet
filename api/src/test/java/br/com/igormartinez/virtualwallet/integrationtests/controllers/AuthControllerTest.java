package br.com.igormartinez.virtualwallet.integrationtests.controllers;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import br.com.igormartinez.virtualwallet.configs.TestConfigs;
import br.com.igormartinez.virtualwallet.data.ApiErrorResponse;
import br.com.igormartinez.virtualwallet.data.dto.RegistrationDTO;
import br.com.igormartinez.virtualwallet.data.dto.UserDTO;
import br.com.igormartinez.virtualwallet.data.security.AccountCredentials;
import br.com.igormartinez.virtualwallet.data.security.Token;
import br.com.igormartinez.virtualwallet.integrationtests.testcontainers.AbstractIntegrationTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestMethodOrder(OrderAnnotation.class)
public class AuthControllerTest extends AbstractIntegrationTest {

    private static Token TOKEN;
    
    private static String USER_NAME = "Auth Controller Test";
    private static String USER_DOCUMENT = "023.008.024-00";
    private static String USER_EMAIL = "authcontroller@integration.test";
	private static String USER_PASSWORD = "securedpassword";
    private static String USER_ROLE = "COMMON";

    @Test
    @Order(0)
    void testSignup() {
        RegistrationDTO registrationDTO = 
            new RegistrationDTO(USER_NAME, USER_DOCUMENT, USER_EMAIL, USER_PASSWORD, USER_ROLE);

        UserDTO output = 
            given()
                .basePath("/auth/signup")
                    .port(TestConfigs.SERVER_PORT)
                    .contentType(TestConfigs.CONTENT_TYPE_JSON)
                    .body(registrationDTO)
                .when()
                    .post()
                .then()
                    .statusCode(HttpStatus.OK.value())
                        .extract()
                            .body()
                                .as(UserDTO.class);

        assertTrue(output.id() > 0);
        assertEquals(USER_NAME, output.name());
        assertEquals(USER_DOCUMENT, output.document());
        assertEquals(USER_EMAIL, output.email());
        assertEquals(new BigDecimal("0.00"), output.accountBalance());
        assertEquals(USER_ROLE, output.role());
    }

    @Test
    @Order(0)
    void testSignupWithoutBody() {
        ApiErrorResponse output = 
            given()
                .basePath("/auth/signup")
                    .port(TestConfigs.SERVER_PORT)
                    .contentType(TestConfigs.CONTENT_TYPE_JSON)
                .when()
                    .post()
                .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                        .extract()
                            .body()
                                .as(ApiErrorResponse.class);
        
        assertEquals("about:blank", output.type());
        assertEquals("Bad Request", output.title());
        assertEquals(HttpStatus.BAD_REQUEST.value(), output.status());
        assertEquals("Failed to read request", output.detail());
        assertEquals("/auth/signup", output.instance());
        assertNull(output.errors());
    }

    @Test
    @Order(0)
    void testSignupWithFieldsBlankOrNull() {
        RegistrationDTO registrationDTO = 
            new RegistrationDTO(" ", USER_DOCUMENT, null, USER_PASSWORD, USER_ROLE);
        
        ApiErrorResponse output = 
            given()
                .basePath("/auth/signup")
                    .port(TestConfigs.SERVER_PORT)
                    .contentType(TestConfigs.CONTENT_TYPE_JSON)
                    .body(registrationDTO)
                .when()
                    .post()
                .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                        .extract()
                            .body()
                                .as(ApiErrorResponse.class);
        
        assertEquals("about:blank", output.type());
        assertEquals("Bad Request", output.title());
        assertEquals(HttpStatus.BAD_REQUEST.value(), output.status());
        assertEquals("Invalid request content.", output.detail());
        assertEquals("/auth/signup", output.instance());
        assertEquals(2, output.errors().size());
        assertEquals("The name must be not blank.", output.errors().get("name"));
        assertEquals("The email must be not blank.", output.errors().get("email"));
    }

    @Test
    @Order(0)
    void testSignupWithRoleNotFound() {
        RegistrationDTO registrationDTO = 
            new RegistrationDTO(USER_NAME, USER_DOCUMENT+"asdfera", USER_EMAIL+"asdfera", USER_PASSWORD, "asdfera");
        
        ApiErrorResponse output = 
            given()
                .basePath("/auth/signup")
                    .port(TestConfigs.SERVER_PORT)
                    .contentType(TestConfigs.CONTENT_TYPE_JSON)
                    .body(registrationDTO)
                .when()
                    .post()
                .then()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                        .extract()
                            .body()
                                .as(ApiErrorResponse.class);
        
        assertEquals("about:blank", output.type());
        assertEquals("Not Found", output.title());
        assertEquals(HttpStatus.NOT_FOUND.value(), output.status());
        assertEquals("The role was not found with the given description.", output.detail());
        assertEquals("/auth/signup", output.instance());
        assertNull(output.errors());
    }

    @Test
    @Order(10)
    void testSignupWithDuplicatedEmail() {
        RegistrationDTO registrationDTO = 
            new RegistrationDTO(USER_NAME, USER_DOCUMENT, USER_EMAIL, USER_PASSWORD, USER_ROLE);
        
        ApiErrorResponse output = 
            given()
                .basePath("/auth/signup")
                    .port(TestConfigs.SERVER_PORT)
                    .contentType(TestConfigs.CONTENT_TYPE_JSON)
                    .body(registrationDTO)
                .when()
                    .post()
                .then()
                    .statusCode(HttpStatus.CONFLICT.value())
                        .extract()
                            .body()
                                .as(ApiErrorResponse.class);
        
        assertEquals("about:blank", output.type());
        assertEquals("Conflict", output.title());
        assertEquals(HttpStatus.CONFLICT.value(), output.status());
        assertEquals("The email or document is already in use.", output.detail());
        assertEquals("/auth/signup", output.instance());
        assertNull(output.errors());
    }

    @Test
    @Order(100)
    void testSignin() {
        AccountCredentials accountCredentials = 
            new AccountCredentials(USER_EMAIL, USER_PASSWORD);

        TOKEN = 
            given()
				.basePath("/auth/signin")
					.port(TestConfigs.SERVER_PORT)
					.contentType(TestConfigs.CONTENT_TYPE_JSON)
				.body(accountCredentials)
				.when()
				    .post()
				.then()
					.statusCode(HttpStatus.OK.value())
					.extract()
					    .body()
						    .as(Token.class);

        assertEquals(USER_EMAIL, TOKEN.getUsername());
        assertTrue(TOKEN.getAuthenticated());
        assertNotNull(TOKEN.getCreated());
        assertNotNull(TOKEN.getExpiration());
        assertNotNull(TOKEN.getAccessToken());
        assertNotNull(TOKEN.getRefreshToken());
    }

    @Test
    @Order(100)
    void testSigninWithoutBody() {
        ApiErrorResponse output = 
            given()
				.basePath("/auth/signin")
					.port(TestConfigs.SERVER_PORT)
					.contentType(TestConfigs.CONTENT_TYPE_JSON)
				.when()
				    .post()
				.then()
					.statusCode(HttpStatus.BAD_REQUEST.value())
					.extract()
					    .body()
						    .as(ApiErrorResponse.class);

        assertEquals("about:blank", output.type());
        assertEquals("Bad Request", output.title());
        assertEquals(HttpStatus.BAD_REQUEST.value(), output.status());
        assertEquals("Failed to read request", output.detail());
        assertEquals("/auth/signin", output.instance());
        assertNull(output.errors());
    }

    @Test
    @Order(100)
    void testSigninWithWrongPassword() {
        AccountCredentials accountCredentials = 
            new AccountCredentials("wrongemail", USER_PASSWORD);

        ApiErrorResponse output = 
            given()
				.basePath("/auth/signin")
					.port(TestConfigs.SERVER_PORT)
					.contentType(TestConfigs.CONTENT_TYPE_JSON)
                    .body(accountCredentials)
				.when()
				    .post()
				.then()
					.statusCode(HttpStatus.UNAUTHORIZED.value())
					.extract()
					    .body()
						    .as(ApiErrorResponse.class);

        assertEquals("about:blank", output.type());
        assertEquals("Unauthorized", output.title());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), output.status());
        assertEquals("Invalid email or password.", output.detail());
        assertEquals("/auth/signin", output.instance());
        assertNull(output.errors());
    }

    @Test
    @Order(200)
    void testRefreshToken() {
        TOKEN = 
            given()
				.basePath("/auth/refresh")
					.port(TestConfigs.SERVER_PORT)
					.contentType(TestConfigs.CONTENT_TYPE_JSON)
                    .header(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + TOKEN.getRefreshToken())
				.when()
				    .put()
				.then()
					.statusCode(HttpStatus.OK.value())
                    .extract()
                        .body()
                            .as(Token.class);
        
        assertEquals(USER_EMAIL, TOKEN.getUsername());
        assertTrue(TOKEN.getAuthenticated());
        assertNotNull(TOKEN.getCreated());
        assertNotNull(TOKEN.getExpiration());
        assertNotNull(TOKEN.getAccessToken());
        assertNotNull(TOKEN.getRefreshToken());
    }

    @Test
    @Order(200)
    void testRefreshTokenWithoutHeader() {
        ApiErrorResponse output = 
            given()
				.basePath("/auth/refresh")
					.port(TestConfigs.SERVER_PORT)
					.contentType(TestConfigs.CONTENT_TYPE_JSON)
				.when()
				    .put()
				.then()
					.statusCode(HttpStatus.BAD_REQUEST.value())
                    .extract()
                        .body()
                            .as(ApiErrorResponse.class);
                            
        assertEquals("about:blank", output.type());
        assertEquals("Bad Request", output.title());
        assertEquals(HttpStatus.BAD_REQUEST.value(), output.status());
        assertEquals("Required header 'Authorization' is not present.", output.detail());
        assertEquals("/auth/refresh", output.instance());
        assertNull(output.errors());
    }

    @Test
    @Order(200)
    void testRefreshTokenWithTokenBlank() {
        ApiErrorResponse output = 
            given()
				.basePath("/auth/refresh")
					.port(TestConfigs.SERVER_PORT)
					.contentType(TestConfigs.CONTENT_TYPE_JSON)
                    .header(TestConfigs.HEADER_PARAM_AUTHORIZATION, "")
				.when()
				    .put()
				.then()
					.statusCode(HttpStatus.BAD_REQUEST.value())
                    .extract()
                        .body()
                            .as(ApiErrorResponse.class);
        
        assertEquals("about:blank", output.type());
        assertEquals("Bad Request", output.title());
        assertEquals(HttpStatus.BAD_REQUEST.value(), output.status());
        assertEquals("The refresh token must be not blank.", output.detail());
        assertEquals("/auth/refresh", output.instance());
        assertNull(output.errors());
    }

    @Test
    @Order(200)
    void testRefreshTokenWithTokenInvalid() {
        ApiErrorResponse output = 
            given()
				.basePath("/auth/refresh")
					.port(TestConfigs.SERVER_PORT)
					.contentType(TestConfigs.CONTENT_TYPE_JSON)
                    .header(TestConfigs.HEADER_PARAM_AUTHORIZATION, "aaaaaa")
				.when()
				    .put()
				.then()
					.statusCode(HttpStatus.UNAUTHORIZED.value())
                    .extract()
                        .body()
                            .as(ApiErrorResponse.class);
        
        assertEquals("about:blank", output.type());
        assertEquals("Unauthorized", output.title());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), output.status());
        assertEquals("Invalid refresh token.", output.detail());
        assertEquals("/auth/refresh", output.instance());
        assertNull(output.errors());
    }

    @Test
    @Order(200)
    void testRefreshTokenWithTokenExpired() {
        ApiErrorResponse output = 
            given()
				.basePath("/auth/refresh")
					.port(TestConfigs.SERVER_PORT)
					.contentType(TestConfigs.CONTENT_TYPE_JSON)
                    .header(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + TestConfigs.EXPIRED_REFRESH_TOKEN)
                
                .when()
				    .put()
				.then()
					.statusCode(HttpStatus.UNAUTHORIZED.value())
                    .extract()
                        .body()
                            .as(ApiErrorResponse.class);
        
        assertEquals("about:blank", output.type());
        assertEquals("Unauthorized", output.title());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), output.status());
        assertEquals("Invalid refresh token.", output.detail());
        assertEquals("/auth/refresh", output.instance());
        assertNull(output.errors());
    }
}
