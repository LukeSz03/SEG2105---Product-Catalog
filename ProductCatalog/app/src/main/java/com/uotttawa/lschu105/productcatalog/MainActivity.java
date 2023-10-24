package com.uotttawa.lschu105.productcatalog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.app.AlertDialog;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.core.persistence.PruneForest;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    EditText editTextName;
    EditText editTextPrice;
    Button buttonAddProduct;
    ListView listViewProducts;

    List<Product> products;
    DatabaseReference databaseProducts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        databaseProducts = FirebaseDatabase.getInstance().getReference("Products");

        editTextName = (EditText) findViewById(R.id.editTextName);
        editTextPrice = (EditText) findViewById(R.id.editTextPrice);
        listViewProducts = (ListView) findViewById(R.id.listViewProducts);
        buttonAddProduct = (Button) findViewById(R.id.addButton);

        products = new ArrayList<>();

        //adding an onclicklistener to button
        buttonAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addProduct();
            }
        });

        listViewProducts.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Product product = products.get(i);
                showUpdateDeleteDialog(product.getId(), product.getProductName());
                return true;
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        databaseProducts.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                products.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()){
                    Product product = postSnapshot.getValue(Product.class);
                    products.add(product);
                }
                ProductList productAdapter = new ProductList(MainActivity.this, products);
                listViewProducts.setAdapter(productAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void showUpdateDeleteDialog(final String productId, String productName) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.update_dialog, null);
        dialogBuilder.setView(dialogView);

        final EditText editTextName = (EditText) dialogView.findViewById(R.id.editTextName);
        final EditText editTextPrice  = (EditText) dialogView.findViewById(R.id.editTextPrice);
        final Button buttonUpdate = (Button) dialogView.findViewById(R.id.buttonUpdateProduct);
        final Button buttonDelete = (Button) dialogView.findViewById(R.id.buttonDeleteProduct);

        dialogBuilder.setTitle(productName);
        final AlertDialog b = dialogBuilder.create();
        b.show();

        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = editTextName.getText().toString().trim();
                String prices = editTextName.getText().toString().trim();

                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(prices)) {
                    Toast.makeText(getApplicationContext(), "Please enter a name and price", Toast.LENGTH_SHORT).show();
                } else {
                    double price = Double.parseDouble(editTextPrice.getText().toString());
                    updateProduct(productId, name, price);
                    b.dismiss();
                }
            }
        });

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteProduct(productId);
                b.dismiss();
            }
        });
    }

    private void updateProduct(String id, String name, double price) {
        DatabaseReference dR = FirebaseDatabase.getInstance().getReference("Products").child(id);
        Product product = new Product(id, name, price);
        dR.setValue(product);

        Toast.makeText(getApplicationContext(), "Product updated", Toast.LENGTH_LONG).show();
    }

    private boolean deleteProduct(String id) {
        DatabaseReference dR = FirebaseDatabase.getInstance().getReference("Products").child(id);
        dR.removeValue();
        Toast.makeText(getApplicationContext(), "Product Deleted", Toast.LENGTH_LONG).show();
        return true;
    }

    private void addProduct(){
        String name = editTextName.getText().toString().trim();
        String prices = editTextPrice.getText().toString().trim();
        if (TextUtils.isEmpty(prices)){
            Toast.makeText(this, "Please enter a price", Toast.LENGTH_SHORT).show();
            return;
        }
        double price = Double.parseDouble(String.valueOf(editTextPrice.getText().toString()));
        if (!TextUtils.isEmpty(name)){
            String id = databaseProducts.push().getKey();
            Product product = new Product(id, name, price);
            databaseProducts.child(id).setValue(product);
            editTextName.setText("");
            editTextPrice.setText("");
            Toast.makeText(this, "Product Added", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show();
        }

    }
}