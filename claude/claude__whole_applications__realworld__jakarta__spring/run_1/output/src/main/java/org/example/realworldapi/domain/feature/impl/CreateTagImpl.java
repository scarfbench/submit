package org.example.realworldapi.domain.feature.impl;

import org.example.realworldapi.domain.feature.CreateTag;
import org.example.realworldapi.domain.model.tag.Tag;
import org.example.realworldapi.domain.model.tag.TagBuilder;
import org.example.realworldapi.domain.model.tag.TagRepository;
import org.springframework.stereotype.Service;

@Service
public class CreateTagImpl implements CreateTag {

    private final TagRepository tagRepository;
    private final TagBuilder tagBuilder;

    public CreateTagImpl(TagRepository tagRepository, TagBuilder tagBuilder) {
        this.tagRepository = tagRepository;
        this.tagBuilder = tagBuilder;
    }

    @Override
    public Tag handle(String name) {
        final var tag = tagBuilder.build(name);
        tagRepository.save(tag);
        return tag;
    }
}
