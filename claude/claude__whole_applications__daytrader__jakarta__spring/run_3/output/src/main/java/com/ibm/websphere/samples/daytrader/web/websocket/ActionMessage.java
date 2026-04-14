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

import com.ibm.websphere.samples.daytrader.util.Log;

/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
public class ActionMessage {

  String decodedAction = null;

  public ActionMessage() {  
  }

  public void doDecoding(String jsonText) {

    try
    {
      // Simple JSON parsing - extract action value
      // Expected format: {"action":"value"}
      if (jsonText != null && jsonText.contains("\"action\"")) {
        int actionStart = jsonText.indexOf("\"action\"");
        int colonIndex = jsonText.indexOf(":", actionStart);
        int valueStart = jsonText.indexOf("\"", colonIndex) + 1;
        int valueEnd = jsonText.indexOf("\"", valueStart);
        if (valueStart > 0 && valueEnd > valueStart) {
          decodedAction = jsonText.substring(valueStart, valueEnd);
        }
      }
    } catch (Exception e) {
      Log.error("ActionMessage:doDecoding(" + jsonText + ") --> failed", e);
    }


    Log.trace("ActionMessage:doDecoding -- decoded action -->" + decodedAction + "<--");

  }


public String getDecodedAction() {
  return decodedAction;
}

}

