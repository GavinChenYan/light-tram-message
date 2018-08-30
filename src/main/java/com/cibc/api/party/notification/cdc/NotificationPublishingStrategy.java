package com.cibc.api.party.notification.cdc;

import com.networknt.eventuate.common.impl.JSonMapper;
import com.networknt.eventuate.server.common.PublishingStrategy;
import com.networknt.tram.cdc.mysql.connector.MessageWithDestination;
import com.networknt.tram.message.common.Message;

import java.util.Optional;

public class NotificationPublishingStrategy implements PublishingStrategy<MessageWithDestination> {

  @Override
  public String partitionKeyFor(MessageWithDestination messageWithDestination) {
    String id = messageWithDestination.getMessage().getId();
    return messageWithDestination.getMessage().getHeader(Message.PARTITION_ID).orElse(id);
  }

  @Override
  public String topicFor(MessageWithDestination messageWithDestination) {
    return messageWithDestination.getDestination();
  }

  @Override
  public String toJson(MessageWithDestination messageWithDestination) {
    return messageWithDestination.getMessage().getPayload();
  }

  @Override
  public Optional<Long> getCreateTime(MessageWithDestination messageWithDestination) {
    return Optional.empty(); // TODO
  }
}
