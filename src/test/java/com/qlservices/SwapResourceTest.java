package com.qlservices;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.MediaType;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class SwapResourceTest {
    //"{\"ID\": \"001\", \"swapType\": \"PAYER\", \"nominal\": 48000000.0, \"startDate\": \"2018-03-14\", \"maturityDate\": \"2028-03-14\", \"fixedLegFrequency\": \"ANNUAL\", \"fixedLegCalendar\": \"TARGET\", \"fixedLegConvention\": \"MODIFIEDFOLLOWING\", \"fixedLegDateGenerationRule\": \"BACKWARD\", \"fixedLegRate\": 0.02, \"fixedLegDayCount\": \"ACTUAL360\", \"floatingLegFrequency\": \"QUARTERLY\", \"floatingLegCalendar\": \"TARGET\", \"floatingLegConvention\": \"MODIFIEDFOLLOWING\", \"floatingLegDateGenerationRule\": \"BACKWARD\", \"floatingLegSpread\": 0.0007, \"floatingLegDayCount\": \"ACTUAL360\"}"

    @Test
    public void testSwapEndpoint() {
        given()
                .body("{\"ID\": \"738641\", \"swapType\": \"PAYER\", \"nominal\": 595000.0, \"startDate\": \"03-06-2012\", \"maturityDate\": \"03-06-2042\", \"fixedLegRate\": 0.028037, \"floatingLegSpread\": 0.0, \"convention\" : \"USD\"}")
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .when().post("/price/vanillaswap")
                .then()
                .statusCode(200)
                .body(is("{\"id\":\"738641\",\"npv\":-212961.84572721188,\"dv01\":-1401.8211129264382,\"fair rate\":0.010191255905995466}"));

        given()
                .body("{\"ID\": \"19199\", \"swapType\": \"RECEIVER\", \"nominal\": 1000000000.0, \"startDate\": \"07-11-2008\", \"maturityDate\": \"07-11-2023\", \"fixedLegRate\": 0.0478, \"floatingLegSpread\": 0.0, \"convention\" : \"USD\"}")
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .when().post("/price/vanillaswap")
                .then()
                .statusCode(200)
                .body(is("{\"id\":\"19199\",\"npv\":135933734.2142064,\"dv01\":-299194.43726980686,\"fair rate\":0.0023739072062620645}"));
        given()
                .body("{\"ID\": \"74839\", \"swapType\": \"RECEIVER\", \"nominal\": 250000000.0, \"startDate\": \"12-23-2009\", \"maturityDate\": \"12-23-2024\", \"fixedLegRate\": 0.0447, \"floatingLegSpread\": 0.0, \"convention\" : \"USD\"}")
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .when().post("/price/vanillaswap")
                .then()
                .statusCode(200)
                .body(is("{\"id\":\"74839\",\"npv\":47143907.62139006,\"dv01\":-111939.46287095547,\"fair rate\":0.0027383946417408322}"));
    }

}