package me.vinceh121.knb;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
					.sendMessage("Merci d'utiliser Kdecole Notification Bot!\n" + "Pingez pour m'initialiser")
					.queue();
		} catch (final InsufficientPermissionException e) {} // Fail silently on no perm
	}

	/*
	 * @Override
	 * public void onPrivateMessageReceived(final PrivateMessageReceivedEvent event)
	 * {
	 * final User author = event.getAuthor();
	 * if (author.getId().equals(this.knb.getJda().getSelfUser().getId())) {
	 * return;
	 * }
	 * 
	 * final PrivateChannel channel = event.getChannel();
	 * 
	 * if (author.isBot()) { // XXX probably a bad idea
	 * channel.sendMessage("Beep boop beep?").queue();
	 * return;
	 * }
	 * 
	 * final String content = event.getMessage().getContentRaw();
	 * 
	 * if (this.knb.isUserAdmin(author.getIdLong()) && !content.contains(":") &&
	 * content.equals("trigger")) {
	 * try {
	 * this.knb.manualTriggerAll();
	 * channel.sendMessage(
	 * "https://cdn.discordapp.com/attachments/579635091894960147/749352437726445729/unknown.png")
	 * .queue();
	 * } catch (final Exception e) {
	 * channel.sendMessage(e.toString()).queue();
	 * }
	 * return;
	 * }
	 * 
	 * if (!REGEX_AUTH.matcher(content).matches()) {
	 * channel.
	 * sendMessage("Pour m'enregistrer, envoyez moi un message de la forme `idguild:username:jeton`\n"
	 * +
	 * "Pour obtenir un jeton, aller dans les préférences de votre ENT, puis dans l'onglet 'Application Mobile'\n"
	 * + "Votre nom d'utilisateur, ni votre jeton sont sauvegardés.\n"
	 * +
	 * "Un cookie côté server sera enregistrer pour identifier votre compte ENT.\n"
	 * +
	 * "Pour obtenir l'id de votre guild, veulliez activer le mode développeur de Discord,"
	 * + "puis faire un clic droit, copier ID sur votre serveur.").queue();
	 * return;
	 * }
	 * 
	 * final String[] parts = content.split(":");
	 * final String guildId = parts[0];
	 * final String username = parts[1];
	 * final String password = parts[2];
	 * 
	 * final Guild guild = this.knb.getJda().getGuildById(guildId);
	 * 
	 * final UserInstance ui
	 * = this.knb.getUserInstance(Filters.and(Filters.eq("stage", "ADDED"),
	 * Filters.eq("guildId", guildId)));
	 * 
	 * if (ui == null) {
	 * channel.sendMessage(
	 * "Il est possible que le bot soit déjà installé sur ce serveur, ou alors qu'il n'y soit pas ajouté."
	 * )
	 * .queue();
	 * return;
	 * }
	 * 
	 * if (guild == null) {
	 * channel.sendMessage("Nous n'avons pas trouvé de server avec l'ID " +
	 * guildId).queue();
	 * return;
	 * }
	 * 
	 * ui.setAdderId(author.getId());
	 * 
	 * CommandListener.LOG.info("Setting up instance {}", ui.getId());
	 * 
	 * channel.
	 * sendMessage("Nous somment entrain de vérifier si Kdecole va être gentil...").
	 * queue();
	 * 
	 * this.knb.setupUserInstance(ui, username, password).thenAccept(msg -> {
	 * channel.sendMessage(msg).queue();
	 * });
	 * }
	 */
	@Override
	public void onGuildMessageReceived(final GuildMessageReceivedEvent event) {
		final Message msg = event.getMessage();
		if (!msg.isMentioned(this.knb.getJda().getSelfUser(), MentionType.USER)) {
			return;
		}

		final List<String> args = Arrays.asList(msg.getContentRaw().split("[\\s]+"));

		args.remove(0); // remove ping
		final String rawCmd = args.remove(0).toLowerCase(); // remove and retrieve called cmd

		final AbstractCommand cmd = this.map.get(rawCmd);

		if (cmd == null) {
			return;
		}

		if (cmd.isAdminCommand() && !knb.isUserAdmin(event.getAuthor().getIdLong())) {
			event.getChannel().sendMessage("Vous devez être admin du bot pour utiliser cette commande").queue();
			return;
		}

		final CommandContext ctx = new CommandContext();
		ctx.setArgs(args);
		ctx.setEvent(event);

		if (!cmd.validateSyntax(ctx)) {
			event.getChannel().sendMessage("Syntaxe invalide: " + cmd.getHelp());
		}

		cmd.execute(ctx).exceptionally(t -> {
			event.getChannel().sendMessage("Erreur inattendue dans l'éxécution de la commande").queue();
			LOG.error("Unhandeled error with command " + rawCmd, t);
			return null;
		});
	}

	@Override
	public void onGuildLeave(final GuildLeaveEvent event) {
		LOG.info("Bot removed from guild " + event.getGuild());
		this.knb.removeGuild(event.getGuild().getId());
	}
}
