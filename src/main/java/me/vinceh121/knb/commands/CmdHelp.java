package me.vinceh121.knb.commands;

import me.vinceh121.knb.AbstractCommand;
import me.vinceh121.knb.CommandContext;
import me.vinceh121.knb.Knb;

public class CmdHelp extends AbstractCommand {

	public CmdHelp(Knb knb) {
		super(knb);
	}

	@Override
	protected void executeSync(final CommandContext ctx) {
		final StringBuilder sb = new StringBuilder();
		sb.append("```lisp\n");
		for (final AbstractCommand cmd : this.knb.getCmdMap().values()) {
			sb.append("(");
			sb.append(cmd.getName());
			sb.append(")\t\t\t\t");
			if (cmd.isAdminCommand())
				sb.append("(#)");
			sb.append(cmd.getHelp());
			sb.append("\n");
		}
		sb.append("```\n");
		ctx.getEvent().getChannel().sendMessage(sb.toString()).queue();
	}

	@Override
	public String getHelp() {
		return "Ceci";
	}
}
