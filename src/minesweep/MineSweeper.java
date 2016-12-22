package minesweep;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * Main class that runs the MineSweeper panel, creates all game components and runs the game loop.
 * @author Martin Wallace 
 *
 */
public class MineSweeper {

	private final static int  BEGINNER = 0, INTERMEDIATE = 1, EXPERT = 2; // index of each of the difficulties available
	private final static int ROW = 0, COL = 1, MINES = 2;  // indexes of the information in the VALUES array
	private final static int [][] VALUES = {{8, 8, 10}, {16, 16, 40}, {16, 32, 99}}; // rows, cols, and mines for each difficulty

	private final static int DEFAULT = INTERMEDIATE; // change this to change the default starting difficulty

	private static Random ran = new java.security.SecureRandom(); // random object for map generation

	private int difficulty;
	private boolean gameOver;
	private boolean newGame; //flag for if the player has requested a new game
	private boolean firstClick; // flag to indicate player has started game
	private JFrame frame;
	private JPanel all;
    private JButton face, timer, mines;
	private MineSweeperButton[][] grid;
	private int[][] map;
	private int totalMines, timeCounter;
	private HighScoreIO scoreManager = new HighScoreIO();


	/**
	 * Main Method to start up program 
	 */
	public static void main(String[]args) {
		MineSweeper game = new MineSweeper();
		game.start();
	}

	/**
	 * Main Game loop that runs until Program finishes. 
	 * Initializes frame then adds the menu and components. 
	 * Updates the timer and mines left text items and checks for game-over conditions. 
	 */
	private void start() {
		difficulty = DEFAULT;
		initializeFrame();
		initializeMenu();
		while(true) {

			gameOver = false;
			this.newGame = false;
			this.firstClick = true;
			createMap();
			addNeighbors();
			setTopButtons();
			frame.pack();
			frame.validate();
			frame.repaint();
			long startTime = System.currentTimeMillis();
			timeCounter = 0;
			timer.setText(String.valueOf(timeCounter));
			while(!update()){ 
				// game running while update returns false
				if(System.currentTimeMillis() - startTime > 1000 && !firstClick && !gameOver){
					timeCounter++;
					timer.setText(String.format("Time: %d", timeCounter));
					startTime = System.currentTimeMillis();
				}
			}
			// game is over and new game has been requested 
			clearComponents();
		}
	}

	/**
	 * Clears components from frame and invalidates 
	 */
	public void clearComponents(){
		frame.getContentPane().removeAll();
		frame.invalidate();
	}

	/**
	 * Parses over game board and checks for game-over conditions. 
	 * @return False if game is over. Else True
	 */
	public boolean update(){
		boolean mineClicked = false;
		int flagCount = 0, uncoveredCount = 0;
		for(int i = 0; i < VALUES[difficulty][ROW]; i++) {
			for(int j = 0; j < VALUES[difficulty][COL]; j++) {
				if(gameOver) { break; }
				if(grid[i][j].getState() == MineSweeperButton.FLAG){
					flagCount ++;
				}
				if(grid[i][j].isUncovered()) {  //means that it has been uncovered
					uncoveredCount ++;
				}
				if(grid[i][j].getState() == MineSweeperButton.MINE_RED){
					mineClicked = true;
				}
			}
		}

		if(uncoveredCount > 0){
			firstClick = false;
		}

		// Handling Face-Icon for Game-over
		if(mineClicked){
			gameOver = true;
			face.setIcon(new ImageIcon(MineSweeperButton.ICON_PATHS[MineSweeperButton.FACE_DEAD]));
			showGameLostScreen();
		}else if(uncoveredCount == (VALUES[difficulty][ROW] * VALUES[difficulty][COL]) - VALUES[difficulty][MINES]) {
			gameOver = true;
			face.setIcon(new ImageIcon(MineSweeperButton.ICON_PATHS[MineSweeperButton.FACE_WIN]));
			if(scoreManager.isHighScore(difficulty, timeCounter)){
				String name = JOptionPane.showInputDialog("You got a high score! Enter your name: \n");
				scoreManager.addScore(difficulty, timeCounter, name);
			}
            JOptionPane.showMessageDialog(null, "Congratulations!! \nHigh scores: \n" + scoreManager.getDifficultyInfo(difficulty));
			
		}

		mines.setText(String.format("Bombs left: %d", Math.max((VALUES[difficulty][MINES] - flagCount), 0)));

		try {
			Thread.sleep(10);
		} catch (InterruptedException ignored) {
			System.err.println("Interrupted Exception");
			ignored.printStackTrace();
		}

		return gameOver && newGame;
	}


	/**
	 * Initializes frame sets title/visible/default close operation
	 */
	private void initializeFrame(){
		frame = new JFrame();
		frame.setTitle("MineSweeper");
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}


	/**
	 * Sets the buttons to be mines or numbers 
	 */
	private void setMap() {

        boolean north, south, east, west;
		for(int i = 0; i < grid.length; i++) {
			for(int j = 0; j < grid[i].length; j++) {
				int mineCount = 0;

                north = i > 0;
				south = i < map.length-1;
				west = j > 0;
				east = j < map[i].length-1;

				// check up down left and right
				if(north && map[i-1][j] == 1){ mineCount++; }
				if(east && map[i][j+1] == 1) { mineCount++; }
				if(south && map[i+1][j] == 1) {mineCount++; }
				if(west && map[i][j-1] == 1) { mineCount++; }

				// check corners
				if(north && east && map[i-1][j+1] == 1){ mineCount++; }
				if(north && west && map[i-1][j-1] == 1){ mineCount++; }
				if(south && east && map[i+1][j+1] == 1){ mineCount++; }
				if(south && west && map[i+1][j-1] == 1){ mineCount++; }

				//set number on button
				grid[i][j].setNum(mineCount);

				//set button if it's a bomb
				if(map[i][j] == 1){
					grid[i][j].setIsAMine();
				}

			}
		}
	}

	/**
	 * Adds the neighbors of each button to its list of neighbors
	 */
	private void addNeighbors(){
		// adding neighbor buttons to each button
        boolean north, south, east, west;
		for(int i = 0; i < VALUES[difficulty][ROW]; i++) {
			for(int j = 0; j < VALUES[difficulty][COL]; j++) {
                north = i > 0;
                south = i < (map.length - 1);
                west = j > 0;
                east = j < (map[0].length - 1);

				// adding up down left and right neighbors
				if(north){grid[i][j].addNeighbor(grid[i-1][j]); }
				if(east) {grid[i][j].addNeighbor(grid[i][j+1]); }
				if(south){grid[i][j].addNeighbor(grid[i+1][j]); }
				if(west) {grid[i][j].addNeighbor(grid[i][j-1]); }
				// adding diagonal neighbors
				if(north && east) { grid[i][j].addNeighbor(grid[i-1][j+1]); }
				if(north && west) { grid[i][j].addNeighbor(grid[i-1][j-1]); }
				if(south && east) { grid[i][j].addNeighbor(grid[i+1][j+1]); }
				if(south && west) { grid[i][j].addNeighbor(grid[i+1][j-1]); }
			}
		}
	}


	/**
	 * Sets the face, timer and mines left buttons 
	 */
	private void setTopButtons(){
        JPanel top = new JPanel(new GridLayout(1, 3));
		mines = new JButton(String.valueOf(totalMines));
		timer = new JButton("Time: 0");
		face = new JButton();

		face.addActionListener(e -> {
            gameOver = true;
            newGame = true;
            face.setIcon( new ImageIcon(MineSweeperButton.ICON_PATHS[MineSweeperButton.FACE_SMILE]));
        });

		face.setIcon( new ImageIcon(MineSweeperButton.ICON_PATHS[MineSweeperButton.FACE_SMILE]));
		top.add(mines);
		top.add(face);
		top.add(timer);
		all.add(top, BorderLayout.NORTH);
	}

	private void createMap(){
		all = new JPanel(new BorderLayout());
        JPanel center = new JPanel(new GridLayout(VALUES[difficulty][ROW], VALUES[difficulty][COL]));
		grid = new MineSweeperButton[VALUES[difficulty][ROW]][VALUES[difficulty][COL]];
		map = new int[VALUES[difficulty][ROW]] [VALUES[difficulty][COL]];
		for(int i = 0; i < VALUES[difficulty][ROW]; i++) {
			for(int j = 0; j < VALUES[difficulty][COL]; j++) {
				grid[i][j] = new MineSweeperButton(this, i ,j);
				center.add(grid[i][j]);
			}
		}


		all.add(center, BorderLayout.CENTER);  // finally add everything to the frame 
		frame.add(all);
	}

	/**
	 * Generates a random map based on the current difficulty and the placement of the first click
     * @param first this button that received the first click.
	 */
    void generateMap(MineSweeperButton first, MouseEvent e) {
		this.firstClick = false;
		map = new int[VALUES[difficulty][ROW]] [VALUES[difficulty][COL]];
		this.totalMines = VALUES[difficulty][MINES];
		int i = 0;
        while(i < totalMines){
			int row = ran.nextInt(VALUES[difficulty][ROW]);
			int col = ran.nextInt(VALUES[difficulty][COL]);

			if(map[row][col] != 1 && !first.equals(grid[row][col]) && !first.getNeighbors().contains(grid[row][col])) {
                map[row][col] = 1;
                i++;
            }
		}
		setMap();
		first.mousePressed(e);
	}

	/**
	 * Sets the menu for the frame with a new game option for different difficulties and and exit option 
	 */
	public void initializeMenu() {
		JMenuBar menu = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		JMenu newGameSubMenu = new JMenu("New Game");
		JMenuItem exit = new JMenuItem("Exit");
		JMenuItem newBeginner = new JMenuItem("Beginner");
		JMenuItem newIntermediate = new JMenuItem("Intermediate");
		JMenuItem newExpert = new JMenuItem("Expert");
		JMenu highScoreSubMenu = new JMenu("High Scores");

		JMenuItem highScoreB = new JMenuItem("Beginner");
		JMenuItem highScoreI = new JMenuItem("Intermediate");
		JMenuItem highScoreE = new JMenuItem("Expert");
		
		highScoreB.addActionListener(e -> JOptionPane.showMessageDialog(null, "High scores: \n" + scoreManager.getDifficultyInfo(BEGINNER)));
		
		highScoreI.addActionListener(e -> JOptionPane.showMessageDialog(null, "High scores: \n" + scoreManager.getDifficultyInfo(INTERMEDIATE)));
		
		highScoreE.addActionListener(e -> JOptionPane.showMessageDialog(null, "High scores: \n" + scoreManager.getDifficultyInfo(EXPERT)));
		
		newBeginner.addActionListener(e -> {
            gameOver = true;
            newGame = true;
            difficulty = BEGINNER;
        });

		newIntermediate.addActionListener(e -> {
            gameOver = true;
            newGame = true;
            difficulty = INTERMEDIATE;
        });

		newExpert.addActionListener(e -> {
            newGame = true;
            gameOver = true;
            difficulty = EXPERT;
        });

		exit.addActionListener(e -> System.exit(0));
		
		highScoreSubMenu.add(highScoreB);
		highScoreSubMenu.add(highScoreI);
		highScoreSubMenu.add(highScoreE);
		newGameSubMenu.add(newBeginner);
		newGameSubMenu.add(newIntermediate);
		newGameSubMenu.add(newExpert);
		fileMenu.add(newGameSubMenu);
		fileMenu.add(highScoreSubMenu);
		fileMenu.add(exit);
		menu.add(fileMenu);
		frame.setJMenuBar(menu);
	}

	/**
	 * Displays all mines that were not flagged or were mis-flagged after the game is lost
	 */
	private void showGameLostScreen() {
        for (MineSweeperButton[] aGrid : grid) {
            for (MineSweeperButton temp : aGrid) {
                if (temp.getState() == MineSweeperButton.COVER && temp.isAMine()) {
                    temp.setState(MineSweeperButton.MINE_GREY);
                }
                if (temp.getState() == MineSweeperButton.FLAG && !temp.isAMine()) {
                    temp.setState(MineSweeperButton.MINE_MISFLAGGED);
                }
            }
        }

	}

    /**
     * @param face sets the icon for the face button
     */
    void setFace(ImageIcon face) {
		this.face.setIcon(face);
	}

    /**
     * @return true if game is in first click state
     */
    boolean isFirstClick(){
		return this.firstClick;
	}


    /**
     * @return true if game over else false
     */
    boolean gameOver() {
        return this.gameOver;
    }
}
