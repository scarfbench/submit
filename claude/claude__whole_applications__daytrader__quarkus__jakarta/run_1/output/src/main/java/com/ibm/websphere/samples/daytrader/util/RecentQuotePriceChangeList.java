package com.ibm.websphere.samples.daytrader.util;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;

import com.ibm.websphere.samples.daytrader.entities.QuoteDataBean;
import com.ibm.websphere.samples.daytrader.interfaces.QuotePriceChange;

@ApplicationScoped
public class RecentQuotePriceChangeList {

    private static final int MAX_SIZE = 5;
    private final List<QuoteDataBean> recentChanges = new CopyOnWriteArrayList<>();

    @Inject
    @QuotePriceChange
    Event<QuoteDataBean> quotePriceChangeEvent;

    public void add(QuoteDataBean quote) {
        recentChanges.add(quote);
        while (recentChanges.size() > MAX_SIZE) {
            recentChanges.remove(0);
        }
        if (quotePriceChangeEvent != null) {
            quotePriceChangeEvent.fireAsync(quote);
        }
    }

    public List<QuoteDataBean> getRecentChanges() {
        return recentChanges;
    }

    public void clear() {
        recentChanges.clear();
    }
}
