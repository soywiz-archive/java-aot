package sample1;

import jflash.backend.EngineContext;
import jflash.display.Quad;
import jflash.display.Stage;
import jflash.util.Color;

public class SampleApp {
    static public void main(Stage stage) {
        stage.addChild(new Quad() {
            {
                this.color = Color.red;
                this.width = 200;
                this.height = 200;
            }

            @Override
            public void update(int dt) {
                x++;
                //System.out.println("Quad.updated!");
            }
        });
    }
}
