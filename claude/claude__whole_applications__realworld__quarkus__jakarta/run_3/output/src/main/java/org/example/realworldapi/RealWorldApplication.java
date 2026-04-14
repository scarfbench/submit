package org.example.realworldapi;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;
import org.example.realworldapi.application.web.resource.*;
import org.example.realworldapi.infrastructure.web.provider.*;

@ApplicationPath("/")
public class RealWorldApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        // Resources
        classes.add(ArticlesResource.class);
        classes.add(UsersResource.class);
        classes.add(UserResource.class);
        classes.add(ProfilesResource.class);
        classes.add(TagsResource.class);
        classes.add(HealthResource.class);

        // Providers
        classes.add(JwtAuthFilter.class);
        classes.add(SecurityFilter.class);
        classes.add(BusinessExceptionMapper.class);
        classes.add(ValidationExceptionMapper.class);
        classes.add(CorsFilter.class);
        classes.add(JacksonConfig.class);
        classes.add(TransactionFilter.class);

        return classes;
    }
}
