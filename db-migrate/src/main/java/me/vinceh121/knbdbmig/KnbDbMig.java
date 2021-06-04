package me.vinceh121.knbdbmig;

import static com.rethinkdb.RethinkDB.r;

import org.bson.Document;

import com.mongodb.ConnectionString;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.rethinkdb.gen.ast.Table;
import com.rethinkdb.net.Connection;

public class KnbDbMig {
	public static void main(String[] args) {
		if (args.length != 2) {
			System.err.println("format: <mongoUrl> <rethinkUrl>");
			return;
		}

		final String mongoUrl = args[0];
		final String rethinkUrl = args[1];

		final ConnectionString mongConString = new ConnectionString(mongoUrl);
		final MongoClient client = MongoClients.create(mongConString);
		final MongoCollection<Document> colInstances
				= client.getDatabase(mongConString.getDatabase() == null ? "knb" : mongConString.getDatabase())
						.getCollection("instances");

		final Connection con = r.connection(rethinkUrl).connect();

		try {
			r.dbCreate(con.db()).run(con);
		} catch (Exception e) {
			System.err.println("Silently ignoring: " + e);
		}

		try {
			r.tableCreate("instances").run(con);
		} catch (Exception e) {
			System.err.println("Silently ignoring: " + e);
		}

		final Table tableInstances = r.table("instances");

		final FindIterable<Document> iter = colInstances.find();
		for (final Document doc : iter) {
			doc.remove("_id");
			doc.remove("lastCheck");
			tableInstances.insert(r.json(doc.toJson())).run(con);
		}
		tableInstances.update(r.hashMap("lastCheck", 0L)).run(con);
	}
}
