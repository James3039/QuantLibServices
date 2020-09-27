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
                .body("{\"ID\": \"001\", \"swapType\": \"PAYER\", \"nominal\": 48000000.0, \"startDate\": \"03-14-2018\", \"maturityDate\": \"03-14-2028\", \"fixedLegFrequency\": \"ANNUAL\", \"fixedLegCalendar\": \"TARGET\", \"fixedLegConvention\": \"MODIFIEDFOLLOWING\", \"fixedLegDateGenerationRule\": \"BACKWARD\", \"fixedLegRate\": 0.02, \"fixedLegDayCount\": \"ACTUAL360\", \"floatingLegFrequency\": \"QUARTERLY\", \"floatingLegCalendar\": \"TARGET\", \"floatingLegConvention\": \"MODIFIEDFOLLOWING\", \"floatingLegDateGenerationRule\": \"BACKWARD\", \"floatingLegSpread\": 0.0007, \"floatingLegDayCount\": \"ACTUAL360\"}")
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .when().post("/swap")
                .then()
                .statusCode(200)
                .body(is("{\"id\":\"001\",\"npv\":107600.37680486124}"));
    }

}