package jflash.backend.swing;

import jflash.backend.*;
import jflash.backend.Component;
import jflash.util.Point;
import jflash.util.Color;

import javax.imageio.ImageIO;
import javax.swing.Timer;
import javax.swing.JFrame;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.io.*;
import java.util.Stack;

public class SwingEngineContext extends EngineContext {
    //System.setProperty("awt.useSystemAAFontSettings","on")
    //System.setProperty("swing.aatext", "true")

    private int width;
    private int height;

    public SwingEngineContext(int width, int height) {
        this.width = width;
        this.height = height;
        instance = this;
    }

    void setTimeout(final Runnable callback, int ms) {
        new Timer(ms, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                callback.run();
            }
        }).start();
    }

    public Point mousePosition() {
        return new Point(0, 0);
    }

    @Override
    public void loop(final jflash.backend.Component root) {
        frame = new JFrame("scala-flash");
        g = null;
        alpha = 1.0f;

        MyCanvas canvas = new MyCanvas(root);
        frame.getContentPane().add(canvas);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setSize(width, height);
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);

        float fps = 60.0f;
        int frameMs = (int) (1000 / fps);
        long lastTime = System.currentTimeMillis();
        try {
            while (true) {
                long currentTime = System.currentTimeMillis();
                int deltaTime = (int) (currentTime - lastTime);
                lastTime = currentTime;
                root.update(deltaTime);
                //frame.repaint();
                canvas.update();
                Thread.sleep(frameMs);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private JFrame frame = null;
    private float alpha = 1.0f;
    //private val gList:mutable.Stack = new mutable.Stack[Graphics2D]()
    private Graphics2D g = null;
    final private EngineContext context = this;

    @Override
    public InputStream openFile(String path) throws FileNotFoundException {
        System.out.println(System.getProperty("user.dir") + "/assets/" + path);
        return new FileInputStream(System.getProperty("user.dir") + "/assets/" + path);
    }

    @Override
    public void clear(jflash.util.Color color) {
        g.setColor(convertColor(color));
        g.fillRect(0, 0, frame.getWidth(), frame.getHeight());
    }

    @Override
    public void present() {

    }

    @Override
    public void drawSolid(int width, int height, jflash.util.Color color) {
        g.setColor(convertColor(color));
        g.fillRect(0, 0, width, height);
    }

    @Override
    public void drawImage(int width, int height, Texture texture) {
        SwingTextureBase base = (SwingTextureBase) texture.base;
        g.drawImage(base.image, 0, 0, width, height, texture.x, texture.y, texture.x + texture.width, texture.y + texture.height, null);
    }

    private java.awt.Color convertColor(jflash.util.Color color) {
        return new java.awt.Color(color.r, color.g, color.b, color.a);
    }

    @Override
    public void drawText(float x, float y, float width, float height, String text, String fontFamily, Color color, int size, TextAlign align) {
        g.setColor(convertColor(color));
        g.setFont(new Font(fontFamily, Font.PLAIN, size));
        FontMetrics fm = g.getFontMetrics();
        float textWidth = fm.stringWidth(text);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        //g.getFontRenderContext.
        g.clipRect((int) x, (int) y, (int) width, (int) height);
        g.drawString(text, (int) (x + (width - textWidth) * align.ratio), y + fm.getAscent());
    }

    @Override
    public void translate(float x, float y) {
        if (x == 0 && y == 0) return;
        g.translate(x, y);
    }

    @Override
    public void rotate(float angle) {
        if (angle == 0) return;
        g.rotate(angle);
    }

    @Override
    public void scale(float sx, float sy) {
        if (sx == 1 && sy == 1) return;
        g.scale(sx, sy);
    }

    @Override
    public void alpha(float alpha) {
        if (alpha == 1) return;
        this.alpha *= alpha;
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, this.alpha));
    }

    Stack<State> states = new Stack<State>();

    @Override
    public void save() {
        states.push(new State(g, alpha));
        g = (Graphics2D) g.create();
    }

    @Override
    public void restore() {
        State s = states.pop();
        this.g = s.g;
        this.alpha = s.alpha;
    }

    @Override
    public Texture createImageFromBytes(byte[] data) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(new ByteArrayInputStream(data));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Texture(new SwingTextureBase(image), 0, 0, image.getWidth(), image.getHeight());
    }

  /*
  override def createImageFromFile(file:File): Texture = {
    val image = ImageIO.read(file)
    new Texture(new SwingTextureBase(image), 0, 0, image.getWidth, image.getHeight)
  }
  */

    class MyCanvas extends Canvas {
        //private var createdBuffers = false
        private VolatileImage volatileImg = null;
        final jflash.backend.Component root;

        MyCanvas(final jflash.backend.Component root) {
            this.root = root;
            enableEvents(
                    AWTEvent.MOUSE_EVENT_MASK |
                            AWTEvent.MOUSE_MOTION_EVENT_MASK |
                            AWTEvent.KEY_EVENT_MASK
            );
        }

        @Override
        public void processMouseEvent(MouseEvent e) {
            super.processMouseEvent(e);
            int x = e.getX(), y = e.getY();
            Point p = new Point(x, y);

            switch (e.getID()) {
                case MouseEvent.MOUSE_ENTERED:
                case MouseEvent.MOUSE_PRESSED:
                case MouseEvent.MOUSE_RELEASED:
                    root.touchUpdate(p, TouchEventType.MOVE);
                    switch (e.getID()) {
                        case MouseEvent.MOUSE_RELEASED:
                            root.touchUpdate(p, TouchEventType.UP);
                            break;
                        case MouseEvent.MOUSE_PRESSED:
                            root.touchUpdate(p, TouchEventType.DOWN);
                            break;
                    }
                    break;
                case MouseEvent.MOUSE_CLICKED:
                    root.touchUpdate(p, TouchEventType.CLICK);
                    break;
            }

            //println(s"processMouseEvent:$e")
        }

        @Override
        public void processMouseMotionEvent(MouseEvent e) {
            super.processMouseMotionEvent(e);

            root.touchUpdate(new Point(e.getX(), e.getY()), TouchEventType.MOVE);

            switch (e.getID()) {
                case MouseEvent.MOUSE_MOVED:
                default:
                    break;
            }
            //println(s"processMouseMotionEvent:$e")
        }

        @Override
        public void processMouseWheelEvent(MouseWheelEvent e) {
            super.processMouseWheelEvent(e);
            //println(s"processMouseWheelEvent:$e")
        }

        public void update() {
            // create the hardware accelerated image.
            createBackBuffer();

            // Main rendering loop. Volatile images may lose their contents.
            // This loop will continually render to (and produce if neccessary) volatile images
            // until the rendering was completed successfully.
            do {

                // Validate the volatile image for the graphics configuration of this
                // component. If the volatile image doesn't apply for this graphics configuration
                // (in other words, the hardware acceleration doesn't apply for the new device)
                // then we need to re-create it.
                GraphicsConfiguration gc = this.getGraphicsConfiguration();
                int valCode = volatileImg.validate(gc);

                // This means the device doesn't match up to this hardware accelerated image.
                if (valCode == VolatileImage.IMAGE_INCOMPATIBLE) {
                    createBackBuffer(); // recreate the hardware accelerated image.
                }

                Graphics offscreenGraphics = volatileImg.getGraphics();

                    /*
                    if (!createdBuffers) {
                    createBufferStrategy(2)
                    createdBuffers = true
                    }
                    */

                BufferStrategy strategy = getBufferStrategy();

                if (strategy == null || strategy.contentsLost()) {
                    createBufferStrategy(2);
                    strategy = getBufferStrategy();
                }
                g = (Graphics2D) strategy.getDrawGraphics();
                //g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                //g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                root.render(context);

                //doPaint(offscreenGraphics); // call core paint method.

                // paint back buffer to main graphics
                //g.drawImage(volatileImg, 0, 0, this);

                g.dispose();
                strategy.show();

                // Test if content is lost
            } while (volatileImg.contentsLost());
        }

        private void createBackBuffer() {
            volatileImg = getGraphicsConfiguration().createCompatibleVolatileImage(getWidth(), getHeight());
        }

        @Override
        public void paint(Graphics _g) {
            //g = (Graphics2D)_g;
            update();
        }
    }

}

class State {
    public Graphics2D g;
    public float alpha;

    State(Graphics2D g, float alpha) {
        this.g = g;
        this.alpha = alpha;
    }
}
