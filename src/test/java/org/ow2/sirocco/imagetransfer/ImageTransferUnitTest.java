package org.ow2.sirocco.imagetransfer;

import java.net.URI;

import javax.ws.rs.core.MediaType;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

public class ImageTransferUnitTest {

    private URI BASE_URI = URI.create("http://localhost:8080/sirocco/imagetransfer");

    private WebResource service;

    @Before
    public void setUp() throws Exception {
        // start the server
        // this.server = ImageTransferServer.createServer();
        // create the client
        ClientConfig config = new DefaultClientConfig();
        Client client = Client.create(config);
        this.service = client.resource(this.BASE_URI);
    }

    @After
    public void tearDown() throws Exception {
        System.out.println("Passed through tearDown");
        // this.server.stop();
    }

    /**
     * Test to see that the message "Got it!" is sent in the response.
     */
    @Test
    public void testGetIt() {
        System.out.println("Passed through testGetIt");
        // test on imageId1 only
        CloudConfiguration sourceCloud = new CloudConfiguration();
        CloudConfiguration targetCloud = new CloudConfiguration();
        ImageTransferRequest imgtRequest = new ImageTransferRequest();

        sourceCloud.setEndpoint("http://10.193.112.81:5000/v2.0");
        sourceCloud.setTenantName("demo");
        sourceCloud.setUserName("demo");
        sourceCloud.setPassword("password");

        targetCloud.setEndpoint("http://ow2-04.xsalto.net:5000/v2.0");
        targetCloud.setTenantName("opencloudware");
        targetCloud.setUserName("sirocco");
        targetCloud.setPassword("cregDyk3");

        imgtRequest.setImageId("cabdf7d1-a7f2-44b2-b515-b950494aec8d");
        imgtRequest.setSource(sourceCloud);
        imgtRequest.setDestination(targetCloud);
        try {
            ClientResponse response1 = this.service.path("tasks").accept(MediaType.APPLICATION_XML_TYPE)
                .entity(imgtRequest, MediaType.APPLICATION_XML_TYPE).post(ClientResponse.class);
            Status responseStat;
            responseStat = response1.getClientResponseStatus();
            if (responseStat != Status.OK) {
                throw new RuntimeException("HTTP operation failed");
            }
            Task task1 = response1.getEntity(Task.class);
            Assert.assertEquals(Task.State.RUNNING, task1.getStatus());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // TODO unit test for the other operation (getTask)
    }
}
