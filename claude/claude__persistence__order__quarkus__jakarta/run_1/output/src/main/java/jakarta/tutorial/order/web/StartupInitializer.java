package jakarta.tutorial.order.web;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.annotation.Resource;
import jakarta.transaction.UserTransaction;
import jakarta.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.tutorial.order.service.OrderConfigService;

@Singleton
@Startup
@TransactionManagement(TransactionManagementType.BEAN)
public class StartupInitializer {
    private static final Logger logger = Logger.getLogger(StartupInitializer.class.getName());

    @Inject
    OrderConfigService orderConfigService;

    @Resource
    UserTransaction utx;

    @PostConstruct
    public void init() {
        logger.info("StartupInitializer @PostConstruct triggered");
        try {
            utx.begin();
            orderConfigService.createData();
            utx.commit();
            logger.info("Dataset initialization completed successfully");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to initialize dataset", e);
            try {
                utx.rollback();
            } catch (Exception re) {
                logger.log(Level.SEVERE, "Rollback failed", re);
            }
        }
    }
}
