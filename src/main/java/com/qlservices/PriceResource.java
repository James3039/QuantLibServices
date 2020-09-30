package com.qlservices;

import com.qlservices.models.VanillaSwap;
import com.qlservices.services.CurveBuilderService;
import com.qlservices.util.Utils;
import org.jboss.logging.Logger;
import org.quantlib.*;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Optional;

@Path("/price")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PriceResource {
    private static final Logger LOG = Logger.getLogger(PriceResource.class);

    @Inject
    CurveBuilderService curveBuilderService;

    @Inject
    MarketData marketData;

    @POST
    @Path("vanillaswap")
    public VanillaSwap price(VanillaSwap swap) throws Exception {
        Calendar cal = new WeekendsOnly(); //UnitedStates();
        RelinkableYieldTermStructureHandle discountTermStructure = new RelinkableYieldTermStructureHandle();
        RelinkableYieldTermStructureHandle projectionTermStructure = new RelinkableYieldTermStructureHandle();

        YieldTermStructure discountCurve = curveBuilderService.buildDiscountCurve("DISC-CURVE", "USD");
        discountTermStructure.linkTo(discountCurve);
        DiscountingSwapEngine engine = new DiscountingSwapEngine(discountTermStructure);

        YieldTermStructure projectionCurve = curveBuilderService.buildProjectionCurve("PROJ-CURVE", "USD", "3M", discountCurve);
        projectionTermStructure.linkTo(projectionCurve);

        LOG.info("Projection curve built");

        USDLibor index = new USDLibor(new Period(Frequency.Quarterly), projectionTermStructure);
        swap.setPricingEngine(engine, index);
        LOG.info("pricing engine set");
        Schedule floatingLegSchedule = swap.getFloatingSchedule();
        Date prevDate = cal.advance(floatingLegSchedule.previousDate(Settings.instance().getEvaluationDate()), -2, TimeUnit.Days);
        Optional<Double> fixing = new Utils().getFixingForDate(prevDate, "USD", "3M");
        if (fixing.isPresent())
            index.addFixing(prevDate, fixing.get());
        LOG.info("fixcing added");
        swap.netPresentValue = swap.npv();
        LOG.info("swap npv:" + swap.netPresentValue);
        swap.fairRate = swap.fairRate();

        double shift = 0.0001;
        discountTermStructure.linkTo(new ZeroSpreadedTermStructure(new YieldTermStructureHandle(discountCurve), new QuoteHandle(new SimpleQuote(shift))));
        projectionTermStructure.linkTo(new ZeroSpreadedTermStructure(new YieldTermStructureHandle(projectionCurve), new QuoteHandle(new SimpleQuote(shift))));
        //swap.npv();
        double npvUp = swap.npv();

        discountTermStructure.linkTo(new ZeroSpreadedTermStructure(new YieldTermStructureHandle(discountCurve), new QuoteHandle(new SimpleQuote(-shift))));
        projectionTermStructure.linkTo(new ZeroSpreadedTermStructure(new YieldTermStructureHandle(projectionCurve), new QuoteHandle(new SimpleQuote(-shift))));
        //swap.npv();
        double npvDown = swap.npv();
        double dv01 = (npvDown - npvUp)/2.0;
        swap.dv01 = swap.swapType == org.quantlib.VanillaSwap.Type.Payer ? dv01 : -1.0 * dv01;

        projectionTermStructure.linkTo(projectionCurve);
        List<com.qlservices.models.Quote> quotes = marketData.getProjectionMarketData(marketData.getEvaluationJavaDate(), "USD","3M");
        for (com.qlservices.models.Quote quote : quotes){
            double value = quote.simpleQuote.value();
            quote.simpleQuote.setValue(value + 0.0001);
            double krdnpv = swap.netPresentValue - swap.npv();
            //LOG.info(quote.tenor + " KRD: " + krdnpv);
            quote.simpleQuote.setValue(value);
        }

        return swap;
    }
}
