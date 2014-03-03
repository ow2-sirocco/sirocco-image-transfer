package org.ow2.sirocco.imagetransfer;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("imagetransfer")
public class ImageTransferResource {

    @POST
    @Path("tasks")
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.APPLICATION_XML)
    public Task transfer(final ImageTransferRequest request) throws ImageTransferException {
        return ImageTransferImpl.getInstance().transfer(request.getImageId(), request.getSource(), request.getDestination());
    }

    @GET
    @Path("tasks/{taskId}")
    @Produces(MediaType.APPLICATION_XML)
    public Task getTask(@PathParam("taskId") final String taskId) throws ImageTransferException {
        Task task = ImageTransferImpl.getInstance().getTask(taskId);
        if (task != null) {
            return task;
        } else {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }

}
