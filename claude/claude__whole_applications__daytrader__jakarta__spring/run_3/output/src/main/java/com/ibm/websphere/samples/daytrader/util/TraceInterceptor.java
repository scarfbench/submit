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
package com.ibm.websphere.samples.daytrader.util;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Arrays;

import org.springframework.stereotype.Component;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import com.ibm.websphere.samples.daytrader.interfaces.Trace;


@Aspect
@Component
public class TraceInterceptor implements Serializable {

  private static final long serialVersionUID = -4195975993998268072L;
  private static final MessageFormat form = new MessageFormat("Method enter -- {0} called with {1}");

  @Around("@annotation(trace)")
  public Object logMethodEntry(ProceedingJoinPoint joinPoint, Trace trace) throws Throwable {
    Log.trace(form.format(
        new String[]{
            joinPoint.getSignature().getDeclaringType().getSimpleName() + ":"+ joinPoint.getSignature().getName(),
            Arrays.deepToString(joinPoint.getArgs())
    }));

    return joinPoint.proceed();
  }
}
