package org.example.realworldapi.domain.feature.impl;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.example.realworldapi.domain.feature.CreateTag;
import org.example.realworldapi.domain.model.tag.Tag;
import org.example.realworldapi.domain.model.tag.TagBuilder;
import org.example.realworldapi.domain.model.tag.TagRepository;

@Singleton
public class CreateTagImpl implements CreateTag {

    @Inject
    TagRepository tagRepository;
    @Inject
    TagBuilder tagBuilder;

    @Override
    public Tag handle(String name) {
        final var tag = tagBuilder.build(name);
        tagRepository.save(tag);
        return tag;
    }
}
