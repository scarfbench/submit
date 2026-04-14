package org.springframework.samples.petclinic.system;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/oups")
public class CrashResource {

	@Inject
	Template error;

	@GET
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance triggerException() {
		return error.data("activeMenu", "error")
			.data("message", "Expected: controller used to showcase what happens when an exception is thrown");
	}

}
