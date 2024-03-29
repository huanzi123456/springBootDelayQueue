/**
 * @Copyright (c) 2018/8/19, Lianjia Group All Rights Reserved.
 */
package com.wilson.rabbitmq.service;

import com.alibaba.fastjson.JSONObject;
import com.wilson.rabbitmq.config.RabbitConfig;
import com.wilson.rabbitmq.enums.ErrorCodeEnum;
import com.wilson.rabbitmq.exception.SystemException;
import com.wilson.rabbitmq.utils.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 延迟消息service
 *
 * @author wilson wei
 * @version 1.0
 * @since 2018/8/19 10:02
 */
@Service
@Slf4j
public class DelaySendService implements RabbitTemplate.ReturnCallback{

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送延时消息，每个消息都自己有自己的过期时间
     *
     * @param msg 消息
     * @author wilson wei
     * @date 10:03 2018/8/19
     */
    public void sendDelayMessage(Message msg) {

        // 消息发送时间
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        log.info("消息发送时间为: {}", sdf.format(new Date()));

        // 设置发送时间，开始发送,消息设置ack确认机制
        try {
            this.rabbitTemplate.setReturnCallback(this);
            this.rabbitTemplate.setConfirmCallback((CorrelationData correlationData, boolean ack, String cause) -> {
                if (!ack) {
                    log.error("PushProvider消息发送失败 {},  {}", cause, correlationData.toString());
                } else {
                    log.info("PushProvider 消息发送成功 ");
                }
            });

            rabbitTemplate.convertAndSend(RabbitConfig.DELAY_EXCHANGE, RabbitConfig.DELAY_ROUTING_KEY, msg,
                    message -> {
                        message.getMessageProperties().setExpiration(String.valueOf(msg.getTtl()));
                        return message;
                    });
        } catch (AmqpException e) {
            log.error("消息发送失败，请检查消息中间件是否正常", JSONObject.toJSONString(msg));
            throw new SystemException(ErrorCodeEnum.UNKNOWING_ERROR);
        }
    }

    /**
     * 发送消息，至指定过期时间的队列中。
     * 适用于一些定时任务
     */
    public void sendDelayQueueMessage(Message msg) {

        // 消息发送时间
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        log.info("消息发送时间为: {}", sdf.format(new Date()));

        // 设置发送时间，开始发送
        try {
            this.rabbitTemplate.setReturnCallback(this);
            this.rabbitTemplate.setConfirmCallback((CorrelationData correlationData, boolean ack, String cause) -> {
                if (!ack) {
                    log.error("PushProvider消息发送失败 {},  {}", cause, correlationData.toString());
                } else {
                    log.info("PushProvider 消息发送成功 ");
                }
            });
            rabbitTemplate.convertAndSend(RabbitConfig.DELAY_QUEUE_EXCHANGE, RabbitConfig.DELAY_ROUTING_KEY, msg);
        } catch (AmqpException e) {
            log.error("消息发送失败，请检查消息中间件是否正常", JSONObject.toJSONString(msg));
            throw new SystemException(ErrorCodeEnum.UNKNOWING_ERROR);
        }
    }

    //消息成功回调
    @Override
    public void returnedMessage(org.springframework.amqp.core.Message message, int i, String s, String s1, String s2) {
        System.out.println("sender return success" + message.toString() + "===" + i + "===" + s1 + "===" + s2);
    }
}
