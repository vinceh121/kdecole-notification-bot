package me.vinceh121.knb.commands;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import me.vinceh121.knb.AbstractCommand;
import me.vinceh121.knb.CommandContext;
import me.vinceh121.knb.Knb;
import me.vinceh121.knb.RelayType;

public class CmdRelay extends AbstractCommand {

	public CmdRelay(Knb knb) {
		super(knb);
	}

	@Override
	public boolean validateSyntax(CommandContext ctx) {
		if (ctx.getArgs().size() == 0)
			return true;
		if (ctx.getArgs().size() >= 1) {
			try {
				RelayType.valueOf(ctx.getArgs().get(0).toUpperCase());
			} catch (final IllegalArgumentException e) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected void executeSync(final CommandContext ctx) {
		if (ctx.getArgs().size() == 0) {
			this.printCurrent(ctx);
			return;
		}

		final RelayType type = RelayType.valueOf(ctx.getArgs().get(0).toUpperCase());

		if (ctx.getUserInstance().getRelays().contains(type)) {
			this.knb.getColInstances()
					.updateOne(Filters.eq(ctx.getUserInstance().getId()), Updates.pull("relays", type.name()));
			ctx.getUserInstance().getRelays().remove(type);
		} else {
			this.knb.getColInstances()
					.updateOne(Filters.eq(ctx.getUserInstance().getId()), Updates.push("relays", type.name()));
			ctx.getUserInstance().getRelays().add(type);
		}

		this.printCurrent(ctx);
	}

	private void printCurrent(final CommandContext ctx) {
		final StringBuilder sb = new StringBuilder();
		sb.append("```sas\n");
		for (final RelayType r : RelayType.values()) {
			if (ctx.getUserInstance().getRelays().contains(r)) {
				sb.append("%" + r.name());
			} else {
				sb.append("&" + r.name());
			}
			sb.append("\n");
		}
		sb.append("\n\n\nLégende:\n");
		sb.append("%active\n");
		sb.append("&désactive\n");
		sb.append("```\n");
		ctx.getEvent().getChannel().sendMessage(sb.toString()).queue();
	}

	@Override
	public boolean isAuthenticatedCommand() {
		return true;
	}

	@Override
	public String getHelp() {
		return "Active ou désactive les différents relais";
	}
}
