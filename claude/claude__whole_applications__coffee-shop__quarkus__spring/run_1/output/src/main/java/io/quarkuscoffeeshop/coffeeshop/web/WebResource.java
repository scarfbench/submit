package io.quarkuscoffeeshop.coffeeshop.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebResource {

    @Value("${app.stream-url}")
    String streamUrl;

    @Value("${app.store-id}")
    String storeId;

    @GetMapping("/")
    public String getIndex(Model model) {
        model.addAttribute("streamUrl", streamUrl);
        model.addAttribute("storeId", storeId);
        return "coffeeshopTemplate";
    }
}
