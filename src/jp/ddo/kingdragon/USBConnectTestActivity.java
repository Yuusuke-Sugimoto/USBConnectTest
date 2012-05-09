package jp.ddo.kingdragon;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

public class USBConnectTestActivity extends Activity {
    private Handler mHandler;
    private ScrollView mScrollView;
    private TextView receiveMessage;
    private EditText sendMessage;
    private Button sendButton;

    private ArrayList<PrintWriter> clientStreams;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.main);

        mHandler = new Handler();

        mScrollView = (ScrollView)findViewById(R.id.scrollView);
        receiveMessage = (TextView)findViewById(R.id.receiveMessage);
        sendMessage = (EditText)findViewById(R.id.sendMessage);
        sendMessage.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    sendMessage();
                }

                return(false);
            }
        });
        sendButton = (Button)findViewById(R.id.send);
        sendButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        clientStreams = new ArrayList<PrintWriter>();
        new Thread(new AcceptThread()).start();
    }

    public void sendMessage() {
        String message = sendMessage.getText().toString();
        if(message.length() > 0 && clientStreams.size() > 0) {
            try {
                Iterator<PrintWriter> it = clientStreams.iterator();
                while(it.hasNext()) {
                    PrintWriter pw = it.next();
                    pw.println(message);
                    pw.flush();
                }
                receiveMessage.append("me : " + message + "\n");
                sendMessage.setText("");
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    class AcceptThread implements Runnable {
        @Override
        public void run() {
            try {
                ServerSocket serverSocket = new ServerSocket(8080);
                while(true) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        PrintWriter pw = new PrintWriter(clientSocket.getOutputStream());
                        clientStreams.add(pw);
                        BufferedReader receiveStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                        new Thread(new ReceiveMessageThread(receiveStream)).start();
                    }
                    catch(IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    class ReceiveMessageThread implements Runnable {
        BufferedReader br;

        public ReceiveMessageThread(BufferedReader inputBR) {
            br = inputBR;
        }

        @Override
        public void run() {
            try {
                String message;
                while((message = br.readLine()) != null) {
                    final String temp = message;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            receiveMessage.append("PC : " + temp + "\n");
                            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                        }
                    });
                }
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }
    }
}