package me.vinceh121.knb;

import java.util.Date;

import org.bson.types.ObjectId;

public class UserInstance {
	private ObjectId id;
	private String kdecoleToken, adderId, guildId, channelId, endpoint;
	private Stage stage;
	private Date lastCheck;

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

	public String getGuildId() {
		return this.guildId;
	}

	public void setGuildId(final String guildId) {
		this.guildId = guildId;
	}

	public String getChannelId() {
		return this.channelId;
	}

	public void setChannelId(final String channelId) {
		this.channelId = channelId;
	}

	public Stage getStage() {
		return this.stage;
	}

	public void setStage(final Stage stage) {
		this.stage = stage;
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

	@Override
	public String toString() {
		return "UserInstance [kdecoleToken="
				+ this.kdecoleToken
				+ ", adderId="
				+ this.adderId
				+ ", guildId="
				+ this.guildId
				+ ", channelId="
				+ this.channelId
				+ "]";
	}
}
