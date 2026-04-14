/**
 * (C) Copyright IBM Corporation 2015.
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
package com.ibm.websphere.samples.daytrader.web.websocket;

import jakarta.websocket.DecodeException;
import jakarta.websocket.Decoder;
import jakarta.websocket.EndpointConfig;

public class JsonDecoder implements Decoder.Text<JsonMessage> {

    @Override
    public void destroy() {
    }

    @Override
    public void init(EndpointConfig ec) {
    }

    @Override
    public JsonMessage decode(String json) throws DecodeException {
        JsonMessage message = new JsonMessage();

        try {
            // Simple JSON parsing - extract key and value
            // Expected format: {"key":"value1","value":"value2"}
            String key = extractJsonValue(json, "key");
            String value = extractJsonValue(json, "value");

            message.setKey(key);
            message.setValue(value);
        } catch (Exception e) {
            throw new DecodeException(json, "Failed to decode JSON", e);
        }

        return message;
    }

    @Override
    public boolean willDecode(String json) {
        try {
            return json != null && json.contains("\"key\"") && json.contains("\"value\"");
        } catch (Exception e) {
            return false;
        }
    }

    private String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\"";
        int keyIndex = json.indexOf(searchKey);
        if (keyIndex < 0) return "";

        int colonIndex = json.indexOf(":", keyIndex);
        int valueStart = json.indexOf("\"", colonIndex) + 1;
        int valueEnd = json.indexOf("\"", valueStart);

        if (valueStart > 0 && valueEnd > valueStart) {
            return json.substring(valueStart, valueEnd);
        }
        return "";
    }

}
