package minesweep;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Score manager class for reading, writing and comparing high scores for all of the difficulties of the MineSweeper game to a simple text file
 * @author Martin Wallace
 */
public class HighScoreIO {
	
	public Score[][] scores; // array of inner class Score. 
	private static final String path = "resources/highscores.txt";  // path to highscore file
	

	public HighScoreIO() {
		this.scores = new Score[3][5];
		readHighScores();
	}
	

	/**
	 * Returns a formatted String representing the top 5 highscores for a given difficulty 
	 * @param difficulty - The difficulty being requested. 0 for Beginner. 1 for Intermediate. 2 for Advanced 
	 * @return - formatted String to be displayed when high scores requested 
	 */
	public String getDifficultyInfo(int difficulty){
		String ret = "";
		for(int i = 0; i < scores[difficulty].length; i++){
			ret += (i+1) + ") " + scores[difficulty][i].name + ": " + scores[difficulty][i].score + " seconds \n";
		}
		return ret;
	}
	
	/**
	 * Reads the highscores off the highscore file and stores then in the Score[] scores
	 */
	private void readHighScores() {
		File file = new File(path);
		if(!file.exists() && !file.isDirectory()){
			try {
				boolean success = file.createNewFile();
				if(!success){
					throw new RuntimeException("Unable to create high score file");
				}
			}catch(IOException e){
				e.printStackTrace();
				throw new RuntimeException("Unable to find or create high score file");
			}
		}
		Scanner fScan;
		try {
			fScan= new Scanner(file);
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Unable to load high score file");
		}
		
		for(int i = 0; i < 3; i++){
			for(int j = 0; j < 5; j++){
				String line;
				try {
					line = fScan.nextLine();
				}catch (NoSuchElementException e){
					line = "default::1000";
				}
				String[]parts = line.split("::");
				scores[i][j] = new Score(parts[0], Integer.parseInt(parts[1]));
			}
		}
		fScan.close();
	}
	
	public void addScore(int difficulty, int time, String name){
		Score temp1 = null;
		Score temp2 = null;
		for(int i = 0; i < 5; i++){
			if(temp1 != null){
				temp2 = scores[difficulty][i];
				scores[difficulty][i] = temp1;
				temp1 = temp2;
				continue;
			}
			if(time < scores[difficulty][i].score){
				temp1 = scores[difficulty][i];
				scores[difficulty][i] = new Score(name, time);
			}
		}
		rewriteFile();
	}
	
	/**
	 * Check to see if a given score qualifies to make the high score list. 
	 * @param difficulty - the difficulty that was played when score was attained
	 * @param time - the score attained 
	 * @return - boolean value representing if the score was good enough to make the high score list 
	 */
	public boolean isHighScore(int difficulty, int time) {
		for(Score s : scores[difficulty]){
			if( time < s.score){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Class to store the high scores 
	 */
	private class Score{
		
		String name; 
		int score;
		Score(String name, int score){
			this.score = score; 
			this.name = name;
		}
		
		public String toString(){
			return this.name + "::" + this.score;
		}
	}
	
	/**
	 * Rewrites the data to the file with the new scores in place 
	 */
	private void rewriteFile( ){
		PrintWriter pw = null;
		File file = new File(path);
		
		try{
			pw = new PrintWriter(file);
		}catch(FileNotFoundException e) {
			System.err.println("File not found for HighScore");
			e.printStackTrace();
		}
		
		if(pw != null) {
			for (int i = 0; i < 3; i++) {
				for (Score s : scores[i]) {
					pw.write(s.toString() + "\n");
				}
			}
			pw.flush();
			pw.close();
		}
		this.readHighScores();
	}
}
