package org.example.realworldapi.domain.feature.impl;

import org.example.realworldapi.domain.feature.FindMostRecentArticlesByFilter;
import org.example.realworldapi.domain.model.article.Article;
import org.example.realworldapi.domain.model.article.ArticleFilter;
import org.example.realworldapi.domain.model.article.ArticleRepository;
import org.example.realworldapi.domain.model.article.PageResult;
import org.springframework.stereotype.Service;

@Service
public class FindMostRecentArticlesByFilterImpl implements FindMostRecentArticlesByFilter {

    private final ArticleRepository articleRepository;

    public FindMostRecentArticlesByFilterImpl(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    @Override
    public PageResult<Article> handle(ArticleFilter articleFilter) {
        return articleRepository.findMostRecentArticlesByFilter(articleFilter);
    }
}
