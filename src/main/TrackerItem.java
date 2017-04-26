package main;

import javax.swing.*;
import java.awt.*;

/** Represents a single creature in the initiative order. */
public class TrackerItem extends JPanel implements Comparable<TrackerItem> {
	private final JTextField name;
	private final JTextField armorClass;
	private final JTextField spellSaveDc;
	private final JTextField initiative;
	
	public TrackerItem(String name, int armorClass, int spellSaveDc, int initiative) {
		this.name = new JTextField(name);
		this.name.setPreferredSize(new Dimension(100, Tracker.LABEL_HEIGHT));
		this.name.selectAll();
		this.name.setVisible(true);
		this.add(this.name);
		this.armorClass = new JTextField(String.valueOf(armorClass));
		this.armorClass.setPreferredSize(Tracker.INTEGER_FIELD_DIMENSION);
		this.armorClass.selectAll();
		this.armorClass.setVisible(true);
		this.add(this.armorClass);
		this.spellSaveDc = new JTextField(String.valueOf(spellSaveDc));
		this.spellSaveDc.setPreferredSize(Tracker.INTEGER_FIELD_DIMENSION);
		this.spellSaveDc.selectAll();
		this.spellSaveDc.setVisible(true);
		this.add(this.spellSaveDc);
		this.initiative = new JTextField(String.valueOf(initiative));
		this.initiative.setPreferredSize(Tracker.INTEGER_FIELD_DIMENSION);
		this.initiative.selectAll();
		this.initiative.setVisible(true);
		this.add(this.initiative);
		
		this.setAlignmentX(Component.LEFT_ALIGNMENT);
	}
	
	public void grabFocus() {
		name.grabFocus();
	}
	
	@Override
	public int compareTo(TrackerItem other) {
		return Integer.parseInt(other.initiative.getText()) - Integer.parseInt(this.initiative.getText());
	}
}
