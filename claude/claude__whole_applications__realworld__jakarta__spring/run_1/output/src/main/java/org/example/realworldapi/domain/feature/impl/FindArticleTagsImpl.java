package org.example.realworldapi.domain.feature.impl;

import org.example.realworldapi.domain.feature.FindArticleTags;
import org.example.realworldapi.domain.model.article.Article;
import org.example.realworldapi.domain.model.article.TagRelationshipRepository;
import org.example.realworldapi.domain.model.tag.Tag;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FindArticleTagsImpl implements FindArticleTags {

    private final TagRelationshipRepository tagRelationshipRepository;

    public FindArticleTagsImpl(TagRelationshipRepository tagRelationshipRepository) {
        this.tagRelationshipRepository = tagRelationshipRepository;
    }

    @Override
    public List<Tag> handle(Article article) {
        return tagRelationshipRepository.findArticleTags(article);
    }
}
