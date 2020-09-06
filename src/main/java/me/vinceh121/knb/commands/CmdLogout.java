package me.vinceh121.knb.commands;

import com.mongodb.client.model.Filters;

import me.vinceh121.knb.AbstractCommand;
import me.vinceh121.knb.CommandContext;
import me.vinceh121.knb.Knb;

public class CmdLogout extends AbstractCommand {

	public CmdLogout(final Knb knb) {
		super(knb);
	}

	@Override
	protected void executeSync(final CommandContext ctx) {
		this.knb.getColInstances().deleteOne(Filters.eq(ctx.getUserInstance().getId()));
		ctx.getEvent().getChannel().sendMessage("Votre intégration de ce salon a était supprimé.").queue();
	}

	@Override
	public String getHelp() {
		return "Se déconnecter";
	}

	@Override
	public boolean isAuthenticatedCommand() {
		return true;
	}
}
