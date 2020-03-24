package me.vinceh121.knb;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.vinceh121.jkdecole.Article;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.TextChannel;

public class CheckingJob implements Job {
	public static int COLOR_ARTICLE = 0xff7b1c;
	private static Logger LOG = LoggerFactory.getLogger(CheckingJob.class);

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		LOG.info("Checking job called");
		final Knb knb = (Knb) context.getMergedJobDataMap().get("knb");
		knb.getAllValidInstances().forEach(u -> {
			final TextChannel chan = knb.getJda().getTextChannelById(u.getChannelId());
			processInstance(knb, u, chan);
		});
	}

	private void processInstance(final Knb knb, final UserInstance ui, final TextChannel chan) {
		final List<Article> news;
		try {
			news = knb.getNewsForInstance(ui);
		} catch (Exception e) {
			LOG.error("Error while gettin news for instance " + ui, e);
			return;
		}

		if (news.size() == 0)
			return;

		final EmbedBuilder embBuild = new EmbedBuilder();

		embBuild.setAuthor("Kdecole", "https://github.com/vinceh121/kdecole-notification-bot",
				"https://cdn.discordapp.com/avatars/691655008076300339/4f492132883b1aa4f5984fe2eab9fa09.png");
		embBuild.setColor(COLOR_ARTICLE);
		embBuild.setTimestamp(Instant.now());
		embBuild.setTitle("Nouveaux articles");
		embBuild.setFooter("By vinceh121");

		for (Article n : news) {
			final Field f = new Field(n.getAuthor(), n.getTitle(), true);
			embBuild.addField(f);
		}

		final MessageEmbed emb = embBuild.build();
		chan.sendMessage(emb).queue();

		ui.setLastCheck(new Date());
		knb.updateUserInstance(ui);
	}

}
