package org.example.realworldapi.domain.feature.impl;

import org.example.realworldapi.domain.feature.CreateArticle;
import org.example.realworldapi.domain.feature.CreateSlugByTitle;
import org.example.realworldapi.domain.feature.FindTagsByNameCreateIfNotExists;
import org.example.realworldapi.domain.feature.FindUserById;
import org.example.realworldapi.domain.model.article.*;
import org.example.realworldapi.domain.model.tag.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CreateArticleImpl implements CreateArticle {

    @Autowired
    private FindUserById findUserById;
    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private ArticleModelBuilder articleBuilder;
    @Autowired
    private CreateSlugByTitle createSlugByTitle;
    @Autowired
    private FindTagsByNameCreateIfNotExists findTagsByNameCreateIfNotExists;
    @Autowired
    private TagRelationshipRepository tagRelationshipRepository;

    @Override
    public Article handle(NewArticleInput newArticleInput) {
        final var author = findUserById.handle(newArticleInput.getAuthorId());
        final var slug = createSlugByTitle.handle(newArticleInput.getTitle());
        final var article =
                articleBuilder.build(
                        slug,
                        newArticleInput.getTitle(),
                        newArticleInput.getDescription(),
                        newArticleInput.getBody(),
                        author);
        articleRepository.save(article);
        List<String> tagList = newArticleInput.getTagList();
        if (tagList != null && !tagList.isEmpty()) {
            final List<Tag> tags = findTagsByNameCreateIfNotExists.handle(newArticleInput.getTagList());
            createTagRelationship(article, tags);
        }
        return article;
    }

    private void createTagRelationship(Article article, List<Tag> tags) {
        tags.forEach(tag -> tagRelationshipRepository.save(new TagRelationship(article, tag)));
    }
}
