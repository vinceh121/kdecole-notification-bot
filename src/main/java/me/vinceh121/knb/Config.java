package me.vinceh121.knb;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Config {
	private String token, dbUrl, feedbackChannelId;
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

	public String getDbUrl() {
		return this.dbUrl;
	}

	public void setDbUrl(final String dbUrl) {
		this.dbUrl = dbUrl;
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
		return this.metrics;
	}

	@JsonProperty(required = false)
	public void setMetrics(final MetricConfig metrics) {
		this.metrics = metrics;
	}

	public static class MetricConfig {
		private String host;
		private int port;
		private long period;

		public String getHost() {
			return this.host;
		}

		public void setHost(final String host) {
			this.host = host;
		}

		public int getPort() {
			return this.port;
		}

		public void setPort(final int port) {
			this.port = port;
		}

		public long getPeriod() {
			return this.period;
		}

		public void setPeriod(final long period) {
			this.period = period;
		}

	}
}
