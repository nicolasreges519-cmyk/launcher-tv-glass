package com.meulauncher.glass;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView texto = new TextView(this);
        texto.setText("Meu Launcher abriu!");
        texto.setTextSize(40);
        texto.setTextColor(Color.WHITE);
        texto.setGravity(Gravity.CENTER);
        texto.setBackgroundColor(Color.rgb(30, 28, 32));
        setContentView(texto);
    }
}
