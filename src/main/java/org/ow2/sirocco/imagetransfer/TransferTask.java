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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.woorea.openstack.glance.Glance;
import com.woorea.openstack.glance.model.Image;
import com.woorea.openstack.glance.model.ImageDownload;
import com.woorea.openstack.glance.model.ImageUpload;
import com.woorea.openstack.keystone.model.Access;
import com.woorea.openstack.keystone.model.Access.Service;
import com.woorea.openstack.keystone.model.Access.Service.Endpoint;
import com.woorea.openstack.keystone.utils.KeystoneTokenProvider;

/**
 * implementation of a image transfer task
 */

public class TransferTask implements Runnable {

    private Task task;

    private CloudConfiguration sourceCloud;

    private CloudConfiguration targetCloud;

    private KeystoneTokenProvider keystone;

    private Access access;

    private Service glanceService;

    private Glance glance;

    private String imageId;

    private String newImageId;

    private ImageUpload uploadImage;

    private ImageDownload downloadImage;

    // NOTE: Current coding is supposed to handle OpenStack transfer case. It
    // has nothing to do with Cloud Location argument!
    public TransferTask(final Task t, final String imageId, final CloudConfiguration source,
        final CloudConfiguration destination) throws ImageTransferException {

        ImageTransferImpl.logger.info("TransferTask - constructor");
        this.task = t;
        this.imageId = imageId;
        this.sourceCloud = source;
        this.targetCloud = destination;
    }

    // whole transfer treatment contained in the thread
    @Override
    public void run() {

        ImageTransferImpl.logger.info("TransferTask - begining of run");

        this.task.setStatus(Task.State.RUNNING);

        Image img = null;
        Image newImage = null;
        List<Endpoint> endpoints;
        Endpoint endpoint;

        // source cloud
        try {
            this.cloudAccess(this.sourceCloud);

            // NOTE: hypothesis: only first retrieved Glance
            // end-point is considered
            // for (Endpoint endpoint : this.glanceService.getEndpoints()) {
            // FIXME concatenation of "/v1" is needed for some OpenStack
            // installation (see Grizzly FDT!) and not needed for some others
            // (see Grizzly FT) which gives an ERROR -> .../v1/v1
            endpoints = this.glanceService.getEndpoints();
            endpoint = endpoints.get(0);
            this.glance = new Glance(endpoint.getPublicURL());
            // this.glance = new Glance(endpoint.getPublicURL() + "/v1");
            this.glance.setTokenProvider(this.keystone.getProviderByTenant(this.sourceCloud.getTenantName()));
        } catch (Exception e) {
            ImageTransferImpl.logger.error("TransferTask - Error on accessing source cloud services: " + e);
            this.task.setStatus(Task.State.ERROR);
            this.task.setErrMsg("TransferTask - Error on accessing source cloud services" + " - Exception: " + e);
            return;
        }

        // for further retrieving properties of the downloaded image
        try {
            img = this.glance.images().show(this.imageId).execute();
        } catch (Exception e) {
            ImageTransferImpl.logger.error("TransferTask - Error on the image id: " + e);
            ImageTransferImpl.logger.error("TransferTask - Error on accessing source cloud services: " + e);
            this.task.setStatus(Task.State.ERROR);
            this.task.setErrMsg("TransferTask - No image has been found for the given imageId: " + this.imageId
                + " - Exception: " + e);
            return;
        }

        try {
            ImageTransferImpl.logger.info("TransferTask - starting to download the image of name:" + img.getName()
                + " and id: " + this.imageId);
            TransferTask.this.downloadImage = TransferTask.this.glance.images().download(TransferTask.this.imageId).execute();
        } catch (Exception e) {
            ImageTransferImpl.logger.error("TransferTask - Error in a thread during image downloading: " + e);
            this.task.setStatus(Task.State.ERROR);
            this.task.setErrMsg("TransferTask - - Error in a thread during image downloading" + " - Exception: " + e);
            return;
        }

        ImageTransferImpl.logger.info("TransferTask - Image downloading succeded");

        // backups input stream into a file because Apache connection needs
        // to be release by closing the input stream (mandatory in Apache lib)!
        File tempFile = null;
        try {
            tempFile = File.createTempFile("tempFile", ".tmp");
        } catch (IOException e) {
            ImageTransferImpl.logger.error("TransferTask - an exception occurred while trying to create temporary file: " + e);
            this.task.setStatus(Task.State.ERROR);
            this.task.setErrMsg("TransferTask - an exception occurred while creating temporary file" + " - Exception: " + e);
            return;
        }
        tempFile.deleteOnExit();
        try {
            FileOutputStream os = new FileOutputStream(tempFile);
            byte[] buf = new byte[1024];
            int len;
            while ((len = this.downloadImage.getInputStream().read(buf)) != -1) {
                os.write(buf, 0, len);
            }
            // flush OutputStream to write any buffered data to file
            os.flush();
            os.close();
        } catch (IOException e) {
            ImageTransferImpl.logger.error("TransferTask - an exception occurred while getting temporary file contents: " + e);
            this.task.setStatus(Task.State.ERROR);
            this.task.setErrMsg("TransferTask - an exception occurred while getting temporary file contents" + " - Exception: "
                + e);
            return;
        }

        // releases HTTP connection by closing the related inputstream
        try {
            this.downloadImage.getInputStream().close();
        } catch (IOException e) {
            ImageTransferImpl.logger
                .error("TransferTask - an exception occurred while closing connexion on source cloud: " + e);
            this.task.setStatus(Task.State.ERROR);
            this.task.setErrMsg("TransferTask - an exception occurred while closing connexion on source cloud"
                + " - Exception: " + e);
            return;
        }

        // uploading the image to the target OpenStack cloud (potentially
        // multiple Glance end-points)
        // target cloud
        ImageTransferImpl.logger.info("TransferTask - establishing connexion on target cloud");
        try {
            this.cloudAccess(this.targetCloud);
        } catch (Exception e) {
            ImageTransferImpl.logger.error("TransferTask - Error on accessing target cloud services : " + e);
            this.task.setStatus(Task.State.ERROR);
            this.task.setErrMsg("TransferTask - Error on accessing target cloud services " + " - Exception: " + e);
            return;
        }

        InputStream imgStream = null;
        try {
            imgStream = new FileInputStream(tempFile);
        } catch (FileNotFoundException e) {
            ImageTransferImpl.logger.info("TransferTask - an exception occurred while trying to access the temporary file: "
                + e);
            this.task.setStatus(Task.State.ERROR);
            this.task.setErrMsg("TransferTask - an exception occurred while trying to access the temporary file"
                + " - Exception: " + e);
            return;
        }

        try {
            endpoints = this.glanceService.getEndpoints();
            endpoint = endpoints.get(0);
            // FIXME concatenation of "/v1" is needed for some OpenStack
            // installation (see Grizzly FDT!) and not needed for some others
            // (see Grizzly FT) which gives an ERROR -> .../v1/v1
            this.glance = new Glance(endpoint.getPublicURL());
            // this.glance = new Glance(endpoint.getPublicURL() + "/v1");
            this.glance.setTokenProvider(this.keystone.getProviderByTenant(this.targetCloud.getTenantName()));
        } catch (Exception e) {
            ImageTransferImpl.logger.error("TransferTask - Error on accessing target cloud services: " + e);
            this.task.setStatus(Task.State.ERROR);
            this.task.setErrMsg("TransferTask - Error on accessing target cloud services" + " - Exception: " + e);
            return;
        }

        ImageTransferImpl.logger.info("TransferTask - creating a new image");
        newImage = new Image();
        newImage.setDiskFormat(img.getDiskFormat());
        newImage.setContainerFormat(img.getContainerFormat());
        newImage.setSize(img.getSize());
        newImage.setName(img.getName());
        // hypothesis: the uploaded image is not public
        newImage.setPublic(false);
        newImage = this.glance.images().create(newImage).execute();
        this.newImageId = newImage.getId();
        this.uploadImage = new ImageUpload(newImage);
        this.uploadImage.setInputStream(imgStream);

        try {
            ImageTransferImpl.logger.info("TransferTask - starting image uploading");
            TransferTask.this.glance.images().upload(TransferTask.this.newImageId, TransferTask.this.uploadImage).execute();
        } catch (Exception e) {
            ImageTransferImpl.logger.error("TransferTask - error in a thread during image uploading: " + e);
            ImageTransferImpl.logger.info("TransferTask - deleting the incompletely uploaded image");
            // to delete the incompletely uploaded image before terminating
            this.glance.images().delete(this.newImageId).execute();
            this.task.setStatus(Task.State.ERROR);
            this.task
                .setErrMsg("TransferTask - error in a thread during image uploading - the incompletely uploaded image has been deleted "
                    + " - Exception: " + e);
            return;
        }

        String status = this.glance.images().show(this.newImageId).execute().getStatus();
        ImageTransferImpl.logger.info("TransferTask - transfered image STATUS recovered from Glance is: " + status);
        if (status.equals("error") || status.equals("queued") || status.equals("killed")) {
            ImageTransferImpl.logger.error("TransferTask - error in status of the uploaded image: " + status);
            // to delete the uploaded image with wrong status before terminating
            this.glance.images().delete(this.newImageId).execute();
            this.glance.images().delete(this.newImageId).execute();
            this.task.setStatus(Task.State.ERROR);
            this.task.setErrMsg("TransferTask - transfer failed - there is an error in the uploaded image status: " + status);
            return;
        }
        ImageTransferImpl.logger.info("TransferTask - Image uploading succeded");
        this.task.setStatus(Task.State.SUCCESS);
    }

    void cloudAccess(final CloudConfiguration cloud) {

        this.keystone = new KeystoneTokenProvider(cloud.getEndpoint(), cloud.getUserName(), cloud.getPassword());
        this.access = this.keystone.getAccessByTenant(cloud.getTenantName());
        this.glanceService = null;
        for (Service service : this.access.getServiceCatalog()) {
            if (service.getType().equals("image")) {
                this.glanceService = service;
                break;
            }
        }
        if (this.glanceService == null) {
            ImageTransferImpl.logger.error("TransferTask - error while retrieving Glance service");
            throw new RuntimeException("Glance service not found");
        }
    }

}
