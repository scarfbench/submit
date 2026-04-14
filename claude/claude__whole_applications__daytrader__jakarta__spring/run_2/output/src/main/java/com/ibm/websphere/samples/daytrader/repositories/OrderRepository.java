package com.ibm.websphere.samples.daytrader.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.ibm.websphere.samples.daytrader.entities.OrderDataBean;

@Repository
public interface OrderRepository extends JpaRepository<OrderDataBean, Integer> {

    @Query("SELECT o FROM orderejb o WHERE o.account.profile.userID = :userID")
    List<OrderDataBean> findByAccountProfileUserID(@Param("userID") String userID);

    @Query("SELECT o FROM orderejb o WHERE o.orderStatus = 'closed' AND o.account.profile.userID = :userID")
    List<OrderDataBean> findClosedOrdersByUserID(@Param("userID") String userID);

    @Modifying
    @Query("UPDATE orderejb o SET o.orderStatus = 'completed' WHERE o.orderStatus = 'closed' AND o.account.profile.userID = :userID")
    int completeClosedOrders(@Param("userID") String userID);

    @Query("SELECT COUNT(o) FROM orderejb o WHERE o.account.profile.userID = :userID")
    long countByUserID(@Param("userID") String userID);
}
