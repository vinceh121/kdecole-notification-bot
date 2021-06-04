package me.vinceh121.knb;

import static com.rethinkdb.RethinkDB.r;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.FormattedMessage;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

import me.vinceh121.jkdecole.JKdecole;
import me.vinceh121.jkdecole.entities.Article;
import me.vinceh121.jkdecole.entities.grades.Grade;
import me.vinceh121.jkdecole.entities.homework.Homework;
import me.vinceh121.jkdecole.entities.info.UserInfo;
import me.vinceh121.jkdecole.entities.messages.CommunicationPreview;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.TextChannel;

public class CheckingJob implements Job {
	public static final int COLOR_ARTICLE = 0xff7b1c;
	private static final Logger LOG = LogManager.getLogger(CheckingJob.class);
	private Counter metricNewsCount, metricEmailsCount, metricGradesCount;
	private Timer metricProcessTime;

	private void setupMetrics(final MetricRegistry regis) {
		this.metricNewsCount = regis.counter(MetricRegistry.name("check", "news", "count"));
		this.metricEmailsCount = regis.counter(MetricRegistry.name("check", "emails", "count"));
		this.metricGradesCount = regis.counter(MetricRegistry.name("check", "grades", "count"));
		this.metricProcessTime = regis.timer(MetricRegistry.name("check", "process", "time"));
	}

	@Override
	public void execute(final JobExecutionContext context) throws JobExecutionException {
		final Knb knb = (Knb) context.getMergedJobDataMap().get("knb");
		final Activity oldAct = knb.getJda().getPresence().getActivity();
		knb.getJda().getPresence().setActivity(Activity.watching("for new articles"));

		this.setupMetrics(knb.getMetricRegistry());

		knb.getAllValidInstances().forEach(u -> {
			this.metricProcessTime.time(() -> {
				final JKdecole kdecole = knb.getKdecole();
				kdecole.setToken(u.getKdecoleToken());
				kdecole.setEndpoint(u.getEndpoint());
				UserInfo info;
				try {
					info = kdecole.getUserInfo();
				} catch (final Exception e) {
					CheckingJob.LOG.error(
							new FormattedMessage("Error while fetching user info for instance {}", u.getId()), e);
					info = new UserInfo();
					info.setNom("");
				}
				if (u.getRelays().contains(RelayType.ARTICLES)) {
					this.processArticles(knb, kdecole, info, u);
				}
				if (u.getRelays().contains(RelayType.EMAILS)) {
					this.processEmails(knb, kdecole, info, u);
				}
				if (u.getRelays().contains(RelayType.NOTES)) {
					this.processGrades(knb, kdecole, info, u);
				}
				if (u.getRelays().contains(RelayType.DEVOIRS)) {
					this.processHomework(knb, kdecole, info, u);
				}
				u.setLastCheck(new Date());
				knb.getTableInstances().update(r.hashMap("lastCheck", new Date().getTime())).run(knb.getDbCon());
			});
		});
		knb.getJda().getPresence().setActivity(oldAct);
	}

	private void processGrades(final Knb knb, final JKdecole kde, final UserInfo info, final UserInstance ui) {
		final TextChannel chan = knb.getJda().getTextChannelById(ui.getChannelId());
		final List<Grade> grades;
		try {
			grades = knb.fetchNewGradesForInstance(kde, ui);
		} catch (final UnsupportedOperationException e) {
			this.sendWarning(knb, chan, ui, "Votre ENT n'a pas de module de notes activé");
			return;
		} catch (final Exception e) {
			CheckingJob.LOG.error(new FormattedMessage("Error while getting grades for instance {}", ui.getId()), e);
			this.sendWarning(knb, chan, ui, "Une érreur est survenue en récupérant les nouvelles notes: " + e);
			return;
		}

		this.metricGradesCount.inc(grades.size());

		if (grades.size() == 0) {
			return;
		}

		final String estabName = info.getEtabs().get(0).getNom();

		final Date oldest = Collections.min(grades, (o1, o2) -> o1.getDate().compareTo(o2.getDate())).getDate();

		final EmbedBuilder embBuild = new EmbedBuilder();

		embBuild.setAuthor("Kdecole", "https://github.com/vinceh121/kdecole-notification-bot",
				"https://cdn.discordapp.com/avatars/691655008076300339/4f492132883b1aa4f5984fe2eab9fa09.png");
		embBuild.setColor(CheckingJob.COLOR_ARTICLE);
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

	private void processEmails(final Knb knb, final JKdecole kde, final UserInfo info, final UserInstance ui) {
		final TextChannel chan = knb.getJda().getTextChannelById(ui.getChannelId());
		final List<CommunicationPreview> coms;
		try {
			coms = knb.fetchNewMailsForInstance(kde, ui);
		} catch (final Exception e) {
			CheckingJob.LOG
					.error(new FormattedMessage("Error while getting communications for instance {}", ui.getId()), e);
			this.sendWarning(knb, chan, ui, "Une érreur est survenue en récupérant les nouvelles communications: " + e);
			return;
		}

		this.metricEmailsCount.inc(coms.size());

		if (coms.size() == 0) {
			return;
		}

		final String estabName = info.getEtabs().get(0).getNom();

		final Date oldest = Collections.min(coms, (o1, o2) -> o1.getLastMessage().compareTo(o2.getLastMessage()))
				.getLastMessage();

		final EmbedBuilder embBuild = new EmbedBuilder();

		embBuild.setAuthor("Kdecole", "https://github.com/vinceh121/kdecole-notification-bot",
				"https://cdn.discordapp.com/avatars/691655008076300339/4f492132883b1aa4f5984fe2eab9fa09.png");
		embBuild.setColor(CheckingJob.COLOR_ARTICLE);
		embBuild.setTimestamp(oldest.toInstant());
		embBuild.setTitle("Nouvelles communications");
		embBuild.setFooter(estabName);

		for (final CommunicationPreview n : coms) {
			final Field f = new Field(n.getCurrentAuthor().getLabel() + ": " + n.getSubject(),
					StringEscapeUtils.unescapeHtml4(n.getPreview()), true);
			embBuild.addField(f);
		}

		final MessageEmbed emb = embBuild.build();
		chan.sendMessage(emb).queue();
	}

	private void processArticles(final Knb knb, final JKdecole kde, final UserInfo info, final UserInstance ui) {
		final TextChannel chan = knb.getJda().getTextChannelById(ui.getChannelId());
		final List<Article> news;
		try {
			news = knb.fetchNewsForInstance(kde, ui);
		} catch (final Exception e) {
			CheckingJob.LOG.error(new FormattedMessage("Error while getting news for instance {}", ui.getId()), e);
			this.sendWarning(knb, chan, ui, "Une érreur est survenue en récupérant les nouveaux articles: " + e);
			return;
		}

		this.metricNewsCount.inc(news.size());

		if (news.size() == 0) {
			return;
		}

		final String estabName = info.getEtabs().get(0).getNom();

		final Date oldest = Collections.min(news, (o1, o2) -> o1.getDate().compareTo(o2.getDate())).getDate();

		final EmbedBuilder embBuild = new EmbedBuilder();

		embBuild.setAuthor("Kdecole", "https://github.com/vinceh121/kdecole-notification-bot",
				"https://cdn.discordapp.com/avatars/691655008076300339/4f492132883b1aa4f5984fe2eab9fa09.png");
		embBuild.setColor(CheckingJob.COLOR_ARTICLE);
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

	private void processHomework(final Knb knb, final JKdecole kde, final UserInfo info, final UserInstance ui) {
		final TextChannel chan = knb.getJda().getTextChannelById(ui.getChannelId());
		final List<Homework> hws;
		try {
			hws = knb.fetchAgendaForInstance(kde, ui);
		} catch (final Exception e) {
			CheckingJob.LOG.error(new FormattedMessage("Error while getting homework for instance {}", ui.getId()), e);
			this.sendWarning(knb, chan, ui, "Une érreur est survenue en récupérant les nouveaux devoirs: " + e);
			return;
		}

		this.metricNewsCount.inc(hws.size());

		if (hws.size() == 0) {
			return;
		}

		final String estabName = info.getEtabs().get(0).getNom();

		final Date oldest = Collections.min(hws, (o1, o2) -> o1.getGivenAt().compareTo(o2.getGivenAt())).getGivenAt();

		final EmbedBuilder embBuild = new EmbedBuilder();

		embBuild.setAuthor("Kdecole", "https://github.com/vinceh121/kdecole-notification-bot",
				"https://cdn.discordapp.com/avatars/691655008076300339/4f492132883b1aa4f5984fe2eab9fa09.png");
		embBuild.setColor(CheckingJob.COLOR_ARTICLE);
		embBuild.setTimestamp(oldest.toInstant());
		embBuild.setTitle("Nouveaux devoirs");
		embBuild.setFooter(estabName);

		for (final Homework n : hws) {
			final Field f = new Field(n.getType() + " : " + n.getSubject(), n.getTitle(), true);
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
			knb.getTableInstances().get(ui.getId()).update(r.hashMap("showWarnings", false)).run(knb.getDbCon());
			ui.setShowWarnings(false);
		}
	}

}
