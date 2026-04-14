package org.example.realworldapi.domain.feature.impl;

import org.example.realworldapi.domain.exception.ArticleNotFoundException;
import org.example.realworldapi.domain.feature.FindArticleByAuthorAndSlug;
import org.example.realworldapi.domain.model.article.Article;
import org.example.realworldapi.domain.model.article.ArticleRepository;

import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class FindArticleByAuthorAndSlugImpl implements FindArticleByAuthorAndSlug {

    private final ArticleRepository articleRepository;
    public FindArticleByAuthorAndSlugImpl(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    @Override
    public Article handle(UUID authorId, String slug) {
        return articleRepository
                .findByAuthorAndSlug(authorId, slug)
                .orElseThrow(ArticleNotFoundException::new);
    }
}
