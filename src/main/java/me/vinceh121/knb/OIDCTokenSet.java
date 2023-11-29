package me.vinceh121.knb;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OIDCTokenSet {
	@JsonProperty("access_token")
	private String accessToken;

	@JsonProperty("expires_at")
	private long expiresAt;

	@JsonProperty("id_token")
	private String idToken;

	@JsonProperty("refresh_token")
	private String refreshToken;

	@JsonProperty("token_type")
	private String token_type;

	private String scope;

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public long getExpiresAt() {
		return expiresAt;
	}

	public void setExpiresAt(long expiresAt) {
		this.expiresAt = expiresAt;
	}

	public String getIdToken() {
		return idToken;
	}

	public void setIdToken(String idToken) {
		this.idToken = idToken;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public String getToken_type() {
		return token_type;
	}

	public void setToken_type(String token_type) {
		this.token_type = token_type;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	@Override
	public String toString() {
		return "OIDCTokenSet [accessToken="
				+ accessToken
				+ ", expiresAt="
				+ expiresAt
				+ ", idToken="
				+ idToken
				+ ", refreshToken="
				+ refreshToken
				+ ", token_type="
				+ token_type
				+ ", scope="
				+ scope
				+ "]";
	}
}
