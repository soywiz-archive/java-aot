package libgame;

public class SDLWindow {
    long id;

    public SDLWindow(long id) {
        this.id = id;
    }

    public void dispose() {
        SDLApi.destroyWindow(id);
    }
}
