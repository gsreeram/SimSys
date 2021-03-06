package edu.utdallas.previewtool.View;

import edu.utdallas.previewtool.Error.PreviewError;
import edu.utdallas.sharedfiles.Search.InputWizard;
import edu.utdallas.sharedfiles.Shared.*;
import edu.utdallas.sharedfiles.Structure.Act;

import java.awt.*;
import java.awt.Color;
import java.util.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import javax.swing.JPopupMenu;
import javax.swing.border.Border;

public class ScenePanel extends JPanel
{
	private static final long serialVersionUID = 1L;
	private static final int MIN_LABEL_DIMENSION = 30;
	private BufferedImage background;
	private ArrayList<JLabel> assetLabels;
	private ArrayList<JLabel> hiddenAssetLabels;
	private String charBaseDir = "Office, Classroom\\Characters\\";
	private String imageBaseDir = "Office, Classroom\\";
	private Point prevClickPoint;
	private ScenePanel that = this;
	private Asset targetedAsset = null;
	private InputWizard parentWizard;
	private boolean resize = false;
	private boolean isHidden = true;
	private final int resizeBorder = 20;
	
	public ScenePanel(InputWizard parent)
	{
		clear();
		parentWizard = parent;
		addMouseListener(new MouseListener() {
		        public void mouseClicked(MouseEvent e) { 
		        	for (JLabel label : assetLabels){
		        		if(label.getIcon()==null && !(label instanceof ConversationBubble || label instanceof ThoughtBubble))
		        		{
		        			label.setBorder(BorderFactory.createLineBorder(Color.BLACK, 5));
		        		}
		        		else
		        		{
		        			label.setBorder(null);
		        		}
		        	}
		        }
		        public void mouseEntered(MouseEvent e) { }
		        public void mouseExited(MouseEvent e) { }
		        public void mousePressed(MouseEvent e) { prevClickPoint = e.getPoint(); }
		        public void mouseReleased(MouseEvent e) { }
		});
		addMouseMotionListener(new MouseMotionListener() {
			public void mouseDragged(MouseEvent e) {}
			public void mouseMoved(MouseEvent e) {
				getRootPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		});
		
		System.out.println("calling clear constructor\n");
	}
	
	public void clear()
	{
		background = null;
		assetLabels = new ArrayList<JLabel>();
		hiddenAssetLabels = new ArrayList<JLabel>();
		removeAll();
		updateUI();
	}
	
	private JPanel createErrorLevelPanel(String levelName, List<PreviewError> errors)
	{
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JLabel("<html><h1>" + levelName + "</h1></html>"), BorderLayout.NORTH);
		
		// 3 columns: severity icon, error message, autocorrect button
		JPanel errorsPanel = new JPanel(new GridLayout(errors.size()+1, 1));
		for(final PreviewError e : errors)
		{
			JPanel rowPanel = new JPanel(new BorderLayout());
			JLabel error = new JLabel("<html><h2>" + e.getSeverity().toString() + "</h2></html>");
			error.setFont(new Font("Comic Sans MS", Font.BOLD, 18));
			rowPanel.add(error, BorderLayout.WEST); //string for now
			JLabel error2 = new JLabel("<html><body style=\"margin-left:4px;\">" + e.getMessage().replace("<", "&lt;").replace(">", "&gt;") + "</body></html>");
			error2.setFont(new Font("Comic Sans MS", Font.BOLD, 18));
			rowPanel.add(error2, BorderLayout.CENTER);
			
			final JButton fixButton = new JButton("Autocorrect");
			fixButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent ev) { 
					e.fixError();
					fixButton.setEnabled(false);
				}
			});
			System.out.println("fixButton.getHeight() " + fixButton.getHeight());
			if(fixButton.getHeight()>50)
				fixButton.setSize(fixButton.getWidth(), 50);
			
			rowPanel.add(fixButton, BorderLayout.EAST);
		//	rowPanel.setPreferredSize(new Dimension(getWidth()-240, 50));
			errorsPanel.add(rowPanel);
			errorsPanel.add(Box.createRigidArea(new Dimension(getWidth(), getHeight()-380)));
			
		}
		panel.add(errorsPanel, BorderLayout.CENTER);
		
		return panel;
	}
	
	public void loadErrors(List<PreviewError> errors)
	{
		ArrayList<PreviewError> gameLevelErrors = new ArrayList<PreviewError>();
		ArrayList<PreviewError> actLevelErrors = new ArrayList<PreviewError>();
		ArrayList<PreviewError> sceneLevelErrors = new ArrayList<PreviewError>();
		ArrayList<PreviewError> screenLevelErrors = new ArrayList<PreviewError>();
		
		clear();
		
		for(PreviewError e : errors)
		{
			switch(e.getLevel())
			{
				case GAME:
					gameLevelErrors.add(e);
					break;
				case ACT:
					actLevelErrors.add(e);
					break;
				case SCENE:
					sceneLevelErrors.add(e);
					break;
				case SCREEN:
					screenLevelErrors.add(e);
					break;
				default: //TODO: exception?
					break;
			}
		}
		
		JPanel errorPanel = new JPanel();
		errorPanel.setLayout(new BoxLayout(errorPanel, BoxLayout.Y_AXIS));
		errorPanel.setBounds(0, 100, getWidth(), getHeight());
		
		Font font = new Font("Comic Sans MS", Font.BOLD, 18);
		JLabel introPanel = new JLabel("<html><p style=\"text-align:left\">" + "Welcome to the SimSYS Preview tool. We have reviewed the Game file for errors. If any have been found, they will be shown below. Some can be corrected automatically before you proceed to previewing the Game using the tree on the left." + "</p></html>");
		introPanel.setFont(font);
		introPanel.setBounds(0, -150, getWidth(), getHeight());
		add(introPanel);
		JPanel gamePanel = createErrorLevelPanel("Game-level Errors:", gameLevelErrors);
		gamePanel.setFont(font);
		if(gameLevelErrors.size()>=1)
			errorPanel.add(gamePanel);
		JPanel actPanel = createErrorLevelPanel("Act-level Errors:", actLevelErrors);
		actPanel.setFont(font);
		if(actLevelErrors.size()>=1)
			errorPanel.add(actPanel);
		JPanel scenePanel = createErrorLevelPanel("Scene-level Errors:", sceneLevelErrors);
		scenePanel.setFont(font);
		if(sceneLevelErrors.size()>=1)
			errorPanel.add(scenePanel);
		JPanel screenPanel = createErrorLevelPanel("Screen-level Errors:", screenLevelErrors);
		screenPanel.setFont(font);
		if(screenLevelErrors.size()>=1)
			errorPanel.add(screenPanel);
		
		//for(int i = 0; i < errors.size(); i++)
		//{
			//errorPanel.add(new JLabel("<html><body style=\"width:" + (getWidth()-240) + "px\">" 
			//		+ errors.get(i).getMessage().replace("<", "&lt;").replace(">", "&gt;") 
			//		+ "</body></html>"));
		//}
		add(errorPanel);
	}
	
	public void loadBackground(String imageFile)
	{
		try 
		{
			background = ImageHelper.getScaledImage(ImageIO.read(new File("Office, Classroom\\" + imageFile)), 1.5);
			repaint();
		} 
		catch (IOException ex) 
		{
			System.out.println(imageFile + " is missing from repository, cannot load");
		}
	}
	
	public void loadAssets(List<Asset> as, boolean readOnly)
	{
		clear();
		System.out.println("calling clear load assets\n");
		
		for(Asset a : as)
		{
			if(a instanceof CharacterAsset)
			{
				loadAsset(a, charBaseDir, readOnly);
			}
			else if(a instanceof ImageAsset)
			{
				loadAsset(a, imageBaseDir, readOnly);
			}
			else if(a instanceof InformationBoxAsset)
			{
				loadAsset(a, null, readOnly);
			}
			else if(a instanceof ConversationBubbleAsset)
			{
				loadAsset(a, imageBaseDir, readOnly);
			}
			else if(a instanceof ThoughtBubbleAsset)
			{
				loadAsset(a, imageBaseDir, readOnly);
			}
			else if (a instanceof ButtonAsset){
				loadAsset(a, imageBaseDir, readOnly);
			}
		}
		
		addNotify();
		revalidate();
		repaint();
	}
	
	public void loadAsset(final Asset a, final String baseDir, final boolean isReadOnly)
	{
		try 
		{
			// Don't load an asset with invalid dimensions
			if(a.getWidth() <= 0 || a.getHeight() <= 0)
			{
				return;
			}
			
			final JLabel label;
			if(a instanceof ButtonAsset || a instanceof InformationBoxAsset)
			{//asset is drawn yellow with a black border
				label = new JLabel(htmlWrap(a.getName()));
				label.setFont(new Font(a.getFontFamily(), Font.BOLD, a.getFontSize()));
				label.setHorizontalAlignment(JLabel.CENTER);
				label.setForeground(Color.BLACK);
				label.setBorder(BorderFactory.createLineBorder(Color.BLACK, 5));
				label.setBackground(Color.YELLOW);
				label.setOpaque(true);
				if(a.getHint() != null){
					JLabel hint = new JLabel(a.getHint().getText());
					hint.setFont(new Font(a.getFontFamily(), Font.PLAIN, a.getFontSize()));
					hint.setHorizontalAlignment(JLabel.CENTER);
					hint.setForeground(Color.BLACK);
					hint.setBorder(BorderFactory.createLineBorder(Color.BLUE, 1));
					hint.setBackground(Color.cyan);
					hint.setOpaque(true);
					hint.setBounds(a.getX()-20, a.getY()-10, 260, 30);
					add(hint);
					setComponentZOrder(hint, 0);
					hiddenAssetLabels.add(hint);
					if(isHidden) { hint.setVisible(false); }
				}
				
				label.setBounds(a.getX(), a.getY(), a.getWidth(), a.getHeight());
				add(label);
			}
			else if(a instanceof ConversationBubbleAsset)
			{
				label = new ConversationBubble(a.getName());
				((ConversationBubble)label).setFont(new Font(a.getFontFamily(), Font.BOLD, a.getFontSize()));
				((ConversationBubble)label).setBounds(a.getX(), a.getY(), a.getWidth(), a.getHeight());
				//if there is a point direction defined, use it. If there is not one it will default to left
				if(((ConversationBubbleAsset)a).getPointDirection() != null) {
					((ConversationBubble)label).setPointDirection(((ConversationBubbleAsset) a).getPointDirection());
				}
				add(label);
			}
			else if(a instanceof ThoughtBubbleAsset)
			{
				label = new ThoughtBubble(a.getName());
				((ThoughtBubble)label).setFont(new Font(a.getFontFamily(), Font.BOLD, a.getFontSize()));
				((ThoughtBubble)label).setBounds(a.getX(), a.getY(), a.getWidth(), a.getHeight());
				//if there is a point direction defined, use it. If there is not one it will default to left
				if(((ThoughtBubbleAsset)a).getPointDirection() != null){
					((ThoughtBubble)label).setPointDirection(((ThoughtBubbleAsset) a).getPointDirection());
				}
				add(label);
			}
			else
			{//asset has an image
				BufferedImage image = ImageIO.read(new File(baseDir + a.getDisplayImage()));
				int width = image.getWidth();
				final double desiredWidth = a.getWidth();
				double scaleFactor = desiredWidth / width;
				BufferedImage scaledImage = ImageHelper.getScaledImage(image, scaleFactor);
				label = new JLabel(new ImageIcon(scaledImage));
				label.setLayout(new BorderLayout());
				label.setBounds(a.getX(), a.getY(), scaledImage.getWidth(), scaledImage.getHeight());
				add(label);
			}
			BufferedImage img = ImageHelper.getScaledImage(ImageIO.read(new File("Office, Classroom/Asst Bitstrips and Composite images/sound-icon.png")), 0.13);

			if(a.getSoundEffect() != null)
			{
				JLabel previewSoundButton = new JLabel(new ImageIcon(img));
				previewSoundButton.setBounds(0, 0, 30, 30);
				previewSoundButton.addMouseListener(new MouseListener() 
				{
					public void mouseClicked(MouseEvent e) 
					{
						AudioPlayer.playAudio("AudioAssetRepository\\" + a.getSoundEffect());
					}
					public void mouseEntered(MouseEvent e) { }
					public void mouseExited(MouseEvent e) { }
					public void mousePressed(MouseEvent e) { }
					public void mouseReleased(MouseEvent e) { }
				});
				label.add(previewSoundButton);
				label.setComponentZOrder(previewSoundButton, 0);
				hiddenAssetLabels.add(previewSoundButton);
				if(isHidden) { previewSoundButton.setVisible(false); }
				
			}
			
			
			if(!isReadOnly){
				label.addMouseListener(new MouseListener() {
			        public void mouseClicked(MouseEvent e) { 
			        	//if right-click
			        	if((e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
			        		System.out.println("right-clicked");
			        		JPopupMenu pMenu = new JPopupMenu();
			        		JMenuItem menuItem = new JMenuItem("Delete");
			        		menuItem.setActionCommand("deleteElement");
			        		menuItem.addActionListener(parentWizard);
			        		
			        		//Preview Sound in right click menu
				        	//TODO: this will be gone
			        		JMenuItem menuItem2 = new JMenuItem("Preview Sound");
			        		menuItem2.setActionCommand("previewSound");
			        		menuItem2.addActionListener(parentWizard);
			        		if(a.getSoundEffect() == null) {
			        			menuItem2.setEnabled(false);
			        		}
			        		
			        		pMenu.add(menuItem);
			        		pMenu.add(menuItem2);
			        		
			        		if(a instanceof ButtonAsset || a instanceof InformationBoxAsset || a instanceof ConversationBubbleAsset || a instanceof ThoughtBubbleAsset)
			        		{
				        		JMenuItem menuItem3 = new JMenuItem("Edit Text");
				        		menuItem3.addActionListener(new ActionListener() {
				        			public void actionPerformed(ActionEvent e) {
				        				String newText = (String)JOptionPane.showInputDialog(that, "Enter the new text and click \"OK\" when finished.", "Edit Text", JOptionPane.QUESTION_MESSAGE, null, null, a.getName());
				        				if(newText != null)
				        				{
					        				a.setName(newText);
					        				// for some reason, label.setText does not update the label, so must re-create it
					        				remove(label);
					        				loadAsset(a, baseDir, isReadOnly);
				        				}
				        			}
				        		});
				        		pMenu.add(menuItem3);
			        		}
			        		
			        		targetedAsset = a;
			        		that.add(pMenu);
			        		pMenu.show(e.getComponent(), e.getX(), e.getY());
						}
						
			        	for(JLabel label : assetLabels) {
			        		if(label.getIcon() == null && !(label instanceof ConversationBubble || label instanceof ThoughtBubble))
			        		{
			        			label.setBorder(BorderFactory.createLineBorder(Color.BLACK, 5));
			        		}
			        		else
			        		{
			        			label.setBorder(null);
			        		}
			        	}
			        	Border highlights = BorderFactory.createLineBorder(Color.MAGENTA, 5);
			        		label.setBorder(highlights);
			        }
			        public void mouseEntered(MouseEvent e) { }
			        public void mouseExited(MouseEvent e) { }
			        public void mousePressed(MouseEvent e) { prevClickPoint = e.getPoint(); }
			        public void mouseReleased(MouseEvent e) { 
			        	if(resize){
			        		parentWizard.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "resizeAsset"));
			        	}
			        }
			    });
				label.addMouseMotionListener(new MouseMotionListener() {
					public void mouseDragged(MouseEvent e) {
			        	Point p = e.getPoint();
			        	int deltaX = p.x - prevClickPoint.x;
			        	int deltaY = p.y - prevClickPoint.y;
			        	int invDeltaX = -deltaX;
			        	int invDeltaY = -deltaY;
			        	String imgPath;
			        	if(a instanceof CharacterAsset)
			        	{
			        		imgPath = charBaseDir + a.getDisplayImage();
			        	}
			        	else
			        	{
			        		imgPath = imageBaseDir + a.getDisplayImage();
			        	}
			        	
			        	if(label.getRootPane().getCursor().getType() == Cursor.NW_RESIZE_CURSOR)
			        	{
			        		if((a.getWidth()+invDeltaX <= MIN_LABEL_DIMENSION || label.getHeight()+invDeltaY <= MIN_LABEL_DIMENSION) && (p.x > prevClickPoint.x || p.y > prevClickPoint.y))
			        		{
			        			//do nothing, don't let the image disappear
			        		}
			        		else
			        		{
				        		a.setWidth(a.getWidth() + invDeltaX);
				        		a.setHeight(a.getHeight() + invDeltaY);
				        		a.setX(a.getX() + deltaX);
				        		a.setY(a.getY() + deltaY);
				        		a.setX2(a.getX() + a.getWidth());
				        		a.setY2(a.getY() + a.getHeight());
				        		
				        		if(a instanceof ButtonAsset || a instanceof InformationBoxAsset || a instanceof ConversationBubbleAsset || label instanceof ThoughtBubble)
				        		{
				        			label.setBounds(a.getX(), a.getY(), a.getWidth(), a.getHeight());
				        		}
				        		else {
					        		label.setBounds(a.getX(), a.getY(), a.getWidth(), label.getHeight() + invDeltaY);
					        		BufferedImage image;
									try {
										image = ImageIO.read(new File(imgPath));
						        		int width = image.getWidth();
						    			double desiredWidth = a.getWidth();
						    			double scaleFactor = desiredWidth / width;
						    			BufferedImage scaledImage = ImageHelper.getScaledImage(image, scaleFactor);
						    			label.setIcon(new ImageIcon(scaledImage));
									} catch (IOException e1) {
										e1.printStackTrace();
									}
								}
				        		resize = true;
			        		}
			        	}
						else if(label.getRootPane().getCursor().getType() == Cursor.SW_RESIZE_CURSOR)
						{
							if((a.getWidth()+invDeltaX <= MIN_LABEL_DIMENSION || label.getHeight()+deltaY <= MIN_LABEL_DIMENSION) && (p.x > prevClickPoint.x || p.y < prevClickPoint.y))
			        		{
			        			//do nothing, don't let the image disappear
			        		}
							else
							{
								a.setWidth(a.getWidth() + invDeltaX);
				        		a.setHeight(a.getHeight() + deltaY);
				        		a.setX(a.getX() + deltaX);
				        		prevClickPoint.y = p.y;
				        		a.setX2(a.getX() + a.getWidth());
				        		a.setY2(a.getY() + a.getHeight());
				        		
				        		if(a instanceof ButtonAsset || a instanceof InformationBoxAsset || a instanceof ConversationBubbleAsset || label instanceof ThoughtBubble)
				        		{
				        			label.setBounds(a.getX(), a.getY(), a.getWidth(), a.getHeight());
				        		}
				        		else {
					        		label.setBounds(a.getX(), a.getY(), a.getWidth(), label.getHeight()+deltaY);
					        		BufferedImage image;
									try {
										image = ImageIO.read(new File(imgPath));
						        		int width = image.getWidth();
						    			double desiredWidth = a.getWidth();
						    			double scaleFactor = desiredWidth / width;
						    			BufferedImage scaledImage = ImageHelper.getScaledImage(image, scaleFactor);
						    			label.setIcon(new ImageIcon(scaledImage));
									} catch (IOException e1) {
										e1.printStackTrace();
									}
								}
				        		resize = true;
							}
						}
						else if(label.getRootPane().getCursor().getType() == Cursor.NE_RESIZE_CURSOR)
						{
							if((a.getWidth()+deltaX <= MIN_LABEL_DIMENSION || label.getHeight()+invDeltaY <= MIN_LABEL_DIMENSION) && (p.x < prevClickPoint.x || p.y > prevClickPoint.y))
			        		{
			        			//do nothing, don't let the image disappear
			        		}
							else
							{
								a.setWidth(a.getWidth() + deltaX);
				        		a.setHeight(a.getHeight() + invDeltaY);
				        		prevClickPoint.x = p.x;
				        		a.setY(a.getY() + deltaY);
				        		a.setX2(a.getX() + a.getWidth());
				        		a.setY2(a.getY() + a.getHeight());
				        		
				        		if(a instanceof ButtonAsset || a instanceof InformationBoxAsset || a instanceof ConversationBubbleAsset || label instanceof ThoughtBubble)
				        		{
				        			label.setBounds(a.getX(), a.getY(), a.getWidth(), a.getHeight());
				        		}
				        		else {
					        		label.setBounds(a.getX(), a.getY(), a.getWidth(), label.getHeight()+invDeltaY);
					        		BufferedImage image;
									try {
										image = ImageIO.read(new File(imgPath));
						        		int width = image.getWidth();
						    			double desiredWidth = a.getWidth();
						    			double scaleFactor = desiredWidth / width;
						    			BufferedImage scaledImage = ImageHelper.getScaledImage(image, scaleFactor);
						    			label.setIcon(new ImageIcon(scaledImage));
									} catch (IOException e1) {
										e1.printStackTrace();
									}
								}
				        		resize = true;
							}
						}
						else if(label.getRootPane().getCursor().getType() == Cursor.SE_RESIZE_CURSOR)
						{
							if((a.getWidth()+deltaX <= MIN_LABEL_DIMENSION || label.getHeight()+deltaY <= MIN_LABEL_DIMENSION) && (p.x < prevClickPoint.x || p.y < prevClickPoint.y))
			        		{
			        			//do nothing, don't let the image disappear
			        		}
							else
							{
								a.setWidth(a.getWidth() + deltaX);
				        		a.setHeight(a.getHeight() + deltaY);
				        		prevClickPoint.x = p.x;
				        		prevClickPoint.y = p.y;
				        		a.setX2(a.getX() + a.getWidth());
				        		a.setY2(a.getY() + a.getHeight());
				        		
				        		if(a instanceof ButtonAsset || a instanceof InformationBoxAsset || a instanceof ConversationBubbleAsset || label instanceof ThoughtBubble)
				        		{
				        			label.setBounds(a.getX(), a.getY(), a.getWidth(), a.getHeight());
				        		}
				        		else {
					        		label.setBounds(a.getX(), a.getY(), a.getWidth(), label.getHeight()+deltaY);
					        		BufferedImage image;
									try {
										image = ImageIO.read(new File(imgPath));
						        		int width = image.getWidth();
						    			double desiredWidth = a.getWidth();
						    			double scaleFactor = desiredWidth / width;
						    			BufferedImage scaledImage = ImageHelper.getScaledImage(image, scaleFactor);
						    			label.setIcon(new ImageIcon(scaledImage));
									} catch (IOException e1) {
										e1.printStackTrace();
									}
								}
				        		resize = true;
							}
						}
						else
						{
				        	int newX = label.getX() + deltaX;
				        	int newY = label.getY() + deltaY;
				        	label.setLocation(newX, newY);
							a.setX(newX);
							a.setY(newY);
							resize = false;
						}
						
						//all - set highlighting
						for (JLabel label : assetLabels){
							if(label.getIcon()==null && !(label instanceof ConversationBubble || label instanceof ThoughtBubble))
			        		{
			        			label.setBorder(BorderFactory.createLineBorder(Color.BLACK, 5));
			        		}
			        		else
			        		{
			        			label.setBorder(null);
			        		}
			        	}
			        	label.setBorder(BorderFactory.createLineBorder(Color.MAGENTA, 5));
					}
					public void mouseMoved(MouseEvent e) {
						Point p = e.getPoint();
						if(p.x <= resizeBorder && p.y <= resizeBorder)
						{
							label.getRootPane().setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
						}
						else if(p.x <= resizeBorder && label.getHeight() - p.y <= resizeBorder)
						{
							label.getRootPane().setCursor(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));
						}
						else if(label.getWidth() - p.x <= resizeBorder && p.y <= resizeBorder)
						{
							label.getRootPane().setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
						}
						else if(label.getWidth() - p.x <= resizeBorder && label.getHeight() - p.y <= resizeBorder)
						{
							label.getRootPane().setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
						}
						else
						{
							label.getRootPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
						}
					}
				});
			}

			assetLabels.add(label);
			repaint();
		} 
		catch (IOException ex) 
		{
			System.out.println(a.getDisplayImage() + " is missing from repository, cannot load");
		}
	}
	
	public void backgroundMusicPreview(boolean ifMusic) {
		System.out.println(ifMusic);
		
		JButton backgroundMusic;
		JButton backgroundMusicStop;

        backgroundMusic = new JButton("Preview");
        backgroundMusic.addActionListener(parentWizard);
        backgroundMusic.setEnabled(ifMusic);
        backgroundMusic.setActionCommand("backgroundMusicPreviewPlay");
        backgroundMusic.setBounds(5, 5, 100, 30);
        BufferedImage img;
		try {
			img = ImageHelper.getScaledImage(ImageIO.read(new File("Office, Classroom/Asst Bitstrips and Composite images/play.png")), 0.5);
			backgroundMusic.setIcon(new ImageIcon(img));
		} catch (IOException e) {
			e.printStackTrace();
		}
        backgroundMusicStop = new JButton("Stop");
        backgroundMusicStop.addActionListener(parentWizard);
        backgroundMusicStop.setEnabled(ifMusic);
        backgroundMusicStop.setActionCommand("backgroundMusicPreviewStop");
        backgroundMusicStop.setBounds(105, 5, 100, 30);
        BufferedImage img2;
		try {
			img2 = ImageHelper.getScaledImage(ImageIO.read(new File("Office, Classroom/Asst Bitstrips and Composite images/stop.png")), 1.0);
			backgroundMusicStop.setIcon(new ImageIcon(img2));
		} catch (IOException e) {
			e.printStackTrace();
		}

        add(backgroundMusic);
        add(backgroundMusicStop);
        
	}
		
    protected void paintComponent(Graphics g) 
    {
        super.paintComponent(g);

        if(background != null)
        {
        	g.drawImage(background, 0, 0, null);
        }
    }
    
	public Asset getTargetedAsset()
	{
		return targetedAsset;
	}

	public void toggleHiddenElements() {
		System.out.println("Toggle");
		isHidden = !isHidden;
		for(JLabel l : hiddenAssetLabels)
		{
			l.setVisible(!isHidden);
		}
	}

	public void displayAct(Act act) {
		// TODO Auto-generated method stub
		JPanel actDisplay = new JPanel();
		actDisplay.setBounds(0,0,that.getWidth(),that.getHeight());
		actDisplay.setBackground(Color.red);
		JLabel actText = new JLabel("<html><p style=\"text-align:center\">" + act.getName() + "</p></html>");
		actText.setBounds(actDisplay.getX()+(actDisplay.getWidth()/8), actDisplay.getY()+(actDisplay.getHeight()/20), actDisplay.getWidth()/2, actDisplay.getHeight()/4);
		actText.setBackground(Color.WHITE);
		actText.setForeground(Color.BLACK);
		actText.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		actText.setFont(new Font("Comic Sans MS", Font.BOLD, 76));
		actText.setHorizontalAlignment(JLabel.CENTER);
		actText.setVerticalAlignment(JLabel.CENTER);
		actText.setOpaque(true);
		JLabel actTextShadow = new JLabel();
		actTextShadow.setBounds(actDisplay.getX()+(actDisplay.getWidth()/8)+4, actDisplay.getY()+(actDisplay.getHeight()/20)+4, actDisplay.getWidth()/2, actDisplay.getHeight()/4);
		actTextShadow.setBackground(new Color(150, 36, 36));
		actTextShadow.setOpaque(true);
		if(act.getLearningObjectives() != null){
			//TODO Hard coded for WDPI but would just need to add a source in XML and a act.getSource
			//TODO Also HTMLListWrap needs a nesting component to nest 1.1.1 within 1.1 when necessary.
			JLabel actLearningObjectives = new JLabel("<html><p style=\"text-align:left\">Learning Objectives provided by WDPI" + htmlListWrap(act.getLearningObjectives()) + "</p></html>");
			actLearningObjectives.setBounds(actDisplay.getWidth()/16, (actDisplay.getHeight()/3) +15, actDisplay.getWidth()/8 *7, (actDisplay.getHeight()/2)+20);
			actLearningObjectives.setBackground(Color.WHITE);
			actLearningObjectives.setForeground(Color.BLACK);
			actLearningObjectives.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
			actLearningObjectives.setFont(new Font("Comic Sans MS", Font.BOLD, 24));
			actLearningObjectives.setHorizontalAlignment(JLabel.LEFT);
			actLearningObjectives.setOpaque(true);
			add(actLearningObjectives);
		}
		add(actText);
		add(actTextShadow);
		add(actDisplay);
		
		
	}
	
	private String htmlListWrap(List<String> list){
		String htmlListString = "";
		for(int i = 0; i<list.size(); i++){
			htmlListString = htmlListString + "<li>"+list.get(i)+"</li>";
		}
		htmlListString = "<ul>"+htmlListString+"</ul>";
		return htmlListString;
	}
	
	private String htmlWrap(String s)
	{
		return "<html><p style=\"text-align:center\">" + s + "</p></html>";
	}

}
