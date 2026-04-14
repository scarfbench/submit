package org.example.realworldapi.domain.feature.impl;

import org.example.realworldapi.domain.exception.ArticleNotFoundException;
import org.example.realworldapi.domain.feature.FindArticleById;
import org.example.realworldapi.domain.model.article.Article;
import org.example.realworldapi.domain.model.article.ArticleRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class FindArticleByIdImpl implements FindArticleById {

    private final ArticleRepository articleRepository;

    public FindArticleByIdImpl(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    @Override
    public Article handle(UUID id) {
        return articleRepository.findArticleById(id).orElseThrow(ArticleNotFoundException::new);
    }
}
