package jakarta.tutorial.concurrency.jobs.web;

import jakarta.inject.Inject;
import jakarta.tutorial.concurrency.jobs.exec.High;
import jakarta.tutorial.concurrency.jobs.exec.Low;
import jakarta.tutorial.concurrency.jobs.store.TokenStore;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

@Path("/webapi/JobService")
@Produces(MediaType.TEXT_PLAIN)
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
public class JobsController {

    private static final Logger log = LoggerFactory.getLogger(JobsController.class);
    public static final String API_TOKEN_HEADER = "X-REST-API-Key";

    @Inject
    @High
    ThreadPoolExecutor highExecutor;

    @Inject
    @Low
    ThreadPoolExecutor lowExecutor;

    @Inject
    TokenStore tokenStore;

    @GET
    @Path("/token")
    public Response getToken() {
        final String token = "123X5-" + UUID.randomUUID();
        tokenStore.put(token);
        return Response.ok(token).build();
    }

    @POST
    @Path("/process")
    public Response process(@HeaderParam(API_TOKEN_HEADER) String token,
                           @QueryParam("jobID") int jobID) {
        try {
            if (token != null && tokenStore.isValid(token)) {
                log.info("Token accepted. Execution with high priority.");
                highExecutor.execute(new JobTask("HIGH-" + jobID));
            } else {
                log.info("Invalid or missing token! {}", token);
                lowExecutor.execute(new JobTask("LOW-" + jobID));
            }
        } catch (RejectedExecutionException ree) {
            return Response.status(503)
                    .entity("Job " + jobID + " NOT submitted. " + ree.getMessage())
                    .build();
        }
        return Response.ok("Job " + jobID + " successfully submitted.").build();
    }

    static class JobTask implements Runnable {
        private static final Logger LOG = LoggerFactory.getLogger(JobTask.class);
        private final String jobID;
        private static final int JOB_EXECUTION_TIME_MS = 10_000;

        JobTask(String id) { this.jobID = id; }

        @Override public void run() {
            try {
                LOG.info("Task started {}", jobID);
                Thread.sleep(JOB_EXECUTION_TIME_MS);
                LOG.info("Task finished {}", jobID);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                LOG.error("Task interrupted {}", jobID, ex);
            }
        }
    }
}