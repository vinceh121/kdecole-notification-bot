package me.vinceh121.knb;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public abstract class AbstractUserInstance {
	private String id;
	private String adderId;
	private String channelId;
	private String guildId;
	private Date lastCheck = new Date(0L);
	private boolean showWarnings = true;
	private boolean allowOthers = false;
	private boolean alwaysShowWarnings = false;
	private final List<RelayType> relays = new ArrayList<>();

	public String getId() {
		return this.id;
	}

	public void setId(final String id) {
		this.id = id;
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

	public String getGuildId() {
		return this.guildId;
	}

	public void setGuildId(final String guildId) {
		this.guildId = guildId;
	}

}
