package spring.tutorial.web.dukeetf;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import io.undertow.servlet.api.ServletInfo;
import io.undertow.servlet.util.ImmediateInstanceFactory;

@ApplicationScoped
public class WebConfig {

    @Inject
    PriceVolumeBean priceVolumeBean;

    @Produces
    @Dependent
    public ServletInfo dukeETFServletInfo() {
        DukeETFServlet servlet = new DukeETFServlet(priceVolumeBean);
        return new ServletInfo("DukeETFServlet", DukeETFServlet.class,
                new ImmediateInstanceFactory<>(servlet))
                .addMapping("/dukeetf")
                .setAsyncSupported(true);
    }
}
