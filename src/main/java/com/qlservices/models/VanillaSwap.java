package com.qlservices.models;

import com.qlservices.adapters.VanillaSwapAdapter;
import org.quantlib.*;

import javax.json.bind.annotation.JsonbTypeAdapter;


@JsonbTypeAdapter(VanillaSwapAdapter.class)
public class VanillaSwap {
    public String ID;
    public org.quantlib.VanillaSwap.Type swapType;
    public double nominal;
    public org.quantlib.Date startDate, maturityDate;
    public org.quantlib.Frequency fixedLegFrequency,floatingLegFrequency;
    public org.quantlib.Calendar fixedLegCalendar,floatingLegCalendar;
    public org.quantlib.BusinessDayConvention fixedLegConvention,floatingLegConvention;
    public DateGeneration.Rule fixedLegDateGenerationRule,floatingLegDateGenerationRule;
    public double fixedLegRate;
    public org.quantlib.DayCounter fixedLegDayCount,floatingLegDayCount;
    public double floatingLegSpread;
    public double npv;
    org.quantlib.VanillaSwap qlSwap;


    public VanillaSwap(){}

    public void setPricingEngine(org.quantlib.DiscountingSwapEngine engine, org.quantlib.IborIndex floatingLegIborIndex){
        Schedule fixedLegSchedule = new Schedule(startDate,maturityDate,new Period(fixedLegFrequency),
                fixedLegCalendar,fixedLegConvention, fixedLegConvention,fixedLegDateGenerationRule,false);
        Schedule floatingLegSchedule = new Schedule(startDate,maturityDate,new Period(floatingLegFrequency),
                floatingLegCalendar,floatingLegConvention, floatingLegConvention,floatingLegDateGenerationRule,false);
        qlSwap = new org.quantlib.VanillaSwap(swapType,nominal,
                fixedLegSchedule,fixedLegRate,fixedLegDayCount,
                floatingLegSchedule,floatingLegIborIndex,floatingLegSpread,floatingLegDayCount);
        qlSwap.setPricingEngine(engine);
    }
    public void npv(){
        npv = qlSwap.NPV();
    }
}
