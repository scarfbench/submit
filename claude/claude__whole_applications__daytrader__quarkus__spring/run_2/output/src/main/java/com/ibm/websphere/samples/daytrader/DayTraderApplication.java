/**
 * (C) Copyright IBM Corporation 2015, 2024.
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
package com.ibm.websphere.samples.daytrader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * DayTrader Spring Boot Application
 *
 * This is the main entry point for the DayTrader application running on Spring Boot 3.x.
 * It replaces the Quarkus-based configuration.
 *
 * @EnableScheduling is required for scheduled tasks like MarketSummarySingleton updates
 */
@SpringBootApplication
@EnableScheduling
public class DayTraderApplication {

    public static void main(String[] args) {
        SpringApplication.run(DayTraderApplication.class, args);
    }
}
