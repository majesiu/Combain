package combainmanager.backend;

import com.datastax.driver.core.*;
import jnr.ffi.annotations.In;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/*
 * For error handling done right see:
 * https://www.datastax.com/dev/blog/cassandra-error-handling-done-right
 *
 * Performing stress tests often results in numerous WriteTimeoutExceptions,
 * ReadTimeoutExceptions (thrown by Cassandra replicas) and
 * OpetationTimedOutExceptions (thrown by the client). Remember to retry
 * failed operations until success (it can be done through the RetryPolicy mechanism:
 * https://stackoverflow.com/questions/30329956/cassandra-datastax-driver-retry-policy )
 */

public class BackendSession {

    private static final Logger logger = LoggerFactory.getLogger(BackendSession.class);

    public static BackendSession instance = null;

    private Session session;

    public BackendSession(String contactPoint, String keyspace) throws BackendException {

        Cluster cluster = Cluster.builder().addContactPoint(contactPoint).build();
        try {
            session = cluster.connect(keyspace);
        } catch (Exception e) {
            throw new BackendException("Could not connect to the cluster. " + e.getMessage() + ".", e);
        }
        prepareStatements();
    }

    private static PreparedStatement SELECT_ALL_FROM_POMIARY;
    private static PreparedStatement SELECT_ALL_FROM_ZBIORY;
    private static PreparedStatement SELECT_ALL_FROM_INTERWENCJE;
    private static PreparedStatement INSERT_INTO_POMIARY;
    private static PreparedStatement INSERT_INTO_ZBIORY;
    private static PreparedStatement INSERT_INTO_INTERWENCJE;
    private static PreparedStatement DELETE_FROM_INTERWENCJE;

    private static final String POMIARY_FORMAT = "- %-16s  %-16s %-16s %-16s\n";
    private static final String ZBIORY_FORMAT = "- %-16s %-16s %-16s\n";
    private static final String INTERWENCJE_FORMAT = "- %-16s %-16s %-16s %-16s\n";

    /**
     * @throws BackendException
     */
    private void prepareStatements() throws BackendException {
        try {
            SELECT_ALL_FROM_ZBIORY = session.prepare("SELECT * FROM zbiory;");
            SELECT_ALL_FROM_POMIARY = session.prepare("SELECT * FROM pomiary;");
            SELECT_ALL_FROM_INTERWENCJE = session.prepare("SELECT * FROM interwencje;");
            INSERT_INTO_POMIARY = session
                    .prepare("INSERT INTO pomiary (maszyna, czujnik, wartosc, wartosc_limit) VALUES (?, ?, ?, ?);");
            INSERT_INTO_ZBIORY = session
                    .prepare("INSERT INTO zbiory (maszyna, ilosc, pojemnosc) VALUES (?, ?, ?);");
            INSERT_INTO_INTERWENCJE = session
                    .prepare("INSERT INTO interwencje (stacja, maszyna, typ, iteracja) VALUES (?, ?, ?, ?);");
            DELETE_FROM_INTERWENCJE = session
                    .prepare("DELETE FROM interwencje WHERE iteracja = ? and maszyna = ? and typ = ? and stacja = ?;");
        } catch (Exception e) {
            throw new BackendException("Could not prepare statements. " + e.getMessage() + ".", e);
        }

        logger.info("Statements prepared");
    }

    /**
     * function that makes database request to delete specified record from table Interwencje
     *
     * @param iteracja
     * @param maszyna
     * @param typ
     * @param stacja
     * @throws BackendException
     */
    public void deleteInterwencja(int iteracja, String maszyna, String typ, int stacja) throws BackendException {
        BoundStatement bs = new BoundStatement(DELETE_FROM_INTERWENCJE);
        bs.bind(iteracja, maszyna, typ, stacja);
        try {
            session.execute(bs);
        } catch (Exception e) {
            throw new BackendException("Could not perform an upsert. " + e.getMessage() + ".", e);
        }

        logger.info("Interwencja " + typ + " nr " + iteracja + " na " + maszyna + " z " + stacja + " wycofana");
    }

    /**
     * function returning list of all records from table zbiory
     *
     * @return List<Zbior>
     * @throws BackendException
     */
    public List<Zbior> selectZbiory() throws BackendException {
        StringBuilder builder = new StringBuilder();
        BoundStatement bs = new BoundStatement(SELECT_ALL_FROM_ZBIORY);

        ResultSet rs = null;

        try {
            rs = session.execute(bs);
        } catch (Exception e) {
            throw new BackendException("Could not perform a query. " + e.getMessage() + ".", e);
        }

        List<Zbior> zbiory = new ArrayList<>();

        for (Row row : rs) {
            String maszyna = row.getString("maszyna");
//			Date ts = row.getTimestamp("data");
            int ilosc = row.getInt("ilosc");
            int pojemnosc = row.getInt("pojemnosc");
            builder.append(String.format(ZBIORY_FORMAT, maszyna, ilosc, pojemnosc));
            zbiory.add(new Zbior(maszyna, ilosc, pojemnosc));
        }

        return zbiory;
    }

    /**
     * function returning list of all records from table pomiary
     *
     * @return List<Pomiar>
     * @throws BackendException
     */
    public List<Pomiar> selectPomiary() throws BackendException {
        StringBuilder builder = new StringBuilder();
        BoundStatement bs = new BoundStatement(SELECT_ALL_FROM_POMIARY);

        ResultSet rs = null;

        try {
            rs = session.execute(bs);
        } catch (Exception e) {
            throw new BackendException("Could not perform a query. " + e.getMessage() + ".", e);
        }

        List<Pomiar> pomiary = new ArrayList<>();

        for (Row row : rs) {
            String maszyna = row.getString("maszyna");
            String czujnik = row.getString("czujnik");
            double wartosc = row.getDouble("wartosc");
            double wartosc_limit = row.getDouble("wartosc_limit");
            pomiary.add(new Pomiar(maszyna, czujnik, wartosc, wartosc_limit));
            builder.append(String.format(POMIARY_FORMAT, maszyna, czujnik, wartosc, wartosc_limit));
        }

        return pomiary;
    }

    /**
     * function returning list of all records from table interwencje
     *
     * @return List<Interwencje>
     * @throws BackendException
     */
    public List<Interwencje> selectInterwencje() throws BackendException {
        StringBuilder builder = new StringBuilder();
        BoundStatement bs = new BoundStatement(SELECT_ALL_FROM_INTERWENCJE);

        ResultSet rs = null;

        try {
            rs = session.execute(bs);
        } catch (Exception e) {
            throw new BackendException("Could not perform a query. " + e.getMessage() + ".", e);
        }

        List<Interwencje> interwencje = new ArrayList<>();

        for (Row row : rs) {
            int stacja = row.getInt("stacja");
            String maszyna = row.getString("maszyna");
            String typ = row.getString("typ");
            int iteracja = row.getInt("iteracja");
            interwencje.add(new Interwencje(stacja, maszyna, typ, iteracja));
            builder.append(String.format(INTERWENCJE_FORMAT, stacja, maszyna, typ, iteracja));
        }

        return interwencje;
    }

    /**
     * function insterting record to table Pomiary
     * @param maszyna
     * @param czujnik
     * @param wartosc
     * @param wartosc_limit
     * @throws BackendException
     */
    public void upsertPomiar(String maszyna, String czujnik, double wartosc, double wartosc_limit) throws BackendException {
        BoundStatement bs = new BoundStatement(INSERT_INTO_POMIARY);
//		bs.bind(maszyna, czujnik, new Timestamp(data), wartosc);
        bs.bind(maszyna, czujnik, wartosc, wartosc_limit);
        try {
            session.execute(bs);
        } catch (Exception e) {
            throw new BackendException("Could not perform an upsert. " + e.getMessage() + ".", e);
        }

        logger.info("Pomiar " + maszyna + " " + czujnik + " dodany z wartoscia: " + wartosc + " " + wartosc_limit);
    }

    /**
     * function insterting record to table Zbiory
     * @param maszyna
     * @param ilosc
     * @param pojemnosc
     * @throws BackendException
     */
    public void upsertZbior(String maszyna, int ilosc, int pojemnosc) throws BackendException {
        BoundStatement bs = new BoundStatement(INSERT_INTO_ZBIORY);
        bs.bind(maszyna, ilosc, pojemnosc);

        try {
            session.execute(bs);
        } catch (Exception e) {
            throw new BackendException("Could not perform an upsert. " + e.getMessage() + ".", e);
        }

        logger.info("Zbior " + maszyna + " " + ilosc + " dodany, limit: " + pojemnosc);
    }

    /**
     * function insterting record to table Interwencje
     * @param stacja
     * @param maszyna
     * @param typ
     * @param iteracja
     * @throws BackendException
     */
    public void upsertInterwencja(int stacja, String maszyna, String typ, int iteracja) throws BackendException {
        BoundStatement bs = new BoundStatement(INSERT_INTO_INTERWENCJE);
        bs.bind(stacja, maszyna, typ, iteracja);

        try {
            session.execute(bs);
        } catch (Exception e) {
            throw new BackendException("Could not perform an upsert. " + e.getMessage() + ".", e);
        }

        logger.info("Interwencja z " + stacja + " na " + maszyna + " " + " typ " + typ + " nr " + iteracja + " wykonana");
    }

    protected void finalize() {
        try {
            if (session != null) {
                session.getCluster().close();
            }
        } catch (Exception e) {
            logger.error("Could not close existing cluster", e);
        }
    }

}
