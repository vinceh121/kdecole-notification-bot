package me.vinceh121.knb.commands;

import java.util.Arrays;

import me.vinceh121.knb.AbstractCommand;
import me.vinceh121.knb.CommandContext;
import me.vinceh121.knb.Knb;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Activity.ActivityType;

public class CmdSetPlaying extends AbstractCommand {

	public CmdSetPlaying(final Knb knb) {
		super(knb);
	}

	@Override
	public boolean validateSyntax(final CommandContext ctx) {
		if (ctx.getArgs().size() < 2) {
			return false;
		}
		try {
			ActivityType.valueOf(ctx.getArgs().get(0));
		} catch (final IllegalArgumentException e) {
			return false;
		}
		return true;
	}

	@Override
	protected void executeSync(final CommandContext ctx) {
		String streamUrl;
		try {
			streamUrl = ctx.getArgs().get(2);
		} catch (final IndexOutOfBoundsException e) {
			streamUrl = "";
		}

		try {
			knb.getJda()
					.getPresence()
					.setActivity(
							Activity.of(ActivityType.valueOf(ctx.getArgs().get(0)), ctx.getArgs().get(1), streamUrl));

			ctx.getEvent().getChannel().sendMessage("Set playing status").queue();
		} catch (final IllegalArgumentException e) {
			ctx.getEvent().getChannel().sendMessage("Error while setting playing status: " + e).queue();
		}
	}

	@Override
	public boolean isAdminCommand() {
		return true;
	}

	@Override
	public String getSyntax() {
		return "setplaying <" + Arrays.toString(ActivityType.values()) + "> <name> [stream url]";
	}

}
