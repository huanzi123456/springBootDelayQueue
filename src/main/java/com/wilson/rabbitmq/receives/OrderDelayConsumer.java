/**
 * @Copyright (c) 2018/8/19, Lianjia Group All Rights Reserved.
 */
package com.wilson.rabbitmq.receives;

import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import com.wilson.rabbitmq.config.RabbitConfig;
import com.wilson.rabbitmq.model.CreateOrderVo;
import com.wilson.rabbitmq.utils.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;


import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 类注释
 */
@Component
//@RabbitListener(queues = "process_queue")
//统一配置
@RabbitListener(queues = RabbitConfig.PROCESS_QUEUE)
@Slf4j
public class OrderDelayConsumer {


    @RabbitHandler
    public void orderConsumer(Message message, Channel channel, org.springframework.amqp.core.Message message1){

        // 消息发送时间
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        log.info("消息接收时间为: {}", sdf.format(new Date()));
        // 接收到处理队列中的消息的，就是指定时间过期的消息
        // 这里处理每一条消息中的订单编号，去查询对应的订单支付状态，如果处于未支付状态，就取消用户的订单
        try {
            CreateOrderVo orderVo = JSONObject.parseObject(message.getContent(), CreateOrderVo.class);
            // 获取订单编号，去查询对应的支付结果
            log.info("订单编号为: {}", orderVo.getOrderNo());

            //确认(告诉服务器)消息已经被消费,服务器会删除对应的消息,确保不会被重复消费
            channel.basicAck(message1.getMessageProperties().getDeliveryTag(),false);
        } catch (Exception e) {
            log.error("订单消息解析异常，请检查消息格式是否正确", message.getContent());
        }
    }
}
