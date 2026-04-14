/**
 * Quarkus CDI bean replacing MarketSummarySingleton (EJB @Singleton).
 * Uses Quarkus @Scheduled instead of EJB @Schedule.
 */
package com.ibm.websphere.samples.daytrader.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;

import io.quarkus.scheduler.Scheduled;

import com.ibm.websphere.samples.daytrader.beans.MarketSummaryDataBean;
import com.ibm.websphere.samples.daytrader.entities.QuoteDataBean;
import com.ibm.websphere.samples.daytrader.interfaces.MarketSummaryUpdate;
import com.ibm.websphere.samples.daytrader.util.FinancialUtils;
import com.ibm.websphere.samples.daytrader.util.Log;
import com.ibm.websphere.samples.daytrader.util.TradeConfig;

@ApplicationScoped
public class MarketSummaryService {

    private MarketSummaryDataBean marketSummaryDataBean;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    @Inject
    EntityManager entityManager;

    @Inject
    @MarketSummaryUpdate
    Event<String> mkSummaryUpdateEvent;

    @Scheduled(every = "20s")
    @Transactional
    void updateMarketSummary() {
        Log.trace("MarketSummaryService:updateMarketSummary -- updating market summary");

        List<QuoteDataBean> quotes;

        try {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<QuoteDataBean> criteriaQuery = criteriaBuilder.createQuery(QuoteDataBean.class);
            Root<QuoteDataBean> quoteRoot = criteriaQuery.from(QuoteDataBean.class);
            criteriaQuery.orderBy(criteriaBuilder.desc(quoteRoot.get("change1")));
            criteriaQuery.select(quoteRoot);
            TypedQuery<QuoteDataBean> q = entityManager.createQuery(criteriaQuery);
            quotes = q.getResultList();
        } catch (Exception e) {
            Log.debug("Warning: The database has not been configured. If this is the first time the application has been started, please create and populate the database tables.");
            return;
        }

        QuoteDataBean[] quoteArray = quotes.toArray(new QuoteDataBean[quotes.size()]);
        ArrayList<QuoteDataBean> topGainers = new ArrayList<QuoteDataBean>(5);
        ArrayList<QuoteDataBean> topLosers = new ArrayList<QuoteDataBean>(5);
        BigDecimal TSIA = FinancialUtils.ZERO;
        BigDecimal openTSIA = FinancialUtils.ZERO;
        double totalVolume = 0.0;

        if (quoteArray.length > 5) {
            for (int i = 0; i < 5; i++) {
                topGainers.add(quoteArray[i]);
            }
            for (int i = quoteArray.length - 1; i >= quoteArray.length - 5; i--) {
                topLosers.add(quoteArray[i]);
            }

            for (QuoteDataBean quote : quoteArray) {
                BigDecimal price = quote.getPrice();
                BigDecimal open = quote.getOpen();
                double volume = quote.getVolume();
                TSIA = TSIA.add(price);
                openTSIA = openTSIA.add(open);
                totalVolume += volume;
            }
            TSIA = TSIA.divide(new BigDecimal(quoteArray.length), FinancialUtils.ROUND);
            openTSIA = openTSIA.divide(new BigDecimal(quoteArray.length), FinancialUtils.ROUND);
        }

        setMarketSummaryDataBean(new MarketSummaryDataBean(TSIA, openTSIA, totalVolume, topGainers, topLosers));
        mkSummaryUpdateEvent.fireAsync("MarketSummaryUpdate");
    }

    public MarketSummaryDataBean getMarketSummaryDataBean() {
        lock.readLock().lock();
        try {
            if (marketSummaryDataBean == null) {
                lock.readLock().unlock();
                updateMarketSummary();
                lock.readLock().lock();
            }
            return marketSummaryDataBean;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setMarketSummaryDataBean(MarketSummaryDataBean marketSummaryDataBean) {
        lock.writeLock().lock();
        try {
            this.marketSummaryDataBean = marketSummaryDataBean;
        } finally {
            lock.writeLock().unlock();
        }
    }
}
