package org.maneau.maventools.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.aether.transfer.AbstractTransferListener;
import org.sonatype.aether.transfer.TransferEvent;
import org.sonatype.aether.transfer.TransferResource;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by maneau on 05/07/2014.
 */
public class ConsoleTransferListener extends AbstractTransferListener {

    private static Logger LOGGER = LoggerFactory.getLogger(ConsoleTransferListener.class);

    private Map<TransferResource, Long> downloads = new ConcurrentHashMap<TransferResource, Long>();

    public ConsoleTransferListener() {
    }

    @Override
    public void transferInitiated(TransferEvent event) {
        String message = event.getRequestType() == TransferEvent.RequestType.PUT ? "Uploading" : "Downloading";

        LOGGER.trace(message + ": " + event.getResource().getRepositoryUrl() + event.getResource().getResourceName());
    }

    @Override
    public void transferProgressed(TransferEvent event) {
        // intentionally left blank, cause we don't want to have
        // progress lines in the log files.
    }

    @Override
    public void transferSucceeded(TransferEvent event) {
        transferCompleted(event);

        TransferResource resource = event.getResource();
        long contentLength = event.getTransferredBytes();
        if (contentLength >= 0) {
            String type = (event.getRequestType() == TransferEvent.RequestType.PUT ? "Uploaded" : "Downloaded");
            String len = contentLength >= 1024 ? toKB(contentLength) + " KB" : contentLength + " B";

            String throughput = "";
            long duration = System.currentTimeMillis() - resource.getTransferStartTime();
            if (duration > 0) {
                DecimalFormat format = new DecimalFormat("0.0", new DecimalFormatSymbols(Locale.ENGLISH));
                double kbPerSec = (contentLength / 1024.0) / (duration / 1000.0);
                throughput = " at " + format.format(kbPerSec) + " KB/sec";
            }

            LOGGER.debug(type + ": " + resource.getRepositoryUrl() + resource.getResourceName() + " (" + len + throughput + ")");
        }
    }

    @Override
    public void transferFailed(TransferEvent event) {
        transferCompleted(event);

        // intentionally only in trace mode, cause we get for every none
        // existing artifact messages on the console.
        LOGGER.trace("Missing artifacts:", event.getException());
    }

    private void transferCompleted(TransferEvent event) {
        downloads.remove(event.getResource());
    }

    public void transferCorrupted(TransferEvent event) {
        LOGGER.warn("TransferCorrupted:", event.getException());
    }

    protected long toKB(long bytes) {
        return (bytes + 1023) / 1024;
    }

}
