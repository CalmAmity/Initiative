package main;

import org.apache.commons.lang3.StringEscapeUtils;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static main.TrackerItem.INTEGER_FIELD_WIDTH;
import static main.TrackerItem.MARGIN;

public class Tracker extends JFrame implements ActionListener, KeyListener {
	public static final int ROW_HEIGHT = 30;
	public static final int LABEL_HEIGHT = ROW_HEIGHT - 10;
	private final JLabel name;
	private final JLabel armorClass;
	private final JLabel spellSaveDc;
	private final JLabel initiative;
	
	private int width, height;
	
	private List<TrackerItem> creatures;
	
	private JPanel background;
	
	public static void main(String[] args) {
		Tracker tracker = new Tracker();
		tracker.redraw();
	}
	
	public Tracker() {
		width = 400;
		height = 500;
		this.setTitle("Initiative");
		creatures = new ArrayList<>();
		// Exit the JVM when the window is closed.
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		
		this.addKeyListener(this);
		
		background = new JPanel();
		background.setVisible(true);
		
		name = new JLabel("Name");
		name.setBounds(10, 10, 100, LABEL_HEIGHT);
		
		armorClass = new JLabel("AC");
		armorClass.setBounds(120, 10, INTEGER_FIELD_WIDTH, LABEL_HEIGHT);
		armorClass.setHorizontalAlignment(SwingConstants.CENTER);
		
		spellSaveDc = new JLabel("SpDC");
		spellSaveDc.setBounds(120 + INTEGER_FIELD_WIDTH + MARGIN - 10, 10, INTEGER_FIELD_WIDTH + 20, Tracker.LABEL_HEIGHT);
		spellSaveDc.setHorizontalAlignment(SwingConstants.CENTER);
		
		initiative = new JLabel("Init");
		initiative.setBounds(120 + (INTEGER_FIELD_WIDTH * 2) + (MARGIN * 2), 10, INTEGER_FIELD_WIDTH, Tracker.LABEL_HEIGHT);
		initiative.setHorizontalAlignment(SwingConstants.CENTER);
	}
	
	/**
	 * Redraws the window.
	 * TODO: should possibly override something?
	 */
	public void redraw() {
		height = (ROW_HEIGHT * 3) + (creatures.size() * ROW_HEIGHT);
		
		this.setSize(width, height);
		
		for (TrackerItem creature : creatures) {
			creature.publishTo(this);
		}
		
		this.add(name);
		this.add(armorClass);
		this.add(spellSaveDc);
		this.add(initiative);
		this.add(background);
		this.setVisible(true);
	}
	
	private void addNewCreature() {
		TrackerItem newCreature = new TrackerItem(10 + ROW_HEIGHT + (creatures.size() * ROW_HEIGHT), "Creature " + creatures.size(), 0, 0, 0);
		// TODO: detect PC's
		creatures.add(newCreature);
		redraw();
		newCreature.grabFocus();
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
	
	}
	
	@Override
	public void keyTyped(KeyEvent e) {
	
	}
	
	@Override
	public void keyPressed(KeyEvent event) {
		char keyPressed = (char)event.getKeyCode();
		String keyString = String.valueOf(keyPressed);
		if (Character.isISOControl(keyPressed)) {
			keyString = StringEscapeUtils.escapeJava(keyString);
		}
		
		System.out.println("Key press registered: KeyEvent " + keyString + ".");
		if (event.getKeyCode() == KeyEvent.VK_N) {
			System.out.println("Key is N. Adding new creature.");
			addNewCreature();
		} else if (event.getKeyCode() == KeyEvent.VK_ENTER) {
			event.getSource();
		}
	}
	
	@Override
	public void keyReleased(KeyEvent e) {
	
	}
}
