package org.example.realworldapi.domain.feature.impl;

import org.example.realworldapi.domain.feature.FindArticlesByFilter;
import org.example.realworldapi.domain.model.article.Article;
import org.example.realworldapi.domain.model.article.ArticleFilter;
import org.example.realworldapi.domain.model.article.ArticleRepository;
import org.example.realworldapi.domain.model.article.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FindArticlesByFilterImpl implements FindArticlesByFilter {

    @Autowired
    private ArticleRepository articleRepository;

    @Override
    public PageResult<Article> handle(ArticleFilter articleFilter) {
        return articleRepository.findArticlesByFilter(articleFilter);
    }
}
