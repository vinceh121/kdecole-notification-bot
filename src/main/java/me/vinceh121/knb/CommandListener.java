package me.vinceh121.knb;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.client.model.Filters;

import io.prometheus.client.Counter;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.MentionType;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CommandListener extends ListenerAdapter {
	private static final Logger LOG = LoggerFactory.getLogger(CommandListener.class);
	private static final Counter METRICS_COMMANDS = Counter.build("knb_cmds", "Counts all command calls").register();
	private final Knb knb;
	private final Map<String, AbstractCommand> map;

	public CommandListener(final Knb knb) {
		this(knb, new Hashtable<>());
	}

	public CommandListener(final Knb knb, final Map<String, AbstractCommand> map) {
		this.knb = knb;
		this.map = map;
	}

	@Override
	public void onGuildJoin(final GuildJoinEvent event) {
		final Guild g = event.getGuild();
		CommandListener.LOG.info("Added to guild: " + g);
		try {
			g.getDefaultChannel()
					.sendMessage("Merci d'utiliser Kdecole Notification Bot!\n" + "Pingez moi pour m'initialiser")
					.queue();
		} catch (final InsufficientPermissionException e) {} // Fail silently on no perm
	}

	@Override
	public void onGuildMessageReceived(final GuildMessageReceivedEvent event) {
		final Message msg = event.getMessage();
		if (!msg.isMentioned(this.knb.getJda().getSelfUser(), MentionType.USER)) {
			return;
		}

		final List<String> args = new Vector<>(Arrays.asList(msg.getContentRaw().split("[\\s]+")));

		args.remove(0); // remove ping
		final String rawCmd = args.remove(0).toLowerCase(); // remove and retrieve called cmd

		final AbstractCommand cmd = this.map.get(rawCmd);

		if (cmd == null) {
			return;
		}

		METRICS_COMMANDS.inc();

		if (cmd.isAdminCommand() && !knb.isUserAdmin(event.getAuthor().getIdLong())) {
			event.getChannel().sendMessage("Vous devez être admin du bot pour utiliser cette commande").queue();
			return;
		}

		final CommandContext ctx = new CommandContext();
		ctx.setArgs(args);
		ctx.setEvent(event);

		if (!cmd.validateSyntax(ctx)) {
			event.getChannel().sendMessage("Syntaxe invalide: " + cmd.getSyntax()).queue();
			return;
		}

		if (cmd.isAuthenticatedCommand()) {
			final UserInstance ui = knb.getUserInstance(Filters.eq("channelId", event.getChannel().getId()));
			if (ui == null) {
				event.getChannel().sendMessage("Il n'y a pas d'intégration dans ce canal").queue();
				return;
			}
			if (!ui.isAllowOthers() && !event.getAuthor().getId().equals(ui.getAdderId())) {
				event.getChannel().sendMessage("Seul l'auteur de l'intégration peut utiliser cette commande").queue();
				return;
			}
			if (!event.getMember().hasPermission(event.getChannel(), Permission.MANAGE_CHANNEL)) {
				event.getChannel()
						.sendMessage("Vous devez avoir la permission `Gérer les salons` pour utiliser cette commande")
						.queue();
				return;
			}
			ctx.setUserInstance(ui);
		}

		cmd.execute(ctx).exceptionally(t -> {
			LOG.error("Unhandeled error with command " + rawCmd, t);
			event.getChannel().sendMessage("Erreur inattendue dans l'éxécution de la commande").queue();
			return null;
		});
	}

	@Override
	public void onGuildLeave(final GuildLeaveEvent event) {
		LOG.info("Bot removed from guild " + event.getGuild());
		this.knb.removeGuild(event.getGuild().getId());
	}
}
