package me.vinceh121.knb;

import static net.dv8tion.jda.api.requests.GatewayIntent.DIRECT_MESSAGES;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MESSAGES;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.security.auth.login.LoginException;

import org.apache.http.client.ClientProtocolException;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.Conventions;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import me.vinceh121.jkdecole.Article;
import me.vinceh121.jkdecole.JKdecole;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

public class Knb {
	private static final Logger LOG = LoggerFactory.getLogger(Knb.class);
	private static final Collection<GatewayIntent> INTENTS = Arrays.asList(GUILD_MESSAGES, DIRECT_MESSAGES);
	private final ObjectMapper mapper;
	private final JKdecole kdecole;
	private final Config config;
	private final Scheduler scheduler;
	private final JDA jda;
	private final RegistrationListener regisListener;
	private final MongoClient mongo;
	private final MongoDatabase mongoDb;
	private final JobDetail job;

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

		Knb.LOG.debug("Starting scheduler");
		try {
			this.scheduler = StdSchedulerFactory.getDefaultScheduler();
			this.scheduler.start();
		} catch (final SchedulerException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		Knb.LOG.info("Connecting to Discord");

		final JDABuilder build = JDABuilder.create(this.config.getToken(), Knb.INTENTS);
		build.setMemberCachePolicy(MemberCachePolicy.NONE);
		build.enableCache(Collections.emptyList());
		build.setActivity(Activity.playing("with wierd APIs"));
		try {
			this.jda = build.build();
			this.jda.awaitReady();
		} catch (final LoginException e) {
			Knb.LOG.error("Failed to login to discord", e);
			throw new RuntimeException(e);
		} catch (final Exception e) {
			Knb.LOG.error("Failed to init JDA", e);
			throw new RuntimeException(e);
		}

		Knb.LOG.info("Connected to Discord. Ping: {}ms", this.jda.getGatewayPing());

		final CodecRegistry codecRegistry
				= CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
						CodecRegistries.fromProviders(PojoCodecProvider.builder()
								.automatic(true)
								.conventions(Arrays.asList(Conventions.CLASS_AND_PROPERTY_CONVENTION,
										Conventions.ANNOTATION_CONVENTION, Conventions.OBJECT_ID_GENERATORS))
								.build()));

		final MongoClientSettings mongoSets = MongoClientSettings.builder()
				.applicationName("Kdecole-Notification-Bot")
				.applyConnectionString(new ConnectionString(this.config.getMongo()))
				.codecRegistry(codecRegistry)
				.build();

		this.mongo = MongoClients.create(mongoSets);
		this.mongoDb = this.mongo.getDatabase("knb");

		this.kdecole = new JKdecole();

		this.regisListener = new RegistrationListener(this);

		this.job = JobBuilder.newJob().ofType(CheckingJob.class).withIdentity("checker", "jobs").build();
	}

	private void start() {
		Knb.LOG.info("Starting...");
		this.jda.addEventListener(this.regisListener);

		final Trigger trig = TriggerBuilder.newTrigger()
				.forJob(this.job)
				.startNow()
				.withIdentity("checker-trig")
				.withSchedule(SimpleScheduleBuilder.repeatMinutelyForever(this.config.getDelay()))
				.build();

		this.job.getJobDataMap().put("knb", this);

		try {
			this.scheduler.scheduleJob(this.job, trig);
		} catch (final SchedulerException e) {
			LOG.error("Could not schedule checking job", e);
			System.exit(-5);
		}
	}

	public void addUserInstance(final UserInstance ui) {
		this.mongoDb.getCollection("instances", UserInstance.class).insertOne(ui);
	}

	public UserInstance getUserInstance(final Bson filter) {
		return this.mongoDb.getCollection("instances", UserInstance.class).find(filter).first();
	}

	public void updateUserInstance(final UserInstance ui) {
		this.mongoDb.getCollection("instances", UserInstance.class).replaceOne(Filters.eq(ui.getId()), ui);
	}

	public UserInstance removeGuild(final String guildId) {
		return this.mongoDb.getCollection("instances", UserInstance.class)
				.findOneAndDelete(Filters.eq("guildId", guildId));
	}

	public FindIterable<UserInstance> getAllValidInstances() {
		return this.mongoDb.getCollection("instances", UserInstance.class)
				.find(Filters.and(Filters.eq("stage", "REGISTERED"), Filters.exists("channelId")));
	}

	public CompletableFuture<String> setupUserInstance(final UserInstance ui, final String username,
			final String password) {
		return CompletableFuture.supplyAsync(() -> {
			boolean success = false;
			try {
				success = this.kdecole.login(username, password, true);
			} catch (final IOException e) {
				Knb.LOG.error("Error while logging into kdecole for instance " + ui.getId(), e);
				return "Un erreur est survenue à la connection à l'ENT";
			}

			if (!success) {
				LOG.error("Login error for instance " + ui);
				return "La connection à l'ENT a échouée";
			}

			ui.setKdecoleToken(this.kdecole.getToken());
			ui.setEndpoint(this.kdecole.getEndpoint());
			ui.setStage(Stage.CHANNEL);

			this.updateUserInstance(ui);

			try {
				return "Succés! La connection a réussit en tant que `"
						+ this.kdecole.getInfoUtilisateur().getNom()
						+ "`. Vous devez maintenant mentioner le bot dans le canal où vous voulez qu'il notifie. "
						+ "Pour cela tapez `@Kdecole Bot#6747`";
			} catch (final Exception e) {
				Knb.LOG.error("Error while getting user info", e);
				return "Il y a eu une erreur à la récupération des infos utilisateur, cependant le bot peut probablement fonctionner.";
			}
		});
	}

	public List<Article> getNewsForInstance(final UserInstance ui) throws ClientProtocolException, IOException {
		this.kdecole.setToken(ui.getKdecoleToken());
		this.kdecole.setEndpoint(ui.getEndpoint());
		final List<Article> news = this.kdecole.getNews();
		final Date last = ui.getLastCheck() == null ? new Date(0L) : ui.getLastCheck();
		final List<Article> newNews = new ArrayList<>();

		for (final Article ar : news) {
			if (last.before(ar.getDate())) {
				newNews.add(ar);
			}
		}
		return newNews;
	}

	public void manualTriggerAll() throws SchedulerException {
		this.scheduler.triggerJob(this.job.getKey());
	}

	public void shutdown() throws Exception {
		Knb.LOG.info("Shutting down");
		this.scheduler.shutdown();
		this.jda.shutdownNow();
		this.mongo.close();
	}

	public JDA getJda() {
		return this.jda;
	}

	public boolean isUserAdmin(final long id) {
		return this.config.getAdmins().contains(id);
	}
}
