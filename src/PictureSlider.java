import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

@SuppressWarnings("serial")
public class PictureSlider extends JFrame implements ChangeListener, MouseListener{

	private JToolBar toolBar;
	private JButton openDirectoryBtn; 
	private JButton resetBtn;
	private JFileChooser fileChooser;
	private JTextArea welcomeLabel;
	private JLabel picturePopUpLabel;
	private JSlider slideBar;
	private JLayeredPane layeredPane; 
	private JRadioButton[] radioButton;
	private ButtonGroup buttonGroup;
	private int loadOption;

	//Picture variables
	private ArrayList<File> files = new ArrayList<File>();
	private	JLabel[] images;
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
		} catch (Exception e) {System.out.println("System L&F not added.");}
		//////////////////////////

		///////File Chooser//////
		fileChooser = new JFileChooser(System.getProperty("user.dir"));
		fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fileChooser.setMultiSelectionEnabled(true);
		fileChooser.setFileFilter(new FileFilter() {
			@Override
			public String getDescription() {
				return "Folder or Multiple Pictures";
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
		///////////////////////

		///////Tool bar////////
		toolBar = new JToolBar();
		toolBar.setFloatable(false);
		///////////////////////

		///////Open button ////
		openDirectoryBtn = new JButton("Open");
		openDirectoryBtn.setToolTipText("Select Picture");
		openDirectoryBtn.setFont(new Font("Arial", Font.PLAIN, 14));
		openDirectoryBtn.addActionListener( e -> {
			if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				try {
					files.clear();
					File[] selectedFiles = fileChooser.getSelectedFiles();
					for (int index = 0; index < selectedFiles.length; index++) {
						if (selectedFiles[index].isDirectory()) {
							loadFromFolder(selectedFiles[index]);
						}
						if (selectedFiles[index].isFile()) {
							loadFromFile(selectedFiles[index]);
						}
					}
					IMAGE_COUNT = files.size();
					if (IMAGE_COUNT > 0) {
						remove(welcomeLabel);
						radioButton[0].setEnabled(false);
						radioButton[1].setEnabled(false);
						radioButton[2].setEnabled(false);
						openDirectoryBtn.setEnabled(false);
						initializeImgSize();
						if (radioButton[0].isSelected()) {
							loadOption = 0;
							setAllImages();
							loadAllImages();
						} else if (radioButton[1].isSelected()) {
							loadOption = 1;
							loadViewingImg();
						} else if (radioButton[2].isSelected() ) {
							loadOption = 2;
							setAllImages();
							loadAllImages(); 
						}
						addSlideBar();
						addPicturePopUpHint();
						repaint();
						revalidate();
					}
					else {
						throw new Exception("No Images Found.");
					}
				} catch(InvalidFileException e1) {
					JOptionPane.showMessageDialog(this, e1.getMessage(), "Error Opening Files", JOptionPane.ERROR_MESSAGE);
					e1.printStackTrace();
				} catch(OutOfMemoryError e3) {
					JOptionPane.showMessageDialog(this, "Out of memory: too many pictures to load", "Error", JOptionPane.ERROR_MESSAGE);
					e3.printStackTrace();
				} catch(Exception e2) {
					JOptionPane.showMessageDialog(this, "Something went wrong and couldn't load pictures", "Error", JOptionPane.ERROR_MESSAGE);
					e2.printStackTrace();
				}
			}
		});
		/////////////////////////
		
		//////Close Button///////////
		resetBtn = new JButton("Reset");
		resetBtn.setFont(new Font("Arial", Font.PLAIN, 14));
		resetBtn.setToolTipText("Resets the PictureSldier");
		resetBtn.addActionListener(e -> {
			toolBar.removeAll();
			getContentPane().removeAll();
			toolBar.add(openDirectoryBtn);
			toolBar.add(resetBtn);
			toolBar.addSeparator();
			toolBar.add(radioButton[0]);
			toolBar.add(radioButton[1]);
			toolBar.add(radioButton[2]);
			add(toolBar, BorderLayout.NORTH);
			add(welcomeLabel, BorderLayout.CENTER);
			radioButton[0].setEnabled(true);
			radioButton[1].setEnabled(true);
			radioButton[2].setEnabled(true);
			openDirectoryBtn.setEnabled(true);
			repaint();
			revalidate();
		});
		//////////////////////////////
		
		/////////Radio Button////////
		radioButton = new JRadioButton[3];
		radioButton[0] = new JRadioButton("Good Quality (SLOW load, HUGE memory, No lag)");
		radioButton[1] = new JRadioButton("Med Quality (FAST load, minimal memory, LAGGY)");
		radioButton[2] = new JRadioButton("Med Quality (MEDIUM load, SMALL memory, No lag)");
		radioButton[2].setSelected(true);
		buttonGroup = new ButtonGroup();
		buttonGroup.add(radioButton[0]);
		buttonGroup.add(radioButton[1]);
		buttonGroup.add(radioButton[2]);
		/////////////////////////////

		//////////Welcome Label//////////
		String welcomeMessage = "\n\nHow to Use Picture Slider\n\n"
				+ "** Click Open and select the folder that contains all your pictures.\n"
				+ "    Alternatively, you can select multiple picture files.\n\n"
				+ "** Resize the application before opening files as they won't be resized after opened.\n\n"
				+ "** Loading images can take a while. It may appear to be frozen but it is actually just loading images.\n"
				+ "    Load up speed varies on the selected load up option.\n"
				+ "    Should take about 1 minute for 200 pictures with third option while it takes 1 min for 80 pictures with the first option.\n\n"
				+ "** After images load, you can click on the image to make view the original size image of the currently viewing image.\n"
				+ "    Click on the pop up to close.\n\n";
		
		welcomeLabel = new JTextArea(welcomeMessage);
		welcomeLabel.setFont(new Font("Arial", Font.PLAIN, 17));
		welcomeLabel.setMaximumSize(new Dimension (700,500));
		welcomeLabel.setEditable(false);
		welcomeLabel.setOpaque(false);
		welcomeLabel.setWrapStyleWord(true);
		welcomeLabel.setLineWrap(true);
		//////////////////////////////////


		/////Add all to frame///
		toolBar.add(openDirectoryBtn);
		toolBar.add(resetBtn);
		toolBar.addSeparator();
		toolBar.add(radioButton[0]);
		toolBar.add(radioButton[1]);
		toolBar.add(radioButton[2]);
		add(toolBar, BorderLayout.NORTH);
		add(welcomeLabel, BorderLayout.CENTER);
		////////////////////////


		////////Settings for the frame ///////////
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
		setSize(1000, 540);
		//////////////////////////////////////////
	}

	private void loadFromFolder(final File folder) throws InvalidFileException{
		File[] filesInFolder = folder.listFiles();
		for (File eachFile : filesInFolder) {
			loadFromFile(eachFile);
		}
	}
	private void loadFromFile(final File file) throws InvalidFileException{
		String extension = Utils.getExtension(file);
		try {
			if (file.isFile()) {
				if (extension.equals(Utils.tiff) ||
						extension.equals(Utils.tif) ||
						extension.equals(Utils.gif) ||
						extension.equals(Utils.jpeg) ||
						extension.equals(Utils.jpg) ||
						extension.equals(Utils.png)
						) {
					System.out.println(file.getName());
					files.add(file);
				} 
			}
		} catch (Exception e) {
			throw new InvalidFileException();
		}
	}
	private void addSlideBar() {
		slideBar = new JSlider();
		slideBar.setMaximum(IMAGE_COUNT);
		slideBar.setMinimum(1);
		slideBar.addChangeListener(this);
		slideBar.setMajorTickSpacing(Math.round( (float) (IMAGE_COUNT * 0.10)));
		slideBar.setMinorTickSpacing(1);
		slideBar.setPaintTicks(true);
		slideBar.setPaintLabels(true);
		slideBar.setValue(0);
		add(slideBar, BorderLayout.SOUTH);
	}
	private void addPicturePopUpHint() {
		picturePopUpLabel = new JLabel("Click on the image to pop up.");
		picturePopUpLabel.setHorizontalAlignment(JLabel.CENTER);
		picturePopUpLabel.setVerticalAlignment(JLabel.BOTTOM);
		add(picturePopUpLabel, BorderLayout.CENTER);
	}
	
	private void initializeImgSize() {
		IMAGE_WIDTH = this.getWidth() / 3;
		IMAGE_HEIGHT = IMAGE_WIDTH;
		h_space = Math.round((float)(IMAGE_WIDTH * .035));
	}
	
	private void loadViewingImg() {
		layeredPane = new JLayeredPane();
		double width_multiplier = 0.465;
		
		try {
			if (currentImage-3 >= 0 && currentImage-3 < IMAGE_COUNT) {
				BufferedImage img = ImageIO.read(files.get(currentImage-3));
				Image resizedImg = img.getScaledInstance(IMAGE_WIDTH, IMAGE_HEIGHT, Image.SCALE_FAST);
				ImageIcon icon = new ImageIcon(resizedImg, files.get(currentImage-3).getName());
				JLabel imgLabel = new JLabel(icon);
				imgLabel.addMouseListener(this);
				imgLabel.setBounds(h_space, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
				layeredPane.add(imgLabel, new Integer(0));
			}

			if (currentImage-2 >= 0 && currentImage-2 < IMAGE_COUNT) {
				BufferedImage img = ImageIO.read(files.get(currentImage-2));
				Image resizedImg = img.getScaledInstance(IMAGE_WIDTH, IMAGE_HEIGHT, Image.SCALE_FAST);
				ImageIcon icon = new ImageIcon(resizedImg, files.get(currentImage-2).getName());
				JLabel imgLabel = new JLabel(icon);
				imgLabel.addMouseListener(this);
				imgLabel.setBounds( Math.round((float) (IMAGE_WIDTH*width_multiplier)) + h_space, 30, IMAGE_WIDTH, IMAGE_HEIGHT);
				layeredPane.add(imgLabel, new Integer(1));
			}

			if (currentImage-1 >= 0 && currentImage-1 < IMAGE_COUNT) {
				BufferedImage img = ImageIO.read(files.get(currentImage-1));
				Image resizedImg = img.getScaledInstance(IMAGE_WIDTH, IMAGE_HEIGHT, Image.SCALE_FAST);
				ImageIcon icon = new ImageIcon(resizedImg, files.get(currentImage-1).getName());
				JLabel imgLabel = new JLabel(icon);
				imgLabel.addMouseListener(this);
				imgLabel.setBounds(Math.round((float) (IMAGE_WIDTH*width_multiplier*2))+h_space, 60, IMAGE_WIDTH, IMAGE_HEIGHT);
				layeredPane.add(imgLabel, new Integer(4));
			}

			if (currentImage >= 0 && currentImage < IMAGE_COUNT) {
				BufferedImage img = ImageIO.read(files.get(currentImage));
				Image resizedImg = img.getScaledInstance(IMAGE_WIDTH, IMAGE_HEIGHT, Image.SCALE_FAST);
				ImageIcon icon = new ImageIcon(resizedImg, files.get(currentImage).getName());
				JLabel imgLabel = new JLabel(icon);
				imgLabel.addMouseListener(this);
				imgLabel.setBounds(Math.round((float) (IMAGE_WIDTH*width_multiplier*3))+h_space, 30, IMAGE_WIDTH, IMAGE_HEIGHT);
				layeredPane.add(imgLabel, new Integer(3));
			}
			if (currentImage+1 >= 0 && currentImage+1 < IMAGE_COUNT) {
				BufferedImage img = ImageIO.read(files.get(currentImage+1));
				Image resizedImg = img.getScaledInstance(IMAGE_WIDTH, IMAGE_HEIGHT, Image.SCALE_FAST);
				ImageIcon icon = new ImageIcon(resizedImg, files.get(currentImage+1).getName());
				JLabel imgLabel = new JLabel(icon);
				imgLabel.addMouseListener(this);
				imgLabel.setBounds(Math.round((float) (IMAGE_WIDTH*width_multiplier*4))+h_space, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
				layeredPane.add(imgLabel, new Integer(2));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		add(layeredPane, BorderLayout.CENTER);
	}
	
	private void setAllImages() {
		images = new JLabel[IMAGE_COUNT];
//		org.apache.commons.lang3.time.StopWatch stopWatch = new org.apache.commons.lang3.time.StopWatch();
//		stopWatch.start();
		try {
			ImageIcon icon;
			
			for (int index = 0; index < IMAGE_COUNT; index++) {
				BufferedImage img = ImageIO.read(files.get(index));
				if (loadOption == 0) {
					Image scaledImg = img.getScaledInstance(IMAGE_WIDTH, IMAGE_HEIGHT, Image.SCALE_AREA_AVERAGING);
					icon = new ImageIcon(scaledImg, files.get(index).getName());
				} else {
					BufferedImage drawnImg = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
					drawnImg.getGraphics().drawImage(img, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, null);
					icon = new ImageIcon(drawnImg, files.get(index).getName());
				}
				images[index] = new JLabel(icon);
				images[index].setBorder(BorderFactory.createLineBorder(Color.GRAY, 4, true));
				images[index].addMouseListener(this);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
//		stopWatch.stop();
		
//		System.out.println("Duration to load pictures: " + stopWatch.toString());
	}

	private void loadAllImages() {
		layeredPane = new JLayeredPane();

		if (currentImage-3 >= 0 && currentImage-3 < IMAGE_COUNT) {
			images[currentImage-3].setBounds(h_space, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
			layeredPane.add(images[currentImage-3], new Integer(0));
		}

		if (currentImage-2 >= 0 && currentImage-2 < IMAGE_COUNT) {
			images[currentImage-2].setBounds( Math.round((float) (IMAGE_WIDTH*.465)) + h_space, 30, IMAGE_WIDTH, IMAGE_HEIGHT);
			layeredPane.add(images[currentImage-2], new Integer(1));
		}

		if (currentImage-1 >= 0 && currentImage-1 < IMAGE_COUNT) {
			images[currentImage-1].setBounds(Math.round((float) (IMAGE_WIDTH*.93))+h_space, 60, IMAGE_WIDTH, IMAGE_HEIGHT);
			layeredPane.add(images[currentImage-1], new Integer(4));
		}

		if (currentImage >= 0 && currentImage < IMAGE_COUNT) {
			images[currentImage].setBounds(Math.round((float) (IMAGE_WIDTH*1.395))+h_space, 30, IMAGE_WIDTH, IMAGE_HEIGHT);
			layeredPane.add(images[currentImage], new Integer(3));
		}
		if (currentImage+1 >= 0 && currentImage+1 < IMAGE_COUNT) {
			images[currentImage+1].setBounds(Math.round((float) (IMAGE_WIDTH*1.86))+h_space, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
			layeredPane.add(images[currentImage+1], new Integer(2));
		}

		add(layeredPane, BorderLayout.CENTER);
	}
	

	@Override
	public void stateChanged(ChangeEvent e) {
		JSlider source = (JSlider) e.getSource();

		if (source.getValue() != currentImage) {
			currentImage = source.getValue();
			layeredPane.removeAll();
			if (loadOption == 0 || loadOption == 2) {
				loadAllImages();
			} else if (loadOption == 1) {
				loadViewingImg();
			}
		}
		repaint();
		revalidate();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		int screen_width = java.awt.Toolkit.getDefaultToolkit().getScreenSize().width;
		int screen_height = java.awt.Toolkit.getDefaultToolkit().getScreenSize().height;
		int new_image_width = screen_width;
		int new_image_height = screen_height;
		new_image_width = Math.round( (float) (new_image_width * .8));
		new_image_height = Math.round( (float) (new_image_height * .8));


		JFrame popUp = new JFrame(files.get(currentImage-1).getName());
		JLabel lb;
		try {
			BufferedImage img = ImageIO.read(files.get(currentImage-1));
			System.out.println("Img.getHeight(): " + img.getHeight());
			System.out.println("new_image_height: " + new_image_height);
			System.out.println("Img.getWidth(): " + img.getWidth());
			System.out.println("new_image_width: " + new_image_width);
			if (img.getHeight() > new_image_height || img.getWidth() > new_image_width) {
				Image resizedImg = img.getScaledInstance(-1, new_image_height, Image.SCALE_SMOOTH);
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
		//		popUp.setUndecorated(true);
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
					a.printStackTrace();
				}
			}
		});
	}


}
