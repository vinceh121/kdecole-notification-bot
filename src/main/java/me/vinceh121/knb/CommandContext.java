package me.vinceh121.knb;

import java.util.List;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class CommandContext {
	private GuildMessageReceivedEvent event;
	private List<String> args;
	private UserInstance userInstance;

	public GuildMessageReceivedEvent getEvent() {
		return this.event;
	}

	public void setEvent(final GuildMessageReceivedEvent event) {
		this.event = event;
	}

	public List<String> getArgs() {
		return this.args;
	}

	public void setArgs(final List<String> args) {
		this.args = args;
	}

	public UserInstance getUserInstance() {
		return this.userInstance;
	}

	public void setUserInstance(final UserInstance userInstance) {
		this.userInstance = userInstance;
	}
}
