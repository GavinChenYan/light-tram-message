package com.cibc.api.party.notification.cdc;


import com.networknt.eventuate.server.common.CdcProcessor;
import com.networknt.tram.cdc.polling.connector.PollingDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class PollingCdcProcessor<EVENT_BEAN, EVENT, ID> implements CdcProcessor<EVENT> {

  private Logger logger = LoggerFactory.getLogger(getClass());
  private PollingDao pollingDao;
  private int pollingIntervalInMilliseconds;
  private AtomicBoolean watcherRunning = new AtomicBoolean(false);

  public PollingCdcProcessor(PollingDao<EVENT_BEAN, EVENT, ID> pollingDao, int pollingIntervalInMilliseconds) {
    this.pollingDao = pollingDao;
    this.pollingIntervalInMilliseconds = pollingIntervalInMilliseconds;
  }

  @Override
  public void start(Consumer<EVENT> eventConsumer) {
    watcherRunning.set(true);

    new Thread() {
      @Override
      public void run() {

        while (watcherRunning.get()) {
          try {

            List<EVENT> eventsToPublish = pollingDao.findEventsToPublish();
            List<EVENT> eventsPublished = new ArrayList<>();
            for (EVENT event:eventsToPublish)  {
              try {
                eventConsumer.accept(event);
                eventsPublished.add(event);
              } catch (Exception e) {
                logger.error(e.getMessage(), e);
              }
            }

         //   eventsToPublish.forEach(eventConsumer::accept);

            if (!eventsPublished.isEmpty()) {
              pollingDao.markEventsAsPublished(eventsPublished);
            }

            try {
              Thread.sleep(pollingIntervalInMilliseconds);
            } catch (Exception e) {
              logger.error(e.getMessage(), e);
            }
          } catch (Exception e) {
            logger.error(e.getMessage(), e);
          }
        }
      }
    }.start();
  }

  @Override
  public void stop() {
    watcherRunning.set(false);
  }
}
