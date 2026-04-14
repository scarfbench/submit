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
package com.ibm.websphere.samples.daytrader.web.jsf;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ibm.websphere.samples.daytrader.util.Log;

// Simple POJO validator (no longer JSF-specific)
public class LoginValidator {

  static String loginRegex = "uid:\\d+";
  static Pattern pattern = Pattern.compile(loginRegex);
  static Matcher matcher;

  // Simple validator to make sure username starts with uid: and at least 1 number.
  public LoginValidator() {
  }

  public void validate(Object value) throws Exception {
    Log.trace("LoginValidator.validate","Validating submitted login name -- " + value.toString());

    matcher = pattern.matcher(value.toString());

    if (!matcher.matches()) {
      throw new Exception("Username validation failed. Please provide username in this format: uid:#");
    }
  }
}