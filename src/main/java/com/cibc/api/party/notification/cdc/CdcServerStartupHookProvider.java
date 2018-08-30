package com.cibc.api.party.notification.cdc;

import com.networknt.config.Config;

import com.networknt.eventuate.server.common.*;
import com.networknt.eventuate.server.common.exception.EventuateLocalPublishingException;
import com.networknt.server.StartupHookProvider;
import com.networknt.service.SingletonServiceFactory;
import com.networknt.tram.cdc.mysql.connector.MessageWithDestination;
import com.networknt.tram.cdc.polling.connector.MessagePollingDataProvider;
//import com.networknt.tram.cdc.polling.connector.PollingCdcProcessor;
import com.networknt.tram.cdc.polling.connector.PollingDao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

/**
 * CdcServer StartupHookProvider. start cdc service
 */
public class CdcServerStartupHookProvider implements StartupHookProvider {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    static String CDC_CONFIG_NAME = "cdc";
    static CdcConfig cdcConfig = (CdcConfig) Config.getInstance().getJsonObjectConfig(CDC_CONFIG_NAME, CdcConfig.class);
    static String PULLING_CONFIG_NAME = "pulling";
    static PullingConfig pullingConfig = (PullingConfig) Config.getInstance().getJsonObjectConfig(PULLING_CONFIG_NAME, PullingConfig.class);

    public static  CdcJmsPublisher<MessageWithDestination> messageCdcJmsPublisher;

    @Override
    @SuppressWarnings("unchecked")
    public void onStartup() {


        MessagePollingDataProvider pollingDataProvider= (MessagePollingDataProvider) SingletonServiceFactory.getBean(MessagePollingDataProvider.class);
        if (pullingConfig != null) pollingDataProvider.reset(pullingConfig.getTableName(), pullingConfig.getIdField(), pullingConfig.getPublishedField(),
                                         pullingConfig.getHeaders(), pullingConfig.getDestination(), pullingConfig.getPayload());

        PublishingStrategy<MessageWithDestination> publishingStrategy = SingletonServiceFactory.getBean(PublishingStrategy.class);

        CdcJmsPublisher<MessageWithDestination>  messageCdcJmsPublisher = new PollingCdcJmsPublisher<>(publishingStrategy, pullingConfig.getJmsEnv(), pullingConfig.getJmsRelease());
        DataSource ds = (DataSource) SingletonServiceFactory.getBean(DataSource.class);

        PollingDao pollingDao =  new PollingDao(pollingDataProvider, ds,
                cdcConfig.getMaxEventsPerPolling(),
                cdcConfig.getMaxAttemptsForPolling(),
                cdcConfig.getPollingRetryIntervalInMilliseconds());


        CdcProcessor<MessageWithDestination> pollingCdcProcessor = new PollingCdcProcessor<>(pollingDao, cdcConfig.getPollingIntervalInMilliseconds());

        messageCdcJmsPublisher.start();
        try {
            pollingCdcProcessor.start(publishedEvent -> {
                try {
                    messageCdcJmsPublisher.handleEvent(publishedEvent);
                } catch (EventuateLocalPublishingException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            if (e.getCause() instanceof EventuateLocalPublishingException) {
                logger.error("Stopping capturing changes due to exception:", e);
                messageCdcJmsPublisher.stop();
            }
        }


        System.out.println("CdcServerStartupHookProvider is called");
    }


}
