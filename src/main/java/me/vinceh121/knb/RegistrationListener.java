package me.vinceh121.knb;

import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.client.model.Filters;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.MentionType;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class RegistrationListener extends ListenerAdapter {
	private static final Logger LOG = LoggerFactory.getLogger(RegistrationListener.class);
	private static final String OWNER = "340473152259751936";
	private static final Pattern REGEX_AUTH = Pattern.compile("[0-9]+:[a-zA-Z0-9._-]+:[a-zA-Z0-9]+");
	private final Knb knb;

	public RegistrationListener(final Knb knb) {
		this.knb = knb;
	}

	@Override
	public void onGuildJoin(final GuildJoinEvent event) {
		final Guild g = event.getGuild();
		RegistrationListener.LOG.info("Added to guild: " + g);
		try {
			g.getDefaultChannel()
					.sendMessage("Merci d'utiliser Kdecole Notification Bot!"
							+ "\nSi vous êtes administrateur envoyez moi un message privé pour m'initialiser")
					.queue();
		} catch (final InsufficientPermissionException e) {} // Fail silently on no perm

		final UserInstance ui = new UserInstance();
		ui.setGuildId(event.getGuild().getId());
		ui.setStage(Stage.ADDED);
		this.knb.addUserInstance(ui);
	}

	@Override
	public void onPrivateMessageReceived(final PrivateMessageReceivedEvent event) {
		final User author = event.getAuthor();
		if (author.getId().equals(this.knb.getJda().getSelfUser().getId())) {
			return;
		}

		final PrivateChannel channel = event.getChannel();

		if (author.isBot()) { // XXX probably a bad idea
			channel.sendMessage("Beep boop beep?").queue();
			return;
		}

		final String content = event.getMessage().getContentRaw();

		if (author.getId().equals(OWNER) && !content.contains(":")) {
			try {
				if (content.equals("trigger")) {
					this.knb.manualTriggerAll();
					channel.sendMessage("triggered").queue();
				}
			} catch (final Exception e) {
				channel.sendMessage(e.toString()).queue();
			}
			return;
		}

		if (!REGEX_AUTH.matcher(content).matches()) {
			channel.sendMessage("Pour m'enregistrer, envoyez moi un message de la forme `idguild:username:jeton`\n"
					+ "Pour obtenir un jeton, aller dans les préférences de votre ENT, puis dans l'onglet 'Application Mobile'\n"
					+ "Votre nom d'utilisateur, ni votre jeton sont sauvegardés.\n"
					+ "Pour obenir id de votre guild, veulliez activer le mode développeur de Discord,"
					+ "puis faire un clic droit, copier ID sur votre serveur.").queue();
			return;
		}

		final String[] parts = content.split(":");
		final String guildId = parts[0];
		final String username = parts[1];
		final String password = parts[2];

		final Guild guild = this.knb.getJda().getGuildById(guildId);

		final UserInstance ui
				= this.knb.getUserInstance(Filters.and(Filters.eq("stage", "ADDED"), Filters.eq("guildId", guildId)));

		if (ui == null) {
			channel.sendMessage(
					"Il est possible que le bot soit déjà installé sur ce serveur, ou alors qu'il n'y soit pas ajouté.")
					.queue();
			return;
		}

		if (guild == null) {
			channel.sendMessage("Nous n'avons pas trouvé de server avec l'ID " + guildId).queue();
			return;
		}

		ui.setAdderId(author.getId());

		RegistrationListener.LOG.info("Setting up instance " + ui);

		channel.sendMessage("Nous somment entrain de vérifier si Kdecole va être gentil...").queue();

		this.knb.setupUserInstance(ui, username, password).thenAccept(msg -> {
			channel.sendMessage(msg).queue();
		});
	}

	@Override
	public void onGuildMessageReceived(final GuildMessageReceivedEvent event) {
		final Message msg = event.getMessage();
		final TextChannel channel = event.getChannel();
		if (!msg.isMentioned(this.knb.getJda().getSelfUser(), MentionType.USER)) {
			return;
		}

		final Member member = event.getMember();

		final UserInstance ui = this.knb.getUserInstance(Filters.and(Filters.eq("guildId", event.getGuild().getId()),
				Filters.or(Filters.eq("stage", "CHANNEL"), Filters.eq("stage", "REGISTERED"))));
		if (ui == null) {
			channel.sendMessage("On dirait que vous n'avez pas encore installé le bot."
					+ "Envoyez moi un message privé pour commencer.").queue();
			return;
		}

		if (member.hasPermission(Permission.MANAGE_CHANNEL)) {
			ui.setChannelId(channel.getId());
			ui.setStage(Stage.REGISTERED);
			this.knb.updateUserInstance(ui);
			channel.sendMessage("Je vais maintenant envoyer mes notifications dans " + channel.getAsMention()).queue();
		} else {
			channel.sendMessage("On dirait que vous n'avez pas la permission `Gèrer les cannaux` dans ce serveur.")
					.queue();
		}

	}

	@Override
	public void onGuildLeave(final GuildLeaveEvent event) {
		LOG.info("Bot removed from guild " + event.getGuild());
		this.knb.removeGuild(event.getGuild().getId());
	}
}
