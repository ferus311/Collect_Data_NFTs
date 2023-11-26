package marketplace.crawl;

import java.net.URI;
import java.net.http.HttpRequest;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

public class Binance extends Crawler {
	
	public enum Chain {
		BNB("BSC"), ETH("ETH"), BTC("BTC");
		
		private String value;
		
		private Chain(String value) {
			this.value = value;
		}

	}
	
	public enum Period {
		ONEHOUR("1H"), SIXHOURS("4H"), ONEDAY("24H"), ONEWEEK("7D");
		
		private String value;
		
		private Period(String value) {
			this.value = value;
		}
	}
	
	
	public Binance(Chain chain, Period period, int rows) {
		super.chain = chain.value;
		super.period = period.value;
		super.rows = rows;
	}
	
	public Binance(String chain, String period) {
		super.chain = chain;
		super.period = period;
		super.rows = 100;
	}
	
	@Override
	protected void getRespone() {
		String requestBody = "{\"network\":\""+ chain + "\",\"period\":\"" + period + "\",\"sortType\":\"volumeDesc\",\"page\":1,\"rows\":"+ rows +"}";
		HttpRequest request = HttpRequest.newBuilder()
			    .uri(URI.create("https://www.binance.com/bapi/nft/v1/friendly/nft/ranking/trend-collection"))
			    .header("Content-Type", "application/json") 
			    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.0.0 Safari/537.36 OPR/103.0.0.0")
			    .method("POST", HttpRequest.BodyPublishers.ofString(requestBody))
			    .build();
		respone = Crawler.getResponeRequest(request);
	
	}
	
	@Override
	protected void preprocessData() {
		JsonArray rowsRaw = JsonParser.parseString(respone)
				.getAsJsonObject()
				.getAsJsonObject("data")
				.getAsJsonArray("rows");
		
		JsonArray rows = new JsonArray();
		for(JsonElement rowRaw : rowsRaw) {
			JsonObject rowRawObj = rowRaw.getAsJsonObject();
			JsonObject row = new JsonObject();
			row.add("id", rowRawObj.get("collectionId"));
			row.add("logo", rowRawObj.get("coverUrl"));
			row.add("name", rowRawObj.get("title"));
			row.add("volume", rowRawObj.get("volume"));
			row.add("volumeChange", rowRawObj.get("volumeRate"));
			row.add("floorPrice", rowRawObj.get("floorPrice"));
			row.add("floorPriceChange", rowRawObj.get("floorPriceRate"));
			row.add("items", rowRawObj.get("itemsCount"));
			row.add("owners", rowRawObj.get("ownersCount"));
			rows.add(row);
		}
		
		data.add("createdAt", new JsonPrimitive(Crawler.getTime("MM/dd/yyy HH:MM:SS")));
		data.add("chain", new JsonPrimitive(chain));
		data.add("period", new JsonPrimitive(period));
		data.add("data", rows);
	}

	@Override
	protected String getFileName() {
		String formatTime = Crawler.getTime("yyy_MM_dd_HH");
		return ".\\data\\binance_" + period + "_" + chain + "_" + formatTime + ".json";
	}
}
