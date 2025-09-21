package Codify.parsing.service.listener;

import Codify.parsing.dto.MessageDto;
import Codify.parsing.service.ParsingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ParsingMessageListener {

    //upload service에서 받은 message 소비

    private final ParsingService  parsingService;

    @RabbitListener(queues = "parsing.queue",containerFactory =
            "rabbitListenerContainerFactory")
    public void handleFileUpload(MessageDto message) {
        log.info("Received file upload message from parsing.queue: {}", message.getGroupId());
        try {
            parsingService.parseFromS3BySubmissionId(message);
        } catch (Exception e) {
            log.error("Failed to process parsing message from parsing.queue", e);
            throw new AmqpRejectAndDontRequeueException("Parsing failed", e);
        }
    }
}
