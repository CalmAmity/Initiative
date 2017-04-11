package main;

import java.awt.*;
import javax.swing.*;

/** Represents a single creature in the initiative order. */
public class TrackerItem {
	public static final int INTEGER_FIELD_WIDTH = 30;
	public static final int MARGIN = 10;
	
	private final JTextField name;
	private final JTextField armorClass;
	private final JTextField spellSaveDc;
	private final JTextField initiative;
	
	public TrackerItem(int height, String name, int armorClass, int spellSaveDc, int initiative) {
		this.name = new JTextField(name);
		this.name.setBounds(10, height, 100, Tracker.LABEL_HEIGHT);
		this.name.setVisible(true);
		this.armorClass = new JTextField(String.valueOf(armorClass));
		this.armorClass.setBounds(120, height, INTEGER_FIELD_WIDTH, Tracker.LABEL_HEIGHT);
		this.armorClass.setVisible(true);
		this.spellSaveDc = new JTextField(String.valueOf(spellSaveDc));
		this.spellSaveDc.setBounds(120 + INTEGER_FIELD_WIDTH + MARGIN, height, INTEGER_FIELD_WIDTH, Tracker.LABEL_HEIGHT);
		this.spellSaveDc.setVisible(true);
		this.initiative = new JTextField(String.valueOf(initiative));
		this.initiative.setBounds(120 + (INTEGER_FIELD_WIDTH * 2) + (MARGIN * 2), height, INTEGER_FIELD_WIDTH, Tracker.LABEL_HEIGHT);
		this.initiative.setVisible(true);
	}
	
	public void publishTo(Container container) {
		container.add(name);
		container.add(armorClass);
		container.add(spellSaveDc);
		container.add(initiative);
	}
	
	public void grabFocus() {
		name.grabFocus();
	}
}
