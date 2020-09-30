package com.qlservices.services;

import java.time.LocalDate;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.qlservices.MarketData;
import com.qlservices.models.Quote;
import com.qlservices.util.EURConventions;
import com.qlservices.util.USDConventions;
import org.jboss.logging.Logger;
import org.quantlib.*;

@ApplicationScoped
public class CurveBuilderService {

    @Inject
    MarketData marketData;

    private static final Logger LOG = Logger.getLogger(CurveBuilderService.class);

    public YieldTermStructure buildDiscountCurve(String curveName, String currency) throws Exception {
        org.quantlib.Date qlToday = Settings.instance().getEvaluationDate();
        LocalDate javaToday = marketData.getEvaluationJavaDate();
        LOG.info("Building discount curve ");
        RelinkableYieldTermStructureHandle discountCurve = null;
        RateHelperVector rateHelpers = new RateHelperVector();
        Calendar cal = null;
        OvernightIndex overnightIndex = null;
        if (currency.equals("USD")) {
            cal = new UnitedStates();
            overnightIndex = new FedFunds();
        } else {
            cal = new TARGET();
            overnightIndex = new Eonia();
        }
        Date settlementDate = cal.advance(qlToday, 2, TimeUnit.Days);
        int settlementDays = settlementDate.serialNumber() - qlToday.serialNumber();

        //build discount curve
        List<Quote> quotes = marketData.getDiscountMarketData(marketData.getEvaluationJavaDate(), currency);
        for (Quote quote : quotes) {
            rateHelpers.add(new OISRateHelper(settlementDays, new Period(quote.tenor), new QuoteHandle(new SimpleQuote(quote.rate)), overnightIndex));
        }
        YieldTermStructure curve = new PiecewiseLogLinearDiscount(settlementDate, rateHelpers, overnightIndex.dayCounter());
        curve.enableExtrapolation();
        LOG.info("Finished building discount curve");
        return curve;
    }

    public YieldTermStructure buildProjectionCurve(String curveName, String currency, String tenor, YieldTermStructure discountCurve) throws Exception {
        LOG.info("building projection curve");
        RelinkableYieldTermStructureHandle discountTermStructure = new RelinkableYieldTermStructureHandle();
        YieldTermStructure projectionCurve = null;
        org.quantlib.Date qlToday = Settings.instance().getEvaluationDate();
        Calendar cal = null;

        if (currency.equals("USD")) {
            cal = new UnitedStates();
        } else {
            cal = new TARGET();
        }
        Date settlementDate = cal.advance(qlToday, 2, TimeUnit.Days);

        //build projection curve
        RateHelperVector rateHelpers = new RateHelperVector();
        //discountTermStructure.linkTo(buildDiscountCurve(curveName, currency));
        discountTermStructure.linkTo(discountCurve);
        List<Quote> quotes = marketData.getProjectionMarketData(marketData.getEvaluationJavaDate(), currency,tenor);
        for (Quote quote : quotes){
            LOG.info("inside loop");
            String quoteType = quote.quoteName;
            String ten = quote.tenor;
            if (currency.equals("USD")) {
                LOG.info("about to add usdratehelper");
                rateHelpers.add(getUSDRateHelper(quoteType, ten, quote.quoteHandle, discountTermStructure));
                LOG.info("done adding usdartehelper");
            } else if (currency.equals("EUR")){
                rateHelpers.add(getEURRateHelper(quoteType, ten, quote.quoteHandle, discountTermStructure));
            }
        }
        LOG.info("added rate helpers");
        if (currency.equals("USD")){
            projectionCurve = new PiecewiseLinearZero(settlementDate, rateHelpers, USDConventions.CURVE_DAY_COUNTER);
            projectionCurve.enableExtrapolation();
            LOG.info("piecewiselinearzero built");
            //ryts = new RelinkableYieldTermStructureHandle(yts);
        } else if (currency.equals("EUR")){
            projectionCurve = new PiecewiseLinearZero(settlementDate, rateHelpers, EURConventions.CURVE_DAY_COUNTER);
            projectionCurve.enableExtrapolation();
            //ryts = new RelinkableYieldTermStructureHandle(yts);
        }
        LOG.info("finsihed building projection curve");
        return projectionCurve;
    }

    private RateHelper getUSDRateHelper(String quoteType, String tenor, QuoteHandle quoteHandle, RelinkableYieldTermStructureHandle discountCurve){
        RateHelper helper = null;
        LOG.info("in usdratehelper");
        if (quoteType.equals("Depos")){
            LOG.info("inside depos");
            helper = new DepositRateHelper(quoteHandle, new Period(tenor),
                    USDConventions.DEPOSIT_FIXING_DAYS,
                    USDConventions.DEPOSIT_CALENDAR,
                    USDConventions.DEPOSIT_BUSINESS_DAY_CONVENTION,
                    USDConventions.DEPOSIT_END_OF_MONTH,
                    USDConventions.DEPOSIT_DAY_COUNTER
            );
            LOG.info("added depos");
        } else if (quoteType.equals("Futures")){
            Date iborStartDate = IMM.nextDate(Settings.instance().getEvaluationDate().add(new Period(tenor)));
            helper = new FuturesRateHelper(quoteHandle, iborStartDate,USDConventions.LENGTH_IN_MONTHS,
                    USDConventions.FUTURE_CALENDAR,
                    USDConventions.FUTURE_BUSINESS_DAY_CONVENTION,
                    USDConventions.FUTURE_END_OF_MONTH,
                    USDConventions.FUTURE_DAY_COUNTER
            );
            LOG.info("added futures");
        } else if (quoteType.equals("Swaps")){
            helper = new SwapRateHelper(quoteHandle, new Period(tenor),
                    USDConventions.SWAP_FIXED_CALENDAR,
                    USDConventions.SWAP_FIXED_FREQUENCY,
                    USDConventions.SWAP_FIXED_CONVENTION,
                    USDConventions.SWAP_FIXED_DAY_COUNTER,
                    USDConventions.SWAP_FLOATING_INDEX,
                    new QuoteHandle(),new Period(0, TimeUnit.Days), discountCurve
                    );
            LOG.info("added swaps");
        }
        LOG.info("done usdratehelper");
        return helper;
    }

    private RateHelper getEURRateHelper(String quoteType, String tenor, QuoteHandle quoteHandle, RelinkableYieldTermStructureHandle discountCurve){
        RateHelper helper = null;
        if (quoteType.equals("Depos")){
            helper = new DepositRateHelper(quoteHandle, new Period(tenor),
                    EURConventions.DEPOSIT_FIXING_DAYS,
                    EURConventions.DEPOSIT_CALENDAR,
                    EURConventions.DEPOSIT_BUSINESS_DAY_CONVENTION,
                    EURConventions.DEPOSIT_END_OF_MONTH,
                    EURConventions.DEPOSIT_DAY_COUNTER
            );
        } else if (quoteType.equals("Futures")){
            Date iborStartDate = IMM.nextDate(Settings.instance().getEvaluationDate().add(new Period(tenor)));
            helper = new FuturesRateHelper(quoteHandle, iborStartDate,EURConventions.LENGTH_IN_MONTHS,
                    EURConventions.FUTURE_CALENDAR,
                    EURConventions.FUTURE_BUSINESS_DAY_CONVENTION,
                    EURConventions.FUTURE_END_OF_MONTH,
                    EURConventions.FUTURE_DAY_COUNTER
            );
        } else if (quoteType.equals("Swaps")){
            helper = new SwapRateHelper(quoteHandle, new Period(tenor),
                    EURConventions.SWAP_FIXED_CALENDAR,
                    EURConventions.SWAP_FIXED_FREQUENCY,
                    EURConventions.SWAP_FIXED_CONVENTION,
                    EURConventions.SWAP_FIXED_DAY_COUNTER,
                    EURConventions.SWAP_FLOATING_INDEX,
                    new QuoteHandle(),new Period(0, TimeUnit.Days), discountCurve);
        }
        return helper;
    }
}
