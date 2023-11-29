package me.vinceh121.knb.commands;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.vinceh121.knb.AbstractCommand;
import me.vinceh121.knb.CommandContext;
import me.vinceh121.knb.Knb;
import me.vinceh121.knb.KdecoleUserInstance;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;

public class CmdAnouncement extends AbstractCommand {
	private static final Logger LOG = LogManager.getLogger(CmdAnouncement.class);

	public CmdAnouncement(final Knb knb) {
		super(knb);
	}

	@Override
	protected void executeSync(final CommandContext ctx) {
		final String text = ":loudspeaker: Annonce :loudspeaker:\n" + String.join(" ", ctx.getArgs());
		CmdAnouncement.LOG.info("Making announcement {}", text);

		this.knb.getTableInstances().hasFields("adderId").run(this.knb.getDbCon(), KdecoleUserInstance.class).forEach(ui -> {
			try {
				final User user = this.knb.getJda().retrieveUserById(ui.getAdderId()).complete();
				final PrivateChannel priv = user.openPrivateChannel().complete();
				CmdAnouncement.LOG.info("Sending annoucement to {}", user);
				priv.sendMessage(text).complete();
			} catch (final Exception e) {
				CmdAnouncement.LOG.error("Failed to send announcement to instance " + ui, e);
				ctx.getEvent()
						.getChannel()
						.sendMessage("Failed to send announcement to instance " + ui + ": " + e)
						.queue();
			}
		});
	}

	@Override
	public boolean isAdminCommand() {
		return true;
	}
}
