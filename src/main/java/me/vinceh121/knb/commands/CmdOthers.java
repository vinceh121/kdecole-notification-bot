package me.vinceh121.knb.commands;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import me.vinceh121.knb.AbstractCommand;
import me.vinceh121.knb.CommandContext;
import me.vinceh121.knb.Knb;

public class CmdOthers extends AbstractCommand {

	public CmdOthers(final Knb knb) {
		super(knb);
	}

	@Override
	protected void executeSync(final CommandContext ctx) {
		this.knb.getColInstances()
				.updateOne(Filters.eq(ctx.getUserInstance().getId()),
						Updates.set("allowOthers", !ctx.getUserInstance().isAllowOthers()));

		if (ctx.getUserInstance().isAllowOthers()) {
			ctx.getEvent()
					.getChannel()
					.sendMessage("Les autres administrateurs du serveur ne peuvent plus configurer cette intégration, "
							+ "uniquement la personne qui l'a ajoutée")
					.queue();
		} else {
			ctx.getEvent()
					.getChannel()
					.sendMessage(
							"Les autres administrateurs du serveur peuvent maintenant configurer cette intégration")
					.queue();
		}
	}

	@Override
	public boolean isAuthenticatedCommand() {
		return true;
	}

}
