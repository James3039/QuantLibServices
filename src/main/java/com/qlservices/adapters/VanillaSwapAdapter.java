package com.qlservices.adapters;

import com.qlservices.models.VanillaSwap;
import com.qlservices.util.Utils;
import org.jboss.logging.Logger;
import org.quantlib.DateGeneration;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.bind.adapter.JsonbAdapter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class VanillaSwapAdapter implements JsonbAdapter<VanillaSwap, JsonObject> {
    private static final Logger LOG = Logger.getLogger(VanillaSwapAdapter.class);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");

    @Override
    public JsonObject adaptToJson(VanillaSwap vanillaSwap) throws Exception {
        return Json.createObjectBuilder()
                .add("id", vanillaSwap.ID)
                .add("npv", vanillaSwap.npv)
                .build();
    }

    @Override
    public VanillaSwap adaptFromJson(JsonObject jsonObject) throws Exception {
        LOG.info(jsonObject.toString());
        //System.loadLibrary("QuantLibJNI");
        VanillaSwap swap = new VanillaSwap();
        swap.ID = jsonObject.getString("ID");
        swap.swapType = jsonObject.getString("swapType").toUpperCase().equals("PAYER")? org.quantlib.VanillaSwap.Type.Payer : org.quantlib.VanillaSwap.Type.Receiver;
        swap.nominal = jsonObject.getJsonNumber("nominal").doubleValue();
        swap.startDate = Utils.javaDateToQLDate(LocalDate.parse(jsonObject.getString("startDate"),formatter));
        swap.maturityDate = Utils.javaDateToQLDate(LocalDate.parse(jsonObject.getString("maturityDate"),formatter));

        swap.fixedLegFrequency = Utils.getFrequency(jsonObject.getString("fixedLegFrequency"));
        swap.fixedLegCalendar = Utils.getCalendar(jsonObject.getString("fixedLegCalendar"));
        swap.fixedLegConvention = Utils.getBusDayConvention(jsonObject.getString("fixedLegConvention"));
        swap.fixedLegDateGenerationRule = jsonObject.getString("fixedLegDateGenerationRule").equals("BACKWARD")? DateGeneration.Rule.Backward : DateGeneration.Rule.Forward;
        swap.fixedLegRate = jsonObject.getJsonNumber("fixedLegRate").doubleValue();
        swap.fixedLegDayCount = Utils.getDayCounter(jsonObject.getString("fixedLegDayCount"));

        swap.floatingLegFrequency = Utils.getFrequency(jsonObject.getString("floatingLegFrequency"));
        swap.floatingLegCalendar = Utils.getCalendar(jsonObject.getString("floatingLegCalendar"));
        swap.floatingLegConvention = Utils.getBusDayConvention(jsonObject.getString("floatingLegConvention"));
        swap.floatingLegDateGenerationRule = jsonObject.getString("floatingLegDateGenerationRule").equals("BACKWARD")? DateGeneration.Rule.Backward : DateGeneration.Rule.Forward;
        swap.floatingLegSpread = jsonObject.getJsonNumber("floatingLegSpread").doubleValue();
        swap.floatingLegDayCount = Utils.getDayCounter(jsonObject.getString("floatingLegDayCount"));

        return swap;
    }
}
