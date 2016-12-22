package minesweep;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;

public class MineSweeperButton extends JButton implements MouseListener{


	// Array of paths to the Icons 
	static String[] ICON_PATHS = {"resources/0.png","resources/1.png","resources/2.png","resources/3.png","resources/4.png","resources/5.png",
			"resources/6.png","resources/7.png","resources/8.png","resources/cover.png", "resources/face-dead.png", "resources/face-smile.png", "resources/face-win.png"
			, "resources/flag.png", "resources/mine-grey.png", "resources/mine-misflagged.png", "resources/mine-red.png", "resources/face-oh.png"};

	// Constants for the possible states of the buttons correlating to the index of their icon path
	static final int ZERO = 0, ONE = 1, TWO = 2, THREE = 3, FOUR = 4, FIVE = 5, SIX = 6, SEVEN = 7, EIGHT = 8,
			COVER = 9, FACE_DEAD = 10, FACE_SMILE = 11, FACE_WIN = 12, FLAG = 13, MINE_GREY = 14, 
			MINE_MISFLAGGED = 15, MINE_RED = 16, FACE_OH = 17;

	private ArrayList<MineSweeperButton>neighbors; // each of the buttons neighbors
	private int NUM; // the number of surrounding mines 
	private boolean isAMine; // true if buttons is a mine
	private int state; // integer representing the current state of the button 
	private MineSweeper game; // the board and frame 
	private int i, j;

	/**
	 * Constructor for MineSweeperButton 
	 * @param i - the row position of this button
	 * @param j - the col position of this button
	 */
	MineSweeperButton(MineSweeper game, int i, int j) {
		this.NUM = -1;
		this.neighbors = new ArrayList<>();
		setState(COVER);
		this.addMouseListener(this);
		this.setBorder(BorderFactory.createEmptyBorder());
		this.game = game;
		this.i = i;
		this.j = j;
	}

	/**
	 * Set the number of mines This button has a neighbor
	 * @param NUM - The number of mines 
	 */
	void setNum(int NUM){
		this.NUM = NUM;
	}

	/**
	 * Sets this button to be a mine
	 */
	void setIsAMine(){
		this.isAMine = true;
	}

	boolean isAMine(){
		return this.isAMine;
	}


	/**
	 * Add Neighboring button to list of neighbors 
	 * @param m - a neighboring button
	 */
	void addNeighbor(MineSweeperButton m ){
		this.neighbors.add(m);
	}

	/**
	 * Get the current state of this button. Will return 0-16
	 * @return - int representing current state. Also an index to the image path string 
	 */
	int getState() {
		return this.state;
	}

	/**
	 * Sets the state and updates the icon of the button 
	 * @param state - The State the button is changing to 
	 */
	void setState(int state) {
		this.state = state;
		this.setIcon(new ImageIcon(ICON_PATHS[state]));
	}

	/**
	 *  Handles the mouse pressed event on this button and contains all the logic for click actions in minesweeper
	 */
	@Override
	public void mousePressed(MouseEvent e) {

		if(game.gameOver()){
			return;
		}
		final boolean LEFT_CLICK = e.getButton() == MouseEvent.BUTTON1;
		final boolean RIGHT_CLICK = e.getButton() == MouseEvent.BUTTON3;

        game.setFace(new ImageIcon(ICON_PATHS[FACE_OH]));

		//first click must be left click
		if(game.isFirstClick()){
			if(LEFT_CLICK) {
				game.generateMap(this, e);
			}
			return;
		}

		//Right click toggles between flag and cover
		if(RIGHT_CLICK && !this.isUncovered()){
            state = getState()==COVER ? FLAG : COVER;

		}else if(LEFT_CLICK){
			if(getState() == COVER){

				if(this.isAMine){
					state = MINE_RED;
				}else{
					state = NUM;
					if(state == ZERO){
						//if this button has 0 mines around it fire the click event to all of it's neighbors
						for(MineSweeperButton m : neighbors){
							m.mousePressed(e);
						}
					}
				}
			}else if(this.isUncovered()){
				// If this button has already been revealed
				//counts the flags around it and if they match the number
				// of this button then it will open up all of it's neighboring buttons
				int flagCount = 0;
				for(MineSweeperButton m : neighbors){ 
					if(m.getState() == FLAG){
						flagCount ++;  // count neighbors flagged 
					}
				}
				if(flagCount == this.NUM){
					// if the right number of neighbors have been flagged
					// send action event to un-flagged neighbors
					getNeighbors().stream()
                            .filter(m -> m.getState() == COVER)
                            .forEach(m -> m.mousePressed(e));
				}
			}
		}
		setState(state); 
	}

	ArrayList<MineSweeperButton>getNeighbors(){
		return this.neighbors;
	}


	@Override
	public void mouseReleased(MouseEvent e) {

		if(!game.gameOver()){
			game.setFace(new ImageIcon(ICON_PATHS[FACE_SMILE]));
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) { }
	
	@Override
	public void mouseEntered(MouseEvent e) { }
	
	@Override
	public void mouseExited(MouseEvent e) { }

	boolean isUncovered() {
		return getState() <= EIGHT;
	}
}
