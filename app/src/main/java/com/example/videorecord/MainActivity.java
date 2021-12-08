package com.example.videorecord;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "Recorder";
    private static final String RECORD_FILE_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath()
            + "/test.3gp";

    private static final String[] sPermissions = new String[] {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private MediaRecorder mRecorder;
    private boolean isRecording;
    private File mVideoFile;
    private SurfaceView mSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 设置横屏显示
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        // 设置全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // 选择支持半透明模式,在有surfaceview的activity中使用。
        getWindow().setFormat(PixelFormat.TRANSLUCENT);

        checkPermissions();
        ImageView iv = findViewById(R.id.iv_record);
        iv.setOnClickListener(this);

        mSurfaceView = findViewById(R.id.surfaceView);
        // 设置分辨率
        mSurfaceView.getHolder().setFixedSize(1280, 720);
        // 设置该组件让屏幕不会自动关闭
        mSurfaceView.getHolder().setKeepScreenOn(true);
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArrayList<String> permissionList = new ArrayList<>();
            for (int i = 0; i < sPermissions.length; i++) {
                if (ContextCompat.checkSelfPermission(this, sPermissions[i])
                        != PackageManager.PERMISSION_GRANTED) {
                    permissionList.add(sPermissions[i]);
                }
            }

            Log.d(TAG, "checkPermissions: " + permissionList);
            if (!permissionList.isEmpty()) {
                String[] permissions = permissionList.toArray(new String[permissionList.size()]);
                ActivityCompat.requestPermissions(this, permissions, 0);
            }
        }
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "isRecording: " + isRecording);
        if (isRecording) {
            stopRecord();
        } else {
            try {
                mVideoFile = new File(RECORD_FILE_PATH);
                Log.d(TAG, "mVideoFile: " + mVideoFile.getAbsolutePath());
                //1. 创建 MediaRecorder 对象。
                mRecorder = new MediaRecorder();
                mRecorder.reset();
                //2. 设置声音来源，一般传入 MediaRecorder. AudioSource.MIC参数指定录制来自麦克风的声音
                mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                // 设置从摄像头采集图像
                mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                //3. 设置录制的声音的输出格式（必须在设置声音编码格式之前设置）
                mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                //4. 设置声音编码的格式
                mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                // 设置图像编码的格式
                mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
                mRecorder.setVideoSize(1280, 720);
                // 每秒 4帧
                //视频的帧率和视频大小是需要硬件支持的，如果设置的帧率和视频大小，如果硬件不支持就会出现错误
                //mRecorder.setVideoFrameRate(20);
                //5. 设置音频文件的保存位置
                mRecorder.setOutputFile(mVideoFile.getAbsolutePath());
                // 指定使用SurfaceView来预览视频
                mRecorder.setPreviewDisplay(mSurfaceView.getHolder().getSurface());
                //6. 准备录制
                mRecorder.prepare();
                //7. 开始录音
                mRecorder.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ImageView iv = (ImageView) v;
        isRecording = isRecording ? false : true;
        iv.setImageDrawable(getDrawable(isRecording ? R.drawable.stop : R.drawable.start));
    }

    private void stopRecord() {
        if (mVideoFile != null && mVideoFile.exists()) {
            //8. 停止录音
            mRecorder.stop();
            //9. 释放资源
            mRecorder.release();
            mRecorder = null;
        }
    }

    @Override
    protected void onDestroy() {
        stopRecord();
        super.onDestroy();
    }
}