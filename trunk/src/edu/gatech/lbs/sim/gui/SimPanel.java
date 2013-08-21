// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import edu.gatech.lbs.core.vector.CartesianVector;
import edu.gatech.lbs.core.world.BoundingBox;
import edu.gatech.lbs.core.world.roadnet.RoadMap;
import edu.gatech.lbs.sim.Simulation;
import edu.gatech.lbs.sim.gui.drawer.IDrawer;

public class SimPanel extends JPanel {
  protected Image image;

  protected boolean doPause = false;

  protected List<IDrawer> drawers;

  protected Simulation sim;

  public double mmPerPixel;
  protected final double zoomRate = 2;

  protected BoundingBox bounds;

  public static SimPanel makeGui(Simulation sim) {
    JFrame frame = new JFrame("GT Mobile Agent Simulator (gt-mobisim)");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    SimPanel panel = new SimPanel(sim);
    frame.add(panel);

    frame.pack();
    frame.setVisible(true);

    return panel;
  }

  public SimPanel(Simulation sim2) {
    this.sim = sim2;
    setBorder(BorderFactory.createLineBorder(Color.black));
    setBackground(Color.WHITE);

    JButton pauseButton = new JButton("|| >");
    pauseButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        doPause = !doPause;
      }
    });
    add(pauseButton);

    setFocusable(true);
    this.addKeyListener(new KeyAdapter() {
      public void keyReleased(KeyEvent e) {
      }

      public void keyTyped(KeyEvent e) {
      }

      public void keyPressed(KeyEvent e) {
        long x0 = bounds.getX0();
        long y0 = bounds.getY0();
        double m = 1;
        switch (e.getKeyCode()) {
        case KeyEvent.VK_LEFT:
          x0 -= bounds.getWidth() / 10;
          break;
        case KeyEvent.VK_RIGHT:
          x0 += bounds.getWidth() / 10;
          break;
        case KeyEvent.VK_UP:
          y0 += bounds.getHeight() / 10;
          break;
        case KeyEvent.VK_DOWN:
          y0 -= bounds.getHeight() / 10;
          break;
        case KeyEvent.VK_ADD:
          m /= zoomRate;
          break;
        case KeyEvent.VK_SUBTRACT:
          m *= zoomRate;
          break;
        case KeyEvent.VK_SPACE:
          doPause = !doPause;
          break;
        }
        bounds = new BoundingBox(x0 + (long) (bounds.getWidth() * (1 - m) / 2), y0 + (long) (bounds.getHeight() * (1 - m) / 2), (long) (bounds.getWidth() * m), (long) (bounds.getHeight() * m));
      }
    });

    this.addMouseListener(new MouseAdapter() {

      public void mouseClicked(MouseEvent e) {
        // JOptionPane.showMessageDialog(null, e.getLocationOnScreen().x + " px, " + e.getLocationOnScreen().y + " px\n" + loc + "\n" + loc.toRoadnetVector((RoadMap) sim.getWorld()));

        double m = 1;
        switch (e.getButton()) {
        case MouseEvent.BUTTON1:
          m /= zoomRate;
          break;
        case MouseEvent.BUTTON3:
          m *= zoomRate;
          break;
        }
        zoom(getLocation(bounds, new Point(e.getX(), e.getY())), m);
      }
    });

    this.addMouseWheelListener(new MouseAdapter() {
      public void mouseWheelMoved(MouseWheelEvent e) {
        double m = (e.getWheelRotation() > 0 ? zoomRate : 1 / zoomRate);
        zoom(getLocation(bounds, new Point(e.getX(), e.getY())), m);
      }
    });

    this.addComponentListener(new ComponentAdapter() {
      // This method is called after the component's size changes
      public void componentResized(ComponentEvent evt) {
        Component c = (Component) evt.getSource();
        Dimension newSize = c.getSize();
      }
    });

    image = null;
  }

  protected void zoom(CartesianVector center, double times) {
    bounds = new BoundingBox((long) (center.getX() - (center.getX() - bounds.getX0()) * times), (long) (center.getY() - (center.getY() - bounds.getY0()) * times), (long) (bounds.getWidth() * times), (long) (bounds.getHeight() * times));
  }

  public void setDrawers(List<IDrawer> drawers) {
    this.drawers = drawers;
  }

  public Dimension getPreferredSize() {
    Toolkit toolkit = Toolkit.getDefaultToolkit();
    Dimension screenSize = toolkit.getScreenSize();

    return new Dimension(screenSize.width - 10, screenSize.height - 60);
  }

  public void paintComponent(Graphics g) {
    super.paintComponent(g);

    // put the offscreen image on the screen:
    g.drawImage(image, 0, 0, null);
  }

  public void redrawSim() {
    // ensure off-screen image reflects component size:
    Dimension d = getSize();
    Image imageNew = createImage(d.width, d.height);

    Graphics g = imageNew.getGraphics();
    g.setColor(getBackground());
    g.fillRect(0, 0, d.width, d.height);

    RoadMap roadmap = (RoadMap) sim.getWorld();
    if (bounds == null) {
      bounds = roadmap.getBounds();
    }

    mmPerPixel = Math.max(bounds.getWidth() / getWidth(), bounds.getHeight() / getHeight());
    // Ensure that mm/pixel is a power of 2.
    mmPerPixel = Math.pow(2, 1 + (int) (Math.log(mmPerPixel) / Math.log(2)));

    for (IDrawer drawer : drawers) {
      drawer.draw(g);
    }

    image = imageNew;

    // pause simulation:
    try {
      if (doPause) {
        doScreenshot();
      }
      while (doPause) {
        Thread.sleep(100);
      }
    } catch (InterruptedException e) {
      System.out.println("Thread interrupted: " + e.getMessage());
    }
  }

  protected void doScreenshot() {
    try {
      File dir = new File("screenshots");
      if (!dir.exists()) {
        dir.mkdir();
      }
      File imageFile = new File("screenshots/sim_" + sim.getTime() / 1000 + "s.png");
      ImageIO.write((RenderedImage) image, "png", imageFile);
    } catch (IOException e) {
      System.out.println("Couldn't write screenshot file.");
    }
  }

  public Point getPixel(CartesianVector vector) {
    return getPixel(bounds, vector);
  }

  protected Point getPixel(BoundingBox bounds, CartesianVector vector) {
    return new Point((int) ((vector.getX() - bounds.getX0()) / mmPerPixel), (int) ((bounds.getNorthBoundary() - vector.getY()) / mmPerPixel));
  }

  public CartesianVector getLocation(BoundingBox bounds, Point px) {
    return new CartesianVector((long) (px.x * mmPerPixel + bounds.getX0()), (long) (bounds.getNorthBoundary() - px.y * mmPerPixel));
  }
}
