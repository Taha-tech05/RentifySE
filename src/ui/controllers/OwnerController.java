package ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class OwnerController {

	@FXML
	private Label dashboardLabel;

	@FXML
	public void initialize() {
		dashboardLabel.setText("Welcome to Owner Dashboard!");
	}
}