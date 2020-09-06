package me.vinceh121.knb.commands;

import com.mongodb.client.model.Filters;

import me.vinceh121.knb.AbstractCommand;
import me.vinceh121.knb.CommandContext;
import me.vinceh121.knb.Knb;
import me.vinceh121.knb.UserInstance;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

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
		final TextChannel chan = ctx.getEvent().getChannel();
		final Member mem = ctx.getEvent().getMember();

		if (!mem.hasPermission(chan, Permission.MANAGE_CHANNEL)) {
			chan.sendMessage("Vous devez avoir la permission `Gérer les salons` pour installer le bot").queue();
			return;
		}

		UserInstance ui = this.knb.getUserInstance(Filters.eq("channelId", chan.getId()));

		if (ui != null) {
			chan.sendMessage("Ce channel est déjà en cours d'utilisation par le bot").queue();
			return;
		}

		ui = new UserInstance();
		ui.setAdderId(mem.getId());
		ui.setChannelId(chan.getId());
		ui.setGuildId(chan.getGuild().getId());

		this.knb.setupUserInstance(ui, ctx.getArgs().get(0), ctx.getArgs().get(1)).handleAsync((info, t) -> {
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
