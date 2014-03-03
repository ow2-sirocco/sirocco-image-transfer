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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Iterator;
import java.util.Properties;

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
public class ImageTransferClientTest {

    private static Logger logger;

    ImageTransferClientTest() throws Exception {
    }

    private static URI getBaseURI() {
        return URI.create("http://localhost:8080/sirocco/imagetransfer/");
    }

    void launchImageTransferTests() throws ImageTransferException, IOException {
        String imageId1 = null, imageId2 = null;
        CloudConfiguration sourceCloud = null;
        CloudConfiguration targetCloud = null;
        ImageTransferRequest imgtRequest1 = new ImageTransferRequest();
        ImageTransferRequest imgtRequest2 = new ImageTransferRequest();
        Task taskT1, taskT2, task1, task2;

        ImageTransferClientTest.logger = Logger.getLogger(ImageTransferClientTest.class);
        ImageTransferClientTest.logger.info("-- launchImageTransferTests");

        Properties prop = new Properties();
        InputStream in = this.getClass().getResourceAsStream("/images.properties");
        prop.load(in);
        in.close();
        Iterator<Object> it = prop.keySet().iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            String idx = key.substring(7);
            if (Integer.parseInt(idx) == 1) {
                imageId1 = prop.getProperty("imageId" + idx);
            } else {
                imageId2 = prop.getProperty("imageId" + idx);
            }
        }

        ImageTransferClientTest.logger.info("-- launchImageTransferTests - ImageId1= " + imageId1 + " ImageId2=" + imageId2);

        prop = new Properties();
        in = this.getClass().getResourceAsStream("/clouds.properties");
        prop.load(in);
        in.close();
        it = prop.keySet().iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            if (key.startsWith("cloudUri")) {
                String idx = key.substring(8);
                CloudConfiguration cloudConfig = new CloudConfiguration();
                cloudConfig.setEndpoint(prop.getProperty("cloudUri" + idx));
                cloudConfig.setTenantName(prop.getProperty("cloudTenantname" + idx));
                cloudConfig.setUserName(prop.getProperty("cloudUsername" + idx));
                cloudConfig.setPassword(prop.getProperty("cloudUserpwd" + idx));
                // OPENSTACK CASE (and no location say, an empty one)
                cloudConfig.setLocation(null);
                if (Integer.parseInt(idx) == 1) {
                    sourceCloud = cloudConfig;
                } else {
                    targetCloud = cloudConfig;
                }
            }
        }

        // test on imageId1 and imageId2
        imgtRequest1.setImageId(imageId1);
        imgtRequest1.setSource(sourceCloud);
        imgtRequest1.setDestination(targetCloud);

        imgtRequest2.setImageId(imageId2);
        imgtRequest2.setSource(sourceCloud);
        imgtRequest2.setDestination(targetCloud);

        ClientConfig config = new DefaultClientConfig();
        Client client = Client.create(config);

        WebResource service = client.resource(ImageTransferClientTest.getBaseURI());
        try {
            // transfer of imageId1
            ClientResponse response1 = service.path("tasks").accept(MediaType.APPLICATION_XML_TYPE)
                .entity(imgtRequest1, MediaType.APPLICATION_XML_TYPE).post(ClientResponse.class);
            Status responseStat;
            responseStat = response1.getClientResponseStatus();
            if (responseStat != Status.OK) {
                throw new RuntimeException("HTTP operation failed");
            }
            // TODO see RestClient in siroccoCdeLineTools
            // this.handleResponseStatus(response);
            taskT1 = response1.getEntity(Task.class);

            // transfer of imageId2
            ClientResponse response2 = service.path("tasks").accept(MediaType.APPLICATION_XML_TYPE)
                .entity(imgtRequest2, MediaType.APPLICATION_XML_TYPE).post(ClientResponse.class);
            responseStat = response2.getClientResponseStatus();
            if (responseStat != Status.OK) {
                throw new RuntimeException("HTTP operation failed");
            }
            // TODO see RestClient in siroccoCdeLineTools
            // this.handleResponseStatus(response);
            taskT2 = response2.getEntity(Task.class);

            while (true) {
                response1 = service.path("tasks").path(taskT1.getId()).accept(MediaType.APPLICATION_XML_TYPE)
                    .get(ClientResponse.class);
                responseStat = response1.getClientResponseStatus();
                if (responseStat != Status.OK) {
                    throw new RuntimeException("HTTP operation failed");
                }
                // TODO see RestClient in siroccoCdeLineTools
                // this.handleResponseStatus(response);
                task1 = response1.getEntity(Task.class);

                response2 = service.path("tasks").path(taskT2.getId()).accept(MediaType.APPLICATION_XML_TYPE)
                    .get(ClientResponse.class);
                responseStat = response1.getClientResponseStatus();
                if (responseStat != Status.OK) {
                    throw new RuntimeException("HTTP operation failed");
                }
                // TODO see RestClient in siroccoCdeLineTools
                // this.handleResponseStatus(response);
                task2 = response2.getEntity(Task.class);
                if ((task1.getStatus() != Task.State.RUNNING) && (task2.getStatus() != Task.State.RUNNING)) {
                    break;
                }
                try {
                    ImageTransferClientTest.logger
                        .info("-- launchImageTransferTests - Waiting for uploading threads to terminate ...");
                    ImageTransferClientTest.logger.info("-- launchImageTransferTests - TASK-1 STATE: " + task1.getStatus());
                    ImageTransferClientTest.logger.info("-- launchImageTransferTests - TASK-2 STATE: " + task2.getStatus());
                    Thread.sleep(20000);
                } catch (InterruptedException ex) {
                    ImageTransferClientTest.logger
                        .error("-- launchImageTransferTests - Error in a thread while trying to transfer the image");
                    throw new RuntimeException("-- launchImageTransferTests - " + ex);
                }
            }
            if (task1.getStatus() == Task.State.SUCCESS) {
                ImageTransferClientTest.logger.info("-- launchImageTransferTests - image transfer of image: " + imageId1
                    + " succeded ...");
            } else {
                ImageTransferClientTest.logger.info("-- launchImageTransferTests - image transfer of image: " + imageId1
                    + " failed ...");
            }

            if (task2.getStatus() == Task.State.SUCCESS) {
                ImageTransferClientTest.logger.info("-- launchImageTransferTests - image transfer of image: " + imageId2
                    + " succeded ...");
            } else {
                ImageTransferClientTest.logger.info("-- launchImageTransferTests - image transfer of image: " + imageId2
                    + " failed ...");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(final String[] args) throws Exception {

        System.out.println("-- ImageTransferClientTest - Beginning of Test Main");
        ImageTransferClientTest iftest = new ImageTransferClientTest();
        iftest.launchImageTransferTests();
        ImageTransferClientTest.logger.info("-- ImageTransferClientTest - End of Test Main");
    }
}
