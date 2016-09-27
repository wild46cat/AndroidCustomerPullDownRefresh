package com.example.administrator.customerpulldownrefresh;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {
    private final String TAG = MainActivity.class.getSimpleName();
    private ListView listView;
    private ViewGroup.MarginLayoutParams marginLayoutParams;
    private ViewGroup.MarginLayoutParams marginLayoutParamspro;
    private ProgressBar progressBar;
    private LinearLayout refreshLayout;
    private TextView textViewTip;
    private ImageView imageArrow;
    private List<String> data;
    private ArrayAdapter<String> adapter;

    private boolean pullFlag;
    private boolean returnFlag;
    //刷新完成标志
    private boolean refreshFinishFlag;

    private int oldY;
    private int newY;
    private int distance;

    //线程
    private Thread tempThead;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (ListView) this.findViewById(R.id.listview);
        refreshLayout = (LinearLayout) this.findViewById(R.id.refreshLayout);
        progressBar = (ProgressBar) this.findViewById(R.id.progress_bar);
        imageArrow = (ImageView)this.findViewById(R.id.imagearrow);
        textViewTip = (TextView)this.findViewById(R.id.progress_bar_tip);
        marginLayoutParams = (ViewGroup.MarginLayoutParams) listView.getLayoutParams();
        marginLayoutParamspro = (ViewGroup.MarginLayoutParams) refreshLayout.getLayoutParams();

        pullFlag = false;
        returnFlag = false;
        refreshFinishFlag = true;

        oldY = 0;
        newY = 0;
        distance = 0;
        data = new ArrayList<String>();
        for (int i = 0; i < 30; i++) {
            data.add("测试数据" + i);
        }
        adapter = new ArrayAdapter<String>(this, R.layout.simple_list, R.id.simple_list_textview, data);
        listView.setAdapter(adapter);
        listView.setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                View firstChild = listView.getChildAt(0);
                if (firstChild != null) {
                    //当前第一个是第几项
                    int firstChildPosition = listView.getFirstVisiblePosition();
                    //第一项的坐标为0
                    int firstChildTop = firstChild.getTop();
                    if (firstChildPosition == 0 && firstChildTop == 0 && refreshFinishFlag) {
                        pullFlag = true;
                        Log.i(TAG, "---->ACTION_DOWNpullFlag" + String.valueOf(pullFlag));
                    }
                    Log.i(TAG, "---->ACTION_DOWN" + String.valueOf(firstChildPosition));
                    Log.i(TAG, "---->ACTION_DOWN" + String.valueOf(firstChildTop));
                }
                returnFlag = false;
                Log.i(TAG, "---->ACTION_DOWN");
                oldY = (int) event.getRawY();
                distance =0;
                break;
            case MotionEvent.ACTION_MOVE:
                newY = (int) event.getRawY();
                distance = newY - oldY;
                if (pullFlag && distance > 0) {
                    returnFlag = true;
                    marginLayoutParams.topMargin = distance / 2;
                    listView.setLayoutParams(marginLayoutParams);
                    if (distance < 300) {
                        //正在下拉
                        downStatus();
                        marginLayoutParamspro.topMargin = (distance / 2) - 100;
                        refreshLayout.setLayoutParams(marginLayoutParamspro);
                        //防止强迫症，拉完有放回来
                        refreshFinishFlag = true;
                    }else{
                        //下拉大于150，能够执行刷新动作
                        //更新标志位，为完成下拉
                        refreshFinishFlag = false;
                        //显示释放
                        upStatus();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                Log.i(TAG, "---->ACTION_UP");
                if (pullFlag) {
                    if (distance < 300) {
                        //下拉没有到位释放
                        marginLayoutParams.topMargin = 0;
                    } else {
                        marginLayoutParams.topMargin = 150;
                        //显示正在刷新
                        refreshingStatus();
                        //下拉到位释放，启动刷新线程
                        tempThead = new Thread(){
                            @Override
                            public void run() {
                                super.run();
                                try {
                                    Thread.sleep(3000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                handler.obtainMessage(0x123).sendToTarget();
                            }
                        };
                        tempThead.start();
                    }
                    listView.setLayoutParams(marginLayoutParams);
                }
                pullFlag = false;
                returnFlag = false;
                break;
        }
        //true时listview不能进行滑动,false时能够进行滑动
        return returnFlag;
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0x123){
                Toast.makeText(MainActivity.this, "刷新完成", Toast.LENGTH_SHORT).show();
                refreshFinishFlag = true;
                data.clear();
                Random random = new Random();
                for (int i = 0; i < 30; i++) {
                    data.add("测试数据" +String.valueOf(random.nextInt(100)+1));
                }
                adapter.notifyDataSetChanged();
                //隐藏刷新界面
                marginLayoutParams.topMargin = 0;
                listView.setLayoutParams(marginLayoutParams);
            }
        }
    };
    public void downStatus(){
        imageArrow.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        imageArrow.setImageResource(R.drawable.arrowdown);
        textViewTip.setText("下拉刷新...");
    }
    public void upStatus(){
        imageArrow.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        imageArrow.setImageResource(R.drawable.arrowup);
        textViewTip.setText("释放刷新...");
    }
    public void refreshingStatus(){
        imageArrow.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        textViewTip.setText("正在刷新...");
    }

    @Override
    protected void onDestroy() {
        tempThead.stop();
        super.onDestroy();
    }
}
