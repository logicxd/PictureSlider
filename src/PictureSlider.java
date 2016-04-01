import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

@SuppressWarnings("serial")
public class PictureSlider extends JFrame implements ChangeListener, MouseListener{

	private GridBagConstraints gc = new GridBagConstraints();
	private JPanel mainPanel = new JPanel();
	private JMenuBar menuBar = new JMenuBar();
	private File folder;
	private JSlider slideBar;
	private JLayeredPane layeredPane;

	//Picture variables
	private File[] imageFiles;
	private	JLabel[] image; 
	private int currentImage = 1;
	private int IMAGE_COUNT;
	private int IMAGE_WIDTH;
	private int IMAGE_HEIGHT;
	private int h_space;

	public PictureSlider() {
		super("Picture Slider");
		setLayout(new BorderLayout());

		////////Add theme ////////
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {e.printStackTrace();}
		//////////////////////////
		mainPanel.setLayout(new GridBagLayout());
		add(menuBar, BorderLayout.NORTH);

		JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir"));
		//fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fileChooser.setMultiSelectionEnabled(true);
		fileChooser.setFileFilter(new FileFilter() {
			@Override
			public String getDescription() {
				return "Pictures or Directories";
			}

			//Taken from JFileChooser tutorial from the Java.
			@Override
			public boolean accept(File f) {
				if (f.isDirectory()) {
					return true;
				}

				String extension = Utils.getExtension(f);
				if (extension != null) {
					if (extension.equals(Utils.tiff) ||
							extension.equals(Utils.tif) ||
							extension.equals(Utils.gif) ||
							extension.equals(Utils.jpeg) ||
							extension.equals(Utils.jpg) ||
							extension.equals(Utils.png)) {
						return true;
					} else {
						return false;
					}
				}

				return false;
			}
		});

		JLabel welcomeLabel = new JLabel("Select the folder that contains all your pictures."
				+ " Also, resize this window before you load your pictures. Loading the pictures may take a while.");
		welcomeLabel.setHorizontalAlignment(JLabel.CENTER);
		welcomeLabel.setFont(new Font("Arial", Font.PLAIN, 15));
		welcomeLabel.setMaximumSize(new Dimension (700,500));
		add(welcomeLabel, BorderLayout.CENTER);

		JButton openDirectoryBtn = new JButton("Open");
		openDirectoryBtn.setFont(new Font("Arial", Font.PLAIN, 15));
		openDirectoryBtn.addActionListener( e -> {
			if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				remove(welcomeLabel);
				repaint();
				folder = fileChooser.getSelectedFile();
				loadImagesFromFolder(folder);
				if (IMAGE_COUNT > 0)
					loadImagesOnScreen();
				else {
					JOptionPane.showMessageDialog(this, "Could not find any pictures", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		menuBar.add(openDirectoryBtn);

		////////Settings for the frame ///////////
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
		setSize(1000, 600);
	}

	private void loadImagesFromFolder(final File folder) {
		imageFiles = folder.listFiles();
		int fileCounter = 0;
		for (File eachFile : imageFiles) {
			String extension = Utils.getExtension(eachFile);
			
			if (eachFile.isFile()) {
				if (extension.equals(Utils.tiff) ||
						extension.equals(Utils.tif) ||
						extension.equals(Utils.gif) ||
						extension.equals(Utils.jpeg) ||
						extension.equals(Utils.jpg) ||
						extension.equals(Utils.png)
				) {
					fileCounter++;
				}
			}
		}
		
		IMAGE_COUNT = fileCounter;
		initializeAndSizeImages();
		
		slideBar = new JSlider();
		slideBar.setMaximum(IMAGE_COUNT);
		slideBar.setMinimum(1);
		slideBar.addChangeListener(this);
		slideBar.setMajorTickSpacing(Math.round( (float) (IMAGE_COUNT * 0.10)));
		slideBar.setMinorTickSpacing(1);
		slideBar.setPaintTicks(true);
		slideBar.setPaintLabels(true);
		slideBar.setValue(0);
		gc.gridx = 0;
		gc.gridy = 1;
		gc.weightx = 1;
		gc.weighty = 1;
		gc.gridwidth = 7;
		mainPanel.add(slideBar, gc);
		add(slideBar, BorderLayout.SOUTH);
	}
	private void initializeAndSizeImages() {
		image = new JLabel[IMAGE_COUNT];
		IMAGE_WIDTH = this.getWidth() / 3;
		IMAGE_HEIGHT = IMAGE_WIDTH;
		h_space = Math.round((float)(IMAGE_WIDTH * .035));
		try {
			for (int index = 0; index < IMAGE_COUNT; index++) {
				BufferedImage img = ImageIO.read(imageFiles[index]);
				Image resizedImg = img.getScaledInstance(IMAGE_WIDTH, IMAGE_HEIGHT, Image.SCALE_AREA_AVERAGING);
				ImageIcon icon = new ImageIcon(resizedImg, imageFiles[index].getName());
				image[index] = new JLabel(icon);
				image[index].setBorder(BorderFactory.createLineBorder(Color.GRAY, 4, true));
				image[index].addMouseListener(this);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void loadImagesOnScreen() {
		layeredPane = new JLayeredPane();
		
		if (currentImage-3 >= 0 && currentImage-3 < IMAGE_COUNT) {
			image[currentImage-3].setBounds(h_space, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
			layeredPane.add(image[currentImage-3], new Integer(0));
		}
		
		if (currentImage-2 >= 0 && currentImage-2 < IMAGE_COUNT) {
			image[currentImage-2].setBounds( Math.round((float) (IMAGE_WIDTH*.465)) + h_space, 30, IMAGE_WIDTH, IMAGE_HEIGHT);
			layeredPane.add(image[currentImage-2], new Integer(1));
		}
		
		if (currentImage-1 >= 0 && currentImage-1 < IMAGE_COUNT) {
			image[currentImage-1].setBounds(Math.round((float) (IMAGE_WIDTH*.93))+h_space, 60, IMAGE_WIDTH, IMAGE_HEIGHT);
			layeredPane.add(image[currentImage-1], new Integer(4));
		}
		
		if (currentImage >= 0 && currentImage < IMAGE_COUNT) {
			image[currentImage].setBounds(Math.round((float) (IMAGE_WIDTH*1.395))+h_space, 30, IMAGE_WIDTH, IMAGE_HEIGHT);
			layeredPane.add(image[currentImage], new Integer(3));
		}
		if (currentImage+1 >= 0 && currentImage+1 < IMAGE_COUNT) {
			image[currentImage+1].setBounds(Math.round((float) (IMAGE_WIDTH*1.86))+h_space, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
			layeredPane.add(image[currentImage+1], new Integer(2));
		}
		
		add(layeredPane, BorderLayout.CENTER);
		repaint();
		revalidate();
	}


	@Override
	public void stateChanged(ChangeEvent e) {
		JSlider source = (JSlider) e.getSource();
		
		if (source.getValue() != currentImage) {
			currentImage = source.getValue();
			layeredPane.removeAll();
			loadImagesOnScreen();
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		int screen_width = java.awt.Toolkit.getDefaultToolkit().getScreenSize().width;
		int screen_height = java.awt.Toolkit.getDefaultToolkit().getScreenSize().height;
		int new_image_width = screen_width;
		int new_image_height = screen_height;
		new_image_width = Math.round( (float) (new_image_width * .6));
		new_image_height = Math.round( (float) (new_image_height * .6));
		
		
		JFrame popUp = new JFrame(imageFiles[currentImage-1].getName());
		JLabel lb;
		try {
			BufferedImage img = ImageIO.read(imageFiles[currentImage-1]);
			if (img.getHeight() > new_image_height || img.getWidth() > new_image_width) {
				Image resizedImg = img.getScaledInstance(new_image_width, screen_height, Image.SCALE_SMOOTH);
				lb = new JLabel(new ImageIcon(resizedImg));
			} else
				lb = new JLabel(new ImageIcon(img));
			lb.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					popUp.dispose();
				}
			});
			popUp.add(lb);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		popUp.setUndecorated(true);
		popUp.setVisible(true);
		popUp.pack();
		popUp.setLocation(20, 20);
		popUp.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	
	}
	
	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					new PictureSlider();
				} catch (Exception a) {
					JOptionPane.showMessageDialog(null, "Picture Slider encountered an error", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
	}


}
