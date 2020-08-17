package com.claudinei.bluetoothapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

import java.io.IOException;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends BluetoochCheckActivity implements View.OnClickListener {
    private Button btnPareados, btnVisivel, btnLampada;
    private Switch swLampada;
    private static final int ATIVA_CONEXAO = 1;
    private static final int SOLICITA_CONEXAO = 2;
    private static final int MESSAGE_READ = 3;

    Handler handler;
    StringBuilder dadosBluetooth = new StringBuilder();
    boolean conexao = false;

    ConnectedThread connectedThread;
    BluetoothDevice meuDevice = null;
    BluetoothSocket meuSkt = null;
    protected static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    private static String MAC = null;
    private static String NOME = null;
    String retorno = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnPareados = (Button) findViewById(R.id.btnPareados);
        btnPareados.setOnClickListener(this);

        btnVisivel = (Button) findViewById(R.id.btnVisivel);
        btnVisivel.setOnClickListener(this);

        btnLampada = (Button) findViewById(R.id.btnLampada);
        btnLampada.setOnClickListener(this);


        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MESSAGE_READ){
                    String recebido = (String) msg.obj;

                    dadosBluetooth.append(recebido);

                    int keyFinal = dadosBluetooth.indexOf("}");

                    if(keyFinal > 0){
                        String dadosCompletos = dadosBluetooth.substring(0, keyFinal);

                        int tamInfo = dadosCompletos.length();

                        if(dadosBluetooth.charAt(0) == '{'){
                            String dadosFinais = dadosBluetooth.substring(1, tamInfo);

                            Log.d("Recebidos", dadosFinais);

                            if(dadosFinais.contains("1-on")){
                                btnLampada.setText("Lampada - on");
                            }else if(dadosFinais.contains("1-of")){
                                btnLampada.setText("Lampada - of");
                            }

                        }

                        dadosBluetooth.delete(0, dadosBluetooth.length());
                    }
                }
            }
        };

    }

    @Override
    protected void onResume() {
        super.onResume();

        swLampada = (Switch) findViewById(R.id.swLampada);
        swLampada.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton botao, boolean isChecked) {
                if(isChecked){
                    connectedThread.enviar("A");
                }else{
                    connectedThread.enviar("A");
                }
            }
        });


    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.btnPareados:
                if(conexao) {
                    try{
                        meuSkt.close();
                        conexao = false;
                        Toast.makeText(this,"Desconectado",Toast.LENGTH_LONG).show();
                        btnPareados.setText("Selecionar Dispositivo");
                        btnLampada.setText("Lampada");
                        btnLampada.setBackgroundResource(R.color.colorBotao);
                    }catch (IOException e){
                        Toast.makeText(this,"Falha: " + e,Toast.LENGTH_LONG).show();
                    }
                }else{
                    Intent intentListaPareados = new Intent(this, ListaPareadosActivity.class);
                    startActivityForResult(intentListaPareados, SOLICITA_CONEXAO);
                }
                break;

            case R.id.btnVisivel:
                Intent intentVisivel = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                intentVisivel.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                startActivity(intentVisivel);
                break;


            case R.id.btnLampada:
                btnLampada.setText("Lampada - On");
                btnLampada.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAmarela));
                connectedThread.enviar("A");
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case SOLICITA_CONEXAO:
                if(resultCode == Activity.RESULT_OK){
                    //substring(info.length() - 17
                    retorno = data.getExtras().getString(ListaPareadosActivity.ENDERECO_MAC);
                    NOME = retorno.substring(0, retorno.length()-18);
                    MAC = retorno.substring(retorno.length()-17);
                    meuDevice = btfAdaptador.getRemoteDevice(MAC);
                    try {
                        meuSkt = meuDevice.createRfcommSocketToServiceRecord(uuid);
                        meuSkt.connect();
                        conexao = true;
                        connectedThread = new ConnectedThread(meuSkt);
                        connectedThread.start();
                        Toast.makeText(this,"MAC: " + MAC +"\nconectado",Toast.LENGTH_LONG).show();
                        btnPareados.setText("Dispositivo - " +NOME);
                    }catch (IOException e){
                        conexao = false;
                        Toast.makeText(this,"Falha: " + e,Toast.LENGTH_LONG).show();
                    }

                }else{
                    Toast.makeText(this,"Falha ao obter MAC",Toast.LENGTH_LONG).show();
                }

        }

    }

    private class ConnectedThread extends Thread {
        //private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer; // mmBuffer store for the stream



        public ConnectedThread(BluetoothSocket socket) {

            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                //Log.e(TAG, "Error occurred when creating input stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }
        public void run() {
            mmBuffer = new byte[1024];
            int numBytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    // Read from the InputStream.
                    numBytes = mmInStream.read(mmBuffer);
                    String dadosBt = new String(mmBuffer,0,numBytes);
                    // Send the obtained bytes to the UI activity.
                    handler.obtainMessage(MESSAGE_READ, numBytes, -1, dadosBt).sendToTarget();
                } catch (IOException e) {
                    //Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }

        // Call this from the main activity to send data to the remote device.
        public void enviar(String dadosEnviar) {
            byte[] msgBuffer = dadosEnviar.getBytes();
            try {
                mmOutStream.write(msgBuffer);
            } catch (IOException e) { }
        }

    }

}