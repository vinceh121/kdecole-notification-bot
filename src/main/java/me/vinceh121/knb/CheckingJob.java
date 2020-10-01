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
	private static final Summary METRICS_NEWS_COUNT
			= Summary.build("knb_news_count", "Numbers of new articles").register();
	private static final Summary METRICS_EMAILS_COUNT
			= Summary.build("knb_emails_count", "Numbers of new emails").register();
	private static final Summary METRICS_GRADES_COUNT
			= Summary.build("knb_grades_count", "Numbers of new grades").register();
	private static final Summary METRICS_PROCESS_TIME
			= Summary.build("knb_process_time", "Time taken to process 1 instance").create();

	@Override
	public void execute(final JobExecutionContext context) throws JobExecutionException {
		final Knb knb = (Knb) context.getMergedJobDataMap().get("knb");
		final Activity oldAct = knb.getJda().getPresence().getActivity();
		knb.getJda().getPresence().setActivity(Activity.watching("for new articles"));
		knb.getAllValidInstances().forEach(u -> {
			METRICS_PROCESS_TIME.time(() -> {
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
			});
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

		METRICS_GRADES_COUNT.observe(grades.size());

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

		METRICS_EMAILS_COUNT.observe(coms.size());

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
		if (ui.isShowWarnings()) {
			chan.sendMessage(text
					+ "\n\n"
					+ "Les prochaines érreures ne sont pas affichés; pour les réactivier utiliser la commande `warnings`")
					.queue();
			knb.getColInstances().updateOne(Filters.eq(ui.getId()), Updates.set("showWarnings", false));
			ui.setShowWarnings(false);
		}
	}

}
