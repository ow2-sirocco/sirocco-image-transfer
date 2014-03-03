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
import java.util.Iterator;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * Test for ImageTransfer
 */
public class SiroccoITClientTest {

    private static Logger logger;

    SiroccoITClientTest() throws Exception {
    }

    void launchITClientTests() throws Exception, IOException {

        ImageTransferClient itClient = new ImageTransferClient("http://localhost:8080/sirocco/imagetransfer/");

        String imageId1 = null, imageId2 = null;
        CloudConfiguration sourceCloud = null;
        CloudConfiguration targetCloud = null;
        ImageTransferRequest imgtRequest1 = new ImageTransferRequest();
        ImageTransferRequest imgtRequest2 = new ImageTransferRequest();
        Task taskT1, taskT2, task1, task2;

        SiroccoITClientTest.logger = Logger.getLogger(SiroccoITClientTest.class);
        SiroccoITClientTest.logger.info("-- launchITClientTests");

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

        SiroccoITClientTest.logger.info("-- launchITClientTests - ImageId1= " + imageId1 + " ImageId2=" + imageId2);

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

        try {
            // test on imageId1 and imageId2
            imgtRequest1.setImageId(imageId1);
            imgtRequest1.setSource(sourceCloud);
            imgtRequest1.setDestination(targetCloud);

            imgtRequest2.setImageId(imageId2);
            imgtRequest2.setSource(sourceCloud);
            imgtRequest2.setDestination(targetCloud);

            taskT1 = itClient.transfer(imgtRequest1);
            taskT2 = itClient.transfer(imgtRequest2);

            while (true) {
                task1 = itClient.getTask(taskT1.getId());
                task2 = itClient.getTask(taskT2.getId());
                if ((task1.getStatus() != Task.State.RUNNING) && (task2.getStatus() != Task.State.RUNNING)) {
                    break;
                }
                try {
                    SiroccoITClientTest.logger.info("-- launchITClientTests - Waiting for uploading threads to terminate ...");
                    SiroccoITClientTest.logger.info("-- launchITClientTests - TASK-1 STATE: " + task1.getStatus());
                    SiroccoITClientTest.logger.info("-- launchITClientTests - TASK-2 STATE: " + task2.getStatus());
                    Thread.sleep(20000);
                } catch (InterruptedException ex) {
                    SiroccoITClientTest.logger
                        .error("-- launchITClientTests - Error in a thread while trying to transfer the image");
                    throw new RuntimeException("-- launchITClientTests - " + ex);
                }
            }
            if (task1.getStatus() == Task.State.SUCCESS) {
                SiroccoITClientTest.logger.info("-- launchITClientTests - image transfer of image: " + imageId1
                    + " succeded ...");
            } else {
                SiroccoITClientTest.logger
                    .info("-- launchITClientTests - image transfer of image: " + imageId1 + " failed ...");
            }
            if (task2.getStatus() == Task.State.SUCCESS) {
                SiroccoITClientTest.logger.info("-- launchITClientTests - image transfer of image: " + imageId2
                    + " succeded ...");
            } else {
                SiroccoITClientTest.logger
                    .info("-- launchITClientTests - image transfer of image: " + imageId2 + " failed ...");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(final String[] args) throws Exception {

        System.out.println("-- SiroccoITClientTest - Beginning of Test Main");
        SiroccoITClientTest iftest = new SiroccoITClientTest();
        iftest.launchITClientTests();
        SiroccoITClientTest.logger.info("-- SiroccoITClientTest - End of Test Main");
    }
}
