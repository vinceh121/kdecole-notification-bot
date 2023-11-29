package me.vinceh121.knb;

import static com.rethinkdb.RethinkDB.r;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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

import me.vinceh121.jkdecole.entities.messages.CommunicationPreview;
import me.vinceh121.jskolengo.JSkolengo;
import me.vinceh121.jskolengo.entities.StudentUserInfo;
import me.vinceh121.jskolengo.entities.agenda.Homework;
import me.vinceh121.jskolengo.entities.evaluation.EvaluationDetail;
import me.vinceh121.jskolengo.entities.info.News;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class SkolengoCheckingJob implements Job {
	public static final int COLOR_ARTICLE = 0xff7b1c;
	private static final Logger LOG = LogManager.getLogger(SkolengoCheckingJob.class);
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

		try (final JSkolengo sko = new JSkolengo()) {

			knb.getAllValidSkolengoInstances().forEach(u -> {
				this.metricProcessTime.time(() -> {
					final TextChannel chan = knb.getJda().getTextChannelById(u.getChannelId());

					sko.setBearerToken(u.getTokens().getAccessToken());
					sko.setEmsCode(u.getEmsCode());
					sko.setSchoolId(u.getSchoolId());

					StudentUserInfo info;
					try {
						info = sko.fetchUserInfo().get();
					} catch (final Exception e) {
						SkolengoCheckingJob.LOG.error(
								new FormattedMessage("Error while fetching user info for instance {}", u.getId()), e);
						this.sendWarning(knb, chan, u,
								"Erreur dans la récupération des informations utilisateur: " + e);
						return;
					}

					if (u.getRelays().contains(RelayType.ARTICLES)) {
						this.processArticles(knb, sko, info, u);
					}
					if (u.getRelays().contains(RelayType.EMAILS)) {
						this.processEmails(knb, sko, info, u);
					}
					if (u.getRelays().contains(RelayType.NOTES)) {
						this.processGrades(knb, sko, info, u);
					}
					if (u.getRelays().contains(RelayType.DEVOIRS)) {
						this.processHomework(knb, sko, info, u);
					}

					u.setLastCheck(new Date());
					knb.getTableSkolengoInstances()
							.get(u.getId())
							.update(r.hashMap("lastCheck", new Date().getTime()))
							.run(knb.getDbCon());
				});
			});
		} catch (IOException e1) {
			LOG.error("Failed to close Skolengo client", e1);
		}

		knb.getJda().getPresence().setActivity(oldAct);
	}

	private void processGrades(final Knb knb, final JSkolengo sko, final StudentUserInfo info,
			final SkolengoUserInstance ui) {
		final TextChannel chan = knb.getJda().getTextChannelById(ui.getChannelId());
		final List<EvaluationDetail> grades;

		try {
			grades = sko.fetchEvaluationsSetting()
					.stream()
					.flatMap(settings -> settings.getPeriods().stream())
					.flatMap(period -> sko.fetchEvaluations(period.getId()).stream())
					.flatMap(eval -> eval.getEvaluations().stream())
					.filter(detail -> detail.getDateTime()
							.toInstant(ZoneOffset.UTC)
							.isAfter(ui.getLastCheck().toInstant()))
					.collect(Collectors.toList());
		} catch (final Exception e) {
			SkolengoCheckingJob.LOG
					.error(new FormattedMessage("Error while getting grades for instance {}", ui.getId()), e);
			this.sendWarning(knb, chan, ui, "Une érreur est survenue en récupérant les nouvelles notes: " + e);
			return;
		}

		this.metricGradesCount.inc(grades.size());

		if (grades.size() == 0) {
			return;
		}

		final String estabName = info.getSchool().getName();

		final LocalDateTime oldest
				= Collections.min(grades, (o1, o2) -> o1.getDateTime().compareTo(o2.getDateTime())).getDateTime();

		final EmbedBuilder embBuild = new EmbedBuilder();

		embBuild.setAuthor("Kdecole", "https://github.com/vinceh121/kdecole-notification-bot",
				"https://cdn.discordapp.com/avatars/691655008076300339/4f492132883b1aa4f5984fe2eab9fa09.png");
		embBuild.setColor(SkolengoCheckingJob.COLOR_ARTICLE);
		embBuild.setTimestamp(oldest);
		embBuild.setTitle("Nouvelles notes");
		embBuild.setFooter(estabName);

		for (final EvaluationDetail n : grades) {
			final Field f = new Field(n.getTopic() + " : " + n.getTitle(),
					n.getEvaluationResult().getMark() + "/" + n.getScale() + "\nCoef: " + n.getCoefficient(), true);
			embBuild.addField(f);
		}

		final MessageEmbed emb = embBuild.build();
		chan.sendMessageEmbeds(emb).queue();
	}

	private void processEmails(final Knb knb, final JSkolengo sko, final StudentUserInfo info,
			final SkolengoUserInstance ui) {
		final TextChannel chan = knb.getJda().getTextChannelById(ui.getChannelId());
		final List<CommunicationPreview> coms;

		try {
			coms = Arrays.asList(); // TODO
		} catch (final Exception e) {
			SkolengoCheckingJob.LOG
					.error(new FormattedMessage("Error while getting communications for instance {}", ui.getId()), e);
			this.sendWarning(knb, chan, ui, "Une erreur est survenue en récupérant les nouvelles communications: " + e);
			return;
		}

		this.metricEmailsCount.inc(coms.size());

		if (coms.size() == 0) {
			return;
		}

		final String estabName = info.getSchool().getName();

		final Date oldest = Collections.min(coms, (o1, o2) -> o1.getLastMessage().compareTo(o2.getLastMessage()))
				.getLastMessage();

		final EmbedBuilder embBuild = new EmbedBuilder();

		embBuild.setAuthor("Kdecole", "https://github.com/vinceh121/kdecole-notification-bot",
				"https://cdn.discordapp.com/avatars/691655008076300339/4f492132883b1aa4f5984fe2eab9fa09.png");
		embBuild.setColor(SkolengoCheckingJob.COLOR_ARTICLE);
		embBuild.setTimestamp(oldest.toInstant());
		embBuild.setTitle("Nouvelles communications");
		embBuild.setFooter(estabName);

		for (final CommunicationPreview n : coms) {
			final Field f = new Field(n.getCurrentAuthor().getLabel() + ": " + n.getSubject(),
					StringEscapeUtils.unescapeHtml4(n.getPreview()), true);
			embBuild.addField(f);
		}

		final MessageEmbed emb = embBuild.build();
		chan.sendMessageEmbeds(emb).queue();
	}

	private void processArticles(final Knb knb, final JSkolengo sko, final StudentUserInfo info,
			final SkolengoUserInstance ui) {
		final TextChannel chan = knb.getJda().getTextChannelById(ui.getChannelId());
		final List<News> news;

		try {
			news = sko.fetchSchoolInfo()
					.get()
					.stream()
					.filter(n -> n.getPublicationDateTime().toInstant().isAfter(ui.getLastCheck().toInstant()))
					.collect(Collectors.toList());
		} catch (final Exception e) {
			SkolengoCheckingJob.LOG.error(new FormattedMessage("Error while getting news for instance {}", ui.getId()),
					e);
			this.sendWarning(knb, chan, ui, "Une erreur est survenue en récupérant les nouveaux articles: " + e);
			return;
		}

		this.metricNewsCount.inc(news.size());

		if (news.size() == 0) {
			return;
		}

		final String estabName = info.getSchool().getName();

		final ZonedDateTime oldest
				= Collections.min(news, (o1, o2) -> o1.getPublicationDateTime().compareTo(o2.getPublicationDateTime()))
						.getPublicationDateTime();

		final EmbedBuilder embBuild = new EmbedBuilder();

		embBuild.setAuthor("Kdecole", "https://github.com/vinceh121/kdecole-notification-bot",
				"https://cdn.discordapp.com/avatars/691655008076300339/4f492132883b1aa4f5984fe2eab9fa09.png");
		embBuild.setColor(SkolengoCheckingJob.COLOR_ARTICLE);
		embBuild.setTimestamp(oldest.toInstant());
		embBuild.setTitle("Nouveaux articles");
		embBuild.setFooter(estabName);

		for (final News n : news) {
			final Field f = new Field(n.getTitle(), n.getShortContent(), true);
			embBuild.addField(f);
		}

		final MessageEmbed emb = embBuild.build();
		chan.sendMessageEmbeds(emb).queue();
	}

	private void processHomework(final Knb knb, final JSkolengo sko, final StudentUserInfo info,
			final SkolengoUserInstance ui) {
		final TextChannel chan = knb.getJda().getTextChannelById(ui.getChannelId());
		final List<Homework> hws;

		try {
			// FIXME
			hws = sko.fetchHomeworkAssignments(
					LocalDate
							.from(ui.getLastCheck().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()),
					LocalDate.now()).stream().collect(Collectors.toList());
		} catch (final Exception e) {
			SkolengoCheckingJob.LOG
					.error(new FormattedMessage("Error while getting homework for instance {}", ui.getId()), e);
			this.sendWarning(knb, chan, ui, "Une érreur est survenue en récupérant les nouveaux devoirs: " + e);
			return;
		}

		this.metricNewsCount.inc(hws.size());

		if (hws.size() == 0) {
			return;
		}

		final String estabName = info.getSchool().getName();

		final Date oldest = ui.getLastCheck(); // FIXME

		final EmbedBuilder embBuild = new EmbedBuilder();

		embBuild.setAuthor("Kdecole", "https://github.com/vinceh121/kdecole-notification-bot",
				"https://cdn.discordapp.com/avatars/691655008076300339/4f492132883b1aa4f5984fe2eab9fa09.png");
		embBuild.setColor(SkolengoCheckingJob.COLOR_ARTICLE);
		embBuild.setTimestamp(oldest.toInstant());
		embBuild.setTitle("Nouveaux devoirs");
		embBuild.setFooter(estabName);

		for (final Homework n : hws) {
			final Field f = new Field(n.getSubject().getLabel(), n.getTitle(), true);
			embBuild.addField(f);
		}

		final MessageEmbed emb = embBuild.build();
		chan.sendMessageEmbeds(emb).queue();
	}

	private void sendWarning(final Knb knb, final TextChannel chan, final SkolengoUserInstance ui, final String text) {
		if (ui.isShowWarnings() || ui.isAlwaysShowWarnings()) {
			chan.sendMessage(text
					+ "\n\n"
					+ "Les prochaines érreures ne sont pas affichés; pour les réactivier utiliser la commande `warnings`")
					.queue();
			knb.getTableKdecoleInstances().get(ui.getId()).update(r.hashMap("showWarnings", false)).run(knb.getDbCon());
			ui.setShowWarnings(false);
		}
	}

}
