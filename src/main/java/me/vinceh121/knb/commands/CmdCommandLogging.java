package me.vinceh121.knb.commands;

import me.vinceh121.knb.AbstractCommand;
import me.vinceh121.knb.CommandContext;
import me.vinceh121.knb.Knb;

public class CmdCommandLogging extends AbstractCommand {

	public CmdCommandLogging(Knb knb) {
		super(knb);
	}

	@Override
	protected void executeSync(CommandContext ctx) {
		this.knb.getRegisListener().setLogCommands(!this.knb.getRegisListener().isLogCommands());

		if (this.knb.getRegisListener().isLogCommands()) {
			ctx.getEvent().getChannel().sendMessage(":shield: Now logging commands").queue();
		} else {
			ctx.getEvent().getChannel().sendMessage(":shield: Not logging commands anymore").queue();
		}
	}

	@Override
	public boolean isAdminCommand() {
		return true;
	}
}
