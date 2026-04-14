package org.example.realworldapi.domain.feature.impl;

import org.example.realworldapi.domain.feature.CreateSlugByTitle;
import org.example.realworldapi.domain.model.article.ArticleRepository;
import org.example.realworldapi.domain.model.provider.SlugProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CreateSlugByTitleImpl implements CreateSlugByTitle {

    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private SlugProvider slugProvider;

    @Override
    public String handle(String title) {
        String slug = slugProvider.slugify(title);
        if (articleRepository.existsBySlug(slug)) {
            slug += UUID.randomUUID().toString();
        }
        return slug;
    }
}
