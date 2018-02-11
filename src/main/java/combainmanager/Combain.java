package combainmanager;

import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;
import java.util.Random;

import combainmanager.backend.BackendException;
import combainmanager.backend.BackendSession;

public class Combain {

	private static final String PROPERTIES_FILENAME = "config.properties";

	public static void main(String[] args) throws IOException, BackendException, InterruptedException {
		String contactPoint = null;
		String keyspace = null;
		int i = 0;

		String nazwa = args[0];
		int pojemnosc = Integer.parseInt(args[1]);

		Properties properties = new Properties();
		try {
			i = 0;
			properties.load(Combain.class.getClassLoader().getResourceAsStream(PROPERTIES_FILENAME));
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
