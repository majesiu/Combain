package combainmanager.backend;

/**
 * Class modeling NoSQL record of table Pomiary
 */
public class Pomiar {
    public String maszyna;
    public String czujnik;
    public double wartosc;
    public double wartosc_limit;

    public Pomiar(String maszyna, String czujnik, double wartosc, double wartosc_limit) {
        this.maszyna = maszyna;
        this.czujnik = czujnik;
        this.wartosc = wartosc;
        this.wartosc_limit = wartosc_limit;
    }

    @Override
    public String toString() {
        return "Pomiar{" +
                "maszyna='" + maszyna + '\'' +
                ", czujnik='" + czujnik + '\'' +
                ", wartosc=" + wartosc +
                ", wartosc_limit=" + wartosc_limit +
                '}';
    }

}
