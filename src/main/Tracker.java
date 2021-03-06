package main;

import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tracker extends JFrame {
	public static final Dimension INTEGER_FIELD_DIMENSION = new Dimension(30, Tracker.LABEL_HEIGHT);
	public static final int ROW_HEIGHT = 30;
	public static final int LABEL_HEIGHT = ROW_HEIGHT - 10;
	
	/**
	 * Contains all pre-defined characters.
	 * @see #parsePcInfo()
	 */
	private Map<String, TrackerItem> playerCharacters;
	
	/** The panel that serves as a parent to all GUI components. */
	private final JPanel background;
	/** The window component in which the current turn number is displayed. */
	private final JPanel roundTracker;
	/** The label that shows the current turn number. */
	private final JLabel roundTrackerLabel;
	/** The row with column headers. */
	private final JPanel columnHeaders;
	/** The creatures that are currently in the tracker. */
	private List<TrackerItem> creatures;
	/** The position in {@link #creatures} of the creature currently at turn. */
	private int currentTurnIndex;
	/** Indicates the current round; starts counting at 1. */
	private int currentRound;
	
	public static void main(String[] args) {
		Tracker tracker = new Tracker();
		tracker.redraw();
	}
	
	public Tracker() {
		this.setTitle("Initiative");
		creatures = new ArrayList<>();
		currentRound = 1;
		// Exit the JVM when the window is closed.
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		
		parsePcInfo();
		
		background = new JPanel();
		background.setVisible(true);
		background.setLayout(new BoxLayout(background, BoxLayout.Y_AXIS));
		
		roundTracker = new JPanel();
		roundTracker.setVisible(true);
		roundTracker.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		roundTrackerLabel = new JLabel();
		roundTracker.add(roundTrackerLabel);
		
		columnHeaders = new JPanel();
		columnHeaders.setVisible(true);
		columnHeaders.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		// Add column headers for the individual fields.
		JLabel name = new JLabel("Name");
		name.setPreferredSize(new Dimension(100, LABEL_HEIGHT));
		name.setHorizontalAlignment(SwingConstants.LEADING);
		columnHeaders.add(name);
		
		JLabel armorClass = new JLabel("AC");
		armorClass.setPreferredSize(INTEGER_FIELD_DIMENSION);
		armorClass.setHorizontalAlignment(SwingConstants.CENTER);
		columnHeaders.add(armorClass);
		
		JLabel spellSaveDc = new JLabel("SDC");
		spellSaveDc.setPreferredSize(INTEGER_FIELD_DIMENSION);
		spellSaveDc.setHorizontalAlignment(SwingConstants.CENTER);
		columnHeaders.add(spellSaveDc);
		
		JLabel initiative = new JLabel("Init");
		initiative.setPreferredSize(INTEGER_FIELD_DIMENSION);
		initiative.setHorizontalAlignment(SwingConstants.CENTER);
		columnHeaders.add(initiative);
		
		createKeyBindings();
	}
	
	/** Creates key bindings for all interactions. */
	private void createKeyBindings() {
		// For some reason, this only works if we first add a dummy keystroke to the input map of a randomly selected component. //TODO check whether necessary
		columnHeaders.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_QUOTE, 0), "something");
		InputMap inputMap = background.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		
		// Key F1: add new creature to the tracker.
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), "newCreature");
		background.getActionMap().put("newCreature", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				TrackerItem newCreature = new TrackerItem("Creature " + creatures.size(), 0, 0, 0);
				creatures.add(newCreature);
				redraw();
				newCreature.requestFocusInWindow();
			}
		});
		
		// Key F2: move the turn to the next creature.
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "nextTurn");
		background.getActionMap().put("nextTurn", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				shiftCurrentTurn(1);
			}
		});
		
		// Key combination Shift+F2: move the turn to the previous creature.
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, InputEvent.SHIFT_MASK), "previousTurn");
		background.getActionMap().put("previousTurn", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				shiftCurrentTurn(-1);
			}
		});
		
		// Key ENTER: reorder the creatures based on their initiative scores.
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "reorder");
		background.getActionMap().put("reorder", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Remember whose turn it is.
				TrackerItem creatureAtTurn = creatures.get(currentTurnIndex);
				
				if (playerCharacters != null && !playerCharacters.isEmpty()) {
					// At least one pre-defined character exists. Check whether any of the items currently in the tracker use one of their keys.
					for (int creatureIndex = 0; creatureIndex < creatures.size(); creatureIndex++) {
						TrackerItem currentCreature = creatures.get(creatureIndex);
						TrackerItem playerCharacter = playerCharacters.get(currentCreature.getName());
						if (playerCharacter != null) {
							// A pre-defined character exists with the current item's name as its key. Overwrite this item with the pre-defined character's information.
							creatures.set(creatureIndex, playerCharacter);
							// If te pre-defined information contains no initiative score (score equals zero), retain the value that was already in the item being replaced.
							playerCharacter.fillInitiativeScore(currentCreature.determineInitiativeScore());
						}
					}
				}
				
				// Reorder the items by initiative score
				Collections.sort(creatures);
				// Reset the turn indicator.
				currentTurnIndex = creatures.indexOf(creatureAtTurn);
				if (currentTurnIndex < 0 || currentTurnIndex >= creatures.size()) {
					currentTurnIndex = 0;
				}
				
				redraw();
				background.requestFocusInWindow();
			}
		});
		
		// Keys arrow-up and page-up: move focus to the previous item in the tracker.
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "focusUp");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0), "focusUp");
		background.getActionMap().put("focusUp", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				shiftFocus(-1);
			}
		});
		
		// Keys arrow-down and page-down: move focus to the next item in the tracker.
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "focusDown");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0), "focusDown");
		background.getActionMap().put("focusDown", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				shiftFocus(1);
			}
		});
		
		// Key F4: remove this item from the tracker.
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0), "deleteCurrent");
		background.getActionMap().put("deleteCurrent", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				deleteCurrentItem();
			}
		});
		
		// Key Esc: move keyboard focus out of the input fields.
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "removeFocus");
		background.getActionMap().put("removeFocus", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				background.requestFocusInWindow();
			}
		});
	}
	
	/** Redraws the window. */
	private void redraw() {
		background.removeAll();
		
		roundTrackerLabel.setText("Current round: " + currentRound);
		background.add(roundTracker);
		
		background.add(columnHeaders);
		
		for (int creatureIndex = 0; creatureIndex < creatures.size(); creatureIndex++) {
			TrackerItem creature = creatures.get(creatureIndex);
			if (creatureIndex == currentTurnIndex) {
				// The current creature is active. Mark this with a divergent background colour.
				creature.setBackground(Color.LIGHT_GRAY);
			} else {
				// Reset the background colour.
				creature.setBackground(null);
			}
			
			background.add(creature);
		}
		
		this.add(background);
		this.pack();
		this.revalidate();
		this.repaint();
		this.setVisible(true);
	}
	
	/** Uses the pcinfo.csv file to initialise {@link #playerCharacters} map. */
	private void parsePcInfo() {
		List<String> lines;
		try {
			// The file should be small; read all lines into memory at once.
			lines = Files.readAllLines(Paths.get("pcinfo.csv"));
		} catch (IOException exception) {
			// Some error occurred; the tracker will still work but not recognise character keys.
			exception.printStackTrace();
			return;
		}
		
		// Initialise the map of character information.
		playerCharacters = new HashMap<>(lines.size());
		
		// Loop through the lines encountered in the file.
		for (String line : lines) {
			if (StringUtils.startsWith(line, "#")) {
				// '#' is used as a single-line comment indicator. Skip this line.
				continue;
			}
			
			// Split the line by semicolons; the resulting array contains the individual values for a single character.
			String[] characterValues = StringUtils.split(line, ";");
			// Split the first value of the array by commas; the resulting array contains all desired keys for the current character.
			String[] keys = StringUtils.split(characterValues[0], ",");
			// Use the remaining values to create a tracker item for the character.
			TrackerItem currentCharacter = new TrackerItem(
					characterValues[1], Integer.parseInt(characterValues[2]), Integer.parseInt(characterValues[3]), Integer.parseInt(characterValues[4]));
			
			// For every key in keys[], put the tracker item in the map using that key.
			for (String key : keys) {
				playerCharacters.put(key, currentCharacter);
			}
		}
	}
	
	/**
	 * Moves the turn indicator to another item in the list.
	 * @param turnShift the number of items to shift the turn downwards for. Use a negative value for upwards shift.
	 */
	private void shiftCurrentTurn(int turnShift) {
		currentTurnIndex += turnShift;
		// Correct underflow.
		while (currentTurnIndex < 0) {
			currentTurnIndex += creatures.size();
			currentRound--;
		}
		// Correct overflow.
		while (currentTurnIndex >= creatures.size()) {
			currentTurnIndex -= creatures.size();
			currentRound++;
		}
		redraw();
		background.requestFocusInWindow();
	}
	
	/** Removes the item that currently has focus from the tracker. */
	private void deleteCurrentItem() {
		try {
			creatures.remove(determineCurrentFocusIndex());
		} catch (IndexOutOfBoundsException exception) {
			// Apparently none of the tracker items has focus. Do nothing.
			return;
		}
		redraw();
	}
	
	/**
	 * Moves the keyboard focus to another item in the list.
	 * @param focusShift the number of items to shift the focus downwards for. Use a negative value for upwards shift.
	 */
	private void shiftFocus(int focusShift) {
		int currentFocusIndex;
		try {
			// Determine the index of the item that currently has focus.
			currentFocusIndex = determineCurrentFocusIndex();
		} catch (IndexOutOfBoundsException exception) {
			// Apparently none of the tracker items has focus. Use a ridiculous calculation to set the current index between the first and last items in the list. This could
			// obviously have been implemented using a simple if-statement, but this was much more fun.
			currentFocusIndex = -(focusShift + Math.abs(focusShift)) / (2 * focusShift);
		}
		
		// Shift the focus by the indicated number of items. Wrap around the top and bottom of the list.
		int newFocusIndex = (currentFocusIndex + focusShift + creatures.size()) % creatures.size();
		creatures.get(newFocusIndex).requestFocusInWindow();
		redraw();
	}
	
	/**
	 * Determines the index of the tracker item that currently has keyboard focus.
	 * @return the index of the currently focused item.
	 * @throws IndexOutOfBoundsException if none of the items have keyboard focus.
	 */
	private int determineCurrentFocusIndex() {
		for (int itemIndex = 0; itemIndex < creatures.size(); itemIndex++) {
			if (creatures.get(itemIndex).isFocusOwner()) {
				return itemIndex;
			}
		}
		
		throw new IndexOutOfBoundsException();
	}
}
