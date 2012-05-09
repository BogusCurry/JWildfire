package org.jwildfire.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;

import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import org.jwildfire.base.Tools;
import org.jwildfire.image.SimpleImage;

public class WelcomeInternalFrame extends JInternalFrame {
  private static final long serialVersionUID = 1L;

  private String[] imageFilenames = { "bronze_bubbles.jpg", "smoky_dreams.jpg", "watchers2.jpg", "woven.jpg" };
  private JButton enterBtn;

  public WelcomeInternalFrame() {
    addInternalFrameListener(new InternalFrameAdapter() {
      @Override
      public void internalFrameActivated(InternalFrameEvent e) {
        enterBtn.requestFocus();
      }
    });
    getContentPane().setBackground(UIManager.getColor("Button.background"));

    JPanel northPanel = new JPanel();
    northPanel.setBackground(Color.BLACK);
    northPanel.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        setVisible(false);
      }
    });
    northPanel.setPreferredSize(new Dimension(10, 68));
    getContentPane().add(northPanel, BorderLayout.NORTH);
    northPanel.setLayout(null);

    JPanel southPanel = new JPanel();
    southPanel.setBackground(Color.BLACK);
    southPanel.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        setVisible(false);
      }
    });
    southPanel.setPreferredSize(new Dimension(10, 100));
    getContentPane().add(southPanel, BorderLayout.SOUTH);
    southPanel.setLayout(null);

    JButton websiteButton = new JButton("Official Website");
    websiteButton.setDefaultCapable(false);
    websiteButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        browse("http://www.andreas-maschke.com/");
      }
    });
    websiteButton.setForeground(SystemColor.control);
    websiteButton.setBackground(Color.BLACK);
    websiteButton.setPreferredSize(new Dimension(128, 24));
    websiteButton.setBorder(null);
    websiteButton.setBorderPainted(false);
    websiteButton.setBounds(6, 0, 134, 28);
    southPanel.add(websiteButton);

    JButton communityBtn = new JButton("User community");
    communityBtn.setDefaultCapable(false);
    communityBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        browse("http://www.facebook.com/groups/jwildfire/");
      }
    });
    communityBtn.setPreferredSize(new Dimension(128, 24));
    communityBtn.setForeground(SystemColor.menu);
    communityBtn.setBorderPainted(false);
    communityBtn.setBackground(Color.BLACK);
    communityBtn.setBounds(178, 67, 122, 28);
    southPanel.add(communityBtn);

    JButton youtubeVideosBtn = new JButton("YouTube videos");
    youtubeVideosBtn.setDefaultCapable(false);
    youtubeVideosBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        browse("http://www.andreas-maschke.com/?page_id=1411");
      }
    });
    youtubeVideosBtn.setPreferredSize(new Dimension(128, 24));
    youtubeVideosBtn.setForeground(SystemColor.menu);
    youtubeVideosBtn.setBorderPainted(false);
    youtubeVideosBtn.setBackground(Color.BLACK);
    youtubeVideosBtn.setBounds(338, 34, 134, 28);
    southPanel.add(youtubeVideosBtn);

    JButton pdfTutorialsBtn = new JButton("PDF Tutorials");
    pdfTutorialsBtn.setDefaultCapable(false);
    pdfTutorialsBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        browse("http://www.andreas-maschke.com/?page_id=899");
      }
    });
    pdfTutorialsBtn.setPreferredSize(new Dimension(128, 24));
    pdfTutorialsBtn.setForeground(SystemColor.menu);
    pdfTutorialsBtn.setBorderPainted(false);
    pdfTutorialsBtn.setBackground(Color.BLACK);
    pdfTutorialsBtn.setBounds(338, 67, 134, 28);
    southPanel.add(pdfTutorialsBtn);

    JButton donationsBtn = new JButton("Donations");
    donationsBtn.setDefaultCapable(false);
    donationsBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        browse("http://www.andreas-maschke.com/?page_id=1401");
      }
    });
    donationsBtn.setPreferredSize(new Dimension(128, 24));
    donationsBtn.setForeground(SystemColor.menu);
    donationsBtn.setBorderPainted(false);
    donationsBtn.setBackground(Color.BLACK);
    donationsBtn.setBounds(6, 67, 134, 28);
    southPanel.add(donationsBtn);

    JButton updatesBtn = new JButton("Updates");
    updatesBtn.setDefaultCapable(false);
    updatesBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        browse("http://www.andreas-maschke.com/?page_id=351");
      }
    });
    updatesBtn.setPreferredSize(new Dimension(128, 24));
    updatesBtn.setForeground(SystemColor.menu);
    updatesBtn.setBorderPainted(false);
    updatesBtn.setBackground(Color.BLACK);
    updatesBtn.setBounds(6, 34, 134, 28);
    southPanel.add(updatesBtn);

    enterBtn = new JButton("Enter");
    enterBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
      }
    });
    enterBtn.setPreferredSize(new Dimension(128, 24));
    enterBtn.setForeground(SystemColor.menu);
    enterBtn.setBorderPainted(false);
    enterBtn.setBackground(Color.BLACK);
    enterBtn.setBounds(178, 0, 122, 62);
    southPanel.add(enterBtn);

    JButton youtubeTutorialsBtn = new JButton("YouTube tutorials");
    youtubeTutorialsBtn.setDefaultCapable(false);
    youtubeTutorialsBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        browse("http://www.andreas-maschke.com/?page_id=1407");
      }
    });
    youtubeTutorialsBtn.setPreferredSize(new Dimension(128, 24));
    youtubeTutorialsBtn.setForeground(SystemColor.menu);
    youtubeTutorialsBtn.setBorderPainted(false);
    youtubeTutorialsBtn.setBackground(Color.BLACK);
    youtubeTutorialsBtn.setBounds(338, 0, 134, 28);
    southPanel.add(youtubeTutorialsBtn);

    JPanel panel_2 = new JPanel();
    panel_2.setBackground(Color.BLACK);
    panel_2.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        setVisible(false);
      }
    });
    getContentPane().add(panel_2, BorderLayout.CENTER);
    setTitle("Welcome to " + Tools.APP_TITLE + " " + Tools.APP_VERSION);
    setBounds(320, 140, 490, 490);

    // Load logo
    try {
      SimpleImage img = getImage("logo.png");
      ImagePanel imgPanel = new ImagePanel(img, 0, 0, img.getImageWidth());
      imgPanel.setPreferredSize(new Dimension(img.getImageWidth(), img.getImageHeight()));
      imgPanel.setLocation(4, 4);
      northPanel.add(imgPanel);
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }

    // Load main image
    try {
      String imageFilename = imageFilenames[(int) (Math.random() * imageFilenames.length)];
      SimpleImage img = getImage(imageFilename);
      panel_2.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

      ImagePanel imgPanel = new ImagePanel(img, 0, 0, img.getImageWidth());
      imgPanel.setPreferredSize(new Dimension(img.getImageWidth(), img.getImageHeight()));
      panel_2.add(imgPanel);
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }

  }

  protected void browse(String pURL) {
    if (java.awt.Desktop.isDesktopSupported()) {
      try {
        java.awt.Desktop.getDesktop().browse(new URI(pURL));
      }
      catch (Throwable ex) {
        ex.printStackTrace();
      }
    }
  }

  private SimpleImage getImage(String pName) throws Exception {
    byte[] imgData = getImagedata(pName);
    Image fileImg = Toolkit.getDefaultToolkit().createImage(imgData);
    MediaTracker tracker = new MediaTracker(this);
    tracker.addImage(fileImg, 0);
    tracker.waitForID(0);
    int width = fileImg.getWidth(null);
    int height = fileImg.getHeight(null);
    BufferedImage bImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    Graphics g = bImg.getGraphics();
    g.drawImage(fileImg, 0, 0, null);
    fileImg = null;
    return new SimpleImage(bImg, width, height);
  }

  private byte[] getImagedata(String pName) throws Exception {
    InputStream is = this.getClass().getResourceAsStream("welcomescreen/" + pName);
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    final int BUFFER_SIZE = 40960;
    byte[] buffer = new byte[BUFFER_SIZE];
    try {
      int n;
      while ((n = is.read(buffer, 0, BUFFER_SIZE)) >= 0) {
        os.write(buffer, 0, n);
      }
      os.flush();
      os.close();
      return os.toByteArray();
    }
    finally {
      is.close();
    }
  }

  public JButton getEnterBtn() {
    return enterBtn;
  }
}