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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

/**
 * ImageTransfer implementation
 */

public class ImageTransferImpl implements ImageTransferService {

    private static ImageTransferImpl instance;

    protected static Logger logger;

    // Pool of threads
    ExecutorService pool;

    // tasks pool
    private Map<String, Task> tasks = new HashMap<String, Task>();

    public static ImageTransferService getInstance() {
        if (ImageTransferImpl.instance == null) {
            ImageTransferImpl.instance = new ImageTransferImpl();
            ImageTransferImpl.instance.init();
        }
        return ImageTransferImpl.instance;
    }

    void init() {
        ImageTransferImpl.logger = Logger.getLogger(ImageTransferImpl.class);
        ImageTransferImpl.logger.info("ImageTransferImpl - init");
        // creates and initializes a pool of thread, a map of Task
        // tasks pool max 10 simultaneous transfers
        // this.tasks = new HashSet<Task>(10);
        // tasks pool max 10 simultaneous transfers
        this.pool = Executors.newFixedThreadPool(10);
    }

    /**
     * TODO
     * 
     * @param
     * @return
     * @throws
     */
    public Task transfer(final String imageId, final CloudConfiguration source, final CloudConfiguration destination)
        throws ImageTransferException {

        ImageTransferImpl.logger.info("ImageTransferImpl - transfer");

        // creates a Task t to pass as an argument to the TransferTask
        Task task = new Task();
        task.setId(UUID.randomUUID().toString());
        // TaskImpl task = new TaskImpl(UUID.randomUUID().toString());
        this.tasks.put(task.getId(), task);

        // creates a (Runnable) TransferTask object whom constructor
        // takes the Task t as an argument
        TransferTask transftask = new TransferTask(task, imageId, source, destination);

        // starts the TransferTask thread
        ImageTransferImpl.logger.info("ImageTransferImpl - STARTING TransferTask thread");
        this.pool.submit(transftask);

        // to return immediately the Task t
        return (task);
    }

    /**
     * retrieves the image transfer Task corresponding to the taskid (several
     * simultaneous transfers are possible)
     * 
     * @param
     * @return
     * @throws
     */
    public Task getTask(final String taskId) throws ImageTransferException {
        return this.tasks.get(taskId);
    }

}
