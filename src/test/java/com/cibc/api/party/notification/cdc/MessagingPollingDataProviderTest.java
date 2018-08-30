package com.cibc.api.party.notification.cdc;

import com.networknt.config.Config;
import com.networknt.eventuate.jdbc.EventuateSchema;
import com.networknt.eventuate.server.common.CdcConfig;
import com.networknt.eventuate.server.common.PublishingStrategy;
import com.networknt.service.SingletonServiceFactory;
import com.networknt.tram.cdc.mysql.connector.MessageWithDestination;
import com.networknt.tram.cdc.polling.connector.MessagePollingDataProvider;
import com.networknt.tram.cdc.polling.connector.PollingDao;

import org.junit.Ignore;
import org.junit.Test;

import javax.sql.DataSource;

import java.util.List;

import static org.junit.Assert.assertTrue;

public class MessagingPollingDataProviderTest {

    static String CDC_CONFIG_NAME = "cdc";
    static CdcConfig cdcConfig = (CdcConfig) Config.getInstance().getJsonObjectConfig(CDC_CONFIG_NAME, CdcConfig.class);
    static String PULLING_CONFIG_NAME = "pulling";
    static PullingConfig pullingConfig = (PullingConfig) Config.getInstance().getJsonObjectConfig(PULLING_CONFIG_NAME, PullingConfig.class);
    static DataSource ds = (DataSource) SingletonServiceFactory.getBean(DataSource.class);

    @Test
    public void testSchema(){
        EventuateSchema schema = (EventuateSchema) SingletonServiceFactory.getBean(EventuateSchema.class);
        MessagePollingDataProvider pollingDataProvider= (MessagePollingDataProvider) SingletonServiceFactory.getBean(MessagePollingDataProvider.class);
        System.out.println(pollingDataProvider.table());
    }

    @Test
    @Ignore
    public void test2(){
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
        List eventsToPublish = pollingDao.findEventsToPublish();



        if (!eventsToPublish.isEmpty()) {

            pollingDao.markEventsAsPublished(eventsToPublish);
        }
    }

    @Test
    public void test3(){
    /*    MessagePollingDataProvider pollingDataProvider= (MessagePollingDataProvider) SingletonServiceFactory.getBean(MessagePollingDataProvider.class);
        if (pullingConfig != null) pollingDataProvider.reset(pullingConfig.getTableName(), pullingConfig.getIdField(), pullingConfig.getPublishedField(),
                pullingConfig.getHeaders(), pullingConfig.getDestination(), pullingConfig.getPayload());
        PollingDao pollingDao =  new PollingDao(pollingDataProvider, ds,
                cdcConfig.getMaxEventsPerPolling(),
                cdcConfig.getMaxAttemptsForPolling(),
                cdcConfig.getPollingRetryIntervalInMilliseconds());

        List<MessageWithDestination>  list = pollingDao.findEventsToPublish();
        System.out.println("list size:" + list);*/
    }
}
