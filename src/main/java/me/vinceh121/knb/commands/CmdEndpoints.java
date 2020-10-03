package me.vinceh121.knb.commands;

import java.util.Map;

import me.vinceh121.jkdecole.Endpoints;
import me.vinceh121.knb.AbstractCommand;
import me.vinceh121.knb.CommandContext;
import me.vinceh121.knb.Knb;

public class CmdEndpoints extends AbstractCommand {

	public CmdEndpoints(final Knb knb) {
		super(knb);
	}

	@Override
	protected void executeSync(final CommandContext ctx) {
		final StringBuilder sb = new StringBuilder("```\nPréfixe de MDP\t\tURL d'endpoint mobile");

		for (final Map.Entry<String, String> e : Endpoints.getEndpoints().entries()) {
			sb.append("\n" + e.getKey() + "\t\t\t\t\t" + e.getValue());
		}

		sb.append("\n```");
		
		ctx.getEvent().getChannel().sendMessage(sb.toString()).queue();
	}

}
