package com.abc.pricing.kafka;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import com.abc.pricing.dto.CompetitorPriceMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
 * Competitor Price Producer will publish msgs on kafka topic
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CompetitorPriceProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    /*
     * send message one by one on kafka topic
     */
    public int sendPrices(List<CompetitorPriceMessage> messages) {
    	log.info("Sending {} price messages to Kafka...", messages.size());
    	int successCount = 0;
    	int failCount = 0;
    	for(CompetitorPriceMessage message: messages) {
    		try {
    			sendPrice(message);
    			successCount++;
    		} catch(Exception e) {
    			log.error("Failed to send message for product {} : {}", message.getProductId(), e.getMessage());
    			failCount++;
    		}
    	}
    	log.info("Batch send completed: {} successful, {} failed", successCount, failCount);
    	return successCount;
    }
    
    public CompletableFuture<SendResult<String, String>> sendPrice(CompetitorPriceMessage message){
    	log.debug("Sending price message to Kafka: productId={}, competitor={}, price={}", message.getProductId(), message.getCompetitorName(), message.getPrice());
    	String key = message.getProductId();
    	String msgString = convertMessageToJson(message);
    	return kafkaTemplate.send("product-prices", key, msgString)
    			.whenComplete((result, ex) ->{
    				if(ex == null) {
    					log.info("Price message sent successfully: topic={}, partition={}, offset={}, key={}"
    							, result.getRecordMetadata().topic(),
    							result.getRecordMetadata().partition(),
    							result.getRecordMetadata().offset(),
    							key);
    				} else {
    					log.error("Failed to send price message: key={}, error={}", key, ex.getMessage());
    				}
    			});
    
    }

	private String convertMessageToJson(CompetitorPriceMessage message) {
		try {
			ObjectMapper mapper =  new ObjectMapper();
			return mapper.writeValueAsString(message);
		} catch (JsonProcessingException e) {
			log.error("Error during serializing message to string : {}", e.getMessage());
			return "[]";
		}
	}
}