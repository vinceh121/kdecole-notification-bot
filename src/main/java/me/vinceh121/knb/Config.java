package me.vinceh121.knb;

import java.util.Collection;

public class Config {
	private String token, mongo;
	private int delay;
	private Collection<Long> admins;

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

	public Collection<Long> getAdmins() {
		return admins;
	}

	public void setAdmins(Collection<Long> admins) {
		this.admins = admins;
	}

}
