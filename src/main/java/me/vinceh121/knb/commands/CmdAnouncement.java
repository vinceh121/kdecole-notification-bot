package me.vinceh121.knb.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.client.model.Filters;

import me.vinceh121.knb.AbstractCommand;
import me.vinceh121.knb.CommandContext;
import me.vinceh121.knb.Knb;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;

public class CmdAnouncement extends AbstractCommand {
	private static final Logger LOG = LoggerFactory.getLogger(CmdAnouncement.class);

	public CmdAnouncement(final Knb knb) {
		super(knb);
	}

	@Override
	protected void executeSync(final CommandContext ctx) {
		final String text = ":loudspeaker: Annonce :loudspeaker:\n" + String.join(" ", ctx.getArgs());
		LOG.info("Making announcement {}", text);

		this.knb.getColInstances().find(Filters.exists("adderId")).forEach(ui -> { // YES everything is sync here
			try {
				final User user = this.knb.getJda().retrieveUserById(ui.getAdderId()).complete();
				final PrivateChannel priv = user.openPrivateChannel().complete();
				LOG.info("Sending annoucement to {}", user);
				priv.sendMessage(text).complete();
			} catch (final Exception e) {
				LOG.error("Failed to send announcement to instance " + ui, e);
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
