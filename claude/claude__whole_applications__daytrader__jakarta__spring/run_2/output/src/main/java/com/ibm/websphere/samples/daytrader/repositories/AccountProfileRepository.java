package com.ibm.websphere.samples.daytrader.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ibm.websphere.samples.daytrader.entities.AccountProfileDataBean;

@Repository
public interface AccountProfileRepository extends JpaRepository<AccountProfileDataBean, String> {
}
