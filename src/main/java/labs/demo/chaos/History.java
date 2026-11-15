package labs.demo.chaos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** Histórico de falhas injetadas em H2 só em memória (sem console, sem arquivo). */
public final class History {

    private final Connection db;

    public History() {
        try {
            db = DriverManager.getConnection("jdbc:h2:mem:");
            try (Statement st = db.createStatement()) {
                // key/value viram palavras reservadas no H2 2.x (caso #14).
                st.execute("CREATE TABLE fault_log (key VARCHAR(64), value VARCHAR(256))");
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Grava uma execução e devolve quantas vezes o cenário já rodou. */
    public synchronized long record(String key, String value) {
        try (PreparedStatement insert = db.prepareStatement("INSERT INTO fault_log (key, value) VALUES (?, ?)");
                PreparedStatement count = db.prepareStatement("SELECT COUNT(*) FROM fault_log WHERE key = ?")) {
            insert.setString(1, key);
            insert.setString(2, value);
            insert.executeUpdate();
            count.setString(1, key);
            try (ResultSet rs = count.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }
}
