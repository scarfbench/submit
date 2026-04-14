package org.example.realworldapi.domain.feature.impl;

import org.example.realworldapi.domain.exception.ArticleNotFoundException;
import org.example.realworldapi.domain.feature.FindArticleById;
import org.example.realworldapi.domain.model.article.Article;
import org.example.realworldapi.domain.model.article.ArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class FindArticleByIdImpl implements FindArticleById {

    @Autowired
    private ArticleRepository articleRepository;

    @Override
    public Article handle(UUID id) {
        return articleRepository.findArticleById(id).orElseThrow(ArticleNotFoundException::new);
    }
}
