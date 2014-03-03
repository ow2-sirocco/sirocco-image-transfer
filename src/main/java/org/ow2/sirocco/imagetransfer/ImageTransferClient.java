/**
 *
 * SIROCCO
 * Copyright (C) 2013 France Telecom
 * Contact: sirocco@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 */

package org.ow2.sirocco.imagetransfer;

import java.net.URI;

import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

/**
 * Test for ImageTransfer
 */
public class ImageTransferClient {

    private static Logger logger;

    private URI BASE_URI = null;

    WebResource service;

    ImageTransferClient(final String serviceURI) throws Exception {
        this.BASE_URI = URI.create(serviceURI);
        System.out.println("ImageTransfer service BASE_URI= " + this.BASE_URI);
        this.init();
    }

    private void init() throws ImageTransferException {
        ImageTransferClient.logger = Logger.getLogger(ImageTransferClient.class);
        ImageTransferClient.logger.info("ImageTransferClient - init");
        ClientConfig config = new DefaultClientConfig();
        Client client = Client.create(config);
        this.service = client.resource(this.BASE_URI);
    }

    public Task transfer(final ImageTransferRequest request) throws ImageTransferException {
        Task task = null;

        ImageTransferClient.logger.info("ImageTransferClient - transfer");
        try {
            ClientResponse response = this.service.path("tasks").accept(MediaType.APPLICATION_XML_TYPE)
                .entity(request, MediaType.APPLICATION_XML_TYPE).post(ClientResponse.class);
            Status responseStat;
            responseStat = response.getClientResponseStatus();
            if (responseStat != Status.OK) {
                throw new RuntimeException("HTTP operation failed");
            }
            // TODO see RestClient in siroccoCdeLineTools
            // this.handleResponseStatus(response);
            task = response.getEntity(Task.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return task;
    }

    public Task getTask(final String taskId) throws ImageTransferException {
        Task task = null;
        ImageTransferClient.logger.info("ImageTransferClient - getTask");

        try {
            ClientResponse response = this.service.path("tasks").path(taskId).accept(MediaType.APPLICATION_XML_TYPE)
                .get(ClientResponse.class);
            Status responseStat;
            responseStat = response.getClientResponseStatus();
            if (responseStat != Status.OK) {
                throw new RuntimeException("HTTP operation failed");
            }
            // TODO see RestClient in siroccoCdeLineTools
            // this.handleResponseStatus(response);
            task = response.getEntity(Task.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return task;
    }

}
