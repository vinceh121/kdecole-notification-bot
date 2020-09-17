package me.vinceh121.knb.commands;

import me.vinceh121.knb.AbstractCommand;
import me.vinceh121.knb.CommandContext;
import me.vinceh121.knb.Knb;

public class CmdPrivacy extends AbstractCommand {

	public CmdPrivacy(final Knb knb) {
		super(knb);
	}

	@Override
	protected void executeSync(final CommandContext ctx) {
		ctx.getEvent().getChannel().sendMessage("Privacy policy: https://knb.vinceh121.me/privacy-policy/").queue();
	}

	@Override
	public String getHelp() {
		return "Lisez notre politique de confidentialité";
	}
}
