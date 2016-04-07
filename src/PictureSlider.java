import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import net.coobird.thumbnailator.Thumbnails;

//Copyright 2016 Aung Moe
//
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
//
//For library used: net.coobird.thumbnailator (Maven)
//Github: https://github.com/coobird/thumbnailator



@SuppressWarnings("serial")
public class PictureSlider extends JFrame implements ChangeListener, MouseListener{

	//Load and Layout
	private JFileChooser fileChooser;
	private int loadOption;
	private JLayeredPane layeredPane; 
	private JSlider slideBar;
	
	private JPanel toolPanel;
	private JToolBar toolBar;
	private JButton openDirectoryBtn; 
	private JButton resetBtn;
	private JRadioButton[] radioButton;
	private ButtonGroup buttonGroup;
	private JMenuBar menuBar;
	private JMenu optionsMenu;
	private JCheckBoxMenuItem sliderCheckBox;
	private JCheckBoxMenuItem reorientCheckBox;

	//Labels
	private JTextArea welcomeLabel;
	private JLabel picturePopUpLabel;
	
	//Picture variables
	private ArrayList<File> files = new ArrayList<File>();
	private	JLabel[] images;
	private int currentImage = 1;
	private int IMAGE_COUNT;
	private int IMAGE_WIDTH;
	private int IMAGE_HEIGHT;
	private int h_space;
	private boolean[] viewingImgSet;

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

		/////Tool Panel////////
		toolPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,5,5));
		//////////////////////
		
		///////Tool bar////////
		toolBar = new JToolBar();
		toolBar.setFloatable(false);
		///////////////////////

		///////Open button ////
		openDirectoryBtn = new JButton("Open");
		openDirectoryBtn.setToolTipText("Select Picture");
		openDirectoryBtn.setFont(new Font("Arial", Font.PLAIN, 14));
		openDirectoryBtn.addActionListener( new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
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
							reorientCheckBox.setEnabled(false);
							viewingImgSet = new boolean[IMAGE_COUNT];
							initializeImgSize();
							setAllImageFrames();
							if (radioButton[0].isSelected()) {
								loadOption = 0;
								showAllImageFrames(); 
								startImageThread(0, IMAGE_COUNT-1);
							} else if (radioButton[1].isSelected()) {
								loadOption = 1;
								showAllImageFrames();
								if (IMAGE_COUNT >= 3)
									startImageThread(0, 2);
								else
									startImageThread(0, IMAGE_COUNT-1);
							} else if (radioButton[2].isSelected() ) {
								loadOption = 2;
								showAllImageFrames(); 
								startImageThread(0, IMAGE_COUNT-1);
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
						JOptionPane.showMessageDialog(null, e1.getMessage(), "Error Opening Files", JOptionPane.ERROR_MESSAGE);
						e1.printStackTrace();
					} catch(OutOfMemoryError e3) {
						JOptionPane.showMessageDialog(null, "Out of memory: too many pictures to load", "Error", JOptionPane.ERROR_MESSAGE);
						e3.printStackTrace();
					} catch(IOException e4) {
						JOptionPane.showMessageDialog(null, e4.getMessage(), "Error Opening Picture(s)", JOptionPane.ERROR_MESSAGE);
						e4.printStackTrace();
					}
					catch(Exception e2) {
						JOptionPane.showMessageDialog(null, "Something went wrong and couldn't load pictures", "Error", JOptionPane.ERROR_MESSAGE);
						e2.printStackTrace();
					}
				}
			}
		});
		/////////////////////////
		
		//////Reset Button///////////
		resetBtn = new JButton("Reset");
		resetBtn.setFont(new Font("Arial", Font.PLAIN, 14));
		resetBtn.setToolTipText("Resets the PictureSldier");
		resetBtn.addActionListener( new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				getContentPane().removeAll();		
				add(toolPanel, BorderLayout.NORTH);
				add(welcomeLabel, BorderLayout.CENTER);
				radioButton[0].setEnabled(true);
				radioButton[1].setEnabled(true);
				radioButton[2].setEnabled(true);
				openDirectoryBtn.setEnabled(true);
				reorientCheckBox.setEnabled(true);
				repaint();
				revalidate();				
			}
		});
		//////////////////////////////
		
		/////////Radio Button////////
		radioButton = new JRadioButton[3];
		radioButton[0] = new JRadioButton("Good Quality");
		radioButton[1] = new JRadioButton("Load Viewing");
		radioButton[2] = new JRadioButton("Load all");
		radioButton[2].setSelected(true);
		radioButton[0].setFont(new Font("Arial", Font.PLAIN, 14));
		radioButton[1].setFont(new Font("Arial", Font.PLAIN, 14));
		radioButton[2].setFont(new Font("Arial", Font.PLAIN, 14));
		buttonGroup = new ButtonGroup();
		buttonGroup.add(radioButton[0]);
		buttonGroup.add(radioButton[1]);
		buttonGroup.add(radioButton[2]);
		/////////////////////////////
		
		/////JMenuBar///////////////
		menuBar = new JMenuBar();
		////////////////////////////
		
		/////Menu Option//////////////
		optionsMenu = new JMenu("Options");
		optionsMenu.setFont(new Font("Arial", Font.PLAIN, 14));
		/////////////////////////////
		
		//////Slider Check Box ////////
		sliderCheckBox = new JCheckBoxMenuItem("Move pictures together with the slider");
		sliderCheckBox.setFont(new Font("Arial", Font.PLAIN, 14));
		sliderCheckBox.setSelected(true);
		////////////////////////////////
		
		//////Slider Check Box ////////
		reorientCheckBox = new JCheckBoxMenuItem("Rotate Pictures");
		reorientCheckBox.setFont(new Font("Arial", Font.PLAIN, 14));
		reorientCheckBox.setSelected(false);
		////////////////////////////////

		//////////Welcome Label//////////
		String welcomeMessage = "\nHow to Use Picture Slider\n\n"
				+ "** Click Open and select the folder that contains all your pictures.\n"
				+ "    Alternatively, you can select multiple picture files.\n"
				+ "    Make sure that there are at least 2 or more pictures, otherwise it won't work.\n"
				+ "** Resize the application before opening files as they won't be resized after opened.\n"
				+ "** Picture Slider can use a lot of memory when there are many pictures loaded.\n\n"
				+ " Load Option   Speed  Memory  Desciption\n"
				+ "- - - - - - - - - - - - - - - - - - - - - - - - - - - - -\n"
				+ "*Good Quality  Slow   Big     Loads all in high quality\n"
				+ "*Load Viewing  Fast   Medium  Loads viewing pictures in med quality\n"
				+ "*Load All      Fast   Medium  Loads all pictures in med quality\n"
				+ "+'Rotate'      Slower Bigger  Gets the correct picture orientation\n";
		
		welcomeLabel = new JTextArea(welcomeMessage);
		welcomeLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 17));
		welcomeLabel.setMaximumSize(new Dimension (700,500));
		welcomeLabel.setEditable(false);
		welcomeLabel.setOpaque(false);
		welcomeLabel.setWrapStyleWord(true);
		welcomeLabel.setLineWrap(true);
		//////////////////////////////////


		/////Add all to frame///
		optionsMenu.add(sliderCheckBox);
		optionsMenu.add(reorientCheckBox);
		menuBar.add(optionsMenu);
		toolPanel.add(menuBar);
		toolBar.addSeparator();
		toolBar.add(openDirectoryBtn);
		toolBar.add(resetBtn);
		toolBar.addSeparator();
		toolBar.add(radioButton[0]);
		toolBar.add(radioButton[1]);
		toolBar.add(radioButton[2]);
		toolPanel.add(toolBar);
		add(toolPanel, BorderLayout.NORTH);
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
	
	private void setAllImageFrames() {
		images = new JLabel[IMAGE_COUNT];
		
		for (int index = 0; index < IMAGE_COUNT; index++) {
			images[index] = new JLabel();
			images[index].setBorder(BorderFactory.createLineBorder(Color.GRAY, 4, true));
			images[index].addMouseListener(this);
			images[index].setOpaque(true);
		}
	}

	private void showAllImageFrames() {
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
			layeredPane.add(images[currentImage-1], new Integer(2));
		}

		if (currentImage >= 0 && currentImage < IMAGE_COUNT) {
			images[currentImage].setBounds(Math.round((float) (IMAGE_WIDTH*1.395))+h_space, 30, IMAGE_WIDTH, IMAGE_HEIGHT);
			layeredPane.add(images[currentImage], new Integer(1));
		}
		if (currentImage+1 >= 0 && currentImage+1 < IMAGE_COUNT) {
			images[currentImage+1].setBounds(Math.round((float) (IMAGE_WIDTH*1.86))+h_space, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
			layeredPane.add(images[currentImage+1], new Integer(0));
		}

		add(layeredPane, BorderLayout.CENTER);
	}
	
	private void startImageThread(int startingIndex, int endingIndex) {
		for (int index = startingIndex; index <= endingIndex; index++) {
			if (index >= 0 && index < IMAGE_COUNT && !viewingImgSet[index]) {
				new ImageLoader(index).execute();
			}
		}
	}

	public void stateChanged(ChangeEvent e) {
		JSlider source = (JSlider) e.getSource();
		boolean moveWithSlider = sliderCheckBox.isSelected();
		boolean change = false;
		
		if (moveWithSlider) {
			if (source.getValue() != currentImage) {
				change = true;
			}
		} else {
			if (!source.getValueIsAdjusting()) {
				change = true;
			}
		}
		
		if (change) {
			currentImage = source.getValue();
			layeredPane.removeAll();
			if (loadOption == 0 || loadOption == 2) {
				showAllImageFrames();
			} else if (loadOption == 1) {
				showAllImageFrames();
				startImageThread(currentImage-3, currentImage+1);
			}
		}
		repaint();
		revalidate();
	}

	public void mouseClicked(MouseEvent e) {
		int screen_width = java.awt.Toolkit.getDefaultToolkit().getScreenSize().width;
		int screen_height = java.awt.Toolkit.getDefaultToolkit().getScreenSize().height;
		int new_image_width = screen_width;
		int new_image_height = screen_height;
		new_image_width = Math.round( (float) (new_image_width * .8));
		new_image_height = Math.round( (float) (new_image_height * .8));


		final JFrame popUp = new JFrame(files.get(currentImage-1).getName());
		JLabel lb;
		try {
			BufferedImage img = Thumbnails.of(files.get(currentImage-1)).scale(1).asBufferedImage();
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

	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	public void mousePressed(MouseEvent e) {
		
	}

	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	class ImageLoader extends SwingWorker<ImageIcon, Void> {
		private int index;
		
		public ImageLoader(int index) {
			this.index = index;
		}
		
		@Override
		protected ImageIcon doInBackground() throws IOException{
			ImageIcon icon;
			BufferedImage img;
			
			if (reorientCheckBox.isSelected()) {
				img = Thumbnails.of(files.get(index)).scale(1).asBufferedImage();
			} else {
				img = ImageIO.read(files.get(index));
			}
			
			if (loadOption == 0) {
				Image scaledImg = img.getScaledInstance(IMAGE_WIDTH, IMAGE_HEIGHT, Image.SCALE_AREA_AVERAGING);
				icon = new ImageIcon(scaledImg, files.get(index).getName());
			} else {
				BufferedImage drawnImg = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_ARGB);
				drawnImg.getGraphics().drawImage(img, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, null);
				icon = new ImageIcon(drawnImg, files.get(index).getName());
			}

			viewingImgSet[index] = true;
			return icon;
		}

		@Override
		protected void done() {
			ImageIcon icon;
			try {
				icon = get();
				images[index].setIcon(icon);
				images[index].repaint();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
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
