package me.vinceh121.knb.commands;

import me.vinceh121.knb.AbstractCommand;
import me.vinceh121.knb.CommandContext;
import me.vinceh121.knb.Knb;

public class CmdLogout extends AbstractCommand {

	public CmdLogout(final Knb knb) {
		super(knb);
	}

	@Override
	protected void executeSync(final CommandContext ctx) {
		this.knb.getTableInstances().get(ctx.getUserInstance().getId()).delete().run(this.knb.getDbCon());
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
