package Codify.parsing.service.producer;

import Codify.parsing.config.RabbitConfig;
import Codify.parsing.dto.ParsingCompleteMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ParsingCompleteProducer {
    private final RabbitTemplate rabbitTemplate;

    public void sendParsingCompleteMessage(Integer assignmentId, Integer studentId, Integer submissionId) {
        try {
            ParsingCompleteMessageDto message = new ParsingCompleteMessageDto(assignmentId, submissionId, studentId);
            rabbitTemplate.convertAndSend(RabbitConfig.PARSING_COMPLETE_QUEUE, message);
            log.info("✅ Sent parsing complete message: {}", message);
        } catch (Exception e) {
            log.error("❌ Failed to send parsing complete message: submissionId={}", submissionId, e);
            throw new RuntimeException("Failed to send message", e);
        }

    }
}
