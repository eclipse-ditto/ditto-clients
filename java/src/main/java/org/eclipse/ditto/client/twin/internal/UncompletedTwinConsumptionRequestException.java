package org.eclipse.ditto.client.twin.internal;

public class UncompletedTwinConsumptionRequestException extends RuntimeException {

    private static final long serialVersionUID = -565137801315595348L;
    private static final String MESSAGE = "First consumption request on this channel must be completed first";

    /**
     * Constructs a new {@code UncompletedTwinConsumptionRequestException} object.
     */
    public UncompletedTwinConsumptionRequestException() {
        super(MESSAGE, null);
    }

}
