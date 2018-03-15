package combainmanager;

import combainmanager.backend.*;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * Class representing station, that reads data from Cassandra and in response sends transport to handle
 * grain deliveries from combine harvesters
 */
public class Station {
    private static final String PROPERTIES_FILENAME = "config.properties";

    public static void main(String[] args) throws IOException, BackendException, InterruptedException {
        String contactPoint = null;
        String keyspace = null;


        int id = Integer.parseInt(args[0]);

        Properties properties = new Properties();
        try {
            properties.load(Combine.class.getClassLoader().getResourceAsStream(PROPERTIES_FILENAME));

            contactPoint = properties.getProperty("contact_point");
            keyspace = properties.getProperty("keyspace");
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        BackendSession session = new BackendSession(contactPoint, keyspace);

        //Deduplication mechanism that ensures no station sends it transport twice for same delivery
        //Conflict resolution is solved via id of each station
        while(true){
            List<Interwencje> interwencje = session.selectInterwencje();
            interwencje.stream().filter(i -> i.stacja == id).forEach( inter -> {
                    interwencje.stream().filter(i2 -> i2.iteracja == inter.iteracja && i2.maszyna.equals(inter.maszyna)
                    && i2.typ.equals(inter.typ) && inter.stacja > i2.stacja).forEach(i -> {
                        try {
                            session.deleteInterwencja(i.iteracja, i.maszyna, i.typ, i.stacja);
                        } catch (BackendException e) {
                            e.printStackTrace();
                        }
                    });
            });
            List<Zbior> zbiory = session.selectZbiory();
            zbiory.forEach(z -> {
                if(z.ilosc > z.pojemnosc){
//                    System.out.println(z.toString());
                    int i = z.ilosc/z.pojemnosc;
                    boolean add = true;
                    for(Interwencje inter : interwencje){
                        if(z.maszyna.equals(inter.maszyna) && inter.iteracja >= i) add = false;
                    }
                    if(add == true){
                        try {
                            session.upsertInterwencja(id, z.maszyna, "Transport", i);
                        } catch (BackendException e) {
                            e.printStackTrace();
                        }
                    }

                }
            });
            Thread.sleep(50);
        }
    }
}
