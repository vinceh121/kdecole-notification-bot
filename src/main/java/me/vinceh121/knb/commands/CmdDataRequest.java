package me.vinceh121.knb.commands;

import static com.rethinkdb.RethinkDB.r;

import java.util.Date;

import com.fasterxml.jackson.databind.node.ObjectNode;

import me.vinceh121.knb.AbstractCommand;
import me.vinceh121.knb.CommandContext;
import me.vinceh121.knb.Knb;
import me.vinceh121.knb.KdecoleUserInstance;
import net.dv8tion.jda.api.entities.Message.MentionType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class CmdDataRequest extends AbstractCommand {

	public CmdDataRequest(final Knb knb) {
		super(knb);
	}

	@Override
	protected void executeSync(final CommandContext ctx) {
		final String requesterId;
		if (ctx.isAdminCalled() && ctx.getArgs().size() >= 1) {
			final String arg = ctx.getArgs().get(0);
			if (MentionType.USER.getPattern().matcher(arg).matches()) {
				requesterId = arg.substring(3, arg.length() - 1);
			} else {
				requesterId = arg;
			}
			ctx.getEvent()
					.getChannel()
					.sendMessage(":shield: Used admin permissions to trigger datarequest for " + requesterId)
					.queue();
		} else {
			requesterId = ctx.getEvent().getAuthor().getId();
		}

		this.knb.getJda().retrieveUserById(requesterId).queue(user -> {
			user.openPrivateChannel().queue(privChan -> {

				final StringBuilder headSb = new StringBuilder();
				headSb.append(":file_cabinet: **Votre export de données**\n");
				headSb.append("\t:shield: Export déclanché par un administrateur\n");
				headSb.append("\tDate: " + new Date().toString() + "\n\n");
				privChan.sendMessage(headSb.toString()).queue();
				this.knb.getTableKdecoleInstances()
						.filter(r.hashMap("adderId", user.getId()))
						.run(this.knb.getDbCon(), KdecoleUserInstance.class)
						.forEach(ui -> {
							final StringBuilder sb = new StringBuilder();
							this.makeInstance(sb, ui);
							privChan.sendMessage(sb.toString()).queue();
						});
			});
		});
	}

	private void makeInstance(final StringBuilder sb, final KdecoleUserInstance ui) {
		final TextChannel chan = this.knb.getJda().getTextChannelById(ui.getChannelId());
		final ObjectNode json = this.knb.getMapper().valueToTree(ui);
		json.put("id", ui.getId());
		sb.append("Channel: " + chan + "\n");
		sb.append("```json\n");
		sb.append(json.toPrettyString());
		sb.append("```\n");
	}

	@Override
	public String getHelp() {
		return "Exécutez vos droits RGPD !";
	}
}
