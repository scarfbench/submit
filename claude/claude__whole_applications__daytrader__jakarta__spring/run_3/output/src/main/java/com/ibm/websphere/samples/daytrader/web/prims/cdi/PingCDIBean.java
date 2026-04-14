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
package com.ibm.websphere.samples.daytrader.web.prims.cdi;

import java.util.Set;

import org.springframework.web.context.annotation.RequestScope;
import org.springframework.stereotype.Component;
import org.springframework.context.ApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;

@Component
@RequestScope
@PingInterceptorBinding
public class PingCDIBean {

  @Autowired
  private ApplicationContext applicationContext;

  private static int helloHitCount = 0;
  private static int getBeanManagerHitCountJNDI = 0;
  private static int getBeanManagerHitCountSPI = 0;


  public int hello() {
    return ++helloHitCount;
  }

  public int getBeanMangerViaJNDI() throws Exception {
    // Spring equivalent: use ApplicationContext instead of BeanManager
    String[] beanNames = applicationContext.getBeanDefinitionNames();
    if (beanNames.length > 0) {
      return ++getBeanManagerHitCountJNDI;
    }
    return 0;

  }

  public int getBeanMangerViaCDICurrent() throws Exception {
    // Spring equivalent: use ApplicationContext instead of BeanManager
    String[] beanNames = applicationContext.getBeanDefinitionNames();

    if (beanNames.length > 0) {
      return ++getBeanManagerHitCountSPI;
    }
    return 0;

  }
}
