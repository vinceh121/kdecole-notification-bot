package me.vinceh121.knb;

import java.util.Date;

import org.bson.types.ObjectId;

public class UserInstance {
	private ObjectId id;
	private String kdecoleToken, adderId, channelId, endpoint;
	private Date lastCheck = new Date(0L);
	private boolean showWarnings = true, allowOthers = false;

	public ObjectId getId() {
		return this.id;
	}

	public void setId(final ObjectId id) {
		this.id = id;
	}

	public String getKdecoleToken() {
		return this.kdecoleToken;
	}

	public void setKdecoleToken(final String kdecoleToken) {
		this.kdecoleToken = kdecoleToken;
	}

	public String getAdderId() {
		return this.adderId;
	}

	public void setAdderId(final String adderId) {
		this.adderId = adderId;
	}

	public String getChannelId() {
		return this.channelId;
	}

	public void setChannelId(final String channelId) {
		this.channelId = channelId;
	}

	public Date getLastCheck() {
		return this.lastCheck;
	}

	public void setLastCheck(final Date lastCheck) {
		this.lastCheck = lastCheck;
	}

	public String getEndpoint() {
		return this.endpoint;
	}

	public void setEndpoint(final String endpoint) {
		this.endpoint = endpoint;
	}

	public boolean isShowWarnings() {
		return showWarnings;
	}

	public void setShowWarnings(final boolean showWarnings) {
		this.showWarnings = showWarnings;
	}

	public boolean isAllowOthers() {
		return allowOthers;
	}

	public void setAllowOthers(boolean allowOthers) {
		this.allowOthers = allowOthers;
	}

	@Override
	public String toString() {
		return "UserInstance [id="
				+ id
				+ ", kdecoleToken="
				+ kdecoleToken
				+ ", adderId="
				+ adderId
				+ ", channelId="
				+ channelId
				+ ", endpoint="
				+ endpoint
				+ ", lastCheck="
				+ lastCheck
				+ "]";
	}
}
