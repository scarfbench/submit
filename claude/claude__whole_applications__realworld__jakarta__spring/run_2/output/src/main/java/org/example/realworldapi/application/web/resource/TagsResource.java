package org.example.realworldapi.application.web.resource;

import org.example.realworldapi.application.web.model.response.TagsResponse;
import org.example.realworldapi.domain.feature.FindTags;
import org.example.realworldapi.domain.model.tag.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class TagsResource {

    @Autowired
    private FindTags findTags;

    @GetMapping(produces = "application/json")
    public ResponseEntity<?> getTags() {
        List<Tag> tags = findTags.handle();
        return ResponseEntity.ok(new TagsResponse(tags));
    }
}
