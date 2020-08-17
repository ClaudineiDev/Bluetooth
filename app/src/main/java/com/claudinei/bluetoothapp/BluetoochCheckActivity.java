package com.claudinei.bluetoothapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class BluetoochCheckActivity extends AppCompatActivity {

    protected BluetoothAdapter btfAdaptador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooch_check);

        btfAdaptador = BluetoothAdapter.getDefaultAdapter();

        if (btfAdaptador == null){
            Toast.makeText(this,"Bluetooth não disponivel!",Toast.LENGTH_LONG).show();
            finish();
        }

        if (btfAdaptador.isEnabled()){
            Toast.makeText(this,"Bluetooth Ativado!",Toast.LENGTH_LONG).show();
        }else{
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, 0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (btfAdaptador.isEnabled()){
            Toast.makeText(this,"Bluetooth foi Ativado!",Toast.LENGTH_LONG).show();
        }
    }
}
