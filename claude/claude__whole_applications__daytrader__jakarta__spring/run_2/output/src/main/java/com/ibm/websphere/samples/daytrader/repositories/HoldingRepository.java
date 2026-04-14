package com.ibm.websphere.samples.daytrader.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.ibm.websphere.samples.daytrader.entities.HoldingDataBean;

@Repository
public interface HoldingRepository extends JpaRepository<HoldingDataBean, Integer> {
    @Query("SELECT h FROM holdingejb h WHERE h.account.profile.userID = :userID")
    List<HoldingDataBean> findByAccountProfileUserID(@Param("userID") String userID);
}
