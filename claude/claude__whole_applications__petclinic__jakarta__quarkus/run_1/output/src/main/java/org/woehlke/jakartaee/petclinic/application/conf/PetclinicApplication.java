package org.woehlke.jakartaee.petclinic.application.conf;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import lombok.extern.java.Log;

/**
 * Created with IntelliJ IDEA.
 * User: tw
 * Date: 05.01.14
 * Time: 09:27
 * To change this template use File | Settings | File Templates.
 */
@Log
@ApplicationScoped
@ApplicationPath("/rest")
public class PetclinicApplication extends Application {

}
