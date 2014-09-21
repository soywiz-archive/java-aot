package libgame;

public class SDLRenderer {
    private long id;

    public SDLRenderer(long id) {
        this.id = id;
    }

    public void dispose() {
        SDLApi.destroyRenderer(id);
    }
}
