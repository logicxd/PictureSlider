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
	private JLabel welcomeLabel;
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
						openDirectoryBtn.setEnabled(false);
						initializeImgSize();
						if (radioButton[0].isSelected()) {
							loadOption = 0;
							setAllImages();
							loadAllImages();
						} else if (radioButton[1].isSelected()) {
							loadOption = 1;
							loadViewingImg();
						}
						addSlideBar();
						repaint();
						revalidate();
					}
					else {
						throw new Exception("No Images Found.");
					}
				} catch(InvalidFileException e1) {
					JOptionPane.showMessageDialog(this, e1.getMessage(), "Error Opening Files", JOptionPane.ERROR_MESSAGE);
					e1.printStackTrace();
				} catch(Exception e2) {
					JOptionPane.showMessageDialog(this, "Couldn't find any pictures", "Error", JOptionPane.ERROR_MESSAGE);
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
			add(toolBar, BorderLayout.NORTH);
			add(welcomeLabel, BorderLayout.CENTER);
			radioButton[0].setEnabled(true);
			radioButton[1].setEnabled(true);
			openDirectoryBtn.setEnabled(true);
			repaint();
			revalidate();
		});
		//////////////////////////////
		
		/////////Radio Button////////
		radioButton = new JRadioButton[2];
		radioButton[0] = new JRadioButton("Load all images selected (SLOW LOAD TIME)");
		radioButton[0].setSelected(true);
		radioButton[1] = new JRadioButton("Load only the pictures showing (LAGGY WHEN CHANGING PICTURES)");
		buttonGroup = new ButtonGroup();
		buttonGroup.add(radioButton[0]);
		buttonGroup.add(radioButton[1]);
		/////////////////////////////

		//////////Welcome Label//////////
		welcomeLabel = new JLabel("Select the folder that contains all your pictures."
				+ " Also, resize this window before you load your pictures. Loading the pictures may take a while.");
		welcomeLabel.setHorizontalAlignment(JLabel.CENTER);
		welcomeLabel.setFont(new Font("Arial", Font.PLAIN, 15));
		welcomeLabel.setMaximumSize(new Dimension (700,500));
		//////////////////////////////////


		/////Add all to frame///
		toolBar.add(openDirectoryBtn);
		toolBar.add(resetBtn);
		toolBar.addSeparator();
		toolBar.add(radioButton[0]);
		toolBar.add(radioButton[1]);
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
		try {
			for (int index = 0; index < IMAGE_COUNT; index++) {
				BufferedImage img = ImageIO.read(files.get(index));
				Image resizedImg = img.getScaledInstance(IMAGE_WIDTH, IMAGE_HEIGHT, Image.SCALE_AREA_AVERAGING);
				ImageIcon icon = new ImageIcon(resizedImg, files.get(index).getName());
				images[index] = new JLabel(icon);
				images[index].setBorder(BorderFactory.createLineBorder(Color.GRAY, 4, true));
				images[index].addMouseListener(this);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
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
			if (loadOption == 0) {
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
