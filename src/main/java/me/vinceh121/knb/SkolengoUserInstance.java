package me.vinceh121.knb;

public class SkolengoUserInstance extends AbstractUserInstance {
	private OIDCTokenSet tokens;
	private String emsCode;
	private String schoolId;
	private String tokenEndpoint;

	public OIDCTokenSet getTokens() {
		return tokens;
	}

	public void setTokens(OIDCTokenSet tokens) {
		this.tokens = tokens;
	}

	public String getEmsCode() {
		return emsCode;
	}

	public void setEmsCode(String emsCode) {
		this.emsCode = emsCode;
	}

	public String getSchoolId() {
		return schoolId;
	}

	public void setSchoolId(String schoolId) {
		this.schoolId = schoolId;
	}

	public String getTokenEndpoint() {
		return tokenEndpoint;
	}

	public void setTokenEndpoint(String tokenEndpoint) {
		this.tokenEndpoint = tokenEndpoint;
	}

	@Override
	public String toString() {
		return "SkolengoUserInstance [tokens="
				+ tokens
				+ ", emsCode="
				+ emsCode
				+ ", schoolId="
				+ schoolId
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
