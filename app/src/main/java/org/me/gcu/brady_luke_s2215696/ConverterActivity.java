package org.me.gcu.brady_luke_s2215696;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class ConverterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_converter);

        // --- Get the selected currency info from MainActivity ---
        String code = getIntent().getStringExtra("code");   // e.g. "THB"
        String name = getIntent().getStringExtra("name");   // e.g. "Thai Baht"
        double rate = getIntent().getDoubleExtra("rateToGBP", 0);

        // --- Link the layout elements ---
        TextView tvTitle   = findViewById(R.id.tvTitle);
        EditText etAmount  = findViewById(R.id.etAmount);
        RadioButton rbGbpTo = findViewById(R.id.rbGbpTo);
        RadioButton rbToGbp = findViewById(R.id.rbToGbp);
        Button btnConvert  = findViewById(R.id.btnConvert);
        TextView tvResult  = findViewById(R.id.tvResult);

        // --- Title still shows both code & name ---
        tvTitle.setText("Converter — " + code + " (" + name + ")");

        // ✅ Labels show the 3-letter code only
        rbGbpTo.setText("GBP → " + code);
        rbToGbp.setText(code + " → GBP");

        // --- Conversion logic ---
        btnConvert.setOnClickListener(v -> {
            String input = etAmount.getText().toString().trim();
            if (input.isEmpty()) {
                etAmount.setError("Enter an amount");
                return;
            }

            double amount = Double.parseDouble(input);
            double result;

            if (rbGbpTo.isChecked()) {
                // Convert from GBP to selected currency
                result = amount * rate;
                tvResult.setText(String.format("%.2f GBP = %.2f %s", amount, result, code));
            } else {
                // Convert from selected currency to GBP
                result = amount / rate;
                tvResult.setText(String.format("%.2f %s = %.2f GBP", amount, code, result));
            }
        });
    }
}
