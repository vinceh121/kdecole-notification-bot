package me.vinceh121.knb.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.vinceh121.knb.AbstractCommand;
import me.vinceh121.knb.CommandContext;
import me.vinceh121.knb.Knb;

public class CmdHelp extends AbstractCommand {

	public CmdHelp(final Knb knb) {
		super(knb);
	}

	@Override
	protected void executeSync(final CommandContext ctx) {
		final StringBuilder sb = new StringBuilder();
		sb.append("```lisp\n");

		final List<AbstractCommand> cmds = new ArrayList<>(this.knb.getCmdMap().values());
		Collections.sort(cmds, (o1, o2) -> o1.getName().compareTo(o2.getName()));

		for (final AbstractCommand cmd : cmds) {
			sb.append("(");
			sb.append(cmd.getName());
			sb.append(")\t\t\t\t");
			if (cmd.isAdminCommand()) {
				sb.append("(#)");
			}
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
