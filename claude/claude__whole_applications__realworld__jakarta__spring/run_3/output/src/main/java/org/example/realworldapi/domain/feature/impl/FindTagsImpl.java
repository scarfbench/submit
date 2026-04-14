package org.example.realworldapi.domain.feature.impl;

import org.example.realworldapi.domain.feature.FindTags;
import org.example.realworldapi.domain.model.tag.Tag;
import org.example.realworldapi.domain.model.tag.TagRepository;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class FindTagsImpl implements FindTags {

    private final TagRepository tagRepository;
    public FindTagsImpl(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Override
    public List<Tag> handle() {
        return tagRepository.findAllTags();
    }
}
