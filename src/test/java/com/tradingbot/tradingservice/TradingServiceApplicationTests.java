package com.tradingbot.tradingservice;

import com.tradingbot.tradingservice.trading.service.TradingService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class TradingServiceApplicationTests {
	@Autowired
	private TradingService tradingService;

	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	@Test
	@Rollback(false)
	@DisplayName("매수로직 테스트")
	void onBiddingTest() {
		String message =
				"{\n" +
						"  \"symbol\": \"ADA\",\n" +
						"  \"tickType\": \"30M\",\n" +
						"  \"date\": \"20210927\",\n" +
						"  \"time\": \"230550\",\n" +
						"  \"openPrice\": 2672,\n" +
						"  \"closePrice\": 2777,\n" +
						"  \"lowPrice\": 2665,\n" +
						"  \"highPrice\": 2675,\n" +
						"  \"value\": 776727545.2384889,\n" +
						"  \"volume\": 290979.98211183,\n" +
						"  \"sellVolume\": 160352.96845635,\n" +
						"  \"buyVolume\": 130627.01365548,\n" +
						"  \"prevClosePrice\": 2782,\n" +
						"  \"chgRate\": -0.07,\n" +
						"  \"chgAmt\": -2,\n" +
						"  \"volumePower\": 81.46,\n" +
						"  \"timeTag\": \"2021-09-27T23:05:51\"\n" +
						"}";
	}



	@Test
	@Rollback(false)
	@DisplayName("매도로직 테스트")
	void onSellingTest() {
		String message =
				"{\n" +
						"  \"symbol\": \"ADA\",\n" +
						"  \"tickType\": \"30M\",\n" +
						"  \"date\": \"20210927\",\n" +
						"  \"time\": \"230550\",\n" +
						"  \"openPrice\": 2672,\n" +
						"  \"closePrice\": 2900,\n" +
						"  \"lowPrice\": 2665,\n" +
						"  \"highPrice\": 2675,\n" +
						"  \"value\": 776727545.2384889,\n" +
						"  \"volume\": 290979.98211183,\n" +
						"  \"sellVolume\": 160352.96845635,\n" +
						"  \"buyVolume\": 130627.01365548,\n" +
						"  \"prevClosePrice\": 2782,\n" +
						"  \"chgRate\": -0.07,\n" +
						"  \"chgAmt\": -2,\n" +
						"  \"volumePower\": 81.46,\n" +
						"  \"timeTag\": \"2021-09-27T23:05:51\"\n" +
						"}";
	}

	@Test
	@Rollback(false)
	@DisplayName("수익저장 테스트")
	void onRecordProfitTest() {
		String message =
				"{\n" +
						"  \"symbol\": \"ADA\",\n" +
						"  \"tickType\": \"30M\",\n" +
						"  \"date\": \"20210927\",\n" +
						"  \"time\": \"230550\",\n" +
						"  \"openPrice\": 2672,\n" +
						"  \"closePrice\": 2900,\n" +
						"  \"lowPrice\": 2665,\n" +
						"  \"highPrice\": 2675,\n" +
						"  \"value\": 776727545.2384889,\n" +
						"  \"volume\": 290979.98211183,\n" +
						"  \"sellVolume\": 160352.96845635,\n" +
						"  \"buyVolume\": 130627.01365548,\n" +
						"  \"prevClosePrice\": 2782,\n" +
						"  \"chgRate\": -0.07,\n" +
						"  \"chgAmt\": -2,\n" +
						"  \"volumePower\": 81.46,\n" +
						"  \"timeTag\": \"2021-09-27T23:05:51\"\n" +
						"}";
		tradingService.onRecordProfit(message);
	}
}
