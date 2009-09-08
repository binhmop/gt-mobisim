// Copyright (c) 2009, Georgia Tech Research Corporation
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
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import edu.gatech.lbs.core.vector.CartesianVector;
import edu.gatech.lbs.core.world.BoundingBox;
import edu.gatech.lbs.core.world.roadnet.RoadMap;
import edu.gatech.lbs.sim.Simulation;
import edu.gatech.lbs.sim.gui.drawer.IDrawer;

public class SimPanel extends JPanel implements ActionListener {
  protected Image image;

  protected boolean doPause = false;

  protected List<IDrawer> drawers;

  protected Simulation sim;

  protected double metersPerPixel;

  protected BoundingBox bounds;

  public static SimPanel makeGui(Simulation sim) {
    JFrame frame = new JFrame("GT Mobile Agent Simulator (gt-mobisim)");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    SimPanel panel = new SimPanel(sim);
    frame.add(panel);

    JButton pauseButton = new JButton("|| >");
    pauseButton.addActionListener(panel);
    panel.add(pauseButton);

    frame.pack();
    frame.setVisible(true);

    return panel;
  }

  public SimPanel(Simulation sim2) {
    this.sim = sim2;
    setBorder(BorderFactory.createLineBorder(Color.black));
    setBackground(Color.WHITE);

    // meter_per_pixel = 10;

    this.addKeyListener(new KeyAdapter() {
      public void keyReleased(KeyEvent e) {
      }

      public void keyTyped(KeyEvent e) {
      }

      public void keyPressed(KeyEvent e) {
        switch (e.getKeyChar()) {
        case 'a':
          metersPerPixel /= 1.1;
          break;
        case 's':
          metersPerPixel *= 1.1;
          break;
        }
      }
    });

    this.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        CartesianVector loc = getLocation(bounds, e.getLocationOnScreen());
        JOptionPane.showMessageDialog(null, e.getLocationOnScreen().x + " px, " + e.getLocationOnScreen().y + " px\n" + loc + "\n" + loc.toRoadnetVector((RoadMap) sim.getWorld()));
      }
    });

    this.addComponentListener(new ComponentAdapter() {
      // This method is called after the component's size changes
      public void componentResized(ComponentEvent evt) {
        Component c = (Component) evt.getSource();

        // Get new size
        Dimension newSize = c.getSize();
      }
    });

    image = null;
  }

  public void setDrawers(List<IDrawer> drawers) {
    this.drawers = drawers;
  }

  public Dimension getPreferredSize() {
    // get the screen size:
    Toolkit toolkit = Toolkit.getDefaultToolkit();
    Dimension screenSize = toolkit.getScreenSize();

    return new Dimension(670, screenSize.height);
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
    bounds = roadmap.getBounds();
    metersPerPixel = Math.max(bounds.getWidth() / getWidth(), bounds.getHeight() / getHeight());

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
    return new Point((int) ((vector.getX() - bounds.getX0()) / metersPerPixel), (int) ((bounds.getNorthBoundary() - vector.getY()) / metersPerPixel));
  }

  public CartesianVector getLocation(BoundingBox bounds, Point px) {
    return new CartesianVector(px.x * metersPerPixel + bounds.getX0(), bounds.getNorthBoundary() - px.y * metersPerPixel);
  }

  // pause button:
  public void actionPerformed(ActionEvent e) {
    doPause = !doPause;
  }
}
