package com.qlservices;

import com.qlservices.util.Utils;
import io.quarkus.runtime.StartupEvent;
import org.jboss.logging.Logger;
import org.quantlib.China;
import org.quantlib.Settings;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.time.LocalDate;

@ApplicationScoped
public class StartUpLoader {
    @Inject
    MarketData marketData;

    private static final Logger LOG = Logger.getLogger(StartUpLoader.class);
    void onStart(@Observes StartupEvent ev) throws Exception {
        LOG.info("in OnStart event, loading JNI library");
        System.loadLibrary("QuantLibJNI");
        LOG.info("in OnStart event, setting evaluation date to today, will be used all over");
        Settings.instance().setEvaluationDate(Utils.javaDateToQLDate(LocalDate.now()));
        LOG.info("in OnStart event, caching projection curve market data files");
        marketData.getProjectionMarketData(LocalDate.now(), "USD", "3M");
        LOG.info("in OnStart event, caching discount curve market data files");
        marketData.getDiscountMarketData(LocalDate.now(), "USD");
        LOG.info("in OnStart event, caching fixngs market data files");
        marketData.getFixingsMarketData("USD", "3M");
    }

}
