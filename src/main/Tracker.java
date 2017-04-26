package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

public class Tracker extends JFrame {
	public static final Dimension INTEGER_FIELD_DIMENSION = new Dimension(30, Tracker.LABEL_HEIGHT);
	public static final int ROW_HEIGHT = 30;
	public static final int LABEL_HEIGHT = ROW_HEIGHT - 10;
	
	private final JPanel background;
	private final JPanel labels;
	
	private List<TrackerItem> creatures;
	
	
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
		
		// Add key bindings.
		background.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), "newCreature");
		background.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), "newCreature");
		background.getActionMap().put("newCreature", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addNewCreature();
			}
		});
	}
	
	/**
	 * Redraws the window.
	 * TODO: should possibly override something?
	 */
	public void redraw() {
		background.add(labels);
		
		for (TrackerItem creature : creatures) {
			background.add(creature);
		}
		
		this.add(background);
		this.pack();
		this.setVisible(true);
	}
	
	private void addNewCreature() {
		TrackerItem newCreature = new TrackerItem(10 + ROW_HEIGHT + (creatures.size() * ROW_HEIGHT), "Creature " + creatures.size(), 0, 0, 0);
		// TODO: detect PC's
		creatures.add(newCreature);
		redraw();
		newCreature.grabFocus();
	}
}
