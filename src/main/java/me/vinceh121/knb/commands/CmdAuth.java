package me.vinceh121.knb.commands;

import static com.rethinkdb.RethinkDB.r;

import java.util.List;

import me.vinceh121.jkdecole.Endpoints;
import me.vinceh121.knb.AbstractCommand;
import me.vinceh121.knb.CommandContext;
import me.vinceh121.knb.Knb;
import me.vinceh121.knb.KdecoleUserInstance;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

public class CmdAuth extends AbstractCommand {

	public CmdAuth(final Knb knb) {
		super(knb);
	}

	@Override
	public boolean validateSyntax(final CommandContext ctx) {
		return ctx.getArgs().size() >= 2;
	}

	@Override
	protected void executeSync(final CommandContext ctx) {
		final GuildMessageChannel chan = ctx.getEvent().getChannel().asGuildMessageChannel();
		final Member mem = ctx.getEvent().getMember();

		if (!mem.hasPermission(chan, Permission.MANAGE_CHANNEL)) {
			chan.sendMessage("Vous devez avoir la permission `Gérer les salons` pour installer le bot").queue();
			return;
		}

		KdecoleUserInstance ui = this.knb.getTableKdecoleInstances()
				.filter(r.hashMap("channelId", chan.getId()))
				.run(this.knb.getDbCon(), KdecoleUserInstance.class)
				.first();

		if (ui != null) {
			chan.sendMessage("Ce channel est déjà en cours d'utilisation par le bot").queue();
			return;
		}

		final List<String> endpoints = Endpoints.getEndpoints(ctx.getArgs().get(1));

		final String endpoint;
		if (ctx.getArgs().size() >= 3) {
			endpoint = ctx.getArgs().get(2);
		} else if (endpoints.size() == 1) {
			endpoint = endpoints.get(0);
		} else if (endpoints.size() > 1) {
			final StringBuilder sb = new StringBuilder("Votre MDP correspond a plusieurs instances Kdecole. ; \n"
					+ "Reenvoyez la commande `auth` avec l'URL qui correspond a "
					+ "celui de votre instance.\n\n");
			for (int i = 0; i < endpoints.size(); i++) {
				sb.append(i + "\t<" + endpoints.get(i) + ">\n");
			}
			ctx.getEvent().getChannel().sendMessage(sb.toString()).queue();
			return;
		} else {
			ctx.getEvent()
					.getChannel()
					.sendMessage("Votre mot-de-passe n'a pas permi d'identifier une instance Kdecole.\n"
							+ "Essayez la commande `endpoints` pour voir quelles instances sont acceptées, ou alors, "
							+ "spécifiez l'URL d'accés mobile de votre ENT")
					.queue();
			return;
		}

		ui = new KdecoleUserInstance();
		ui.setAdderId(mem.getId());
		ui.setChannelId(chan.getId());
		ui.setGuildId(chan.getGuild().getId());

		this.knb.setupUserInstance(ui, ctx.getArgs().get(0), ctx.getArgs().get(1), endpoint).handleAsync((info, t) -> {
			if (t != null) {
				chan.sendMessage("Kdecole n'est pas gentil: " + t.getMessage()).queue();
				return null;
			}

			chan.sendMessage("Connecté en tant que "
					+ info.getNom()
					+ "\n"
					+ "Vous pouvez maintenant configurer votre intégration").queue();
			return null;
		});
	}

	@Override
	public String getHelp() {
		return "S'authetifier a Kdecole";
	}

	@Override
	public String getSyntax() {
		return "auth <username> <jeton mobile> [index d'endpoint]";
	}

}
