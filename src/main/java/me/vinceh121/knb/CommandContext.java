package me.vinceh121.knb;

import java.util.List;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class CommandContext {
	private GuildMessageReceivedEvent event;
	private List<String> args;
	private UserInstance userInstance;

	public GuildMessageReceivedEvent getEvent() {
		return event;
	}

	public void setEvent(GuildMessageReceivedEvent event) {
		this.event = event;
	}

	public List<String> getArgs() {
		return args;
	}

	public void setArgs(List<String> args) {
		this.args = args;
	}

	public UserInstance getUserInstance() {
		return userInstance;
	}

	public void setUserInstance(UserInstance userInstance) {
		this.userInstance = userInstance;
	}
}
