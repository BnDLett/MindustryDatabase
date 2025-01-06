package net.ddns.mindustry.database.client;

import net.ddns.mindustry.database.schema.tables.pojos.Server;
import org.jooq.types.UShort;
import java.util.Optional;

public interface ServerQueries {

    Optional<Server> find(String ip, UShort port);
}
