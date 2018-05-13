package edu.tjrac.swant.download;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.yckj.baselib.common.base.BaseToolbarActivity;
import com.yckj.baselib.common.dialog.EditTextDialog;
import com.yckj.baselib.util.FileUtils;
import com.yckj.baselib.util.L;

import butterknife.BindView;
import butterknife.ButterKnife;
import edu.tjrac.swant.download.bean.DownloadFileInfo;
import edu.tjrac.swant.unicorn.R;

public class DownloadActivity extends BaseToolbarActivity {

    int NOTIFICATION_ID = 100;

    @BindView(R.id.toolbar) Toolbar mToolbar;

    DownLoadService download;

    private DownloadThreadCallback threadCallback = new DownloadThreadCallback() {

        @Override
        public void onThreadProgress(String tag, DownloadFileInfo info) {
            L.i("threadId:" + tag, info.getUrl() + "_" + info.getDownloadSize());
        }

        @Override
        public void onThreadSuccess(String tag, DownloadFileInfo info) {

        }

        @Override
        public void onThreadFaild(String tag, DownloadFileInfo info, String err) {

        }
    };
    DownLoadService.DownLoadBinder binder;
    @BindView(R.id.recycler) RecyclerView mRecycler;
    @BindView(R.id.swiper) SwipeRefreshLayout mSwiper;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = (DownLoadService.DownLoadBinder) service;
            download = binder.getService();
            if (download != null) {
                download.startForeground(NOTIFICATION_ID, getNotification());
                download.setThreadcallback(threadCallback);
//                download.bindToRecycler(mRecycler);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            L.i("onServiceDisconnected", "ServiceConnection:onServiceDisconnected");
        }
    };

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);

        Intent intent = new Intent(DownloadActivity.this, DownLoadService.class);
        bindService(intent, mConnection, BIND_AUTO_CREATE);
//        startForegroundService(new Intent(DownloadActivity.this,DownLoadService.class));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(DownloadActivity.this).inflate(R.menu.download, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.add) {
            EditTextDialog dialog = new EditTextDialog(mContext, "新建", "请输入路径(url)");
            dialog.setPositive(new DialogInterface.OnClickListener() {
                @SuppressLint("NewApi")
                @Override
                public void onClick(DialogInterface d, int which) {
                    String url = dialog.getEditText().getText().toString().trim();
                    String name = FileUtils.getName(url);
                    Intent intent = new Intent(DownloadActivity.this, DownLoadService.class)
                            .putExtra("downloadInfo",
                                    new DownloadFileInfo(name, url));
//                    bindService(intent,mConnection, BIND_AUTO_CREATE);
                    if (download == null) {

                    } else {
                    }


                }
            });
            dialog.show(getSupportFragmentManager(), "urls");
        }
        return super.onOptionsItemSelected(item);
    }

    private Notification getNotification() {
        Notification.Builder mBuilder = new Notification.Builder(DownloadActivity.this);
        mBuilder.setShowWhen(false);
        mBuilder.setAutoCancel(false);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher_round);
        mBuilder.setLargeIcon(((BitmapDrawable) getDrawable(R.mipmap.ic_launcher)).getBitmap());
        mBuilder.setContentText("this is content");
        mBuilder.setContentTitle("this is title");
        return mBuilder.build();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
    }
}
