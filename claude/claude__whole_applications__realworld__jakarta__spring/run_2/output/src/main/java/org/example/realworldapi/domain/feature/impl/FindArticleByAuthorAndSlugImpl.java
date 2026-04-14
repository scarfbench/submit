package org.example.realworldapi.domain.feature.impl;

import org.example.realworldapi.domain.exception.ArticleNotFoundException;
import org.example.realworldapi.domain.feature.FindArticleByAuthorAndSlug;
import org.example.realworldapi.domain.model.article.Article;
import org.example.realworldapi.domain.model.article.ArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class FindArticleByAuthorAndSlugImpl implements FindArticleByAuthorAndSlug {

    @Autowired
    private ArticleRepository articleRepository;

    @Override
    public Article handle(UUID authorId, String slug) {
        return articleRepository
                .findByAuthorAndSlug(authorId, slug)
                .orElseThrow(ArticleNotFoundException::new);
    }
}
