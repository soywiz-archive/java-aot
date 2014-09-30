package libgame;

public class SDLWindow {
    public long id;

    public SDLWindow(long id) {
        this.id = id;
    }

    public void swap() {
        SDLApi.swapWindow(id);
    }

    public SDLRenderer createRenderer() {
        return new SDLRenderer(SDLApi.createRenderer(id));
    }

    public void dispose() {
        SDLApi.destroyWindow(id);
    }
}
