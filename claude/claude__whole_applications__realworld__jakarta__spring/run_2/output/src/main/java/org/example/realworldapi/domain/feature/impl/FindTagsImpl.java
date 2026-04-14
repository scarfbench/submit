package org.example.realworldapi.domain.feature.impl;

import org.example.realworldapi.domain.feature.FindTags;
import org.example.realworldapi.domain.model.tag.Tag;
import org.example.realworldapi.domain.model.tag.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FindTagsImpl implements FindTags {

    @Autowired
    private TagRepository tagRepository;

    @Override
    public List<Tag> handle() {
        return tagRepository.findAllTags();
    }
}
