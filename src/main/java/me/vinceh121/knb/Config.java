package me.vinceh121.knb;

public class Config {
	private String token, mongo;
	private int delay;

	public String getToken() {
		return this.token;
	}

	public void setToken(final String token) {
		this.token = token;
	}

	public int getDelay() {
		return this.delay;
	}

	public void setDelay(final int delay) {
		this.delay = delay;
	}

	public String getMongo() {
		return this.mongo;
	}

	public void setMongo(final String mongo) {
		this.mongo = mongo;
	}

}
