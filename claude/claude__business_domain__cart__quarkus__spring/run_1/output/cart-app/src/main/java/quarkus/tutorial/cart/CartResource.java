package quarkus.tutorial.cart;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import quarkus.tutorial.cart.common.BookException;
import quarkus.tutorial.cart.common.Cart;

@RestController
@RequestMapping("/cart")
public class CartResource {

    @Autowired
    Cart cart;

    @PostMapping("/initialize")
    public void initialize(@RequestParam("name") String name, @RequestParam(value = "id", required = false) String id)
            throws BookException {
        if (id == null) {
            cart.initialize(name);
        } else {
            cart.initialize(name, id);
        }
    }

    @PostMapping("/add")
    public void add(@RequestParam("title") String title) {
        cart.addBook(title);
    }

    @GetMapping("/contents")
    public List<String> getContents() {
        return cart.getContents();
    }

    @DeleteMapping("/remove")
    public void remove(@RequestParam("title") String title) throws BookException {
        cart.removeBook(title);
    }

    @PostMapping("/clear")
    public void clear(HttpServletRequest request) {
        var session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        cart.remove();
    }
}
