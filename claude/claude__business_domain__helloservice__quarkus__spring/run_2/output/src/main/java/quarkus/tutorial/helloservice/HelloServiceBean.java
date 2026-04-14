package quarkus.tutorial.helloservice;

import org.springframework.stereotype.Service;
import jakarta.jws.WebMethod;
import jakarta.jws.WebService;

@Service
@WebService(serviceName = "HelloService", portName = "HelloServicePort", targetNamespace = "http://helloservice.tutorial.quarkus/")
public class HelloServiceBean {
    private final String message = "Hello, ";

    public HelloServiceBean() {}

    @WebMethod
    public String sayHello(String name) {
        return message + name + ".";
    }
}
