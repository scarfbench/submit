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
package com.ibm.websphere.samples.daytrader.jaxrs;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import com.ibm.websphere.samples.daytrader.messaging.MessageProducerService;
import com.ibm.websphere.samples.daytrader.util.Log;

@Path("/messaging")
@Produces(MediaType.APPLICATION_JSON)
public class MessagingResource {

    @Inject
    MessageProducerService messageProducer;

    @GET
    @Path("/ping/broker")
    public Response pingBroker() {
        try {
            messageProducer.sendBrokerPing("Ping from MessagingResource at " + new java.util.Date());
            return Response.ok("{\"status\":\"broker ping sent\"}").build();
        } catch (Exception e) {
            Log.error("MessagingResource:pingBroker", e);
            return Response.serverError().entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }
    }

    @GET
    @Path("/ping/streamer")
    public Response pingStreamer() {
        try {
            messageProducer.sendStreamerPing("Ping from MessagingResource at " + new java.util.Date());
            return Response.ok("{\"status\":\"streamer ping sent\"}").build();
        } catch (Exception e) {
            Log.error("MessagingResource:pingStreamer", e);
            return Response.serverError().entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }
    }
}
