package me.vinceh121.knb.commands;

import me.vinceh121.knb.AbstractCommand;
import me.vinceh121.knb.CommandContext;
import me.vinceh121.knb.Knb;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

public class CmdFeedback extends AbstractCommand {

	public CmdFeedback(Knb knb) {
		super(knb);
	}

	@Override
	public boolean validateSyntax(CommandContext ctx) {
		return String.join(" ", ctx.getArgs()).length() <= 2048;
	}

	@Override
	protected void executeSync(CommandContext ctx) {
		final TextChannel feedbackChannel
				= this.knb.getJda().getTextChannelById(this.knb.getConfig().getFeedbackChannelId());

		final User author = ctx.getEvent().getAuthor();
		final Guild guild = ctx.getEvent().getGuild();
		final TextChannel channel = ctx.getEvent().getChannel();
		final Message msg = ctx.getEvent().getMessage();

		feedbackChannel.sendMessage(new EmbedBuilder().setTitle("Feedback", msg.getJumpUrl())
				.setDescription(String.join(" ", ctx.getArgs()))
				.setAuthor(author.getAsTag(), author.getEffectiveAvatarUrl(), author.getEffectiveAvatarUrl())
				.addField("Guild", guild.getName() + " (" + guild.getId() + ")", true)
				.addField("User", author.getAsTag() + " (" + author.getId() + ")", true)
				.addField("Channel", channel.getName() + " (" + channel.getId() + ")", true)
				.build()).queue(m -> {
					channel.sendMessage("Votre commentaire a était envoyé, merci !").queue();
				});
	}

	@Override
	public String getHelp() {
		return "Dites ce que vous pensez du bot !";
	}

	@Override
	public String getSyntax() {
		return "feedback <votre commentaire (limité a 2048 charactères)>";
	}
}
