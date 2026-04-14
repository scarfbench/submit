package org.example.realworldapi.application.web.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.AllArgsConstructor;
import org.example.realworldapi.application.web.model.response.TagsResponse;
import org.example.realworldapi.domain.feature.FindTags;
import org.example.realworldapi.domain.model.tag.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tags")
@AllArgsConstructor
public class TagsResource {

  private final ObjectMapper noWrapRootValueObjectMapper;
  private final FindTags findTags;

  @GetMapping
  public ResponseEntity<String> getTags() throws JsonProcessingException {
    List<Tag> tags = findTags.handle();
    return ResponseEntity.ok(
        noWrapRootValueObjectMapper.writeValueAsString(new TagsResponse(tags)));
  }
}
