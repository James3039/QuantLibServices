package com.qlservices;

import com.qlservices.models.Fixing;
import com.qlservices.models.Quote;
import com.qlservices.util.Utils;
import io.quarkus.cache.CacheKey;
import io.quarkus.cache.CacheResult;
import org.jboss.logging.Logger;
import org.quantlib.Date;
import org.quantlib.Settings;

import javax.inject.Singleton;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class MarketData {
    private static final Logger LOG = Logger.getLogger(MarketData.class);
    public MarketData(){}

    @CacheResult(cacheName = "evluation-date-cache")
    public LocalDate getEvaluationJavaDate(){
        Date qlDate = Settings.instance().getEvaluationDate();
        return Utils.qlDateToJavaDate(qlDate);
    }

    @CacheResult(cacheName = "projection-curve-cache")
    public List<Quote> getProjectionMarketData(@CacheKey LocalDate date, @CacheKey String currency, @CacheKey String tenor) throws IOException {
        List<Quote> quotes = new ArrayList<>();
        String fileName = currency + "-LIBOR-" + tenor + ".csv";
        InputStream inputStream = getClass().getResourceAsStream("/market-data/" + fileName);
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        for (String line = br.readLine(); line != null; line = br.readLine()) {
            String quote = line.split(",")[0];
            String quoteType = quote.split("\\.")[0];
            String ten = quote.split("\\.")[1];
            double rate = Double.parseDouble(line.split(",")[1]);
            quotes.add(new Quote(quoteType,ten,rate));
        }
        LOG.info("Loaded projection market data records:" + quotes.size());
        return quotes;
    }

    @CacheResult(cacheName = "discount-curve-cache")
    public List<Quote> getDiscountMarketData(@CacheKey LocalDate date, @CacheKey String currency) throws IOException {
        List<Quote> quotes = new ArrayList<>();
        String fileName = null;
        if (currency.equals("USD")) {
            fileName = currency + "-FedFunds.csv";
        } else if (currency.equals("EUR")){
            fileName = currency + "-EONIA.csv";
        }
        InputStream inputStream = getClass().getResourceAsStream("/market-data/" + fileName);
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        for (String line = br.readLine(); line != null; line = br.readLine()) {
            String ten = line.split(",")[0];
            double rate = Double.parseDouble(line.split(",")[1]);
            quotes.add(new Quote("OISSWAP",ten,rate));
        }
        LOG.info("Loaded discount market data records:" + quotes.size());
        return quotes;
    }

    @CacheResult(cacheName = "fixings-cache")
    public List<Fixing> getFixingsMarketData(@CacheKey String currency, @CacheKey String tenor) throws IOException {
        List<Fixing> fixings = new ArrayList<>();



        /*MongoCollection fixingsCollection = mongoClient.getDatabase("marketdata").getCollection("USD-FIXINGS-3M");
        MongoCursor<Document> cursor = fixingsCollection.find().iterator();
        try {
            while (cursor.hasNext()) {
                Document document = cursor.next();
                Fixing fixing = new Fixing();
                LocalDate dt = LocalDate.parse(document.getString("date"),DateTimeFormatter.ofPattern("MM-dd-yyyy"));
                double rate = Double.parseDouble(document.getString("rate"));
                fixings.add(new Fixing(Utils.javaDateToQLDate(dt),rate));
            }
        } finally {
            cursor.close();
        }*/
        String fileName = currency + "-FIXINGS-" + tenor + ".csv";
        InputStream inputStream = getClass().getResourceAsStream("/market-data/" + fileName);
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        for (String line = br.readLine(); line != null; line = br.readLine()) {
            LocalDate dt = LocalDate.parse(line.split(",")[0],DateTimeFormatter.ofPattern("MM-dd-yyyy"));
            double rate = Double.parseDouble(line.split(",")[1]);
            fixings.add(new Fixing(Utils.javaDateToQLDate(dt),rate));
        }
        LOG.info("Loaded fixings market data records:" + fixings.size());
        return fixings;
    }
}
