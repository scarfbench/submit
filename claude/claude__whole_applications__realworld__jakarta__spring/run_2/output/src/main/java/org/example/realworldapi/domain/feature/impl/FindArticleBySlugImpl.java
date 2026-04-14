package org.example.realworldapi.domain.feature.impl;

import org.example.realworldapi.domain.exception.ArticleNotFoundException;
import org.example.realworldapi.domain.feature.FindArticleBySlug;
import org.example.realworldapi.domain.model.article.Article;
import org.example.realworldapi.domain.model.article.ArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FindArticleBySlugImpl implements FindArticleBySlug {

    @Autowired
    private ArticleRepository articleRepository;

    @Override
    public Article handle(String slug) {
        return articleRepository.findBySlug(slug).orElseThrow(ArticleNotFoundException::new);
    }
}
