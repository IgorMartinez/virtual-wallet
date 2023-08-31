package br.com.igormartinez.virtualwallet.integrationtests.controllers;

import static io.restassured.RestAssured.given;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

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
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestMethodOrder(OrderAnnotation.class)
public class UserControllerTest extends AbstractIntegrationTest {

    private static RequestSpecification specification;
    
    private static Long USER_ID;
    private static String USER_NAME = "User Controller Test";
    private static String USER_DOCUMENT = "202.308.250-17";
    private static String USER_EMAIL = "usercontroller@integration.test";
	private static String USER_PASSWORD = "securedpassword";
    private static String USER_ROLE = "COMMON";

    @Test
    @Order(0)
    void testFindByIdAsUnauthenticated() {
        ApiErrorResponse output = 
            given()
				.basePath("/api/v1/user")
					.port(TestConfigs.SERVER_PORT)
					.contentType(TestConfigs.CONTENT_TYPE_JSON)
                    .pathParam("user-id", 1)
				.when()
				    .get("/{user-id}")
				.then()
					.statusCode(HttpStatus.FORBIDDEN.value())
						.extract()
							.body()
                                .as(ApiErrorResponse.class);
        
        assertEquals("about:blank", output.type());
        assertEquals("Forbidden", output.title());
        assertEquals(HttpStatus.FORBIDDEN.value(), output.status());
        assertEquals("Authentication required.", output.detail());
        assertEquals("/api/v1/user/1", output.instance());
        assertNull(output.errors());
    }

    @Test
    @Order(100)
    void signupAndAuthentication() {
        RegistrationDTO registrationDTO = 
            new RegistrationDTO(USER_NAME, USER_DOCUMENT, USER_EMAIL, USER_PASSWORD, USER_ROLE);

        USER_ID = 
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
                                .as(UserDTO.class)
                                    .id();
        
        AccountCredentials accountCredentials = new AccountCredentials(USER_EMAIL, USER_PASSWORD);

        String accessToken = 
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
								.as(Token.class)
								    .getAccessToken();
        
        specification = new RequestSpecBuilder()
			.addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + accessToken)
			.setBasePath("/api/v1/user")
			.setPort(TestConfigs.SERVER_PORT)
			.setContentType(TestConfigs.CONTENT_TYPE_JSON)
			.build();
    }

    @Test
    @Order(110)
    void testFindByIdWithSameUser() {
        UserDTO output = 
            given()
                .spec(specification)
                    .pathParam("user-id", USER_ID)
                .when()
                    .get("{user-id}")
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
    @Order(110)
    void testFindByIdWithOtherUser() {
        ApiErrorResponse output = 
            given()
                .spec(specification)
                    .pathParam("user-id", USER_ID+1)
				.when()
				    .get("/{user-id}")
				.then()
					.statusCode(HttpStatus.UNAUTHORIZED.value())
						.extract()
							.body()
                                .as(ApiErrorResponse.class);
        
        assertEquals("about:blank", output.type());
        assertEquals("Unauthorized", output.title());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), output.status());
        assertEquals("The user is not authorized to access this resource.", output.detail());
        assertEquals("/api/v1/user/"+(USER_ID+1), output.instance());
        assertNull(output.errors());
    }

    @Test
    @Order(110)
    void testFindByIdWithIdInvalid() {
        ApiErrorResponse output = 
            given()
                .spec(specification)
                    .pathParam("user-id", 0)
                .when()
                    .get("{user-id}")
                .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract()
                    .body()
                        .as(ApiErrorResponse.class);
        
        assertEquals("about:blank", output.type());
        assertEquals("Bad Request", output.title());
        assertEquals(HttpStatus.BAD_REQUEST.value(), output.status());
        assertEquals("The user-id must be a positive integer value.", output.detail());
        assertEquals("/api/v1/user/0", output.instance());
        assertNull(output.errors());
    }
}
