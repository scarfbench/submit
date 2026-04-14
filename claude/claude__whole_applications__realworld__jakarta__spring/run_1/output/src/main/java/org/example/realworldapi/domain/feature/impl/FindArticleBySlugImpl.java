package org.example.realworldapi.domain.feature.impl;

import org.example.realworldapi.domain.exception.ArticleNotFoundException;
import org.example.realworldapi.domain.feature.FindArticleBySlug;
import org.example.realworldapi.domain.model.article.Article;
import org.example.realworldapi.domain.model.article.ArticleRepository;
import org.springframework.stereotype.Service;

@Service
public class FindArticleBySlugImpl implements FindArticleBySlug {

    private final ArticleRepository articleRepository;

    public FindArticleBySlugImpl(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    @Override
    public Article handle(String slug) {
        return articleRepository.findBySlug(slug).orElseThrow(ArticleNotFoundException::new);
    }
}
