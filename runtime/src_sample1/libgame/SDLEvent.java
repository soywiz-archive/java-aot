package libgame;

public class SDLEvent {
    private int type;
    private int code;
    private long data1;
    private long data2;

    public SDLEvent(int type, int code, long data1, long data2) {
        this.type = type;
        this.code = code;
        this.data1 = data1;
        this.data2 = data2;
    }

    public int getType() {
        return type;
    }

    public int getCode() {
        return code;
    }

    public long getData1() {
        return data1;
    }

    public long getData2() {
        return data2;
    }
}
