package com.abc.pricing.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.abc.pricing.dto.CompetitorPriceMessage;
import com.abc.pricing.dto.CompetitorUrlMapping;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriceCrawlerService {

	@Value("${crawler.timeout-ms:10000}")
	private int timeoutMs;
	
	@Value("${crawler.user-agent:Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36}")
	private String userAgent;
	
	private static final Map<String, List<CompetitorUrlMapping>> PRODUCT_URL_MAPPING = new ConcurrentHashMap<>();
	
	static {
		PRODUCT_URL_MAPPING.put("prod-001", List.of(
				new CompetitorUrlMapping("AMAZON", "https://www.amazon.in/iPhone-Pro-256-Promotion-Breakthrough/dp/B0FQG1LPVF", "Amazon")
				));
	}
	
	public List<CompetitorPriceMessage> crawlAllPrices() {
		log.info("Starting price crawl for all products ....");
		
		List<CompetitorPriceMessage> allPrices = new ArrayList<>();
		
		for(String productId : PRODUCT_URL_MAPPING.keySet()) {
			List<CompetitorUrlMapping> mappings = PRODUCT_URL_MAPPING.get(productId);
			for(CompetitorUrlMapping mapping : mappings) {
				
	       //     Document doc = crawlPrice(mapping);
	            
	       //     BigDecimal price = parsePrice(doc, mapping.getSource());
	           
	            CompetitorPriceMessage message = CompetitorPriceMessage.builder()
	            		.productId(productId)
	            		.productName(getProductName(productId))
	            		.competitorName(mapping.getCompetitorName())
	            		.competitorUrl(mapping.getUrl())
	          //  		.price(price)
	            		.price(new BigDecimal(134900))
	            		.currency("INR")
	            		.source(mapping.getSource())
	            		.build();
	            allPrices.add(message);
			}
			
		}
		return allPrices;
	}

	private Document crawlPrice(CompetitorUrlMapping mapping) {
		// Connect to Amazon URL with timeout and User-Agent
		try {
			return Jsoup.connect(mapping.getUrl())
		        .userAgent(userAgent)
		        .timeout(timeoutMs)
		        .get();
		} catch(IOException e) {
			log.error("Error during crawling for url {} - {}", mapping.getUrl(), e.getMessage());
			return null;
		}
	}

	private BigDecimal parsePrice(Document doc, String source) {
		String priceText = "0";
		if(doc == null) {
			return BigDecimal.ZERO;
		}
		switch(source) {
		case "AMAZON":
			 Element priceElement = doc.selectFirst(".a-price-whole");
			 priceText = (priceElement != null) ?  priceElement.text() : "0";
			 break;
		default:
			priceText = "0";
		}
		
		String cleanPrice = priceText.replaceAll("[^\\d.]", "");
		return cleanPrice.isEmpty() ? BigDecimal.ZERO : new BigDecimal(cleanPrice);
	}

	private String getProductName(String productId) {
		Map<String, String> productNames = Map.of(
				"prod-001","iPhone 15 Pro"
				);
		return productNames.getOrDefault(productId, "Unknown Product");
	}
}
