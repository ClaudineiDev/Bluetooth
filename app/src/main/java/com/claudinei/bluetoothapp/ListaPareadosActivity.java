package com.claudinei.bluetoothapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ListaPareadosActivity extends ListActivity {

    protected List<BluetoothDevice> listaPareados;
    static String ENDERECO_MAC = null;
    protected BluetoothAdapter btfAdaptador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ArrayAdapter<String> arrayBluetooth = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

        btfAdaptador = BluetoothAdapter.getDefaultAdapter();

        Set<BluetoothDevice> dispositivosPareados = btfAdaptador.getBondedDevices();

        if (dispositivosPareados.size() > 0){
            for(BluetoothDevice dispositivos : dispositivosPareados){
                String nomeBt = dispositivos.getName();
                String macBt = dispositivos.getAddress();
                arrayBluetooth.add(nomeBt + "\n" + macBt);
            }
        }
        setListAdapter(arrayBluetooth);
    }


    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        String info = ((TextView)v).getText().toString();

        Intent retornoMac = new Intent();

        retornoMac.putExtra(ENDERECO_MAC,info);
        setResult(RESULT_OK, retornoMac);
        finish();
    }
}
