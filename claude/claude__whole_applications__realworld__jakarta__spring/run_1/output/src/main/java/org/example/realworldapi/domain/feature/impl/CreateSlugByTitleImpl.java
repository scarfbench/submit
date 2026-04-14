package org.example.realworldapi.domain.feature.impl;

import org.example.realworldapi.domain.feature.CreateSlugByTitle;
import org.example.realworldapi.domain.model.article.ArticleRepository;
import org.example.realworldapi.domain.model.provider.SlugProvider;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CreateSlugByTitleImpl implements CreateSlugByTitle {

    private final ArticleRepository articleRepository;
    private final SlugProvider slugProvider;

    public CreateSlugByTitleImpl(ArticleRepository articleRepository, SlugProvider slugProvider) {
        this.articleRepository = articleRepository;
        this.slugProvider = slugProvider;
    }

    @Override
    public String handle(String title) {
        String slug = slugProvider.slugify(title);
        if (articleRepository.existsBySlug(slug)) {
            slug += UUID.randomUUID().toString();
        }
        return slug;
    }
}
