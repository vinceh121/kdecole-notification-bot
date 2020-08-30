package me.vinceh121.knb.commands;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import me.vinceh121.knb.AbstractCommand;
import me.vinceh121.knb.CommandContext;
import me.vinceh121.knb.Knb;

public class CmdWarnings extends AbstractCommand {

	public CmdWarnings(final Knb knb) {
		super(knb);
	}

	@Override
	protected void executeSync(final CommandContext ctx) {
		knb.getColInstances().updateOne(Filters.eq(ctx.getUserInstance().getId()), Updates.set("showWarnings", true));
		ctx.getEvent().getChannel().sendMessage("Les avertissements seront affichés").queue();
	}

	@Override
	public boolean isAuthenticatedCommand() {
		return true;
	}
}
