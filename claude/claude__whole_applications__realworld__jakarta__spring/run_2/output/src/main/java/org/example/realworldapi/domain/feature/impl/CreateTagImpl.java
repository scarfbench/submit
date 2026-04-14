package org.example.realworldapi.domain.feature.impl;

import org.example.realworldapi.domain.feature.CreateTag;
import org.example.realworldapi.domain.model.tag.Tag;
import org.example.realworldapi.domain.model.tag.TagBuilder;
import org.example.realworldapi.domain.model.tag.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CreateTagImpl implements CreateTag {

    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private TagBuilder tagBuilder;

    @Override
    public Tag handle(String name) {
        final var tag = tagBuilder.build(name);
        tagRepository.save(tag);
        return tag;
    }
}
