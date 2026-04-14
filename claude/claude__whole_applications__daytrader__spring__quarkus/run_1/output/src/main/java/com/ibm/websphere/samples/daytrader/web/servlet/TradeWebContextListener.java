package com.ibm.websphere.samples.daytrader.web.servlet;

import com.ibm.websphere.samples.daytrader.util.Log;
import com.ibm.websphere.samples.daytrader.util.TradeConfig;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.InputStream;
import java.util.Properties;

/**
 * Quarkus startup initializer for DayTrader config.
 * In-place replacement for the old ServletContextListener+JSF class.
 */
@ApplicationScoped
@io.quarkus.runtime.Startup
public class TradeWebContextListener {

    public TradeWebContextListener() {
    }

    /** Helper: try ENV first, then MicroProfile Config, then legacy properties file */
    private String s(Properties prop, String... keys) {
        // Try system properties and environment variables first
        for (String k : keys) {
            String v = System.getProperty(k);
            if (v != null) return v;
            v = System.getenv(k);
            if (v != null) return v;
        }
        // Try MicroProfile Config
        try {
            org.eclipse.microprofile.config.Config config = org.eclipse.microprofile.config.ConfigProvider.getConfig();
            for (String k : keys) {
                java.util.Optional<String> v = config.getOptionalValue(k, String.class);
                if (v.isPresent()) return v.get();
            }
        } catch (Exception e) {
            // Config not available, continue
        }
        // Fall back to properties file
        for (String k : keys) {
            String v = prop.getProperty(k);
            if (v != null) return v;
        }
        return null;
    }

    @PostConstruct
    public void init() {
        Log.trace("TradeWebContextListener initializing (Quarkus)");

        // Optional legacy file: classpath:properties/daytrader.properties
        Properties prop = new Properties();
        try {
            InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("properties/daytrader.properties");
            if (in != null) {
                try (InputStream stream = in) {
                    prop.load(stream);
                    System.out.println(
                        "Settings from daytrader.properties: " + prop
                    );
                }
            } else {
                System.out.println(
                    "daytrader.properties not found (classpath)"
                );
            }
        } catch (Exception e) {
            System.out.println("Failed to load daytrader.properties (ignored)");
        }

        try {
            // Mirrors your original precedence but adds daytrader.* YAML keys in between.
            String rt = s(
                prop,
                "RUNTIME_MODE",
                "daytrader.runtime-mode",
                "runtimeMode"
            );
            if (rt != null) TradeConfig.setRunTimeMode(Integer.parseInt(rt));

            String op = s(
                prop,
                "ORDER_PROCESSING_MODE",
                "daytrader.order-processing-mode",
                "orderProcessingMode"
            );
            if (op != null) TradeConfig.setOrderProcessingMode(
                Integer.parseInt(op)
            );

            String mu = s(prop, "MAX_USERS", "daytrader.max-users", "maxUsers");
            if (mu != null) TradeConfig.setMAX_USERS(Integer.parseInt(mu));

            String mq = s(
                prop,
                "MAX_QUOTES",
                "daytrader.max-quotes",
                "maxQuotes"
            );
            if (mq != null) TradeConfig.setMAX_QUOTES(Integer.parseInt(mq));

            String pub = s(
                prop,
                "PUBLISH_QUOTES",
                "daytrader.publish-quotes",
                "publishQuotePriceChange"
            );
            if (pub != null) TradeConfig.setPublishQuotePriceChange(
                Boolean.parseBoolean(pub)
            );

            String alerts = s(
                prop,
                "DISPLAY_ORDER_ALERTS",
                "daytrader.display-order-alerts",
                "displayOrderAlerts"
            );
            if (alerts != null) TradeConfig.setDisplayOrderAlerts(
                Boolean.parseBoolean(alerts)
            );

            String wi = s(
                prop,
                "WEB_INTERFACE",
                "daytrader.web-interface",
                "webInterface"
            );
            if (wi != null) TradeConfig.setWebInterface(Integer.parseInt(wi));

            String freq = s(
                prop,
                "LIST_QUOTE_PRICE_CHANGE_FREQUENCY",
                "daytrader.list-quote-price-change-frequency",
                "listQuotePriceChangeFrequency"
            );
            if (freq != null) TradeConfig.setListQuotePriceChangeFrequency(
                Integer.parseInt(freq)
            );

            // File-only in your original code — we also allow YAML keys
            String prim = s(
                prop,
                "daytrader.prim-iterations",
                "primIterations"
            );
            if (prim != null) TradeConfig.setPrimIterations(
                Integer.parseInt(prim)
            );

            String msi = s(
                prop,
                "daytrader.market-summary-interval",
                "marketSummaryInterval"
            );
            if (msi != null) TradeConfig.setMarketSummaryInterval(
                Integer.parseInt(msi)
            );

            String lr = s(prop, "daytrader.long-run", "longRun");
            if (lr != null) TradeConfig.setLongRun(Boolean.parseBoolean(lr));

            System.out.print(
                "Running in " +
                TradeConfig.getRunTimeModeNames()[TradeConfig.getRunTimeMode()] +
                " Mode"
            );
            System.out.print(
                " | Order Processing " +
                TradeConfig.getOrderProcessingModeNames()[TradeConfig.getOrderProcessingMode()]
            );
            System.out.print(" | MAX_USERS=" + TradeConfig.getMAX_USERS());
            System.out.print(" | MAX_QUOTES=" + TradeConfig.getMAX_QUOTES());
        } catch (Exception e) {
            Log.error("Error initializing TradeConfig", e);
        }
    }
}
