package ui.controllers;

import domain.entities.Product;
import domain.managers.ProductService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ProductFormController {

	@FXML
	private Label formTitle;
	@FXML
	private TextField nameField;
	@FXML
	private TextField typeField;
	@FXML
	private TextField priceField;

	private ProductService productService;
	private OwnerController ownerController;
	private Product editingProduct; // null if adding new

	// Inject dependencies from OwnerController
	public void setDependencies(ProductService ps, OwnerController owner) {
		this.productService = ps;
		this.ownerController = owner;
	}

	// Load product for editing
	public void setEditingProduct(Product p) {
		this.editingProduct = p;
		formTitle.setText("Update Product");
		nameField.setText(p.getName());
		typeField.setText(p.getType());
		priceField.setText(String.valueOf(p.getPricePerDay()));
	}

	// Save button clicked
	@FXML
	private void onSaveClicked() {
		String name = nameField.getText().trim();
		String type = typeField.getText().trim();
		double price;

		try {
			price = Double.parseDouble(priceField.getText().trim());
		} catch (NumberFormatException e) {
			System.out.println("Invalid price!");
			return;
		}

		if (editingProduct != null) {
			editingProduct.setName(name);
			editingProduct.setType(type);
			editingProduct.setPricePerDay(price);
			productService.updateProduct(editingProduct);
		} else {
			Product p = new Product(ownerController.getOwnerUserId(), name, type, price);
			productService.addProduct(p);
		}

		ownerController.loadProductInventory(); // ← full reload instead of renderProductCard
		closeWindow();
	}

	// Cancel button clicked
	@FXML
	private void onCancelClicked() {
		closeWindow();
	}

	// Helper to close the window
	private void closeWindow() {
		Stage stage = (Stage) nameField.getScene().getWindow();
		stage.close();
	}
}
