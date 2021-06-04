package me.vinceh121.knb.commands;

import me.vinceh121.knb.AbstractCommand;
import me.vinceh121.knb.CommandContext;
import me.vinceh121.knb.Knb;
import me.vinceh121.knb.RelayType;
import net.dv8tion.jda.api.EmbedBuilder;
import static com.rethinkdb.RethinkDB.r;

public class CmdRelay extends AbstractCommand {

	public CmdRelay(final Knb knb) {
		super(knb);
	}

	@Override
	public boolean validateSyntax(final CommandContext ctx) {
		if (ctx.getArgs().size() == 0) {
			return true;
		}
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
			ctx.getUserInstance().getRelays().remove(type);
		} else {
			ctx.getUserInstance().getRelays().add(type);
		}
		this.knb.getTableInstances()
				.update(r.hashMap("relays", ctx.getUserInstance().getRelays()))
				.run(this.knb.getDbCon());

		this.printCurrent(ctx);
	}

	private void printCurrent(final CommandContext ctx) {
		final EmbedBuilder build = new EmbedBuilder();
		build.setTitle("Relais");
		for (final RelayType r : RelayType.values()) {
			build.addField(r.name(), ctx.getUserInstance().getRelays().contains(r) ? "ON" : "OFF", true);
		}
		ctx.getEvent().getChannel().sendMessage(build.build()).queue();
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
