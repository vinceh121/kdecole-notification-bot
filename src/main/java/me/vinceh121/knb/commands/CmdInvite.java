package me.vinceh121.knb.commands;

import me.vinceh121.knb.AbstractCommand;
import me.vinceh121.knb.CommandContext;
import me.vinceh121.knb.Knb;

public class CmdInvite extends AbstractCommand {

	public CmdInvite(Knb knb) {
		super(knb);
	}

	@Override
	protected void executeSync(CommandContext ctx) {
		ctx.getEvent()
				.getChannel()
				.sendMessage(
						"https://discordapp.com/oauth2/authorize?client_id=691655008076300339&scope=bot&permissions=18432")
				.queue();
	}

}
