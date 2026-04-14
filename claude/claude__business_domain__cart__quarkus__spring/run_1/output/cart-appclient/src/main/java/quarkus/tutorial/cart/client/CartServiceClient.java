package quarkus.tutorial.cart.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import quarkus.tutorial.cart.common.BookException;

import java.util.List;

@FeignClient(name = "cart-service-client", url = "${cart-service-client.url}", configuration = CookieFilter.class)
public interface CartServiceClient {

    @PostMapping("/cart/initialize")
    void initialize(@RequestParam("name") String name, @RequestParam("id") String id)
            throws BookException;

    @PostMapping("/cart/add")
    void addBook(@RequestParam("title") String title);

    @GetMapping("/cart/contents")
    List<String> getContents();

    @DeleteMapping("/cart/remove")
    void removeBook(@RequestParam("title") String title) throws BookException;

    @PostMapping("/cart/clear")
    void clearCart();
}
