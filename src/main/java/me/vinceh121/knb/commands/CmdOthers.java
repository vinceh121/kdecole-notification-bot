package me.vinceh121.knb.commands;

import static com.rethinkdb.RethinkDB.r;

import me.vinceh121.knb.AbstractCommand;
import me.vinceh121.knb.CommandContext;
import me.vinceh121.knb.Knb;

public class CmdOthers extends AbstractCommand {

	public CmdOthers(final Knb knb) {
		super(knb);
	}

	@Override
	protected void executeSync(final CommandContext ctx) {
		this.knb.getTableKdecoleInstances()
				.get(ctx.getUserInstance().getId())
				.update(r.hashMap("allowOthers", !ctx.getUserInstance().isAllowOthers()))
				.run(this.knb.getDbCon());

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

	@Override
	public String getHelp() {
		return "Autorise/Interdit l'accès en modification aux autre modérateurs du serveur à cette intégration";
	}
}
