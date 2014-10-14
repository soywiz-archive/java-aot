package sample1;

import jflash.backend.EngineContext;
import jflash.backend.swing.SwingEngineContext;
import jflash.display.Quad;
import jflash.display.Stage;
import jflash.util.Color;
import libgame.as3.As3EngineContext;

public class MainJava {
    static public void main(String[] args) {
        EngineContext context = new SwingEngineContext(800, 600);
        Stage stage = new Stage(context);
        SampleApp.main(stage);
        context.loop(stage);
    }
}
