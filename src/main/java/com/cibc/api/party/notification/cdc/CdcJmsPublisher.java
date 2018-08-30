package com.cibc.api.party.notification.cdc;

import com.cibc.api.party.notification.handler.NotificationMessageHandler;
import com.cibc.api.party.notification.handler.NotificationMessageHandlerFactory;
import com.networknt.eventuate.server.common.PublishingStrategy;
import com.networknt.eventuate.server.common.exception.EventuateLocalPublishingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CdcJmsPublisher<EVENT> {

  protected PublishingStrategy<EVENT> publishingStrategy;
  protected NotificationMessageHandler notifSender;
  protected String env;
  protected String release;
  private Logger logger = LoggerFactory.getLogger(this.getClass());

  public CdcJmsPublisher(PublishingStrategy<EVENT> publishingStrategy, String env, String release) {
    this.publishingStrategy = publishingStrategy;
    this.env = env;
    this.release = release;
  }

  public void start() {
    logger.debug("Starting NotificationMessageHandler");
    notifSender = NotificationMessageHandlerFactory.NOTIFICATIONHANDLER
              .getNotificationHandler();
  }

  public abstract void handleEvent(EVENT publishedEvent) throws EventuateLocalPublishingException;

  public void stop() {
    logger.debug("Stopping JMS sender ");

  }

}
