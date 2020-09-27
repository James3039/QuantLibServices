package com.qlservices;

import org.jboss.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.quantlib.*;

@Path("/hello")
public class ExampleResource {
    private static final Logger LOG = Logger.getLogger(ExampleResource.class);
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        Calendar cal = new TARGET();
        Date settlementDate = new Date(18, Month.September, 2008);
        // must be a business day
        settlementDate = cal.adjust(settlementDate);
        LOG.info(settlementDate);
        return "hello on " + settlementDate;
    }
}