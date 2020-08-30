package me.vinceh121.knb;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public abstract class AbstractCommand {
	protected final Knb knb;

	public AbstractCommand(final Knb knb) {
		this.knb = knb;
	}

	public boolean validateSyntax(final CommandContext ctx) {
		return true;
	}

	protected abstract void executeSync(final CommandContext ctx);

	public CompletionStage<Void> execute(final CommandContext ctx) {
		return CompletableFuture.runAsync(() -> this.executeSync(ctx));
	}

	public String getHelp() {
		return "Pas d'aide fournie";
	}

	public String getSyntax() {
		return this.getName();
	}

	public String getName() {
		return this.getClass().getSimpleName().substring(3).toLowerCase();
	}

	public boolean isAdminCommand() {
		return false;
	}

	public boolean isAuthenticatedCommand() {
		return false;
	}
}
