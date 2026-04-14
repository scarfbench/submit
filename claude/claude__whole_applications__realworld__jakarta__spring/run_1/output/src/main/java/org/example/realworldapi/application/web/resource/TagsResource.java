package org.example.realworldapi.application.web.resource;

import org.example.realworldapi.application.web.model.response.TagsResponse;
import org.example.realworldapi.domain.feature.FindTags;
import org.example.realworldapi.domain.model.tag.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/tags")
public class TagsResource {

    private final FindTags findTags;

    public TagsResource(FindTags findTags) {
        this.findTags = findTags;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TagsResponse> getTags() {
        List<Tag> tags = findTags.handle();
        return ResponseEntity.ok(new TagsResponse(tags));
    }
}
