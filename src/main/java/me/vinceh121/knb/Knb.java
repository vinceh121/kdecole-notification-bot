package me.vinceh121.knb;

import static com.rethinkdb.RethinkDB.r;
import static net.dv8tion.jda.api.requests.GatewayIntent.DIRECT_MESSAGES;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MESSAGES;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.FormattedMessage;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.reflections.Reflections;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rethinkdb.gen.ast.Table;
import com.rethinkdb.net.Connection;
import com.rethinkdb.net.Result;

import me.vinceh121.jkdecole.JKdecole;
import me.vinceh121.jkdecole.entities.Article;
import me.vinceh121.jkdecole.entities.grades.Grade;
import me.vinceh121.jkdecole.entities.grades.GradeMessage;
import me.vinceh121.jkdecole.entities.homework.Agenda;
import me.vinceh121.jkdecole.entities.homework.HWDay;
import me.vinceh121.jkdecole.entities.homework.Homework;
import me.vinceh121.jkdecole.entities.info.UserInfo;
import me.vinceh121.jkdecole.entities.messages.CommunicationPreview;
import me.vinceh121.knb.Config.MetricConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import redis.clients.jedis.JedisPool;

public class Knb {
	private static final Logger LOG = LogManager.getLogger(Knb.class);
	private static final Collection<GatewayIntent> INTENTS = Arrays.asList(GUILD_MESSAGES, DIRECT_MESSAGES);
	private final HttpClient http;
	private final ObjectMapper mapper;
	private final Config config;
	private final Scheduler scheduler;
	private final JDA jda;
	private final CommandListener regisListener;
	private final Connection dbCon;
	private final Table tableKdecoleInstances, tableSkolengoInstances;
	private final JobDetail kdecoleJob, skolengoJob;
	private final Map<String, AbstractCommand> cmdMap = new HashMap<>();
	private final MetricRegistry metricRegistry = new MetricRegistry();
	private final JedisPool redisPool;

	public static void main(final String[] args) {
		final Knb knb = new Knb();
		knb.start();
	}

	public Knb() {
		Knb.LOG.info("Init Kdecole Notification Bot");
		this.mapper = new ObjectMapper();
		try {
			this.config = this.mapper.readValue(new File("/etc/kdecole-bot/config.json"), Config.class);
		} catch (final Exception e1) {
			Knb.LOG.error("Error while loading config.json: ", e1);
			throw new RuntimeException(e1);
		}

		this.initMetrics();

		this.http = HttpClients.custom()
				.setUserAgent("Kdecole Notification Bot/0.0.1 (github.com/vinceh121/kdecole-notification-bot)")
				.build();

		Knb.LOG.debug("Starting scheduler");
		try {
			this.scheduler = StdSchedulerFactory.getDefaultScheduler();
			this.scheduler.start();
		} catch (final SchedulerException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		Knb.LOG.info("Connecting to DB");

		this.dbCon = r.connection(this.config.getDbUrl()).connect();
		this.tableKdecoleInstances = r.table("kdecoleInstances");
		this.tableSkolengoInstances = r.table("skolengoInstances");

		Knb.LOG.info("Connecting to Redis");

		this.redisPool = new JedisPool(URI.create(this.config.getRedisUri()));

		Knb.LOG.info("Connecting to Discord");

		final JDABuilder build = JDABuilder.create(this.config.getToken(), Knb.INTENTS);
		build.setMemberCachePolicy(MemberCachePolicy.NONE);
		build.enableCache(Collections.emptyList());
		build.setActivity(Activity.playing("with wierd APIs"));
		try {
			this.jda = build.build();
			this.jda.awaitReady();
		} catch (final Exception e) {
			Knb.LOG.error("Failed to init JDA", e);
			throw new RuntimeException(e);
		}

		Runtime.getRuntime().addShutdownHook(new Thread(() -> this.jda.shutdown()));

		Knb.LOG.info("Connected to Discord. Ping: {}ms", this.jda.getGatewayPing());

		try {
			this.registerCommands();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			Knb.LOG.error("Failed to load commands", e);
			throw new RuntimeException(e);
		}
		this.regisListener = new CommandListener(this, this.cmdMap);

		this.kdecoleJob
				= JobBuilder.newJob().ofType(KdecoleCheckingJob.class).withIdentity("kdecole-checker", "jobs").build();
		this.skolengoJob = JobBuilder.newJob()
				.ofType(SkolengoCheckingJob.class)
				.withIdentity("skolengo-checker", "jobs")
				.build();
	}

	private void start() {
		Knb.LOG.info("Starting...");
		this.jda.addEventListener(this.regisListener);

		final Trigger kdecoleTrig = TriggerBuilder.newTrigger()
				.forJob(this.kdecoleJob)
				.startNow()
				.withIdentity("kdecole-checker-trig")
				.withSchedule(SimpleScheduleBuilder.repeatMinutelyForever(this.config.getDelay()))
				.build();

		this.kdecoleJob.getJobDataMap().put("knb", this);

		final Trigger skolengoTrig = TriggerBuilder.newTrigger()
				.forJob(this.skolengoJob)
				.startNow()
				.withIdentity("skolengo-checker-trig")
				.withSchedule(SimpleScheduleBuilder.repeatMinutelyForever(this.config.getDelay()))
				.build();

		this.skolengoJob.getJobDataMap().put("knb", this);

		try {
			this.scheduler.scheduleJob(this.kdecoleJob, kdecoleTrig);
			this.scheduler.scheduleJob(this.skolengoJob, skolengoTrig);
		} catch (final SchedulerException e) {
			Knb.LOG.error("Could not schedule checking job", e);
			System.exit(-5);
		}
	}

	private void initMetrics() {
		if (this.config.getMetrics() == null) {
			return;
		}

		final MetricConfig metc = this.config.getMetrics();

		Knb.LOG.info("Starting metrics");
		this.metricRegistry.registerAll("knb-gc", new GarbageCollectorMetricSet());
		this.metricRegistry.registerAll("knb-mem", new MemoryUsageGaugeSet());

		final Graphite graphite = new Graphite(new InetSocketAddress(metc.getHost(), metc.getPort()));
		final GraphiteReporter graphiteReporter = GraphiteReporter.forRegistry(this.metricRegistry)
				.prefixedWith("knb")
				.convertRatesTo(TimeUnit.MILLISECONDS)
				.convertDurationsTo(TimeUnit.MILLISECONDS)
				.filter(MetricFilter.ALL)
				.build(graphite);

		graphiteReporter.start(metc.getPeriod(), TimeUnit.MINUTES);

		final Gauge<Integer> gaugeGuilds = () -> this.jda.getGuilds().size();
		this.metricRegistry.register(MetricRegistry.name("discord", "guilds"), gaugeGuilds);

		final Gauge<Long> gaugeGatewayPing = () -> this.jda.getGatewayPing();
		this.metricRegistry.register(MetricRegistry.name("discord", "ping", "gateway"), gaugeGatewayPing);

		final Gauge<Long> gaugeRestPing = () -> this.jda.getRestPing().complete();
		this.metricRegistry.register(MetricRegistry.name("discord", "ping", "rest"), gaugeRestPing);
	}

	public void addUserInstance(final KdecoleUserInstance ui) {
		this.tableKdecoleInstances.insert(ui).run(dbCon);
	}

	public KdecoleUserInstance removeGuild(final String guildId) {
		return this.tableKdecoleInstances.filter(r.hashMap("guildId", guildId))
				.delete()
				.run(dbCon, KdecoleUserInstance.class)
				.first();
	}

	public Result<KdecoleUserInstance> getAllValidKdecoleInstances() {
		return this.tableKdecoleInstances.hasFields("kdecoleToken").run(this.dbCon, KdecoleUserInstance.class);
	}

	public Result<SkolengoUserInstance> getAllValidSkolengoInstances() {
		return this.tableSkolengoInstances.hasFields("tokens").run(this.dbCon, SkolengoUserInstance.class);
	}

	public CompletableFuture<UserInfo> setupUserInstance(final KdecoleUserInstance ui, final String username,
			final String password, final String endpoint) {
		return CompletableFuture.supplyAsync(() -> {
			final JKdecole kdecole = this.getKdecole();
			kdecole.setEndpoint(endpoint);
			boolean success = false;
			try {
				success = kdecole.login(username, password, false);
			} catch (final IOException e) {
				Knb.LOG.error(new FormattedMessage("Error while logging into kdecole for instance {}", ui.getId()), e);
				throw new RuntimeException("Un erreur est survenue à la connection à l'ENT");
			}

			if (!success) {
				Knb.LOG.error("Login error for instance " + ui);
				throw new RuntimeException("La connection à l'ENT a échouée");
			}

			ui.setKdecoleToken(kdecole.getToken());
			ui.setEndpoint(kdecole.getEndpoint());
			ui.getRelays().add(RelayType.ARTICLES);

			this.addUserInstance(ui);

			try {
				return kdecole.getUserInfo();
			} catch (final Exception e) {
				Knb.LOG.error("Error while getting user info", e);
				throw new RuntimeException(
						"Il y a eu une erreur à la récupération des infos utilisateur, cependant le bot peut probablement fonctionner.");
			}
		});
	}

	public List<Homework> fetchAgendaForInstance(final JKdecole kdecole, final KdecoleUserInstance ui)
			throws ClientProtocolException, IOException {
		final Agenda agenda = kdecole.getAgenda();
		if (!agenda.isHwOpen()) {
			throw new RuntimeException("Votre ENT n'a pas d'agenda");
		}
		final List<HWDay> days = agenda.getDays();
		final Date last = ui.getLastCheck() == null ? new Date(0L) : ui.getLastCheck();
		final List<Homework> homeworks = new ArrayList<>();

		for (final HWDay ar : days) {
			for (final Homework hw : ar.getHomeworks()) {
				if (last.before(hw.getGivenAt())) {
					homeworks.add(hw);
				}
			}
		}
		return homeworks;
	}

	public List<Article> fetchNewsForInstance(final JKdecole kdecole, final KdecoleUserInstance ui)
			throws ClientProtocolException, IOException {
		final List<Article> news = kdecole.getNews();
		final Date last = ui.getLastCheck() == null ? new Date(0L) : ui.getLastCheck();
		final List<Article> newNews = new ArrayList<>();

		for (final Article ar : news) {
			if (last.before(ar.getDate())) {
				newNews.add(ar);
			}
		}
		return newNews;
	}

	public List<CommunicationPreview> fetchNewMailsForInstance(final JKdecole kdecole, final KdecoleUserInstance ui)
			throws ClientProtocolException, IOException {
		final List<CommunicationPreview> coms = kdecole.getInbox(-1).getComs();
		final List<CommunicationPreview> updatedComs = new ArrayList<>();

		for (final CommunicationPreview c : coms) {
			if (ui.getLastCheck().before(c.getLastMessage())) {
				updatedComs.add(c);
			}
		}

		return updatedComs;
	}

	public List<Grade> fetchNewGradesForInstance(final JKdecole kdecole, final KdecoleUserInstance ui)
			throws ClientProtocolException, IOException {
		final GradeMessage msg = kdecole.getStudentGrades();

		if (!msg.isGradeModulesEnabled()) {
			throw new UnsupportedOperationException("Grades modules disabled");
		}

		final List<Grade> grades = msg.getGrades();
		final List<Grade> updatedGrades = new ArrayList<>();

		for (final Grade g : grades) {
			if (ui.getLastCheck().before(g.getDate())) {
				updatedGrades.add(g);
			}
		}

		return updatedGrades;
	}

	public void manualTriggerAll() throws SchedulerException {
		this.scheduler.triggerJob(this.kdecoleJob.getKey());
		this.scheduler.triggerJob(this.skolengoJob.getKey());
	}

	public JKdecole getKdecole() {
		return new JKdecole(this.http);
	}

	private void registerCommands() throws InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException {
		final Reflections reflec = new Reflections("me.vinceh121.knb.commands");

		for (final Class<? extends AbstractCommand> cmds : reflec.getSubTypesOf(AbstractCommand.class)) {
			final AbstractCommand c = cmds.getConstructor(Knb.class).newInstance(this);
			this.cmdMap.put(c.getName(), c);
		}

		Knb.LOG.info("Loaded {} commands", this.cmdMap.size());
	}

	public void shutdown() throws Exception {
		Knb.LOG.info("Shutting down");
		this.scheduler.shutdown();
		this.jda.shutdownNow();
		this.dbCon.close();
	}

	public JDA getJda() {
		return this.jda;
	}

	public Map<String, AbstractCommand> getCmdMap() {
		return new Hashtable<>(this.cmdMap);
	}

	public ObjectMapper getMapper() {
		return this.mapper;
	}

	public boolean isUserAdmin(final long id) {
		return this.config.getAdmins().contains(id);
	}

	public Config getConfig() {
		return this.config;
	}

	public CommandListener getRegisListener() {
		return this.regisListener;
	}

	public MetricRegistry getMetricRegistry() {
		return this.metricRegistry;
	}

	public Connection getDbCon() {
		return dbCon;
	}

	public JedisPool getRedisPool() {
		return redisPool;
	}

	public Table getTableKdecoleInstances() {
		return tableKdecoleInstances;
	}

	public Table getTableSkolengoInstances() {
		return tableSkolengoInstances;
	}
}
