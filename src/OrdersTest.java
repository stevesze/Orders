import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import edu.sze.processor.TradeProcessor;

class OrdersTest {
	
	@Test
	void runTradeProcessorAllGood() {
		
		assertEquals(518, TradeProcessor.getSuccessful());

		assertEquals(1, TradeProcessor.getFail_missingFields());
		
		assertEquals(35, TradeProcessor.getFail_symbolsInvalid());
		
		assertEquals(0, TradeProcessor.getFail_brokerLimit());
		
		assertEquals(0, TradeProcessor.getFail_brokerID());
		
		assertEquals(554, TradeProcessor.getTotal_transaction());

	}

}
