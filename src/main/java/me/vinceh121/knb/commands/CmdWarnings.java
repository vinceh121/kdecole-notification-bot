package me.vinceh121.knb.commands;

import static com.rethinkdb.RethinkDB.r;

import me.vinceh121.knb.AbstractCommand;
import me.vinceh121.knb.CommandContext;
import me.vinceh121.knb.Knb;

public class CmdWarnings extends AbstractCommand {

	public CmdWarnings(final Knb knb) {
		super(knb);
	}

	@Override
	protected void executeSync(final CommandContext ctx) {
		this.knb.getTableKdecoleInstances()
				.get(ctx.getUserInstance().getId())
				.update(r.hashMap("showWarnings", true))
				.run(this.knb.getDbCon());
		ctx.getEvent().getChannel().sendMessage("Les avertissements seront affichés").queue();
	}

	@Override
	public boolean isAuthenticatedCommand() {
		return true;
	}

	@Override
	public String getHelp() {
		return "Réactive l'envoie d'avertissements en cas d'échec sur la récupération";
	}
}
