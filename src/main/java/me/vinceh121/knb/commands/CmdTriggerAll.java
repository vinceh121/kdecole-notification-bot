package me.vinceh121.knb.commands;

import org.quartz.SchedulerException;

import me.vinceh121.knb.AbstractCommand;
import me.vinceh121.knb.CommandContext;
import me.vinceh121.knb.Knb;

public class CmdTriggerAll extends AbstractCommand {

	public CmdTriggerAll(final Knb knb) {
		super(knb);
	}

	@Override
	protected void executeSync(final CommandContext ctx) {
		try {
			this.knb.manualTriggerAll();
			ctx.getEvent()
					.getChannel()
					.sendMessage(
							"https://cdn.discordapp.com/attachments/579635091894960147/749352437726445729/unknown.png")
					.queue();
		} catch (final SchedulerException e) {
			ctx.getEvent().getChannel().sendMessage("Error with manual trigger: " + e);
		}
	}

	@Override
	public boolean isAdminCommand() {
		return true;
	}
}
