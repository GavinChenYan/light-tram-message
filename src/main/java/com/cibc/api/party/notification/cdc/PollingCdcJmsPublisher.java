package com.cibc.api.party.notification.cdc;

import com.cibc.api.party.notification.model.UpdateEvent;
import com.networknt.eventuate.common.impl.JSonMapper;
import com.networknt.eventuate.server.common.PublishingStrategy;
import com.networknt.eventuate.server.common.exception.EventuateLocalPublishingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PollingCdcJmsPublisher<EVENT> extends CdcJmsPublisher<EVENT> {

  private Logger logger = LoggerFactory.getLogger(this.getClass());

  public PollingCdcJmsPublisher( PublishingStrategy<EVENT> publishingStrategy, String env, String release) {
    super(publishingStrategy, env, release);
  }

  @Override
  public void handleEvent(EVENT event) throws EventuateLocalPublishingException {
    logger.trace("Got record " + event.toString());

  //  String aggregateTopic = publishingStrategy.topicFor(event);
    String json = publishingStrategy.toJson(event);


    System.out.println("message out--->:" + json);
    UpdateEvent updateEvent = JSonMapper.fromJson(json, UpdateEvent.class);
    try {
		notifSender.sendNotification(updateEvent, env, release);
	} catch (Exception e) {
		String msg = "notification message send failed ";
		logger.error(msg+ e.getMessage());
		throw new EventuateLocalPublishingException(msg, e);
	}
    

 /*   for (int i = 0; i < 5; i++) {
      try {
        /*
        producer.send(
                aggregateTopic,
                publishingStrategy.partitionKeyFor(event),
                json
        ).get(10, TimeUnit.SECONDS);

        return;
      } catch (Exception e) {
        logger.warn("error publishing to " + aggregateTopic, e);
        lastException = e;

        try {
          Thread.sleep((int) Math.pow(2, i) * 1000);
        } catch (InterruptedException ie) {
          throw new RuntimeException(ie);
        }
      }
    }*/
  }
}
