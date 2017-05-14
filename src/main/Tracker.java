package main;

import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
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
	
	private Map<String, TrackerItem> playerCharacters;
	
	/**
	 * The panel that serves as a parent to all GUI components.
	 */
	private final JPanel background;
	/** The row with column headers. */
	private final JPanel labels;
	
	private List<TrackerItem> creatures;
	
	private int currentTurnIndex = 0;
	
	public static void main(String[] args) {
		Tracker tracker = new Tracker();
		tracker.redraw();
	}
	
	public Tracker() {
		this.setTitle("Initiative");
		creatures = new ArrayList<>();
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
		
		labels = new JPanel();
		labels.setVisible(true);
		labels.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		// Add column labels for the individual fields.
		JLabel name = new JLabel("Name");
		name.setPreferredSize(new Dimension(100, LABEL_HEIGHT));
		name.setHorizontalAlignment(SwingConstants.LEADING);
		labels.add(name);
		
		JLabel armorClass = new JLabel("AC");
		armorClass.setPreferredSize(INTEGER_FIELD_DIMENSION);
		armorClass.setHorizontalAlignment(SwingConstants.CENTER);
		labels.add(armorClass);
		
		JLabel spellSaveDc = new JLabel("SDC");
		spellSaveDc.setPreferredSize(INTEGER_FIELD_DIMENSION);
		spellSaveDc.setHorizontalAlignment(SwingConstants.CENTER);
		labels.add(spellSaveDc);
		
		JLabel initiative = new JLabel("Init");
		initiative.setPreferredSize(INTEGER_FIELD_DIMENSION);
		initiative.setHorizontalAlignment(SwingConstants.CENTER);
		labels.add(initiative);
		
		// Add key bindings. For some reason, this only works if we first add a dummy keystroke to the input map of a randomly selected component. //TODO check whether necessary
		labels.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_QUOTE, 0), "something");
		InputMap inputMap = background.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		
		// Key F1: add new creature to the tracker.
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), "newCreature");
		background.getActionMap().put("newCreature", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				TrackerItem newCreature = new TrackerItem(Tracker.this, "Creature " + creatures.size(), 0, 0, 0);
				creatures.add(newCreature);
				redraw();
				newCreature.grabFocus();
			}
		});
		
		// Key F2: move the turn to the next creature.
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "nextTurn");
		background.getActionMap().put("nextTurn", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Raise the turn index.
				currentTurnIndex++;
				// Reset it to 0 if it is outside the list.
				if (currentTurnIndex >= creatures.size()) {
					currentTurnIndex = 0;
				}
				// Redraw the window.
				redraw();
				background.grabFocus();
			}
		});
		
		// Key ENTER: reorder the creatures based on their initiative scores.
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "reorder");
		background.getActionMap().put("reorder", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Remember whose turn it is.
				TrackerItem creatureAtTurn = creatures.get(currentTurnIndex);
				
				for (int creatureIndex = 0; creatureIndex < creatures.size(); creatureIndex++) {
					TrackerItem currentCreature = creatures.get(creatureIndex);
					TrackerItem playerCharacter = playerCharacters.get(currentCreature.getName());
					if (playerCharacter != null) {
						creatures.set(creatureIndex, playerCharacter);
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
				background.grabFocus();
			}
		});
	}
	
	/**
	 * Redraws the window.
	 */
	public void redraw() {
		background.removeAll();
		
		background.add(labels);
		
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
	
	/**
	 * Uses the pcinfo.csv file to initialise {@link #playerCharacters} map.
	 */
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
					this, characterValues[1], Integer.parseInt(characterValues[2]), Integer.parseInt(characterValues[3]), Integer.parseInt(characterValues[4]));
			
			// For every key in keys[], put the tracker item in the map using that key.
			for (String key : keys) {
				playerCharacters.put(key, currentCharacter);
			}
		}
	}
	
	/**
	 * Removes an item from the tracker.
	 *
	 * @param trackerItem The item to be removed.
	 */
	void deleteItem(TrackerItem trackerItem) {
		creatures.remove(trackerItem);
		redraw();
	}
}
