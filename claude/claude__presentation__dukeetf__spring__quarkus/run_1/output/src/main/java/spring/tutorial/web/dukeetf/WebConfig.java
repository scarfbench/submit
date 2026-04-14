package spring.tutorial.web.dukeetf;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import io.undertow.servlet.api.ServletInfo;

@ApplicationScoped
public class WebConfig {

  @Produces
  @ApplicationScoped
  public DukeETFServlet dukeETFServlet(PriceVolumeBean priceVolumeBean) {
    return new DukeETFServlet(priceVolumeBean);
  }

  @Produces
  public ServletInfo dukeETFServletRegistration() {
    return new ServletInfo("DukeETFServlet", DukeETFServlet.class)
        .addMapping("/dukeetf")
        .setAsyncSupported(true);
  }
}
