package jakarta.tutorial.concurrency.jobs.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
public class JobClient {
    private static final Logger logger = Logger.getLogger(JobClient.class.getName());

    @Autowired
    private JobServiceClient jobService;

    private String token;
    private int jobID;

    public boolean submit(int jobID, String token) {
        ResponseEntity<String> response = jobService.processJob(jobID, token);
        boolean ok = response.getStatusCode().is2xxSuccessful();
        logger.info(ok
                ? String.format("Job %d successfully submitted", jobID)
                : String.format("Job %d was NOT submitted", jobID));
        return ok;
    }

    private void clear() {
        this.token = "";
    }

    /**
     * @return the token
     */
    public String getToken() {
        return token;
    }

    /**
     * @param token the token to set
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * @return the jobID
     */
    public int getJobID() {
        return jobID;
    }

    /**
     * @param jobID the jobID to set
     */
    public void setJobID(int jobID) {
        this.jobID = jobID;
    }
}
