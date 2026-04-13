package org.seyf.cardetection.service;


import lombok.AllArgsConstructor;
import lombok.Setter;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class IngestionService {

    private final AmqpTemplate amqpTemplate;
    private final String queueName;

    public IngestionService(AmqpTemplate amqpTemplate,
                            @Value("${queue.name}") String queueName) {
        this.amqpTemplate = amqpTemplate;
        this.queueName = queueName;
    }

    public boolean sendToMessageBroker(MultipartFile file,String cameraId,Long timestamp,String parkingName,String groundLevel) {

       try {
           Message message = MessageBuilder.withBody(file.getBytes())
                   .setHeader("camera_id", cameraId)
                   .setHeader("timestamp", timestamp)
                   .setHeader("parking_name",parkingName)
                   .setHeader("ground_level_id",groundLevel)
                   .setContentType(MessageProperties.CONTENT_TYPE_BYTES).build();

           amqpTemplate.send(queueName,message);
           return true;
       }catch (Exception e){
           return false;
       }
    }
}