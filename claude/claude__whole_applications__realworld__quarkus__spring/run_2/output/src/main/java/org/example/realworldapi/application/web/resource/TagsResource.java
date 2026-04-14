package org.example.realworldapi.application.web.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.example.realworldapi.application.web.model.response.TagsResponse;
import org.example.realworldapi.domain.feature.FindTags;
import org.example.realworldapi.domain.model.tag.Tag;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tags")
public class TagsResource {

  private final ObjectMapper noWrapMapper;
  private final FindTags findTags;

  public TagsResource(
      @Qualifier("noWrapRootValueObjectMapper") ObjectMapper noWrapMapper,
      FindTags findTags) {
    this.noWrapMapper = noWrapMapper;
    this.findTags = findTags;
  }

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> getTags() throws JsonProcessingException {
    List<Tag> tags = findTags.handle();
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(noWrapMapper.writeValueAsString(new TagsResponse(tags)));
  }
}
