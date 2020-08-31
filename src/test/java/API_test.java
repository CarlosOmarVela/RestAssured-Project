import io.restassured.config.EncoderConfig;
import io.restassured.http.ContentType;
import org.junit.FixMethodOrder;
import org.junit.Test;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.runners.MethodSorters;

import java.util.Base64;
import static org.junit.Assert.*;
import static io.restassured.RestAssured.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class API_test {
    //environment variables
    static private String baseUrl  = "https://webapi.segundamano.mx";
    static private String token;
    static private String accountID;
    static private String name;
    static private String uuid;
    static private String newText;
    static private String adID;
    static private String token2;
    static private String addressID;
    static private String email = "apitest@mailinator.com";
    static private String pass = "12345";
    static private String phone;

    @Test
    public void t01_get_token_fail(){
        //Request an account token without authorization header
        RestAssured.baseURI = String.format("%s/nga/api/v1.1/private/accounts",baseUrl);
        Response response = given().log().all()
                .post();
        //validations
        System.out.println("Status expected: 400" );
        System.out.println("Result: " + response.getStatusCode());
        assertEquals(400,response.getStatusCode());
        String errorCode = response.jsonPath().getString("error.code");
        System.out.println("Error Code expected: VALIDATION FAILED \nResult: " + errorCode);
        assertEquals("VALIDATION_FAILED",errorCode);
    }

    @Test
    public void t02_get_token_correct(){
        //Request an account token with an authorization header
        String authorizationToken = "cGFwaXRhc2xleXM5MUBnbWFpbC5jb206Y29udHJhMTIz";
        RestAssured.baseURI = String.format("%s/nga/api/v1.1/private/accounts",baseUrl);
        Response response = given().log().all()
                .header("Authorization","Basic " + authorizationToken)
                .post();
        //validations
        String body = response.getBody().asString();
        System.out.println("Status expected: 200" );
        System.out.println("Result: " + response.getStatusCode());
        assertEquals(200,response.getStatusCode());
        assertTrue(body.contains("access_token"));
    }

    @Test
    public void t03_create_user_fail(){
        //Create an user without authorization header
        String username = "agente" + (Math.floor(Math.random() * 7685) + 3) + "@mailinator.com";
        String bodyRequest = "{\"account\":{\"email\":\""+ username +"\"}}";
        RestAssured.baseURI = String.format("%s/nga/api/v1.1/private/accounts",baseUrl);
        Response response = given().log().all()
                .contentType("application/json")
                .body(bodyRequest)
                .post();
        //validations
        System.out.println("Status expected: 400" );
        System.out.println("Result: " + response.getStatusCode());
        assertEquals(400,response.getStatusCode());
        String errorCode = response.jsonPath().getString("error.code");
        System.out.println(errorCode);
        assertEquals("VALIDATION_FAILED",errorCode);
    }

    @Test
    public void t04_create_user(){
        //successufully create a new user, retrieve its data
        String username = "agente" + (int)(Math.floor(Math.random() * 999999) +3) + "@mailinator.com";
        double password = (Math.floor(Math.random() * 57684) + 10000);
        String datos = username + ":" + password;
        String encodedAuth = Base64.getEncoder().encodeToString(datos.getBytes());
        String bodyRequest = "{\"account\":{\"email\":" + "\""+ username + "\"" +"}}";
        //String bodyRequest = "{\"account\":{\"email\":\""+ username +"\"}}";
        RestAssured.baseURI = String.format("%s/nga/api/v1.1/private/accounts", baseUrl);
        Response response = given().queryParam("lang","es")
                .log().all()
                .header("Authorization","Basic " + encodedAuth)
                .contentType("application/json")
                .body(bodyRequest)
                .post();
        //validations
        String body = response.getBody().asString();
        System.out.println("Body response= " + body);
        assertEquals(401,response.getStatusCode());
        assertTrue(body.contains("code"));
        //save account data to environment variables
        //token = response.jsonPath().getString("access_token");
    }

    @Test
    public void t05_update_phone_number(){
        //update user created adding its phone number
        String email = "apitest@mailinator.com";
        String pass = "12345";
        String ToEncode = email + ":" + pass;
        String Basic_encoded = Base64.getEncoder().encodeToString(ToEncode.getBytes());

        RestAssured.baseURI = String.format("%s/nga/api/v1.1/private/accounts", baseUrl);
        Response response = given().queryParam("lang","es")
                .log().all()
                .header("Authorization","Basic " + Basic_encoded)
                .post();
        String body = response.getBody().asString();
        assertEquals(200,response.getStatusCode());
        assertTrue(body.contains("access_token"));
        token = response.jsonPath().getString("access_token");
        accountID = response.jsonPath().getString("account.account_id").split("/")[3];
        phone = response.jsonPath().getString("account.phone");

        String bodyRequest = "{\"account\":{\"name\":\"Juanito Escarcha\",\"phone\":\"5512345678\",\"professional\":false,\"phone_hidden\":false}}";
        RestAssured.baseURI = String.format("%s/api/v1/private/accounts/" + accountID, baseUrl);
        Response response2 = given().queryParam("lang","es")
                .log().all()
                .header("Authorization","tag:scmcoord.com,2013:api " + token)
                .header("Accept","application/json, text/plain, */*")
                .header("Content-type","application/json;charset=UTF-8")
                .body(bodyRequest)
                .patch();
        String body2 = response2.getBody().asString();

        //validations
        assertEquals(200,response.getStatusCode());
        String userPhone = response.jsonPath().getString("account.phone");
        assertEquals(userPhone, "" + phone);
    }

    @Test
    public void t06_add_new_add_fail(){
        //add a new add with an ivalid token should fail
        String ToEncode2 = uuid + ":" + token;
        String Basic_encoded2 = Base64.getEncoder().encodeToString(ToEncode2.getBytes());
        System.out.println("Basic_encoded2 = " + Basic_encoded2);
        String bodyRequest = "{" +
                "\"category\":\"8041\"," +
                "\"subject\":\"Servico de consultoria\"," +
                "\"body\":\"Consultoría de arquitectos a tus ordenes, pregunta por nuestros servicios\"," +
                "\"price\":\"1\"," +
                "\"region\":\"28\"," +
                "\"municipality\":\"1963\"," +
                "\"area\":\"83521\"," +
                "\"phone_hidden\":\"true\"" + "}";
        RestAssured.baseURI = String.format("%s/accounts/"+ uuid + "/up", baseUrl);
        Response response2 = given()
                .log().all()
                .header("Authorization","Basic  " + Basic_encoded2)
                .header("Accept","application/json,text/plain, */*")
                .header("x-source","PHOENIX_DESKTOP")
                .header("Content-type","application/json")
                .body(bodyRequest)
                .post();
        String body2 = response2.getBody().asString();
        System.out.println("Body response= " + body2);
        //Validaciones
        System.out.println("Status expected: 401" );
        System.out.println("Result: " + response2.getStatusCode());
        assertEquals(401,response2.getStatusCode());
        String errorCode = response2.jsonPath().getString("error");
        System.out.println("Error Code expected: UNAUTHORIZED \nResult: " + errorCode);
        assertEquals("UNAUTHORIZED",errorCode);
    }

    @Test
    public void t07_add_new_add(){
        //Add a new add with a valid token
        String ToEncode = email + ":" + pass;
        String Basic_encoded = Base64.getEncoder().encodeToString(ToEncode.getBytes());

        RestAssured.baseURI = String.format("%s/nga/api/v1.1/private/accounts", baseUrl);
        Response response = given().queryParam("lang","es")
                .log().all()
                .header("Authorization","Basic " + Basic_encoded)
                .post();
        String body = response.getBody().asString();
        assertEquals(200,response.getStatusCode());
        assertTrue(body.contains("access_token"));
        token = response.jsonPath().getString("access_token");
        accountID = response.jsonPath().getString("account.account_id");
        uuid = response.jsonPath().getString("account.uuid");

        String ToEncode2 = uuid + ":" + token;
        String Basic_encoded2 = Base64.getEncoder().encodeToString(ToEncode2.getBytes());
        String bodyRequest = "{\n" +
                "    \"category\":\"8041\",\n" +
                "    \"subject\":\"Servico de consultoria\",\n" +
                "    \"body\":\"Consultoría de arquitectos a tus ordenes, pregunta por nuestros servicios\",\n" +
                "    \"price\":\"1\",\n" +
                "    \"region\":\"28\",\n" +
                "    \"municipality\":\"1963\",\n" +
                "    \"area\":\"83521\",\n" +
                "    \"phone_hidden\":\"true\"\n" +
                "}";
        RestAssured.baseURI = String.format("%s/accounts/"+ uuid + "/up", baseUrl);
        Response response2 = given().queryParam("lang","es")
                .log().all()
                .header("Authorization","Basic " + Basic_encoded2)
                .header("Accept","application/json,text/plain, */*")
                .header("x-source","PHOENIX_DESKTOP")
                .header("Content-Type","application/json")
                .body(bodyRequest)
                .post();

        //Validaciones

        String body2 = response2.getBody().asString();
        System.out.println("Body2: " + body2 );

        System.out.println("Status expected: 200" );
        System.out.println("Result: " + response2.getStatusCode());
        assertEquals(200, response2.getStatusCode());
        String actionType = response2.jsonPath().getString("action.action_type");
        System.out.println("Action expected to be: null \nResult: " + actionType);
        assertEquals(null, actionType);
        //Save adID to be modified and delete laterad.ad_id
        adID = response2.jsonPath().getString("data.ad.ad_id");
        System.out.println("Ad Created with id: " + adID);
    }

    @Test
    public void t08_update_add(){
        //change a text on the description of the add
        String ToEncode = email + ":" + pass;
        String Basic_encoded = Base64.getEncoder().encodeToString(ToEncode.getBytes());

        RestAssured.baseURI = String.format("%s/nga/api/v1.1/private/accounts", baseUrl);
        Response response = given().queryParam("lang","es")
                .log().all()
                .header("Authorization","Basic " + Basic_encoded)
                .post();
        String body = response.getBody().asString();
        assertEquals(200,response.getStatusCode());
        assertTrue(body.contains("access_token"));
        token = response.jsonPath().getString("access_token");
        accountID = response.jsonPath().getString("account.account_id");
        uuid = response.jsonPath().getString("account.uuid");

        String ToEncode2 = uuid + ":" + token;
        String Basic_encoded2 = Base64.getEncoder().encodeToString(ToEncode2.getBytes());
        String bodyRequest = "{\n" +
                "    \"category\":\"8041\",\n" +
                "    \"subject\":\"Servico de consultoria\",\n" +
                "    \"body\":\"Consultoría de arquitectos a tus ordenes, pregunta por nuestros servicios\",\n" +
                "    \"price\":\"1\",\n" +
                "    \"region\":\"28\",\n" +
                "    \"municipality\":\"1963\",\n" +
                "    \"area\":\"83521\",\n" +
                "    \"phone_hidden\":\"true\"\n" +
                "}";
        RestAssured.baseURI = String.format("%s/accounts/"+ uuid + "/up", baseUrl);
        Response response2 = given().queryParam("lang","es")
                .log().all()
                .header("Authorization","Basic " + Basic_encoded2)
                .header("Accept","application/json,text/plain, */*")
                .header("x-source","PHOENIX_DESKTOP")
                .header("Content-Type","application/json")
                .body(bodyRequest)
                .post();


        adID = response2.jsonPath().getString("data.ad.ad_id");
        System.out.println("Ad Created with id: " + adID);

        newText = "" + (Math.random()*99999999+999999999);
        RestAssured.baseURI = String.format("%s/nga/api/v1%s/klfst/%s/actions",baseUrl,accountID,adID);
        String bodyRequest2 = "{\"ad\":" +
                "{\"locations\":[{\"code\":\"5\",\"key\":\"region\",\"label\":\"Baja California Sur\"," +
                "\"locations\":[{\"code\":\"51\",\"key\":\"municipality\",\"label\":\"Comondú\"," +
                "\"locations\":[{\"code\":\"3748\",\"key\":\"area\",\"label\":\"4 de Marzo\"}]}]}]," +
                "\"subject\":\"Paseo perros a domicilio\",\"body\":" +
                "\"Para su comodidad, paseo perros en su domicilio, use la promoción " + newText + "\"," +
                "\"category\":{\"code\":\"3042\"},\"images\":[],\"price\":{\"currency\":\"mxn\",\"price_value\":1}," +
                "\"ad_details\":{},\"phone_hidden\":1,\"plate\":\"\",\"vin\":\"\",\"type\":{\"code\":\"s\"," +
                "\"label\":\"\"},\"ad\":\"Paseo perros a domicilio\"},\"category_suggestion\":false,\"commit\":true}";
        Response response3 = given()
                .log().all()
                .header("Authorization","tag:scmcoord.com,2013:api " + token)
                .header("Accept", "application/json, text/plain, */*")
                .header("x-nga-source", "PHOENIX_DESKTOP")
                .contentType("application/json")
                .body(bodyRequest2)
                .post();
        //Validaciones
        System.out.println("Status expected: 201" );
        System.out.println("Result: " + response3.getStatusCode());
        assertEquals(201, response3.getStatusCode());
        String actionType = response3.jsonPath().getString("action.action_type");
        System.out.println("Action expected to be: edit \nResult: " + actionType);
        assertEquals("edit", actionType);
    }

    @Test
    public void t09_get_address_fail(){
        //get user address with an invalid token should fail
        RestAssured.baseURI = String.format("%s/addresses/v1/get",baseUrl);
        Response response = given()
                .log().all()
                .header("Authorization","Basic " + token)
                .get();
        //validations
        System.out.println("Status expected: 403" );
        System.out.println("Result: " + response.getStatusCode());
        assertEquals(403,response.getStatusCode());
        String errorCode = response.jsonPath().getString("error");
        System.out.println("Error Code expected: Authorization failed \nResult: " + errorCode);
        assertEquals("Authorization failed",errorCode);
    }

    @Test
    public void t10_user_has_no_address(){
        //Get user addresses shoudl be an empty list
        String ToEncode = email + ":" + pass;
        String Basic_encoded = Base64.getEncoder().encodeToString(ToEncode.getBytes());

        RestAssured.baseURI = String.format("%s/nga/api/v1.1/private/accounts", baseUrl);
        Response response = given().queryParam("lang","es")
                .log().all()
                .header("Authorization","Basic " + Basic_encoded)
                .post();
        String body = response.getBody().asString();
        assertEquals(200,response.getStatusCode());
        assertTrue(body.contains("access_token"));
        token = response.jsonPath().getString("access_token");
        accountID = response.jsonPath().getString("account.account_id");
        uuid = response.jsonPath().getString("account.uuid");

        String ToEncode2 = uuid + ":" + token;
        String Basic_encoded2 = Base64.getEncoder().encodeToString(ToEncode2.getBytes());
        RestAssured.baseURI = String.format("%s/addresses/v1/get", baseUrl);
        Response response2 = given()
                .log().all()
                .header("Authorization","Basic " + Basic_encoded2)
                .header("Accept","application/json, text/plain, */*")
                .get();
        //Validations
        String body2 = response2.getBody().asString();
        assertEquals(200,response.getStatusCode());
        assertTrue(body2.contains("addresses"));
        String addressesList = response2.jsonPath().getString("addresses");
        System.out.println("List expected to be empty \nResult: " + addressesList);
        assertEquals("[:]",addressesList);

    }

    @Test
    public void t11_update_user_address(){
        //add a new address to user
        String ToEncode = email + ":" + pass;
        String Basic_encoded = Base64.getEncoder().encodeToString(ToEncode.getBytes());

        RestAssured.baseURI = String.format("%s/nga/api/v1.1/private/accounts", baseUrl);
        Response response = given().queryParam("lang","es")
                .log().all()
                .header("Authorization","Basic " + Basic_encoded)
                .post();
        String body = response.getBody().asString();
        System.out.println("Body response= " + body);
        System.out.println("Status response: " + response.getStatusCode());
        assertEquals(200,response.getStatusCode());
        assertTrue(body.contains("access_token"));
        //consultamos las direcciones
        token = response.jsonPath().getString("access_token");
        System.out.println("token: " + token);
        accountID = response.jsonPath().getString("account.account_id");
        System.out.println("accountID: " + accountID);
        uuid = response.jsonPath().getString("account.uuid");
        System.out.println("uuid: " + uuid);

        String token2Keys = uuid + ":" + token;
        token2 = Base64.getEncoder().encodeToString(token2Keys.getBytes());
        RestAssured.baseURI = String.format("%s/addresses/v1/create",baseUrl);
        Response response2;
        response2 = given()
                .log().all()
                .config(RestAssured.config()
                        .encoderConfig(EncoderConfig.encoderConfig()
                                .encodeContentTypeAs("x-www-form-urlencoded",
                                        ContentType.URLENC)))
                .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                .formParam("contact", "Vendedor")
                .formParam("phone","5512345678")
                .formParam("rfc", "VESC9212113E3")
                .formParam("zipCode", "07580")
                .formParam("exteriorInfo", "COATZACOALCOSss")
                .formParam("region", "16")
                .formParam("municipality", "755")
                .formParam("alias", "CASA")
                .header("Authorization","Basic " + token2)
                .post();
        //Validaciones
        String body2 = response.getBody().asString();

        System.out.println("Body addres: " + body2 );
        System.out.println("Status expected: 200" );
        System.out.println("Result: " + response.getStatusCode());
        assertEquals(200, response.getStatusCode());
        //save address to enviromnt variable
        System.out.printf("Body not null: " + body);
        assertNotNull(body);
    }

    @Test
    public void t12_update_user_address_duplicated(){
        //try to add same address should fail
        String ToEncode = email + ":" + pass;
        String Basic_encoded = Base64.getEncoder().encodeToString(ToEncode.getBytes());

        RestAssured.baseURI = String.format("%s/nga/api/v1.1/private/accounts", baseUrl);
        Response response = given().queryParam("lang","es")
                .log().all()
                .header("Authorization","Basic " + Basic_encoded)
                .post();
        String body = response.getBody().asString();
        System.out.println("Body response= " + body);
        System.out.println("Status response: " + response.getStatusCode());
        assertEquals(200,response.getStatusCode());
        assertTrue(body.contains("access_token"));
        //consultamos las direcciones
        token = response.jsonPath().getString("access_token");
        System.out.println("token: " + token);
        accountID = response.jsonPath().getString("account.account_id");
        System.out.println("accountID: " + accountID);
        uuid = response.jsonPath().getString("account.uuid");
        System.out.println("uuid: " + uuid);

        String token2Keys = uuid + ":" + token;
        token2 = Base64.getEncoder().encodeToString(token2Keys.getBytes());
        RestAssured.baseURI = String.format("%s/addresses/v1/create",baseUrl);
        Response response2;
        response2 = given()
                .log().all()
                .config(RestAssured.config()
                        .encoderConfig(EncoderConfig.encoderConfig()
                                .encodeContentTypeAs("x-www-form-urlencoded",
                                        ContentType.URLENC)))
                .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                .formParam("contact", "Vendedor")
                .formParam("phone","5512345678")
                .formParam("rfc", "VESC9212113E3")
                .formParam("zipCode", "07580")
                .formParam("exteriorInfo", "COATZACOALCOSss")
                .formParam("region", "16")
                .formParam("municipality", "755")
                .formParam("alias", "CASA")
                .header("Authorization","Basic " + token2)
                .post();
        //Validaciones
        String body2 = response2.getBody().asString();
        System.out.println("Status expected: 201" );
        System.out.println("Result: " + response2.getStatusCode());
        assertEquals(201, response2.getStatusCode());
        String errorCode = response2.jsonPath().getString("error");
        System.out.println("Request expected to return duplicate \nResult: " + errorCode);
        assertTrue(body2.contains("Duplicate"));
    }

    @Test
    public void t13_get_created_address() {
        String ToEncode = email + ":" + pass;
        String Basic_encoded = Base64.getEncoder().encodeToString(ToEncode.getBytes());

        RestAssured.baseURI = String.format("%s/nga/api/v1.1/private/accounts", baseUrl);
        Response response = given().queryParam("lang","es")
                .log().all()
                .header("Authorization","Basic " + Basic_encoded)
                .post();
        String body = response.getBody().asString();
        assertEquals(200,response.getStatusCode());
        assertTrue(body.contains("access_token"));
        //consultamos las direcciones
        token = response.jsonPath().getString("access_token");
        accountID = response.jsonPath().getString("account.account_id");
        uuid = response.jsonPath().getString("account.uuid");

        String ToEncode2 = uuid + ":" + token;
        String Basic_encoded2 = Base64.getEncoder().encodeToString(ToEncode2.getBytes());
        RestAssured.baseURI = String.format("%s/addresses/v1/get", baseUrl);
        Response response2 = given()
                .log().all()
                .header("Authorization","Basic " + Basic_encoded2)
                .header("Accept","application/json, text/plain, */*")
                .get();
        String body2 = response2.getBody().asString();
        assertEquals(200,response.getStatusCode());
        assertTrue(body2.contains("addresses"));
    }

    @Test
    public void t14_shop_not_found(){
        //fail to found a shop with this account
        RestAssured.baseURI = String.format("%s/shops/api/v2/public/accounts/10613126/shop",baseUrl);
        Response response = given().log().all()
                .get();
        //validations
        System.out.println("Status expected: 404" );
        System.out.println("Result: " + response.getStatusCode());
        assertEquals(404,response.getStatusCode());
        String errorCode = response.jsonPath().getString("message");
        System.out.println("Error Code expected: Account not found \nResult: " + errorCode);
        assertEquals("Account not found",errorCode);
    }

    @Test
    public void t15_delete_ad() {
        //Delete the ad created - possible fail with 403
        String ToEncode = email + ":" + pass;
        String Basic_encoded = Base64.getEncoder().encodeToString(ToEncode.getBytes());

        RestAssured.baseURI = String.format("%s/nga/api/v1.1/private/accounts", baseUrl);
        Response response = given().queryParam("lang","es")
                .log().all()
                .header("Authorization","Basic " + Basic_encoded)
                .post();
        String body = response.getBody().asString();
        assertEquals(200,response.getStatusCode());
        assertTrue(body.contains("access_token"));
        //consultamos las direcciones
        token = response.jsonPath().getString("access_token");
        accountID = response.jsonPath().getString("account.account_id");
        uuid = response.jsonPath().getString("account.uuid");

        String bodyRequest = "{\"delete_reason\":{\"code\":\"5\"} }";
        RestAssured.baseURI = String.format("%s/nga/api/v1/private/accounts/%s/%s", baseUrl, accountID, adID);
        Response response2 = given().log().all()
                .header("Authorization", "tag:scmcoord.com,2013:api " + token)
                .header("Accept", "application/json, text/plain, */*")
                .header("Accept-Language", "es")
                .contentType("application/json;charset=UTF-8")
                .body(bodyRequest)
                .delete();
        //Validations
        assertEquals(403, response2.getStatusCode());
        String actionType = response2.jsonPath().getString("action.action_type");
        assertNull(actionType);
    }
}
