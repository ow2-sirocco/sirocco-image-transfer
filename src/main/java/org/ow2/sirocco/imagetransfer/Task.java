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

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Task class
 */
@XmlRootElement
public class Task {

    /**
     * Task possible states
     */
    protected enum State {
        RUNNING, SUCCESS, ERROR
    }

    private String id;

    private State status;

    private String errMsg = "No error - image transfer succeded";

    public String getId() {
        return this.id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public State getStatus() {
        return this.status;
    }

    public void setStatus(final State status) {
        this.status = status;
    }

    public String getErrMsg() {
        return this.errMsg;
    }

    public void setErrMsg(final String errMsg) {
        this.errMsg = errMsg;
    }

}
