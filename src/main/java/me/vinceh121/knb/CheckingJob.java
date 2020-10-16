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

import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import me.vinceh121.jkdecole.entities.Article;
import me.vinceh121.jkdecole.entities.grades.Grade;
import me.vinceh121.jkdecole.entities.info.UserInfo;
import me.vinceh121.jkdecole.entities.messages.CommunicationPreview;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.TextChannel;

public class CheckingJob implements Job {
	public static final int COLOR_ARTICLE = 0xff7b1c;
	private static final Logger LOG = LoggerFactory.getLogger(CheckingJob.class);
	private Histogram metricNewsCount, metricEmailsCount, metricGradesCount, metricProcessTime;

	private void setupMetrics(final MetricRegistry regis) {
		this.metricNewsCount = regis.histogram(MetricRegistry.name(CheckingJob.class, "check", "news", "count"));
		this.metricEmailsCount = regis.histogram(MetricRegistry.name(CheckingJob.class, "check", "emails", "count"));
		this.metricGradesCount = regis.histogram(MetricRegistry.name(CheckingJob.class, "check", "grades", "count"));
		this.metricProcessTime = regis.histogram(MetricRegistry.name(CheckingJob.class, "check", "process", "time"));
	}

	@Override
	public void execute(final JobExecutionContext context) throws JobExecutionException {
		final Knb knb = (Knb) context.getMergedJobDataMap().get("knb");
		final Activity oldAct = knb.getJda().getPresence().getActivity();
		knb.getJda().getPresence().setActivity(Activity.watching("for new articles"));

		this.setupMetrics(knb.getMetricRegistry());

		knb.getAllValidInstances().forEach(u -> {
			final long startTime = System.currentTimeMillis();
			if (u.getRelays().contains(RelayType.ARTICLES)) {
				this.processArticles(knb, u);
			}
			if (u.getRelays().contains(RelayType.EMAILS)) {
				this.processEmails(knb, u);
			}
			if (u.getRelays().contains(RelayType.NOTES)) {
				this.processGrades(knb, u);
			}
			u.setLastCheck(new Date());
			knb.updateUserInstance(u);
			metricProcessTime.update(System.currentTimeMillis() - startTime);
		});
		knb.getJda().getPresence().setActivity(oldAct);
	}

	private void processGrades(final Knb knb, final UserInstance ui) {
		final TextChannel chan = knb.getJda().getTextChannelById(ui.getChannelId());
		final List<Grade> grades;
		try {
			grades = knb.getNewGradesForInstance(ui);
		} catch (final UnsupportedOperationException e) {
			this.sendWarning(knb, chan, ui, "Votre ENT n'a pas de module de notes activé");
			return;
		} catch (final Exception e) {
			LOG.error("Error while getting grades for instance " + ui.getId(), e);
			this.sendWarning(knb, chan, ui, "Une érreur est survenue en récupérant les nouvelles notes: " + e);
			return;
		}

		metricGradesCount.update(grades.size());

		if (grades.size() == 0) {
			return;
		}

		String estabName = "";
		try {
			final UserInfo info = knb.getUserInfoForInstace(ui);
			estabName = info.getEtabs().get(0).getNom();
		} catch (final NullPointerException | ArrayIndexOutOfBoundsException | IOException e) {
			LOG.error("Failed to get user info for instance " + ui.getId(), e);
		}

		final Date oldest = Collections.min(grades, (o1, o2) -> o1.getDate().compareTo(o2.getDate())).getDate();

		final EmbedBuilder embBuild = new EmbedBuilder();

		embBuild.setAuthor("Kdecole", "https://github.com/vinceh121/kdecole-notification-bot",
				"https://cdn.discordapp.com/avatars/691655008076300339/4f492132883b1aa4f5984fe2eab9fa09.png");
		embBuild.setColor(COLOR_ARTICLE);
		embBuild.setTimestamp(oldest.toInstant());
		embBuild.setTitle("Nouvelles notes");
		embBuild.setFooter(estabName);

		for (final Grade n : grades) {
			final Field f = new Field(n.getSubject() + " : " + n.getTitle(),
					n.getGrade() + "/" + n.getBareme() + "\nCoef: " + n.getCoef(), true);
			embBuild.addField(f);
		}

		final MessageEmbed emb = embBuild.build();
		chan.sendMessage(emb).queue();
	}

	private void processEmails(final Knb knb, final UserInstance ui) {
		final TextChannel chan = knb.getJda().getTextChannelById(ui.getChannelId());
		final List<CommunicationPreview> coms;
		try {
			coms = knb.getNewMailsForInstance(ui);
		} catch (final Exception e) {
			LOG.error("Error while getting emails for instance " + ui.getId(), e);
			this.sendWarning(knb, chan, ui, "Une érreur est survenue en récupérant les nouveaux mails: " + e);
			return;
		}

		metricEmailsCount.update(coms.size());

		if (coms.size() == 0) {
			return;
		}
	}

	private void processArticles(final Knb knb, final UserInstance ui) {
		final TextChannel chan = knb.getJda().getTextChannelById(ui.getChannelId());
		final List<Article> news;
		try {
			news = knb.getNewsForInstance(ui);
		} catch (final Exception e) {
			LOG.error("Error while getting news for instance " + ui.getId(), e);
			this.sendWarning(knb, chan, ui, "Une érreur est survenue en récupérant les nouveaux articles: " + e);
			return;
		}

		metricNewsCount.update(news.size());

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

		final Date oldest = Collections.min(news, (o1, o2) -> o1.getDate().compareTo(o2.getDate())).getDate();

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
	}

	private void sendWarning(final Knb knb, final TextChannel chan, final UserInstance ui, final String text) {
		if (ui.isShowWarnings() || ui.isAlwaysShowWarnings()) {
			chan.sendMessage(text
					+ "\n\n"
					+ "Les prochaines érreures ne sont pas affichés; pour les réactivier utiliser la commande `warnings`")
					.queue();
			knb.getColInstances().updateOne(Filters.eq(ui.getId()), Updates.set("showWarnings", false));
			ui.setShowWarnings(false);
		}
	}

}
