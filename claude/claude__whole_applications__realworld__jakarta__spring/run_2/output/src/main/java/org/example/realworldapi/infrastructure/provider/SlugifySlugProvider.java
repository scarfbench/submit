package org.example.realworldapi.infrastructure.provider;

import com.github.slugify.Slugify;
import org.example.realworldapi.domain.model.provider.SlugProvider;
import org.springframework.stereotype.Component;

@Component
public class SlugifySlugProvider implements SlugProvider {

    private final Slugify slugify;

    public SlugifySlugProvider() {
        this.slugify = Slugify.builder().build();
    }

    @Override
    public String slugify(String text) {
        return slugify.slugify(text);
    }
}
