package me.vinceh121.knb;

import com.nimbusds.openid.connect.sdk.token.OIDCTokens;

public class SkolengoUserInstance extends AbstractUserInstance {
	private OIDCTokens tokens;
	private String emsCode;
	private String schoolId;

	public OIDCTokens getTokens() {
		return tokens;
	}

	public void setTokens(OIDCTokens tokens) {
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
