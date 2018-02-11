package combainmanager.backend;

public class Zbior {
    public String maszyna;
    public int ilosc;
    public int pojemnosc;

    public Zbior(String maszyna, int ilosc, int pojemnosc) {
        this.maszyna = maszyna;
        this.ilosc = ilosc;
        this.pojemnosc = pojemnosc;
    }

    @Override
    public String toString() {
        return "Zbior{" +
                "maszyna='" + maszyna + '\'' +
                ", ilosc=" + ilosc +
                ", pojemnosc=" + pojemnosc +
                '}';
    }
}
