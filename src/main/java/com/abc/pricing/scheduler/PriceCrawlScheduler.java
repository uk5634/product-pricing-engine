package com.abc.pricing.scheduler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.abc.pricing.dto.CompetitorPriceMessage;
import com.abc.pricing.kafka.CompetitorPriceProducer;
import com.abc.pricing.service.PriceCrawlerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PriceCrawlScheduler {
	
	@Autowired
	private PriceCrawlerService priceCrawlerService;
	
	@Autowired
	private CompetitorPriceProducer competitorPriceProducer;
	
	private final AtomicBoolean isRunning = new AtomicBoolean(false);
	
	@Value("${crawler.scheduler.enabled:true}")
	private boolean schedulerEnabled;
	
	@Scheduled(fixedRate = 300000)
	public void scheduledPrice() {
		if(!schedulerEnabled) {
			log.debug("Price crawl scheduler is disabled");
			return;
		}
		
		if(!isRunning.compareAndSet(false, true)){
			log.warn("Previous price crawl still running, skipping this execution");
			return;
		}
		
		try {
			executeCrawl();
		} finally {
			isRunning.set(false);
		}

	}

	private void executeCrawl() {
		LocalDateTime startTime = LocalDateTime.now();
		log.info("========= Price Crawl Started at {} =========", startTime);
		
		List<CompetitorPriceMessage> prices = priceCrawlerService.crawlAllPrices();
		
		LocalDateTime endTime = LocalDateTime.now();
		log.info("========= Price Crawl Completed at {} =========", endTime);

		competitorPriceProducer.sendPrices(prices);
	}

	

}
