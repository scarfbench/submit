package org.example.realworldapi.domain.feature.impl;

import org.example.realworldapi.domain.feature.FindArticleTags;
import org.example.realworldapi.domain.model.article.Article;
import org.example.realworldapi.domain.model.article.TagRelationshipRepository;
import org.example.realworldapi.domain.model.tag.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FindArticleTagsImpl implements FindArticleTags {

    @Autowired
    private TagRelationshipRepository tagRelationshipRepository;

    @Override
    public List<Tag> handle(Article article) {
        return tagRelationshipRepository.findArticleTags(article);
    }
}
