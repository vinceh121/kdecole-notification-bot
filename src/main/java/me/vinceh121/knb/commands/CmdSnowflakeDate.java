package me.vinceh121.knb.commands;

import java.util.Date;

import me.vinceh121.knb.AbstractCommand;
import me.vinceh121.knb.CommandContext;
import me.vinceh121.knb.Knb;

public class CmdSnowflakeDate extends AbstractCommand {

	public CmdSnowflakeDate(final Knb knb) {
		super(knb);
	}

	@Override
	public boolean validateSyntax(final CommandContext ctx) {
		if (ctx.getArgs().size() != 1) {
			return false;
		}
		try {
			Long.parseLong(ctx.getArgs().get(0));
		} catch (final NumberFormatException e) {
			return false;
		}
		return true;
	}

	@Override
	protected void executeSync(final CommandContext ctx) {
		final long snowflake = Long.parseLong(ctx.getArgs().get(0));
		final long time = (snowflake >> 22L) + 1420070400000L;
		final Date date = new Date(time);

		ctx.getEvent().getChannel().sendMessage("Time: " + time + "\nDate: " + date).queue();
	}
}
