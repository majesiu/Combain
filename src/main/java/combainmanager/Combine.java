package combainmanager;

import java.io.IOException;
import java.util.Properties;
import java.util.Random;

import combainmanager.backend.BackendException;
import combainmanager.backend.BackendSession;

/**
 * Class representing combain Harvester, that repeatedly sends it samples to cassandra DB
 */
public class Combine {

	private static final String PROPERTIES_FILENAME = "config.properties";

    /**
     * @param args String, int - combine name, combine capacity
     * @throws IOException
     * @throws BackendException
     * @throws InterruptedException
     */
	public static void main(String[] args) throws IOException, BackendException, InterruptedException {
		String contactPoint = null;
		String keyspace = null;
		int i = 0;

		String nazwa = args[0];
		int pojemnosc = Integer.parseInt(args[1]);

		Properties properties = new Properties();
		try {
			i = 0;
			properties.load(Combine.class.getClassLoader().getResourceAsStream(PROPERTIES_FILENAME));
			contactPoint = properties.getProperty("contact_point");
			keyspace = properties.getProperty("keyspace");
		} catch (IOException ex) {
			ex.printStackTrace();
		}
			
		BackendSession session = new BackendSession(contactPoint, keyspace);
		Random generator = new Random();
		while(true){
            session.upsertPomiar(nazwa,"temperatura silnika",generator.nextInt(101), 100);
            session.upsertPomiar(nazwa,"temperatura oleju",generator.nextInt(101), 100);
			session.upsertPomiar(nazwa,"AdBlue",generator.nextInt(121), 120);
            session.upsertZbior(nazwa, i++, pojemnosc);
        }
	}
}
