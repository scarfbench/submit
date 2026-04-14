package io.quarkuscoffeeshop.coffeeshop.web;

import io.quarkuscoffeeshop.coffeeshop.counter.api.OrderService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebResource {

    @Value("${stream.url:http://localhost:8080/dashboard/stream}")
    private String streamUrl;

    @Value("${store.id:ATLANTA}")
    private String storeId;

    private final OrderService orderService;

    public WebResource(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/")
    public String getIndex(Model model) {
        model.addAttribute("streamUrl", streamUrl);
        model.addAttribute("storeId", storeId);
        return "coffeeshopTemplate";
    }
}
