package me.vinceh121.knb.commands;

import me.vinceh121.knb.AbstractCommand;
import me.vinceh121.knb.CommandContext;
import me.vinceh121.knb.Knb;
import net.dv8tion.jda.api.entities.User;

public class CmdAdmins extends AbstractCommand {

	public CmdAdmins(final Knb knb) {
		super(knb);
	}

	@Override
	protected void executeSync(final CommandContext ctx) {
		final StringBuilder sb = new StringBuilder();
		sb.append(":shield: Administrateurs :\n");
		for (final long id : this.knb.getConfig().getAdmins()) {
			final User user = this.knb.getJda().retrieveUserById(id).complete();
			sb.append("\t");
			sb.append(user == null ? "<@" + id + ">" : user.getAsTag() + "\n");
		}
		ctx.getEvent().getChannel().sendMessage(sb.toString()).queue();
	}

	@Override
	public String getHelp() {
		return "Donne la liste des administrateurs du bot";
	}
}
