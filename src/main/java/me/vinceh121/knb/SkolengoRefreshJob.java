package me.vinceh121.knb;

import java.io.IOException;
import java.net.URI;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.RefreshTokenGrant;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.token.RefreshToken;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponseParser;
import com.nimbusds.openid.connect.sdk.token.OIDCTokens;
import com.rethinkdb.model.MapObject;

import me.vinceh121.jskolengo.SkolengoConstants;

public class SkolengoRefreshJob implements Job {
	private static final Logger LOG = LogManager.getLogger(SkolengoRefreshJob.class);

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		final Knb knb = (Knb) context.getMergedJobDataMap().get("knb");

		knb.getAllValidSkolengoInstances().forEach(ui -> {
			try {
				TokenRequest req = new TokenRequest(URI.create(ui.getTokenEndpoint()),
						new ClientID(SkolengoConstants.OIDC_CLIENT_ID),
						new RefreshTokenGrant(new RefreshToken(ui.getTokens().getRefreshToken())), new Scope("openid"),
						null, null, null, null);

				TokenResponse res = OIDCTokenResponseParser.parse(req.toHTTPRequest().send());

				if (!res.indicatesSuccess()) {
					throw new IOException("Refresh failed: " + res.toErrorResponse().toJSONObject());
				}

				OIDCTokenResponse tokensResponse = (OIDCTokenResponse) res.toSuccessResponse();
				OIDCTokens tokens = tokensResponse.getOIDCTokens();

				OIDCTokenSet tokenSet = new OIDCTokenSet();
				tokenSet.setAccessToken(tokens.getAccessToken().toString());
				tokenSet.setRefreshToken(tokens.getRefreshToken().toString());
				tokenSet.setIdToken(tokens.getIDTokenString());

				knb.getTableSkolengoInstances()
						.get(ui.getId())
						.update(new MapObject<>().with("tokens", tokenSet))
						.run(knb.getDbCon());

			} catch (ParseException | IOException e) {
				LOG.error("Error while refreshing token", e);
			}
		});
	}
}
