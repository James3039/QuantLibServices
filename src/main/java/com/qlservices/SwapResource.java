package com.qlservices;

import com.qlservices.models.VanillaSwap;
import org.jboss.logging.Logger;
import org.quantlib.*;

import javax.ws.rs.*;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

@Path("/swap")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SwapResource {
    private static final Logger LOG = Logger.getLogger(SwapResource.class);

    @POST
    public VanillaSwap price(VanillaSwap swap) {

        Settings.instance().setEvaluationDate(new Date(5, Month.July, 2019));
        YieldTermStructureHandle yts = new YieldTermStructureHandle(new FlatForward(new Date(5, Month.July, 2019), 0.02, new Actual360()));
        DiscountingSwapEngine engine = new DiscountingSwapEngine(yts);
        USDLibor index = new USDLibor(new Period(Frequency.Quarterly), yts);
        index.addFixing(new Date(12, Month.June,2019), 0.02);
        swap.setPricingEngine(engine, index);
        swap.npv();
        return swap;
    }
}
