package uk.co.complex.lvs.ggp.forms;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.text.DefaultCaret;

import uk.co.complex.lvs.ggp.GameManager;
import uk.co.complex.lvs.ggp.Player;
import uk.co.complex.lvs.ggp.StateMachine;
import uk.co.complex.lvs.ggp.forms.elements.DataItem;
import uk.co.complex.lvs.ggp.games.connectfour.ConnectFour;
import uk.co.complex.lvs.ggp.games.connectfour.ConnectFourHuman;
import uk.co.complex.lvs.ggp.games.tictactoe.TicTacToe;
import uk.co.complex.lvs.ggp.players.FixedDepthPlayer;
import uk.co.complex.lvs.ggp.players.MCTSPlayer;
import uk.co.complex.lvs.ggp.players.MinimaxPlayer;
import uk.co.complex.lvs.ggp.players.RandomPlayer;
import uk.co.complex.lvs.ggp.players.VariableDepthPlayer;

public class GameManagerUI extends JPanel implements ActionListener, GameOutput {
	private JComboBox<DataItem> gameSelector;
	private JComboBox<DataItem> playerSelector1;
	private JComboBox<DataItem> playerSelector2;
	private SpinnerNumberModel timeModel;
	private JTextArea stateText;
	private JTextArea logText;

	public GameManagerUI(){
		super(new GridBagLayout());
		initComponents();
		
	}
	
	private void initComponents() {
		GridBagConstraints c = new GridBagConstraints();
		
		// Add game label
		c.gridx = 0;
		c.gridy = 0;
		add(new JLabel("Game:"), c);
		
		// Add game selection box
		DataItem[] games = new DataItem[] {new DataItem("Tic Tac Toe", new TicTacToe()),
										   new DataItem("Connect Four", new ConnectFour())};
		gameSelector = new JComboBox<>(games);
		c.gridx = 0;
		c.gridy = 1;
		add(gameSelector, c);
		
		// Add player label 1
		c.gridx = 0;
		c.gridy = 2;
		add(new JLabel("Player 1:"), c);
		
		// Add player selection box 1
		DataItem[] players = new DataItem[] {new DataItem("Variable-depth player", new VariableDepthPlayer("VD Player")),
											 new DataItem("MCTS player", new MCTSPlayer("MCTS Player")),
											 new DataItem("Fixed-depth player", new FixedDepthPlayer("FD Player")),
											 new DataItem("Random player", new RandomPlayer("Random player")),
											 new DataItem("Minimax player", new MinimaxPlayer("Minimax player"))};
		playerSelector1 = new JComboBox<>(players);
		c.gridx = 0;
		c.gridy = 3;
		add(playerSelector1, c);
		
		// Add player label 2
		c.gridx = 0;
		c.gridy = 4;
		add(new JLabel("Player 2:"), c);
				
		// Add player selection box 2
		playerSelector2 = new JComboBox<>(players);
		c.gridx = 0;
		c.gridy = 5;
		add(playerSelector2, c);
		
		// Add time label
		c.gridx = 0;
		c.gridy = 6;
		add(new JLabel("Time (ms):"), c);
		
		// Add time selection spinner
		timeModel = new SpinnerNumberModel(1_000, 200, 10_000, 100);
		JSpinner timeSpinner = new JSpinner(timeModel);
		c.gridx = 0;
		c.gridy = 7;
		c.anchor = GridBagConstraints.NORTH;
		add(timeSpinner, c);
		
		// Add start button
		JButton startButton = new JButton("Start");
		startButton.setVerticalAlignment(AbstractButton.CENTER);
		startButton.setHorizontalAlignment(AbstractButton.LEADING);
		startButton.setMnemonic(KeyEvent.VK_S);
		startButton.setActionCommand("start");
		startButton.addActionListener(this);
		c.gridx = 0;
		c.gridy = 8;
		add(startButton, c);
		
		// Add game state text area
		stateText = new JTextArea(10, 10);
		stateText.setEditable(false);
		stateText.setBorder(new LineBorder(Color.black));
		stateText.setFont(new Font("Monospaced", Font.PLAIN, 12));
		DefaultCaret caret = (DefaultCaret)stateText.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		JScrollPane scroll = new JScrollPane(stateText);
		c.gridx = 1;
		c.gridy = 0;
		c.weighty = 1;
		c.weightx = 1;
		c.fill = GridBagConstraints.BOTH;
		c.gridheight = 8;
		add(scroll, c);

		// Add game log text area
		logText = new JTextArea(1, 10);
		logText.setEditable(false);
		logText.setBorder(new LineBorder(Color.black));
		c.gridx = 1;
		c.gridy = 8;
		c.gridheight = 1;
		c.weighty = 0;
		add(logText, c);
	}
	
	@Override
	public void print(Object message) {
		String current = stateText.getText();
		if (!current.equals("")) current += "\n";
		stateText.setText(current + message.toString());
	}

	@Override
	public void log(Object message) {
		String current = logText.getText();
		if (!current.equals("")) current += "\n";
		logText.setText(current + message.toString());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if("start".equals(e.getActionCommand())) {
			StartGame();
		}
	}
	
	private void StartGame() {
		// Clear the screen
		stateText.setText("");
		logText.setText("");
		
		final GameManager gm = new GameManager();
		
		// Get the selected game
		final DataItem selectedGame = (DataItem)gameSelector.getSelectedItem();
		final StateMachine game = (StateMachine)selectedGame.getData();
		
		// Get the selected players
		final List<Player> players = new ArrayList<>(2);
		final DataItem selectedPlayer1 = (DataItem)playerSelector1.getSelectedItem();
		final DataItem selectedPlayer2 = (DataItem)playerSelector2.getSelectedItem();
		players.add(((Player)selectedPlayer1.getData()).clone());
		players.add(((Player)selectedPlayer2.getData()).clone());
		
		// Get the time
		final int time = (int) timeModel.getValue();
		
		// Play the game (start a new thread for running the game)
		Thread t = new Thread() {
			@Override
			public void run() {
				gm.play(game, players, time, GameManagerUI.this);
			}
		};
		t.start();
	}
	
	private static void createAndShowGUI() {
		// Create and set up the windows
		JFrame frame = new JFrame("Game Manager");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// Create and set up the content pane
		JComponent contentPane = new GameManagerUI();
		contentPane.setOpaque(true);
		frame.setContentPane(contentPane);
		
		// Display the window
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		//Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}
}