package com.ibm.websphere.samples.daytrader.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;
import com.ibm.websphere.samples.daytrader.entities.QuoteDataBean;

@Repository
public interface QuoteRepository extends JpaRepository<QuoteDataBean, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT q FROM quoteejb q WHERE q.symbol = :symbol")
    QuoteDataBean findBySymbolForUpdate(@Param("symbol") String symbol);

    @Query("SELECT q FROM quoteejb q ORDER BY q.change1 DESC")
    List<QuoteDataBean> findAllOrderByChangeDesc();
}
