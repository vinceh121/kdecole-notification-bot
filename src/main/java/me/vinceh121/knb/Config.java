package me.vinceh121.knb;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Config {
	private String token, mongo, feedbackChannelId;
	private int delay;
	private Collection<Long> admins;
	private MetricConfig metrics;

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

	@JsonProperty(required = false)
	public MetricConfig getMetrics() {
		return metrics;
	}

	@JsonProperty(required = false)
	public void setMetrics(MetricConfig metrics) {
		this.metrics = metrics;
	}

	public static class MetricConfig {
		private String host;
		private int port;
		private long period;

		public String getHost() {
			return host;
		}

		public void setHost(String host) {
			this.host = host;
		}

		public int getPort() {
			return port;
		}

		public void setPort(int port) {
			this.port = port;
		}

		public long getPeriod() {
			return period;
		}

		public void setPeriod(long period) {
			this.period = period;
		}

	}
}
