package me.vinceh121.knb;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UserInstance {
	private String id;
	private String kdecoleToken, adderId, channelId, guildId, endpoint;
	private Date lastCheck = new Date(0L);
	private boolean showWarnings = true, allowOthers = false, alwaysShowWarnings = false;
	private List<RelayType> relays = new ArrayList<>();

	public String getId() {
		return this.id;
	}

	public void setId(final String id) {
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

	public boolean isAlwaysShowWarnings() {
		return this.alwaysShowWarnings;
	}

	public void setAlwaysShowWarnings(final boolean alwaysShowWarnings) {
		this.alwaysShowWarnings = alwaysShowWarnings;
	}

	public List<RelayType> getRelays() {
		return this.relays;
	}

	public void setRelays(final List<RelayType> relays) {
		this.relays = relays;
	}

	public String getGuildId() {
		return this.guildId;
	}

	public void setGuildId(final String guildId) {
		this.guildId = guildId;
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
				+ ", guildId="
				+ guildId
				+ ", endpoint="
				+ endpoint
				+ ", lastCheck="
				+ lastCheck
				+ ", showWarnings="
				+ showWarnings
				+ ", allowOthers="
				+ allowOthers
				+ ", alwaysShowWarnings="
				+ alwaysShowWarnings
				+ ", relays="
				+ relays
				+ "]";
	}
}
