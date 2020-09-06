package me.vinceh121.knb.commands;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import me.vinceh121.knb.AbstractCommand;
import me.vinceh121.knb.CommandContext;
import me.vinceh121.knb.Knb;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;

public class CmdApropos extends AbstractCommand {
	private static final Random RND = new Random();
	private static final boolean INLINE = true;
	private static final List<Field> EASTER_FIELD
			= Arrays.asList(new Field("Hotel", "Trivago", INLINE), new Field("Astolfo", "Cute", INLINE),
					new Field("weeb", "services", INLINE), new Field("Stallman", "Was right", INLINE));

	public CmdApropos(Knb knb) {
		super(knb);
	}

	@Override
	protected void executeSync(CommandContext ctx) {
		final EmbedBuilder build = new EmbedBuilder();
		build.setAuthor("vinceh121", "https://vinceh121.me", "https://vinceh121.me/assets/profile.png");
		build.setColor(0x3f51b5);
		build.setTitle("Kdecole Notification Bot", "https://github.com/vinceh121/kdecole-notification-bot");
		build.setDescription(
				"Kdecole Notification Bot est un bot Discord ayant pour but de relayer les notifications d'un"
						+ " ENT Kdecole/Skolengo dans un salon");

		build.addField("Version", "0.0.1-SNAPSHOT", INLINE);
		build.addField("Licence", "GNU GPL V3", INLINE);

		build.addField(EASTER_FIELD.get(RND.nextInt(EASTER_FIELD.size())));

		ctx.getEvent().getChannel().sendMessage(build.build()).queue();
	}

	@Override
	public String getHelp() {
		return "A propos de ce bot";
	}

}
