package combainmanager.backend;

public class Interwencje {
    public int stacja;
    public String maszyna;
    public String typ;
    public int iteracja;

    public Interwencje(int stacja, String maszyna, String typ, int iteracja) {
        this.stacja = stacja;
        this.maszyna = maszyna;
        this.typ = typ;
        this.iteracja = iteracja;
    }

    @Override
    public String toString() {
        return "Interwencje{" +
                "stacja='" + stacja + '\'' +
                ", maszyna='" + maszyna + '\'' +
                ", typ='" + typ + '\'' +
                ", iteracja=" + iteracja +
                '}';
    }
}
