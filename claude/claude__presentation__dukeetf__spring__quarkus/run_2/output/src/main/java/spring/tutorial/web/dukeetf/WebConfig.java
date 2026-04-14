package spring.tutorial.web.dukeetf;

import io.undertow.servlet.api.ServletInfo;
import io.undertow.servlet.util.ImmediateInstanceFactory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

@ApplicationScoped
public class WebConfig {

    @Inject
    PriceVolumeBean priceVolumeBean;

    @Produces
    @ApplicationScoped
    public DukeETFServlet dukeETFServlet() {
        return new DukeETFServlet(priceVolumeBean);
    }

    @Produces
    public ServletInfo dukeETFServletRegistration(DukeETFServlet servlet) {
        ServletInfo servletInfo = new ServletInfo("DukeETFServlet", DukeETFServlet.class,
            new ImmediateInstanceFactory<>(servlet));
        servletInfo.addMapping("/dukeetf");
        servletInfo.setAsyncSupported(true);
        return servletInfo;
    }
}
