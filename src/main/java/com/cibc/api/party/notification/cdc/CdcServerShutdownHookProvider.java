package com.cibc.api.party.notification.cdc;

import com.networknt.server.ShutdownHookProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * cDc service ShutdownHookProvider, stop cDc service
 */
public class CdcServerShutdownHookProvider implements ShutdownHookProvider {
    protected static Logger logger = LoggerFactory.getLogger(CdcServerShutdownHookProvider.class);

    @Override
    public void onShutdown() {
        if(CdcServerStartupHookProvider.messageCdcJmsPublisher != null) {
            CdcServerStartupHookProvider.messageCdcJmsPublisher.stop();
        }
        System.out.println("CdcServerShutdownHookProvider is called");
    }
}
