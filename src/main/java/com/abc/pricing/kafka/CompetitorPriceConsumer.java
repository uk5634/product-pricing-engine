package com.abc.pricing.kafka;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import com.abc.pricing.domain.CompetitorPrice;
import com.abc.pricing.dto.CompetitorPriceMessage;
import com.abc.pricing.dto.PriceCalculationRequest;
import com.abc.pricing.dto.PriceResponse;
import com.abc.pricing.repository.CompetitorPriceRepository;
import com.abc.pricing.service.PriceCacheService;
import com.abc.pricing.service.PriceCalculationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
 * Competitor Price Consumer to consume Price message and process it.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CompetitorPriceConsumer {

	@Autowired
	private PriceCalculationService priceCalculationService;
	
	@Autowired
	private CompetitorPriceRepository competitorPriceRepository;
	
	@Autowired
	private PriceCacheService priceCacheService;
	
	
   /*
    * Consume kafka msgs
    */
    @KafkaListener(topics = "product-prices", groupId = "price-group")
    public void consumeCompetitorPrice(@Payload String messageString,
    		@Header(KafkaHeaders.RECEIVED_KEY) String key,
    		@Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
    		@Header(KafkaHeaders.OFFSET) long offset) {
       log.info("Received message : partition={}, offset={}, key={}", partition, offset, key);
       
       try {
    	   // Deserialize String to Message object
    	   CompetitorPriceMessage message  = convertStringToMessage(messageString);
    	   // Validate Message
    	   if(!isValidMessage(message)) {
    		   log.warn("Invalid message received, skipping: {}", message);
    		   return;
    	   }
    	   // Process Message
    	   processMessage(message);
    	   
    	   log.info("Successfully processed price update: productId={}, competitor={}, newPrice={}", message.getProductId(), message.getCompetitorName(), message.getPrice());
       } catch(Exception e) {
    	   log.error("Error processing competitor price message: {}", e.getMessage());
       }
    }


	private CompetitorPriceMessage convertStringToMessage(String messageString) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			CompetitorPriceMessage msg = mapper.readValue(messageString,CompetitorPriceMessage.class);
			return msg;
		} catch (JsonMappingException e) {
			log.error("Error during deserilize of string to Message : {}", e.getMessage());
			return CompetitorPriceMessage.builder().build();
		} catch (JsonProcessingException e) {
			log.error("Error during deserilize of string to Message : {}", e.getMessage());
			return CompetitorPriceMessage.builder().build();
		}
	}


	private void processMessage(CompetitorPriceMessage message) {
		String productId = message.getProductId();
		String competitorName = message.getCompetitorName();
		// Checking Competitor Price exist in database or not
		CompetitorPrice existingPrice =  competitorPriceRepository.findByProductIdAndCompetitorName(productId, competitorName).block();
		log.debug("Updating existing price for {} from {}: {} -> {}",
				productId, competitorName, existingPrice.getPrice(), message.getPrice());
		Integer returnValue = competitorPriceRepository.upsert(existingPrice != null ? existingPrice.getId(): UUID.randomUUID().toString(), productId, competitorName, message.getPrice() , LocalDateTime.now()).block();
		if(returnValue == 1) {
			priceCacheService.invalidateByPattern(productId + ":*").block();
			PriceCalculationRequest request = PriceCalculationRequest.builder().productId(productId).userId("user-002").build();
			PriceResponse response = priceCalculationService.calculatePriceRealTime(request).block();
			String cacheKey = priceCalculationService.buildCacheKey(request);
			priceCacheService.put(cacheKey, response);
		}
	}


	private boolean isValidMessage(CompetitorPriceMessage message) {
		if(message == null) {
			return false;
		}
		if(message.getProductId() == null || message.getProductId().isBlank()) {
			log.warn("Message missing product id");
			return false;
		}
		if(message.getCompetitorName() == null || message.getCompetitorName().isBlank()) {
			log.warn("Message missing competitor name");
			return false;
		}
		if(message.getPrice() == null || message.getPrice().signum() <= 0) {
			log.warn("Message missing invalid price: {}",message.getPrice());
			return false;
		}
		return true;
	}
	
}