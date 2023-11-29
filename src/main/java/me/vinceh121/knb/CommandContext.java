package me.vinceh121.knb;

import java.util.List;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandContext {
	private MessageReceivedEvent event;
	private List<String> args;
	private KdecoleUserInstance userInstance;
	private boolean adminCalled;

	public MessageReceivedEvent getEvent() {
		return this.event;
	}

	public void setEvent(final MessageReceivedEvent event) {
		this.event = event;
	}

	public List<String> getArgs() {
		return this.args;
	}

	public void setArgs(final List<String> args) {
		this.args = args;
	}

	public KdecoleUserInstance getUserInstance() {
		return this.userInstance;
	}

	public void setUserInstance(final KdecoleUserInstance userInstance) {
		this.userInstance = userInstance;
	}

	public boolean isAdminCalled() {
		return this.adminCalled;
	}

	public void setAdminCalled(final boolean adminCalled) {
		this.adminCalled = adminCalled;
	}
}
