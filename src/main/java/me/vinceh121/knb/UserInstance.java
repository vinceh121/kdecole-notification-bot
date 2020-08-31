package me.vinceh121.knb;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;

public class UserInstance {
	private ObjectId id;
	private String kdecoleToken, adderId, channelId, endpoint;
	private Date lastCheck = new Date(0L);
	private boolean showWarnings = true, allowOthers = false;
	private List<RelayType> relays = new ArrayList<>();

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
		return this.showWarnings;
	}

	public void setShowWarnings(final boolean showWarnings) {
		this.showWarnings = showWarnings;
	}

	public boolean isAllowOthers() {
		return this.allowOthers;
	}

	public void setAllowOthers(final boolean allowOthers) {
		this.allowOthers = allowOthers;
	}

	public List<RelayType> getRelays() {
		return relays;
	}

	public void setRelays(List<RelayType> relays) {
		this.relays = relays;
	}

	@Override
	public String toString() {
		return "UserInstance [id="
				+ this.id
				+ ", kdecoleToken="
				+ this.kdecoleToken
				+ ", adderId="
				+ this.adderId
				+ ", channelId="
				+ this.channelId
				+ ", endpoint="
				+ this.endpoint
				+ ", lastCheck="
				+ this.lastCheck
				+ "]";
	}
}
