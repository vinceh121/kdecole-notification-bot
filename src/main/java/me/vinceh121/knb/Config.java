package me.vinceh121.knb;

import java.util.Collection;

public class Config {
	private String token, mongo, feedbackChannelId;
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
		return this.admins;
	}

	public void setAdmins(final Collection<Long> admins) {
		this.admins = admins;
	}

	public String getFeedbackChannelId() {
		return this.feedbackChannelId;
	}

	public void setFeedbackChannelId(final String feedbackChannelId) {
		this.feedbackChannelId = feedbackChannelId;
	}
}
