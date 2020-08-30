package me.vinceh121.knb;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import io.prometheus.client.Summary;
import me.vinceh121.jkdecole.entities.Article;
import me.vinceh121.jkdecole.entities.info.UserInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.TextChannel;

public class CheckingJob implements Job {
	public static final int COLOR_ARTICLE = 0xff7b1c;
	private static final Logger LOG = LoggerFactory.getLogger(CheckingJob.class);
	private static final Summary METRICS_NEWS_COUNT
			= Summary.build("knb_news_count", "Numbers of new articles").register();

	@Override
	public void execute(final JobExecutionContext context) throws JobExecutionException {
		LOG.info("Checking job called");
		final Knb knb = (Knb) context.getMergedJobDataMap().get("knb");
		knb.getAllValidInstances().forEach(u -> {
			final TextChannel chan = knb.getJda().getTextChannelById(u.getChannelId());
			this.processInstance(knb, u, chan);
		});
	}

	private void processInstance(final Knb knb, final UserInstance ui, final TextChannel chan) {
		final List<Article> news;
		try {
			news = knb.getNewsForInstance(ui);
		} catch (final Exception e) {
			LOG.error("Error while getting news for instance " + ui.getId(), e);
			knb.getColInstances().updateOne(Filters.eq(ui.getId()), Updates.set("showWarnings", false));
			ui.setShowWarnings(false);
			if (ui.isShowWarnings())
				chan.sendMessage("Une érreur est survenue en récupérant contactant l'ENT: "
						+ e
						+ "\n"
						+ "Les prochaines érreures ne sont pas affichés; pour les réactivier utiliser la commande `warnings`")
						.queue();
			return;
		}

		METRICS_NEWS_COUNT.observe(news.size());

		if (news.size() == 0) {
			return;
		}

		String estabName = "";
		try {
			final UserInfo info = knb.getUserInfoForInstace(ui);
			estabName = info.getEtabs().get(0).getNom();
		} catch (final NullPointerException | ArrayIndexOutOfBoundsException | IOException e) {
			LOG.error("Failed to get user info for instance " + ui.getId(), e);
		}

		final Date oldest = Collections.min(news, (o1, o2) -> {
			return o1.getDate().compareTo(o2.getDate());
		}).getDate();

		final EmbedBuilder embBuild = new EmbedBuilder();

		embBuild.setAuthor("Kdecole", "https://github.com/vinceh121/kdecole-notification-bot",
				"https://cdn.discordapp.com/avatars/691655008076300339/4f492132883b1aa4f5984fe2eab9fa09.png");
		embBuild.setColor(COLOR_ARTICLE);
		embBuild.setTimestamp(oldest.toInstant());
		embBuild.setTitle("Nouveaux articles");
		embBuild.setFooter(estabName);

		for (final Article n : news) {
			final Field f = new Field(n.getAuthor(), n.getTitle(), true);
			embBuild.addField(f);
		}

		final MessageEmbed emb = embBuild.build();
		chan.sendMessage(emb).queue();

		ui.setLastCheck(new Date());
		knb.updateUserInstance(ui);
	}

}
