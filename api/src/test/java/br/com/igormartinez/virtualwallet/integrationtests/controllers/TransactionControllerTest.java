package br.com.igormartinez.virtualwallet.integrationtests.controllers;

import static io.restassured.RestAssured.given;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import br.com.igormartinez.virtualwallet.configs.TestConfigs;
import br.com.igormartinez.virtualwallet.data.ApiErrorResponse;
import br.com.igormartinez.virtualwallet.data.dto.PersonalTransactionDTO;
import br.com.igormartinez.virtualwallet.data.dto.RegistrationDTO;
import br.com.igormartinez.virtualwallet.data.dto.TransactionDTO;
import br.com.igormartinez.virtualwallet.data.dto.TransferTransactionDTO;
import br.com.igormartinez.virtualwallet.data.dto.UserDTO;
import br.com.igormartinez.virtualwallet.data.security.AccountCredentials;
import br.com.igormartinez.virtualwallet.data.security.Token;
import br.com.igormartinez.virtualwallet.enums.TransactionType;
import br.com.igormartinez.virtualwallet.integrationtests.testcontainers.AbstractIntegrationTest;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestMethodOrder(OrderAnnotation.class)
public class TransactionControllerTest extends AbstractIntegrationTest {

    private static RequestSpecification specification;

    private static Long TRANSACTION_DEPOSIT_ID; // testDepositWithSameUser()
    private static Long TRANSACTION_WITHDRAWAL_ID; // testWithdrawalWithSameUserAndSuficientBalance()
    private static Long TRANSACTION_TRANSFER_ID; // testTransferWithSameUserAndSuficientBalance()
    
    private static Long USER_ID; // signupAndAuthentication()
    private static String USER_ACCESS_TOKEN; // signupAndAuthentication()
    private static String USER_NAME = "Transaction Controller Test";
    private static String USER_DOCUMENT = "202.308.290-14";
    private static String USER_EMAIL = "transactioncontroller@integration.test";
	private static String USER_PASSWORD = "securedpassword";
    private static String USER_ROLE = "COMMON";

    private static Long TRANSFER_USER_ID; // signupTransferUser()
    private static String TRANSFER_USER_NAME = "Transaction Controller Test 2";
    private static String TRANSFER_USER_DOCUMENT = "202.308.290-15";
    private static String TRANSFER_USER_EMAIL = "transactioncontroller2@integration.test";
	private static String TRANSFER_USER_PASSWORD = "securedpassword";
    private static String TRANSFER_USER_ACCESS_TOKEN; // signupTransferUserAndGetAccessToken()
    private static String TRANSFER_USER_ROLE = "COMMON";
    
    @Test
    @Order(0)
    void testDepositAsUnauthenticated() {
        ApiErrorResponse output = 
            given()
				.basePath("/api/v1/transaction")
					.port(TestConfigs.SERVER_PORT)
					.contentType(TestConfigs.CONTENT_TYPE_JSON)
				.when()
				    .post("/deposit")
				.then()
					.statusCode(HttpStatus.FORBIDDEN.value())
						.extract()
							.body()
                                .as(ApiErrorResponse.class);
        
        assertEquals("about:blank", output.type());
        assertEquals("Forbidden", output.title());
        assertEquals(HttpStatus.FORBIDDEN.value(), output.status());
        assertEquals("Authentication required.", output.detail());
        assertEquals("/api/v1/transaction/deposit", output.instance());
        assertNull(output.errors());
    }

    @Test
    @Order(0)
    void testWithdrawalAsUnauthenticated() {
        ApiErrorResponse output = 
            given()
				.basePath("/api/v1/transaction")
					.port(TestConfigs.SERVER_PORT)
					.contentType(TestConfigs.CONTENT_TYPE_JSON)
				.when()
				    .post("/withdrawal")
				.then()
					.statusCode(HttpStatus.FORBIDDEN.value())
						.extract()
							.body()
                                .as(ApiErrorResponse.class);
        
        assertEquals("about:blank", output.type());
        assertEquals("Forbidden", output.title());
        assertEquals(HttpStatus.FORBIDDEN.value(), output.status());
        assertEquals("Authentication required.", output.detail());
        assertEquals("/api/v1/transaction/withdrawal", output.instance());
        assertNull(output.errors());
    }

    @Test
    @Order(0)
    void testTransferAsUnauthenticated() {
        ApiErrorResponse output = 
            given()
				.basePath("/api/v1/transaction")
					.port(TestConfigs.SERVER_PORT)
					.contentType(TestConfigs.CONTENT_TYPE_JSON)
				.when()
				    .post("/transfer")
				.then()
					.statusCode(HttpStatus.FORBIDDEN.value())
						.extract()
							.body()
                                .as(ApiErrorResponse.class);
        
        assertEquals("about:blank", output.type());
        assertEquals("Forbidden", output.title());
        assertEquals(HttpStatus.FORBIDDEN.value(), output.status());
        assertEquals("Authentication required.", output.detail());
        assertEquals("/api/v1/transaction/transfer", output.instance());
        assertNull(output.errors());
    }

    @Test
    @Order(0)
    void testFindByIdAsUnauthenticated() {
        ApiErrorResponse output = 
            given()
				.basePath("/api/v1/transaction")
					.port(TestConfigs.SERVER_PORT)
					.contentType(TestConfigs.CONTENT_TYPE_JSON)
                    .pathParam("transaction-id", 1)
				.when()
				    .post("/{transaction-id}")
				.then()
					.statusCode(HttpStatus.FORBIDDEN.value())
						.extract()
							.body()
                                .as(ApiErrorResponse.class);
        
        assertEquals("about:blank", output.type());
        assertEquals("Forbidden", output.title());
        assertEquals(HttpStatus.FORBIDDEN.value(), output.status());
        assertEquals("Authentication required.", output.detail());
        assertEquals("/api/v1/transaction/1", output.instance());
        assertNull(output.errors());
    }

    @Test
    @Order(0)
    void testFindAllByUserAsUnauthenticated() {
        ApiErrorResponse output = 
            given()
				.basePath("/api/v1/transaction")
					.port(TestConfigs.SERVER_PORT)
					.contentType(TestConfigs.CONTENT_TYPE_JSON)
                    .pathParam("user-id", 1)
				.when()
				    .post("/user/{user-id}")
				.then()
					.statusCode(HttpStatus.FORBIDDEN.value())
						.extract()
							.body()
                                .as(ApiErrorResponse.class);
        
        assertEquals("about:blank", output.type());
        assertEquals("Forbidden", output.title());
        assertEquals(HttpStatus.FORBIDDEN.value(), output.status());
        assertEquals("Authentication required.", output.detail());
        assertEquals("/api/v1/transaction/user/1", output.instance());
        assertNull(output.errors());
    }

    @Test
    @Order(99)
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

        USER_ACCESS_TOKEN = 
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
			.addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + USER_ACCESS_TOKEN)
			.setBasePath("/api/v1/transaction")
			.setPort(TestConfigs.SERVER_PORT)
			.setContentType(TestConfigs.CONTENT_TYPE_JSON)
			.build();
    }

    @Test
    @Order(99)
    void signupTransferUserAndGetAccessToken() {
        RegistrationDTO registrationDTO = 
            new RegistrationDTO(TRANSFER_USER_NAME, TRANSFER_USER_DOCUMENT, 
                TRANSFER_USER_EMAIL, TRANSFER_USER_PASSWORD, TRANSFER_USER_ROLE);

        TRANSFER_USER_ID = 
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

        AccountCredentials accountCredentials = new AccountCredentials(TRANSFER_USER_EMAIL, TRANSFER_USER_PASSWORD);
        TRANSFER_USER_ACCESS_TOKEN = 
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
    }

    @Test
    @Order(100)
    void testDepositWithoutBody() {
        ApiErrorResponse output = 
            given()
				.spec(specification)
				.when()
				    .post("/deposit")
				.then()
					.statusCode(HttpStatus.BAD_REQUEST.value())
						.extract()
							.body()
                                .as(ApiErrorResponse.class);

        assertEquals("about:blank", output.type());
        assertEquals("Bad Request", output.title());
        assertEquals(HttpStatus.BAD_REQUEST.value(), output.status());
        assertEquals("Failed to read request", output.detail());
        assertEquals("/api/v1/transaction/deposit", output.instance());
        assertNull(output.errors());
    }

    @Test
    @Order(100)
    void testDepositWithFieldsNull() {
        PersonalTransactionDTO personalTransactionDTO 
            = new PersonalTransactionDTO(null, null);

        ApiErrorResponse output = 
            given()
				.spec(specification)
                    .body(personalTransactionDTO)
				.when()
				    .post("/deposit")
				.then()
					.statusCode(HttpStatus.BAD_REQUEST.value())
						.extract()
							.body()
                                .as(ApiErrorResponse.class);

        assertEquals("about:blank", output.type());
        assertEquals("Bad Request", output.title());
        assertEquals(HttpStatus.BAD_REQUEST.value(), output.status());
        assertEquals("Invalid request content.", output.detail());
        assertEquals("/api/v1/transaction/deposit", output.instance());
        assertEquals(2, output.errors().size());
        assertEquals("The user must be provided.", output.errors().get("user"));
        assertEquals("The transaction value must be provided.", output.errors().get("value"));
    }

    @Test
    @Order(100)
    void testDepositWithOtherUser() {
        PersonalTransactionDTO personalTransactionDTO 
            = new PersonalTransactionDTO(USER_ID+1, new BigDecimal("1234.56"));

        ApiErrorResponse output = 
            given()
				.spec(specification)
                    .body(personalTransactionDTO)
				.when()
				    .post("/deposit")
				.then()
					.statusCode(HttpStatus.UNAUTHORIZED.value())
						.extract()
							.body()
                                .as(ApiErrorResponse.class);

        assertEquals("about:blank", output.type());
        assertEquals("Unauthorized", output.title());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), output.status());
        assertEquals("The user is not authorized to access this resource.", output.detail());
        assertEquals("/api/v1/transaction/deposit", output.instance());
        assertNull(output.errors());
    }

    @Test
    @Order(100)
    void testDepositWithSameUser() {
        PersonalTransactionDTO personalTransactionDTO 
            = new PersonalTransactionDTO(USER_ID, new BigDecimal("1234.56"));

        TransactionDTO output = 
            given()
				.spec(specification)
                    .body(personalTransactionDTO)
				.when()
				    .post("/deposit")
				.then()
					.statusCode(HttpStatus.OK.value())
						.extract()
							.body()
                                .as(TransactionDTO.class);

        assertTrue(output.id() > 0);
        assertEquals("DEPOSIT", output.type());
        assertEquals(new BigDecimal("1234.56"), output.value());
        assertNotNull(output.datetime());

        TRANSACTION_DEPOSIT_ID = output.id();

        // Check if the balance as updated
        UserDTO userOutput = 
            given()
				.basePath("/api/v1/user")
			        .header(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + USER_ACCESS_TOKEN)
					.port(TestConfigs.SERVER_PORT)
					.contentType(TestConfigs.CONTENT_TYPE_JSON)
                    .pathParam("user-id", USER_ID)
                .when()
                    .get("{user-id}")
                .then()
                    .statusCode(HttpStatus.OK.value())
                .extract()
                    .body()
                        .as(UserDTO.class);
        assertEquals(new BigDecimal("1234.56"), userOutput.accountBalance());
    }

    @Test
    @Order(200)
    void testWithdrawalWithoutBody() {
        ApiErrorResponse output = 
            given()
				.spec(specification)
				.when()
				    .post("/withdrawal")
				.then()
					.statusCode(HttpStatus.BAD_REQUEST.value())
						.extract()
							.body()
                                .as(ApiErrorResponse.class);

        assertEquals("about:blank", output.type());
        assertEquals("Bad Request", output.title());
        assertEquals(HttpStatus.BAD_REQUEST.value(), output.status());
        assertEquals("Failed to read request", output.detail());
        assertEquals("/api/v1/transaction/withdrawal", output.instance());
        assertNull(output.errors());
    }

    @Test
    @Order(200)
    void testWithdrawalWithFieldsNegative() {
        PersonalTransactionDTO personalTransactionDTO 
            = new PersonalTransactionDTO(-1L, new BigDecimal("-10"));

        ApiErrorResponse output = 
            given()
				.spec(specification)
                    .body(personalTransactionDTO)
				.when()
				    .post("/withdrawal")
				.then()
					.statusCode(HttpStatus.BAD_REQUEST.value())
						.extract()
							.body()
                                .as(ApiErrorResponse.class);

        assertEquals("about:blank", output.type());
        assertEquals("Bad Request", output.title());
        assertEquals(HttpStatus.BAD_REQUEST.value(), output.status());
        assertEquals("Invalid request content.", output.detail());
        assertEquals("/api/v1/transaction/withdrawal", output.instance());
        assertEquals(2, output.errors().size());
        assertEquals("The user must be a positive number.", output.errors().get("user"));
        assertEquals("The value of transaction must be greater than zero.", output.errors().get("value"));
    }

    @Test
    @Order(200)
    void testWithdrawalWithOtherUser() {
        PersonalTransactionDTO personalTransactionDTO 
            = new PersonalTransactionDTO(USER_ID+1, new BigDecimal("1234.56"));

        ApiErrorResponse output = 
            given()
				.spec(specification)
                    .body(personalTransactionDTO)
				.when()
				    .post("/withdrawal")
				.then()
					.statusCode(HttpStatus.UNAUTHORIZED.value())
						.extract()
							.body()
                                .as(ApiErrorResponse.class);

        assertEquals("about:blank", output.type());
        assertEquals("Unauthorized", output.title());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), output.status());
        assertEquals("The user is not authorized to access this resource.", output.detail());
        assertEquals("/api/v1/transaction/withdrawal", output.instance());
        assertNull(output.errors());
    }

    @Test
    @Order(200)
    void testWithdrawalWithSameUserAndInsuficientBalance() {
        PersonalTransactionDTO personalTransactionDTO 
            = new PersonalTransactionDTO(USER_ID, new BigDecimal("2000.00"));

        ApiErrorResponse output = 
            given()
				.spec(specification)
                    .body(personalTransactionDTO)
				.when()
				    .post("/withdrawal")
				.then()
					.statusCode(HttpStatus.BAD_REQUEST.value())
						.extract()
							.body()
                                .as(ApiErrorResponse.class);

        assertEquals("about:blank", output.type());
        assertEquals("Bad Request", output.title());
        assertEquals(HttpStatus.BAD_REQUEST.value(), output.status());
        assertEquals("The user does not have enough balance to carry out the transaction.", output.detail());
        assertEquals("/api/v1/transaction/withdrawal", output.instance());
        assertNull(output.errors());
    }

    @Test
    @Order(200)
    void testWithdrawalWithSameUserAndSuficientBalance() {
        PersonalTransactionDTO personalTransactionDTO 
            = new PersonalTransactionDTO(USER_ID, new BigDecimal("234.56"));

        TransactionDTO output = 
            given()
				.spec(specification)
                    .body(personalTransactionDTO)
				.when()
				    .post("/withdrawal")
				.then()
					.statusCode(HttpStatus.OK.value())
						.extract()
							.body()
                                .as(TransactionDTO.class);

        assertTrue(output.id() > 0);
        assertEquals("WITHDRAWAL", output.type());
        assertEquals(new BigDecimal("234.56"), output.value());
        assertNotNull(output.datetime());

        TRANSACTION_WITHDRAWAL_ID = output.id();

        // Check if the balance as updated
        UserDTO userOutput = 
            given()
				.basePath("/api/v1/user")
			        .header(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + USER_ACCESS_TOKEN)
					.port(TestConfigs.SERVER_PORT)
					.contentType(TestConfigs.CONTENT_TYPE_JSON)
                    .pathParam("user-id", USER_ID)
                .when()
                    .get("{user-id}")
                .then()
                    .statusCode(HttpStatus.OK.value())
                .extract()
                    .body()
                        .as(UserDTO.class);
        assertEquals(new BigDecimal("1000.00"), userOutput.accountBalance());
    }

    @Test
    @Order(300)
    void testTransferWithoutBody() {
        ApiErrorResponse output = 
            given()
				.spec(specification)
				.when()
				    .post("/transfer")
				.then()
					.statusCode(HttpStatus.BAD_REQUEST.value())
						.extract()
							.body()
                                .as(ApiErrorResponse.class);

        assertEquals("about:blank", output.type());
        assertEquals("Bad Request", output.title());
        assertEquals(HttpStatus.BAD_REQUEST.value(), output.status());
        assertEquals("Failed to read request", output.detail());
        assertEquals("/api/v1/transaction/transfer", output.instance());
        assertNull(output.errors());
    }

    @Test
    @Order(300)
    void testTransferWithFieldsInvalid() {
        TransferTransactionDTO transferTransactionDTO 
            = new TransferTransactionDTO(null, -10L, new BigDecimal("120.555"));

        ApiErrorResponse output = 
            given()
				.spec(specification)
                    .body(transferTransactionDTO)
				.when()
				    .post("/transfer")
				.then()
					.statusCode(HttpStatus.BAD_REQUEST.value())
						.extract()
							.body()
                                .as(ApiErrorResponse.class);

        assertEquals("about:blank", output.type());
        assertEquals("Bad Request", output.title());
        assertEquals(HttpStatus.BAD_REQUEST.value(), output.status());
        assertEquals("Invalid request content.", output.detail());
        assertEquals("/api/v1/transaction/transfer", output.instance());
        assertEquals(3, output.errors().size());
        assertEquals("The payer must be provided.", output.errors().get("payer"));
        assertEquals("The payee must be a positive number.", output.errors().get("payee"));
        assertEquals("The value must have up to 10 integer digits and 2 decimal digits of precision.", output.errors().get("value"));
    }

    @Test
    @Order(300)
    void testTransferWithOtherUser() {
        TransferTransactionDTO transferTransactionDTO 
            = new TransferTransactionDTO(USER_ID+10, TRANSFER_USER_ID, new BigDecimal("98.99"));

        ApiErrorResponse output = 
            given()
				.spec(specification)
                    .body(transferTransactionDTO)
				.when()
				    .post("/transfer")
				.then()
					.statusCode(HttpStatus.UNAUTHORIZED.value())
						.extract()
							.body()
                                .as(ApiErrorResponse.class);

        assertEquals("about:blank", output.type());
        assertEquals("Unauthorized", output.title());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), output.status());
        assertEquals("The user is not authorized to access this resource.", output.detail());
        assertEquals("/api/v1/transaction/transfer", output.instance());
        assertNull(output.errors());
    }

    @Test
    @Order(300)
    void testTransferWithSameUserAndInsuficientBalance() {
        TransferTransactionDTO transferTransactionDTO 
            = new TransferTransactionDTO(USER_ID, TRANSFER_USER_ID, new BigDecimal("2000.00"));
        
        ApiErrorResponse output = 
            given()
				.spec(specification)
                    .body(transferTransactionDTO)
				.when()
				    .post("/transfer")
				.then()
					.statusCode(HttpStatus.BAD_REQUEST.value())
						.extract()
							.body()
                                .as(ApiErrorResponse.class);

        assertEquals("about:blank", output.type());
        assertEquals("Bad Request", output.title());
        assertEquals(HttpStatus.BAD_REQUEST.value(), output.status());
        assertEquals("The payer does not have enough balance to carry out the transaction.", output.detail());
        assertEquals("/api/v1/transaction/transfer", output.instance());
        assertNull(output.errors());
    }

    @Test
    @Order(300)
    void testTransferWithSameUserAndSuficientBalance() {
        TransferTransactionDTO transferTransactionDTO 
            = new TransferTransactionDTO(USER_ID, TRANSFER_USER_ID, new BigDecimal("98.99"));
        
        TransactionDTO output = 
            given()
				.spec(specification)
                    .body(transferTransactionDTO)
				.when()
				    .post("/transfer")
				.then()
					.statusCode(HttpStatus.OK.value())
						.extract()
							.body()
                                .as(TransactionDTO.class);

        assertTrue(output.id() > 0);
        assertEquals("TRANSFER", output.type());
        assertEquals(new BigDecimal("98.99"), output.value());
        assertNotNull(output.datetime());

        TRANSACTION_TRANSFER_ID = output.id();

        // Check if the balance of user as updated
        UserDTO userOutput = 
            given()
				.basePath("/api/v1/user")
			        .header(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + USER_ACCESS_TOKEN)
					.port(TestConfigs.SERVER_PORT)
					.contentType(TestConfigs.CONTENT_TYPE_JSON)
                    .pathParam("user-id", USER_ID)
                .when()
                    .get("{user-id}")
                .then()
                    .statusCode(HttpStatus.OK.value())
                .extract()
                    .body()
                        .as(UserDTO.class);
        assertEquals(new BigDecimal("901.01"), userOutput.accountBalance());

        // Check if the destiny have the balance updated
        UserDTO userTargetOutput = 
            given()
				.basePath("/api/v1/user")
			        .header(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + TRANSFER_USER_ACCESS_TOKEN)
					.port(TestConfigs.SERVER_PORT)
					.contentType(TestConfigs.CONTENT_TYPE_JSON)
                    .pathParam("user-id", TRANSFER_USER_ID)
                .when()
                    .get("{user-id}")
                .then()
                    .statusCode(HttpStatus.OK.value())
                .extract()
                    .body()
                        .as(UserDTO.class);
        assertEquals(new BigDecimal("98.99"), userTargetOutput.accountBalance());
    }

    @Test
    @Order(300)
    void testTransferWithSameUserAndNotFoundPayee() {
        TransferTransactionDTO transferTransactionDTO 
            = new TransferTransactionDTO(USER_ID, TRANSFER_USER_ID+10000, new BigDecimal("98.99"));
        
        ApiErrorResponse output = 
            given()
				.spec(specification)
                    .body(transferTransactionDTO)
				.when()
				    .post("/transfer")
				.then()
					.statusCode(HttpStatus.NOT_FOUND.value())
						.extract()
							.body()
                                .as(ApiErrorResponse.class);

        assertEquals("about:blank", output.type());
        assertEquals("Not Found", output.title());
        assertEquals(HttpStatus.NOT_FOUND.value(), output.status());
        assertEquals("The payee was not found with the given ID.", output.detail());
        assertEquals("/api/v1/transaction/transfer", output.instance());
        assertNull(output.errors());
    }

    @Test
    @Order(400)
    void testFindByIdWithParamInvalid() {
        ApiErrorResponse output = 
            given()
				.spec(specification)
                    .pathParam("transaction-id", 0)
				.when()
				    .get("/{transaction-id}")
				.then()
					.statusCode(HttpStatus.BAD_REQUEST.value())
						.extract()
							.body()
                                .as(ApiErrorResponse.class);

        assertEquals("about:blank", output.type());
        assertEquals("Bad Request", output.title());
        assertEquals(HttpStatus.BAD_REQUEST.value(), output.status());
        assertEquals("The transaction-id must be a positive integer value.", output.detail());
        assertEquals("/api/v1/transaction/0", output.instance());
        assertNull(output.errors());
    }

    @Test
    @Order(400)
    void testFindByIdWithTransactionNotFound() {
        ApiErrorResponse output = 
            given()
				.spec(specification)
                    .pathParam("transaction-id", 100000)
				.when()
				    .get("/{transaction-id}")
				.then()
					.statusCode(HttpStatus.NOT_FOUND.value())
						.extract()
							.body()
                                .as(ApiErrorResponse.class);

        assertEquals("about:blank", output.type());
        assertEquals("Not Found", output.title());
        assertEquals(HttpStatus.NOT_FOUND.value(), output.status());
        assertEquals("The transaction was not found with the given ID.", output.detail());
        assertEquals("/api/v1/transaction/100000", output.instance());
        assertNull(output.errors());
    }

    @Test
    @Order(400)
    void testFindByIdWithOtherUser() {
        // To test this case is necessary use other account
        ApiErrorResponse output = 
            given()
				.basePath("/api/v1/transaction")
			        .header(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + TRANSFER_USER_ACCESS_TOKEN)
					.port(TestConfigs.SERVER_PORT)
					.contentType(TestConfigs.CONTENT_TYPE_JSON)
                    .pathParam("transaction-id", TRANSACTION_DEPOSIT_ID)
				.when()
				    .get("/{transaction-id}")
				.then()
					.statusCode(HttpStatus.UNAUTHORIZED.value())
						.extract()
							.body()
                                .as(ApiErrorResponse.class);

        assertEquals("about:blank", output.type());
        assertEquals("Unauthorized", output.title());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), output.status());
        assertEquals("The user is not authorized to access this resource.", output.detail());
        assertEquals("/api/v1/transaction/"+TRANSACTION_DEPOSIT_ID, output.instance());
        assertNull(output.errors());
    }
    
    @Test
    @Order(400)
    void testFindByIdWithSameUser() {
        TransactionDTO output = 
            given()
				.spec(specification)
                    .pathParam("transaction-id", TRANSACTION_DEPOSIT_ID)
				.when()
				    .get("/{transaction-id}")
				.then()
					.statusCode(HttpStatus.OK.value())
						.extract()
							.body()
                                .as(TransactionDTO.class);

        assertEquals(output.id(), TRANSACTION_DEPOSIT_ID);
        assertEquals("DEPOSIT", output.type());
        assertEquals(new BigDecimal("1234.56"), output.value());
        assertNotNull(output.datetime());
    }

    @Test
    @Order(500)
    void testFindAllByWithParamInvalid() {
        ApiErrorResponse output = 
            given()
				.spec(specification)
                    .pathParam("user-id", 0)
				.when()
				    .get("/user/{user-id}")
				.then()
					.statusCode(HttpStatus.BAD_REQUEST.value())
						.extract()
							.body()
                                .as(ApiErrorResponse.class);

        assertEquals("about:blank", output.type());
        assertEquals("Bad Request", output.title());
        assertEquals(HttpStatus.BAD_REQUEST.value(), output.status());
        assertEquals("The user-id must be a positive integer value.", output.detail());
        assertEquals("/api/v1/transaction/user/0", output.instance());
        assertNull(output.errors());
    }

    @Test
    @Order(500)
    void testFindAllByUserWithOtherUser() {
        // To test this case is necessary use other account
        ApiErrorResponse output = 
            given()
				.basePath("/api/v1/transaction")
			        .header(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + TRANSFER_USER_ACCESS_TOKEN)
					.port(TestConfigs.SERVER_PORT)
					.contentType(TestConfigs.CONTENT_TYPE_JSON)
                    .pathParam("user-id", USER_ID)
				.when()
				    .get("/user/{user-id}")
				.then()
					.statusCode(HttpStatus.UNAUTHORIZED.value())
						.extract()
							.body()
                                .as(ApiErrorResponse.class);

        assertEquals("about:blank", output.type());
        assertEquals("Unauthorized", output.title());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), output.status());
        assertEquals("The user is not authorized to access this resource.", output.detail());
        assertEquals("/api/v1/transaction/user/"+USER_ID, output.instance());
        assertNull(output.errors());
    }

    @Test
    @Order(500)
    void testFindAllByUserWithSameUserAndListNotEmpty() {
        List<TransactionDTO> output = 
            given()
				.spec(specification)
                    .pathParam("user-id", USER_ID)
				.when()
				    .get("/user/{user-id}")
				.then()
					.statusCode(HttpStatus.OK.value())
						.extract()
							.body()
                                .jsonPath()
                                    .getList(".", TransactionDTO.class);

        assertEquals(3, output.size());

        TransactionDTO outputPosition0 = output.get(0);
        assertEquals(TRANSACTION_DEPOSIT_ID, outputPosition0.id());
        assertEquals(TransactionType.DEPOSIT.name(), outputPosition0.type());
        assertEquals(new BigDecimal("1234.56"), outputPosition0.value());
        assertNotNull(outputPosition0.datetime());

        TransactionDTO outputPosition1 = output.get(1);
        assertEquals(TRANSACTION_WITHDRAWAL_ID, outputPosition1.id());
        assertEquals(TransactionType.WITHDRAWAL.name(), outputPosition1.type());
        assertEquals(new BigDecimal("234.56"), outputPosition1.value());
        assertNotNull(outputPosition1.datetime());

        TransactionDTO outputPosition2 = output.get(2);
        assertEquals(TRANSACTION_TRANSFER_ID, outputPosition2.id());
        assertEquals(TransactionType.TRANSFER.name(), outputPosition2.type());
        assertEquals(new BigDecimal("98.99"), outputPosition2.value());
        assertNotNull(outputPosition2.datetime());
    }

    @Test
    @Order(500)
    void testFindAllByUserWithSameUserAndListEmpty() {
        List<TransactionDTO> output = 
            given()
				.basePath("/api/v1/transaction")
			        .header(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + TRANSFER_USER_ACCESS_TOKEN)
					.port(TestConfigs.SERVER_PORT)
					.contentType(TestConfigs.CONTENT_TYPE_JSON)
                    .pathParam("user-id", TRANSFER_USER_ID)
				.when()
				    .get("/user/{user-id}")
				.then()
					.statusCode(HttpStatus.OK.value())
						.extract()
							.body()
                                .jsonPath()
                                    .getList(".", TransactionDTO.class);

        assertEquals(0, output.size());
    }
}
