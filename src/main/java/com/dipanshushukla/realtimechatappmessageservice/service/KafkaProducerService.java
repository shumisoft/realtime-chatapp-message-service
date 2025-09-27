package com.dipanshushukla.realtimechatappmessageservice.service;

import com.dipanshushukla.realtimechatappmessageservice.dto.MessageDTO;

public interface KafkaProducerService {

  void publish(MessageDTO dto);

}