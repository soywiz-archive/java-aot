package sample1;

import jflash.backend.EngineContext;
import jflash.display.Stage;
import libgame.as3.As3EngineContext;

public class MainAs3 {
    static public void main(String[] args) {
        EngineContext context = new As3EngineContext();
        Stage stage = new Stage(context);
        SampleApp.main(stage);
        context.loop(stage);
    }
}
