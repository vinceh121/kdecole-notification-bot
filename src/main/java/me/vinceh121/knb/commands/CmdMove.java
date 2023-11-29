package me.vinceh121.knb.commands;

import static com.rethinkdb.RethinkDB.r;

import me.vinceh121.knb.AbstractCommand;
import me.vinceh121.knb.CommandContext;
import me.vinceh121.knb.Knb;
import me.vinceh121.knb.KdecoleUserInstance;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class CmdMove extends AbstractCommand {

	public CmdMove(final Knb knb) {
		super(knb);
	}

	@Override
	public boolean validateSyntax(final CommandContext ctx) {
		if (ctx.getArgs().size() != 1) {
			return false;
		}
		try {
			Long.parseLong(ctx.getArgs().get(0));
		} catch (final NumberFormatException e) {
			return false;
		}
		return true;
	}

	@Override
	protected void executeSync(final CommandContext ctx) {
		final TextChannel toChan = this.knb.getJda().getTextChannelById(ctx.getArgs().get(0));
		final Guild toGuild = toChan.getGuild();
		if (!toGuild.getId().equals(ctx.getUserInstance().getGuildId())) {
			ctx.getEvent().getChannel().sendMessage("Vous ne pouvez pas déplacer l'intégration entre guilds").queue();
			return;
		}
		this.knb.getJda().retrieveUserById(ctx.getUserInstance().getAdderId()).queue(user -> {
			final KdecoleUserInstance existing = this.knb.getTableInstances()
					.filter(r.hashMap("channelId", toChan.getId()))
					.run(this.knb.getDbCon(), KdecoleUserInstance.class)
					.first();

			if (existing != null) {
				ctx.getEvent().getChannel().sendMessage("Il existe déjà une intégration dans le salon cible").queue();
				return;
			}

			this.knb.getTableInstances()
					.get(ctx.getUserInstance().getId())
					.update(r.hashMap("channelId", toChan.getId()))
					.run(this.knb.getDbCon());
			ctx.getEvent()
					.getChannel()
					.sendMessage("Les notifications seront envoyées dans le salon " + toChan.getName())
					.queue();
			toChan.sendMessage("Les notifications seront maintenant envoyées dans ce salon").queue();
		});
	}

	@Override
	public boolean isAuthenticatedCommand() {
		return true;
	}

	@Override
	public String getSyntax() {
		return "move <channel id>";
	}

	@Override
	public String getHelp() {
		return "Déplacer l'intégration vers un autre channel";
	}
}
