import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
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
public class PictureSlider extends JFrame implements ChangeListener{

	//Load and Layout
	private BorderLayout layout;
	private JFileChooser fileChooser;
	private int loadOption;					//Values can be: 0 - Good quality, 1 -  Load Viewing, 2 - Load all. 
	private JLayeredPane layeredPane;
	private JSlider slideBar;

	private JPanel toolPanel;
	private JToolBar toolBar;
	private JButton openDirectoryBtn; 
	private JButton resetBtn;
	private JRadioButton[] radioButton;
	private ButtonGroup buttonGroup;

	private JLabel changeViewLabel;
	private JComboBox<String> changeViewComboBox;	//Index 0 - Single. Index 1 - Multi
	private JLabel ratioLabel;
	private JComboBox<String> ratioComboBox;	//Index 0 - 16:9. Index 1 - 4:3. Index 2 - 1:1.

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
	private boolean[] viewingImgSet;
	private ArrayList<Integer> viewingMultiHolder;
	private ArrayList<ImageLoader> viewingSingleLoader;
	private JLabel viewingSingleImage;
	private JProgressBar progressBar;
	
	private int currentImage = 1;
	private int IMAGE_COUNT;
	private int IMAGE_WIDTH;
	private int IMAGE_HEIGHT;
	private int h_space;
	private double IMAGE_WIDTH_POS;
	private int IMAGE_HEIGHT_POS;

	//Timer

	public PictureSlider() {
		super("Picture Slider");
		layout = new BorderLayout();
		setLayout(layout);

		////////Add theme ////////
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			UIManager.put("ProgressBar.cycleTime", new Integer(1000));
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

						//There's at least one picture. 
						if (IMAGE_COUNT > 0) 
						{
							remove(welcomeLabel);
							changeViewComboBox.setEnabled(false);
							ratioComboBox.setEnabled(false);
							radioButton[0].setEnabled(false);
							radioButton[1].setEnabled(false);
							radioButton[2].setEnabled(false);
							openDirectoryBtn.setEnabled(false);
							reorientCheckBox.setEnabled(false);
							viewingImgSet = new boolean[IMAGE_COUNT];
							currentImage = 1;
							
							//If single viewing is selected.
							if ("Single".equals((String) changeViewComboBox.getSelectedItem()))
							{ 
								if (radioButton[0].isSelected()) {
									loadOption = 0;
									initializeSingleImgSize();
									setAllImageFrames();  
									showSingleImageFrame();
									startMultiImageThread(0, IMAGE_COUNT-1);
								} else if (radioButton[1].isSelected()) {
									loadOption = 1;
									initializeSingleImgSize();
									setAllImageFrames();  
									showSingleImageFrame();
									viewingSingleLoader = new ArrayList<ImageLoader>();
									viewingSingleLoader.add(new ImageLoader(0, 0));
									viewingSingleLoader.get(0).execute();
									viewingSingleLoader.remove(0);
								} else if (radioButton[2].isSelected() ) {
									loadOption = 2;
									initializeSingleImgSize();
									setAllImageFrames();  
									showSingleImageFrame();
									startMultiImageThread(0, IMAGE_COUNT-1);
								}
								addSlideBar();
							}
							
							//If multiple viewing is selected. 
							else if ("Multi".equals((String) changeViewComboBox.getSelectedItem()))
							{
								if (radioButton[0].isSelected()) {
									loadOption = 0;
									initializeMultiImgSize();
									setAllImageFrames();
									showAllImageFrames(); 
									startMultiImageThread(0, IMAGE_COUNT-1);
								} else if (radioButton[1].isSelected()) {
									loadOption = 1;
									initializeMultiImgSize();
									setAllImageFrames();
									showAllImageFrames();
									viewingMultiHolder = new ArrayList<Integer>();
									if (IMAGE_COUNT <= 3)
										startMultiImageThread(0, IMAGE_COUNT-1);
									else 
										startMultiImageThread(0, 2);
								} else if (radioButton[2].isSelected() ) {
									loadOption = 2;
									initializeMultiImgSize();
									setAllImageFrames();
									showAllImageFrames(); 
									startMultiImageThread(0, IMAGE_COUNT-1);
								}
								addSlideBar();
								addPicturePopUpHint();
							}
							
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
						JOptionPane.showMessageDialog(null, "Something went wrong: " + e2.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
				changeViewComboBox.setEnabled(true);
				if ("Multi".equals((String)changeViewComboBox.getSelectedItem()))
					ratioComboBox.setEnabled(true);
				else
					ratioComboBox.setEnabled(false);
				radioButton[0].setEnabled(true);
				radioButton[1].setEnabled(true);
				radioButton[2].setEnabled(true);
				openDirectoryBtn.setEnabled(true);
				reorientCheckBox.setEnabled(true);
				if (radioButton[1].isSelected())
					sliderCheckBox.setEnabled(false);
				else
					sliderCheckBox.setEnabled(true);
				images = null;
				repaint();
				revalidate();				
			}
		});
		//////////////////////////////

		///////Change View Label/////////
		changeViewLabel = new JLabel("View: ");
		changeViewLabel.setFont(new Font("Arial", Font.PLAIN, 14));
		/////////////////////////////////

		///////Change View Drop Down Selector//////
		changeViewComboBox = new JComboBox<String>();
		changeViewComboBox.addItem("Single");
		changeViewComboBox.addItem("Multi");
		changeViewComboBox.setEditable(false);
		changeViewComboBox.setSelectedIndex(0);
		changeViewComboBox.setFont(new Font("Arial", Font.PLAIN, 14));
		changeViewComboBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				String changedItem = (String)e.getItem();
				
				if (changedItem.equals("Single")) {
					ratioComboBox.setEnabled(false);
				} else {
					ratioComboBox.setEnabled(true);
				}
			}
		});
		///////////////////////////////

		/////Ratio Label////
		ratioLabel = new JLabel("Ratio: ");
		ratioLabel.setFont(new Font("Arial", Font.PLAIN, 14));

		/////////////////////////

		/////Ratio Drop Down Selector/////
		ratioComboBox = new JComboBox<String>();
		ratioComboBox.addItem("16:9");
		ratioComboBox.addItem("4:3");
		ratioComboBox.addItem("1:1");
		ratioComboBox.setEditable(false);
		ratioComboBox.setSelectedIndex(0);
		ratioComboBox.setFont(new Font("Arial", Font.PLAIN, 14));
		ratioComboBox.setEnabled(false);
		/////////////////////////////////////

		/////////Radio Button////////
		radioButton = new JRadioButton[3];
		radioButton[0] = new JRadioButton("Good Quality");
		radioButton[1] = new JRadioButton("Load Viewing");
		radioButton[1].setSelected(true);
		radioButton[2] = new JRadioButton("Load all");
		radioButton[0].setFont(new Font("Arial", Font.PLAIN, 14));
		radioButton[1].setFont(new Font("Arial", Font.PLAIN, 14));
		radioButton[2].setFont(new Font("Arial", Font.PLAIN, 14));
		radioButton[1].addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
			    if (e.getStateChange() == ItemEvent.SELECTED) {
			    	sliderCheckBox.setEnabled(false);
			    } else if (e.getStateChange() == ItemEvent.DESELECTED) {
			    	sliderCheckBox.setEnabled(true);
			    }
			}
		});
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
		reorientCheckBox.setSelected(true);
		////////////////////////////////
		
		///////Progress Bar ///////////
		progressBar = new JProgressBar();
		progressBar.setPreferredSize(new Dimension(80,20));
		/////////////////////////////////

		//////////Welcome Label//////////
		String welcomeMessage = "\nHow to Use Picture Slider\n\n"
				+ "** Click Open and select the folder that contains all your pictures.\n"
				+ "    Alternatively, you can select multiple picture files.\n"
				+ "** Resize the application before opening files as they won't be resized after opened.\n\n"
				+ " Load Option   Speed     Memory  Desciption\n"
				+ "- - - - - - - - - - - - - - - - - - - - - - - - - - - - -\n"
				+ "*Good Quality  Slow      Big     Loads all in high quality\n"
				+ "*Load Viewing  Fast-Med  Low     Loads just the viewing pictures in good quality\n"
				+ "*Load All      Med-Fast  Med     Loads all pictures in good quality\n\n"
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
		toolBar.add(changeViewLabel);
		toolBar.add(changeViewComboBox);
		toolBar.add(ratioLabel);
		toolBar.add(ratioComboBox);
		toolBar.addSeparator();
		toolBar.add(radioButton[0]);
		toolBar.add(radioButton[1]);
		toolBar.add(radioButton[2]);
		toolBar.addSeparator();
		toolPanel.add(toolBar);
		toolPanel.add(progressBar);
		add(toolPanel, BorderLayout.NORTH);
		add(welcomeLabel, BorderLayout.CENTER);
		////////////////////////


		////////Settings for the frame ///////////
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
		setSize(1000, 540);
		//////////////////////////////////////////
	}

	//Loads pictures from the folder. 
	private void loadFromFolder(final File folder) throws InvalidFileException{
		File[] filesInFolder = folder.listFiles();
		for (File eachFile : filesInFolder) {
			loadFromFile(eachFile);
		}
	}

	//Loads pictures from a file.
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

	
	//Add the slider bar.
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

	
	//Adds the hint at the bottom of the picture slider.
	private void addPicturePopUpHint() {
		picturePopUpLabel = new JLabel("Click on the image to pop up.");
		picturePopUpLabel.setHorizontalAlignment(JLabel.CENTER);
		picturePopUpLabel.setVerticalAlignment(JLabel.BOTTOM);
		add(picturePopUpLabel, BorderLayout.CENTER);
	}
	
		
	//Sets the width and height of the pictures.
	//Changes IMAGE_WIDTH, IMAGE_HEIGHT, and h_space.
	private void initializeMultiImgSize() {

		//Ratio 16:9
		if (ratioComboBox.getSelectedIndex() == 0) {
			IMAGE_WIDTH = Math.round( (float) (this.getWidth() / 1.65) );
			IMAGE_HEIGHT = (IMAGE_WIDTH * 9) / 16;
			h_space = Math.round( (float) (IMAGE_WIDTH * .035 * 9) / 16);
			IMAGE_WIDTH_POS = 0.145;
			IMAGE_HEIGHT_POS = 30;
		}

		//Ratio 4:3
		else if (ratioComboBox.getSelectedIndex() == 1) {
			IMAGE_WIDTH = Math.round( (float) (this.getWidth() / 2.3) );
			IMAGE_HEIGHT = (IMAGE_WIDTH * 3) / 4;
			h_space = Math.round( (float) (IMAGE_WIDTH * .035 * 3) / 4);
			IMAGE_WIDTH_POS = 0.3;
			IMAGE_HEIGHT_POS = 30;
		}

		//Ratio 1:1
		else if (ratioComboBox.getSelectedIndex() == 2) {
			IMAGE_WIDTH = this.getWidth() / 3;
			IMAGE_HEIGHT = IMAGE_WIDTH;
			h_space = Math.round((float)(IMAGE_WIDTH * .035));
			IMAGE_WIDTH_POS = 0.465;
			IMAGE_HEIGHT_POS = 30;
		}
	}
	
	//Sets the width and height of the SINGLE pictures.
	//Changes IMAGE_WIDTH, IMAGE_HEIGHT. 
	private void initializeSingleImgSize() {
		IMAGE_WIDTH = Math.round( (float) (this.getWidth()) );
		IMAGE_HEIGHT = Math.round( (float) (this.getHeight() *.765) );
	}
	

	//Sets up the frames that will hold the pictures
	private void setAllImageFrames() {
		if (loadOption == 0 || loadOption == 2 || (loadOption == 1 && "Multi".equals((String) changeViewComboBox.getSelectedItem()))) 
		{
			images = new JLabel[IMAGE_COUNT];
			for (int index = 0; index < IMAGE_COUNT; index++) {
				images[index] = new JLabel();
				images[index].setBorder(BorderFactory.createLineBorder(Color.BLACK, 1, true));
				images[index].setOpaque(true);
				images[index].setBackground(Color.LIGHT_GRAY);
				if ("Multi".equals((String) changeViewComboBox.getSelectedItem()))
					images[index].addMouseListener(new PopUpImageClickListener());
			}
		}
		else 
		{
			viewingSingleImage = new JLabel();
			viewingSingleImage.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1, true));
			viewingSingleImage.setOpaque(true);
			viewingSingleImage.setBackground(Color.LIGHT_GRAY);
			if ("Multi".equals((String) changeViewComboBox.getSelectedItem()))
				viewingSingleImage.addMouseListener(new PopUpImageClickListener());
		}
		
	}

	//Sets the size of the frames, adds them to a layer, and then adds to display on screen.
	private void showAllImageFrames() {
		layeredPane = new JLayeredPane();

		if (currentImage-3 >= 0 && currentImage-3 < IMAGE_COUNT) {
			images[currentImage-3].setBounds(h_space, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
			layeredPane.add(images[currentImage-3], new Integer(1));
		}

		if (currentImage-2 >= 0 && currentImage-2 < IMAGE_COUNT) {
			images[currentImage-2].setBounds( Math.round((float) (IMAGE_WIDTH * IMAGE_WIDTH_POS)) + h_space, IMAGE_HEIGHT_POS, IMAGE_WIDTH, IMAGE_HEIGHT);
			layeredPane.add(images[currentImage-2], new Integer(2));
		}

		if (currentImage-1 >= 0 && currentImage-1 < IMAGE_COUNT) {
			images[currentImage-1].setBounds(Math.round((float) (IMAGE_WIDTH * IMAGE_WIDTH_POS * 2)) + h_space, IMAGE_HEIGHT_POS * 2, IMAGE_WIDTH, IMAGE_HEIGHT);
			layeredPane.add(images[currentImage-1], new Integer(5));
		}

		if (currentImage >= 0 && currentImage < IMAGE_COUNT) {
			images[currentImage].setBounds(Math.round((float) (IMAGE_WIDTH * IMAGE_WIDTH_POS * 3)) + h_space, IMAGE_HEIGHT_POS, IMAGE_WIDTH, IMAGE_HEIGHT);
			layeredPane.add(images[currentImage], new Integer(4));
		}
		if (currentImage+1 >= 0 && currentImage+1 < IMAGE_COUNT) {
			images[currentImage+1].setBounds(Math.round((float) (IMAGE_WIDTH * IMAGE_WIDTH_POS * 4)) + h_space, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
			layeredPane.add(images[currentImage+1], new Integer(3));
		}

		if (IMAGE_COUNT == 1) {
			add(images[0], BorderLayout.CENTER);
		}
		else {
			add(layeredPane, BorderLayout.CENTER);
		}
	}
	private void showSingleImageFrame() {
		if (loadOption == 0 || loadOption == 2) {
			add(images[currentImage-1], BorderLayout.CENTER);
		} else if (loadOption == 1) {
			add(viewingSingleImage, BorderLayout.CENTER);
		}
	}

	//Load the pictures using threads.
	private void startMultiImageThread(int startingIndex, int endingIndex) {
		for (int index = startingIndex; index <= endingIndex; index++) {
			if (index >= 0 && index < IMAGE_COUNT && !viewingImgSet[index]) {
				new ImageLoader(index, endingIndex).execute();
			}
		}
	}
	
	//When slider changes, this activates and change the pictures accordingly
	public void stateChanged(ChangeEvent e) {
		JSlider source = (JSlider) e.getSource();
		boolean moveWithSlider = sliderCheckBox.isSelected();
		boolean change = false;

		if (loadOption != 1 && moveWithSlider) {
			if (source.getValue() != currentImage) {
				change = true;
			}
		} else if (!source.getValueIsAdjusting()) {
			change = true;
		}

		//Multiple images view
		if (change && "Multi".equals((String) changeViewComboBox.getSelectedItem())) 
		{
			currentImage = source.getValue();
			layeredPane.removeAll();
			if (loadOption == 0 || loadOption == 2) {
				showAllImageFrames();
			} else if (loadOption == 1) {
				showAllImageFrames();
				
				//Range (inclusive)
				int minIndex = currentImage - 3;
				int maxIndex = currentImage + 1;
				int indexCursor = currentImage - 1; //Center
				int counter = 0;
				
				while (counter < 5) {
					
					//Slides a, b, c, d, e
					//Load slides c, d, e, b, a in order.
					
					if (indexCursor >= currentImage - 1 && indexCursor <= maxIndex) {
						if (indexCursor >= 0 && indexCursor < IMAGE_COUNT) {
							if (!viewingImgSet[indexCursor]) {
								new ImageLoader(indexCursor, indexCursor).execute();
							}
						}
					}
					else if (indexCursor >= minIndex && indexCursor < currentImage - 1) {
						if (indexCursor >= 0 && indexCursor < IMAGE_COUNT) {
							if (!viewingImgSet[indexCursor]) {
								new ImageLoader(indexCursor, indexCursor).execute();
							}
						}
					}
					
					if (indexCursor == maxIndex)
						indexCursor = minIndex;
					else
						indexCursor++;
					
					counter++;
				}
				
				//Remove unused pictures
				for (int index = viewingMultiHolder.size() - 1; index >= 0; index--) {
					int i = viewingMultiHolder.get(index);
					
					if (i >= 0 && i < IMAGE_COUNT) {
						if (i < minIndex  || i > maxIndex) {
							images[i].setIcon(null);
							viewingImgSet[i] = false;
							viewingMultiHolder.remove(index);
						}
					}
				}
			}
		}
		//Single image view
		else if (change && "Single".equals((String) changeViewComboBox.getSelectedItem()))
		{
			currentImage = source.getValue();
			remove(layout.getLayoutComponent(BorderLayout.CENTER));
			if (loadOption == 0 || loadOption == 2) {
				showSingleImageFrame();
			} else if (loadOption == 1) {
				showSingleImageFrame();
				
				if (!viewingSingleLoader.isEmpty()) {
					viewingSingleLoader.get(0).cancel(true);
					viewingSingleLoader.set(0, new ImageLoader(currentImage-1, currentImage-1));
				}
				else {
					viewingSingleLoader.add(new ImageLoader(currentImage-1, currentImage-1));
				}
				viewingSingleLoader.get(0).execute();
				
				//This might never execute unless if two instances of viewingImageLoader.isEmpty is called true
				//on a very FAST speed then this will run. Hasn't run so far yet for me
				//so it might not be necessary. 
				while (viewingSingleLoader.size() != 1) {
					viewingSingleLoader.remove(viewingSingleLoader.size()-1).cancel(true);
				}
			}
		}
		repaint();
		revalidate();
	}

	//Thread that loads a picture to image.
	//Changes image at the given index or loadViewing. 
	class ImageLoader extends SwingWorker<ImageIcon, Void> {
		private int index;
		private int stopPBIndex;
		
		public ImageLoader(int index, int stopPBIndex) {
			this.index = index;
			this.stopPBIndex = stopPBIndex;

			if (!progressBar.isIndeterminate()) 
				progressBar.setIndeterminate(true);
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

			//Good quality pictures
			if (loadOption == 0) 
			{

				//Picture is smaller than the picture frame
				if (img.getWidth() <= IMAGE_WIDTH && img.getHeight() <= IMAGE_HEIGHT) 
				{
					icon = new ImageIcon(img);
				}
				//Reduce picture to fit into the frame by ratio.
				else 
				{
					//Convert ratio to make picture width fit the frame.
					int newHeight = IMAGE_HEIGHT;
					int newWidth = Math.round(img.getWidth() * ( (float) IMAGE_HEIGHT / img.getHeight()));

					//If width is still too big, resize to make the width fit by a percentage.
					//Change height down by that same percentage to keep same aspect ratio. 
					if (newWidth >= IMAGE_WIDTH) {
						int changingWidth = (int)((IMAGE_WIDTH * newHeight) / (double) IMAGE_HEIGHT * .98);
						newHeight = (int)((changingWidth / (double) newWidth) * newHeight);
						newWidth = changingWidth;
						//						Image scaledImg = Thumbnails.of(img).scale( (double) IMAGE_WIDTH / newWidth).asBufferedImage();
						//						icon = new ImageIcon(scaledImg, files.get(index).getName());
					} 

					Image scaledImg = img.getScaledInstance(newWidth, newHeight, Image.SCALE_AREA_AVERAGING);
					icon = new ImageIcon(scaledImg, files.get(index).getName());
				}
				/*
				else
				{
					Image scaledImg = img.getScaledInstance(IMAGE_WIDTH, IMAGE_HEIGHT, Image.SCALE_AREA_AVERAGING);
					icon = new ImageIcon(scaledImg, files.get(index).getName());
				}
				*/
			} 

			//Load all, and Load as you go.
			else 
			{

				if (img.getWidth() <= IMAGE_WIDTH && img.getHeight() <= IMAGE_HEIGHT) 
				{
					BufferedImage drawnImg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
					drawnImg.getGraphics().drawImage(img, 0, 0, img.getWidth(), img.getHeight(), null);
					icon = new ImageIcon(drawnImg, files.get(index).getName());
				} 
				//Reduce picture to fit into the frame by ratio.
				else 
				{
					//Convert ratio to make picture width fit the frame.
					int newHeight = IMAGE_HEIGHT;
					int newWidth = Math.round(img.getWidth() * ( (float) IMAGE_HEIGHT / img.getHeight()));

					//If width is still too big, resize to make the width fit by a percentage.
					//Change height down by that same percentage to keep same aspect ratio. 
					if (newWidth >= IMAGE_WIDTH) 
					{
						int changingWidth = (int)((IMAGE_WIDTH * newHeight) / (double) IMAGE_HEIGHT * .98);
						newHeight = (int)((changingWidth / (double) newWidth) * newHeight);
						newWidth = changingWidth;
					}

					//BufferedImage drawnImg = new BufferedImage(newWidth	, newHeight, BufferedImage.TYPE_INT_ARGB);
					//drawnImg.getGraphics().drawImage(img, 0, 0, newWidth, newHeight, null);
					BufferedImage drawnImg = Thumbnails.of(img).size(newWidth, newHeight).asBufferedImage();
					icon = new ImageIcon(drawnImg, files.get(index).getName());
				}
			}
			if (!"Single".equals((String) changeViewComboBox.getSelectedItem()) || loadOption != 1)
				viewingImgSet[index] = true;
			
			if ("Multi".equals((String) changeViewComboBox.getSelectedItem()) && loadOption == 1) {
				viewingMultiHolder.add(index);
			}
			return icon;
		}

		@Override
		protected void done() {
			ImageIcon icon;
			try {
				icon = get();
				//Single load viewing only
				if ("Single".equals((String) changeViewComboBox.getSelectedItem()) && loadOption == 1) 
				{
					viewingSingleImage.setIcon(icon);
					viewingSingleImage.repaint();
					viewingSingleImage.setHorizontalAlignment(SwingConstants.CENTER);
				}
				//For anything else
				else
				{
					images[index].setIcon(icon);
					images[index].repaint();
					images[index].setHorizontalAlignment(SwingConstants.CENTER);
				}
				
				if (index == stopPBIndex)
					progressBar.setIndeterminate(false);
					
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			} catch (CancellationException e) {
				//ignore this.
			}
		}
	}
	
	class PopUpImageClickListener extends MouseAdapter {
		//Listens for mouse clicks
		//Shows a original size picture with the same resolution.
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

				System.out.println("Clicked image width: " + img.getWidth());
				System.out.println("Clicked image height: " + img.getHeight());

				lb.addMouseListener(new MouseAdapter() {
					public void mouseClicked(MouseEvent e) {
						popUp.dispose();
					} 
				});
				popUp.add(lb);
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			popUp.setVisible(true);
			popUp.pack();
			popUp.setLocation(20, 20);
			popUp.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
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
