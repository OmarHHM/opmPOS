/*  
Copyright (C) 2021  Open Source Mexico
This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/


package com.opm.pos.controllers.modal;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

import com.opm.pos.controllers.SaleController;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import com.opm.pos.models.Product;
import com.opm.pos.models.Sale;
import com.opm.pos.models.Transaction;
import com.opm.pos.service.ProductService;
import com.opm.pos.service.StockService;
import com.opm.pos.service.impl.ProductServiceImpl;
import com.opm.pos.service.impl.StockServiceImpl;



public class PaymentController implements Initializable {
		
	
	private SaleController controller;
	
	
	public void setSaleController(SaleController controller){
	    this.controller = controller;
	}

	@FXML
    private TextField txtCash;

	@FXML
    private Label lblTotal;

    @FXML
    private Button btnCancel;

    @FXML
    private Button btnAdd;
	private ProductService productService = new ProductServiceImpl();    
	private StockService stockService = new StockServiceImpl();
    Alert alert = new Alert(AlertType.NONE);
    private Product product= new Product();
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    	txtCash.textProperty().addListener(new ChangeListener<String>() {
    	    @Override
    	    public void changed(ObservableValue<? extends String> observable, String oldValue, 
    	        String newValue) {
    	        if (!newValue.matches("\\d*")) {
    	        	txtCash.setText(newValue.replaceAll("[^\\d]", ""));
    	        }
    	    }
    	});
        	
    }

    public void initData(String total) {
    	lblTotal.setText("$"+total);
    }
    
    
    @FXML
    private void handleCancel (ActionEvent event) {    	
    	Node node = (Node) event.getSource();
	    Stage stage = (Stage) node.getScene().getWindow();
	    stage.close();
    }

    @FXML
    private void handlePay (ActionEvent event) {   
    	
    	if("".equals(txtCash.getText()) || (Double.parseDouble(txtCash.getText().replace("$", "")) < Double.parseDouble(lblTotal.getText().replace("$","")))){    		
    		alert.setAlertType(AlertType.ERROR);
            alert.setContentText("Debe de ingresar una monto valido");
            alert.show();
    	}else {
    	
    		
    		try {
        		List<Sale> sales = controller.tblData.getItems();
        		int sum = sales.stream().mapToInt(o -> o.getCount()).sum();
        		Transaction transaction = new Transaction();
        		transaction.setProductCount(sum);
        		transaction.setTotal(Double.parseDouble(lblTotal.getText().replace("$", "")));
        		
        		String folio= stockService.createTransaction(transaction);
        		stockService.createDetails(sales, folio);    			
        
        		alert.setAlertType(AlertType.INFORMATION);
                alert.setContentText("Pago Exitoso");
            //  alert.setContentText("Pago Exitoso, �Desea imprimir su ticket?");
                alert.show();
                controller.data.clear();
        		controller.tblData.getItems().clear();    		
        		controller.txtSubtotal.setText("$0.00");
        		controller.txtTotal.setText("$0.00");
        	}catch(SQLException e) {
    			System.out.println("Error al realizar pago " +e);
    			alert.setAlertType(AlertType.INFORMATION);
                alert.setContentText("Error de Sistema, no se ha podido realizar la operacion.");
            //  alert.setContentText("Pago Exitoso, �Desea imprimir su ticket?");
                alert.show();
             }
    		
        	Node node = (Node) event.getSource();
    	    Stage stage = (Stage) node.getScene().getWindow();
    	    stage.close();
    	}
    }


    public boolean verifyStock(String serie,int count){
    	product= productService.getProduct(serie);
    	if(product.getCount()<count) {
    		return false;
    	}else {
    		return true;
    	}
    }
}
