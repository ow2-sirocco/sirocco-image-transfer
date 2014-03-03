package org.ow2.sirocco.imagetransfer;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement
@XmlType(propOrder = {"imageId", "source", "destination"})
public class ImageTransferRequest {

    private String imageId;

    private CloudConfiguration source;

    private CloudConfiguration destination;

    public String getImageId() {
        return this.imageId;
    }

    public void setImageId(final String imageId) {
        this.imageId = imageId;
    }

    public CloudConfiguration getSource() {
        return this.source;
    }

    public void setSource(final CloudConfiguration source) {
        this.source = source;
    }

    public CloudConfiguration getDestination() {
        return this.destination;
    }

    public void setDestination(final CloudConfiguration destination) {
        this.destination = destination;
    }

}
