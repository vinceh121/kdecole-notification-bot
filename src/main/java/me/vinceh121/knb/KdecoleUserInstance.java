package me.vinceh121.knb;

public class KdecoleUserInstance extends AbstractUserInstance {
	private String kdecoleToken, endpoint;

	public String getKdecoleToken() {
		return this.kdecoleToken;
	}

	public void setKdecoleToken(final String kdecoleToken) {
		this.kdecoleToken = kdecoleToken;
	}

	public String getEndpoint() {
		return this.endpoint;
	}

	public void setEndpoint(final String endpoint) {
		this.endpoint = endpoint;
	}

	@Override
	public String toString() {
		return "KdecoleUserInstance [kdecoleToken="
				+ kdecoleToken
				+ ", endpoint="
				+ endpoint
				+ ", getId()="
				+ getId()
				+ ", getAdderId()="
				+ getAdderId()
				+ ", getChannelId()="
				+ getChannelId()
				+ ", getLastCheck()="
				+ getLastCheck()
				+ ", isShowWarnings()="
				+ isShowWarnings()
				+ ", isAllowOthers()="
				+ isAllowOthers()
				+ ", isAlwaysShowWarnings()="
				+ isAlwaysShowWarnings()
				+ ", getRelays()="
				+ getRelays()
				+ ", getGuildId()="
				+ getGuildId()
				+ "]";
	}
}
