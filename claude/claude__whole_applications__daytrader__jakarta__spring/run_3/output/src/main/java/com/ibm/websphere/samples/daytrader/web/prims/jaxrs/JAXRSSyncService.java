/**
 * (C) Copyright IBM Corporation 2019.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ibm.websphere.samples.daytrader.web.prims.jaxrs;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/jaxrs/sync")
public class JAXRSSyncService {

  /**
   * note: this should be the basic code path for jaxrs process
   * @param input
   * @return
   */
  @GetMapping("/echoText")
  public String echoString(@RequestParam("input") String input) {
    return input;
  }

  /**
   *  note: this code path involves JSON marshaller & un-marshaller based on basic code path
   * @param jsonObject  JSON Object
   * @return  JSON Object
   */
  @PostMapping(value = "/echoJSON", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public TestJSONObject echoObject(@RequestBody TestJSONObject jsonObject) {
    return jsonObject;
  }

  @PostMapping(value = "/echoXML", produces = {MediaType.TEXT_XML_VALUE, MediaType.APPLICATION_XML_VALUE}, consumes = {MediaType.TEXT_XML_VALUE, MediaType.APPLICATION_XML_VALUE})
  public XMLObject echoXMLObject(@RequestBody XMLObject xmlObject) {
    return xmlObject;
  }
}

