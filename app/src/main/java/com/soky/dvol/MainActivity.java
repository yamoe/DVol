package com.soky.dvol;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.soky.dvol.control.Config;
import com.soky.dvol.control.Controller;
import com.soky.dvol.control.DecibelMeter;
import com.soky.dvol.control.ResidentNotification;
import com.soky.dvol.control.Volume;
import com.soky.dvol.util.BackButtonExiter;
import com.soky.dvol.util.ElapsedToast;
import com.soky.dvol.util.ProjectInfo;

import static com.soky.dvol.control.Controller.getServiceController;

public class MainActivity extends AppCompatActivity {
    public final String TAG = this.getClass().getSimpleName();

    // 안드로이드 권한 요청 결과 코드(임의 지정)
    private final int PERM_RECORD_AUDIO_REQUEST_CODE = 100;

    private Volume mVolume;
    private ElapsedToast mUnckeckToast;
    private ElapsedToast mStopToast;
    private BackButtonExiter mBackButtonExiter;
    private ResidentNotification mNotification;

    private TextView mStatusDecibelTextView;
    private SeekBar mStatusDecibelSeekBar;
    private TextView mStatusVolumeTextView;
    private SeekBar mStatusVolumeSeekBar;

    private CheckBox mControlUseNow;

    private TextView mControlDecibelTextView;
    private SeekBar mControlDecibelSeekBar;
    private TextView mControlVolumeTextView;
    private SeekBar mControlVolumeSeekBar;
    private ImageButton mStartButton;



    /**
     * 서비스로 부터 오는 콜백
     */
    private AutoVolumeService.ServiceCallback callback_ = new AutoVolumeService.ServiceCallback() {
        @Override
        public void onResult(int decibel, int amplitude, int volume) {

            Resources res = getResources();
            String str = String.format(res.getString(R.string.decibel_status), decibel, amplitude);

            mStatusDecibelTextView.setText(str);
            mStatusDecibelSeekBar.setProgress(decibel);

            if (mControlUseNow.isChecked()) {
                if (!isStartedAutoVolume()) {
                    setControlDecibelWidget(decibel, true);
                }
            }
        }
    };

    /**
     *  권한 요청
     *  마시멜로우(android 6, api 23) 부터는 AndroidMenifest.xml 에도 권한 설정(uses-permission 태그) 및 requestPermissions 을 통해 권한 요청을 구현해야 함.
     *  requestPermission 메소드에서 권한요청 다이얼로그를 띄우고
     *  onRequestPermissionsResult 메소드에서 사용자가 선택한 권한 허용 여부를 전달 받음
     */
    private void requestPermission() {
        int hasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        if (hasPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.RECORD_AUDIO}, PERM_RECORD_AUDIO_REQUEST_CODE);
        }
    }

    /**
     *  권한 요청 결과
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERM_RECORD_AUDIO_REQUEST_CODE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, getResources().getString(R.string.exit_because_not_allowed_permission), Toast.LENGTH_SHORT).show();
                Controller.exit();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate !!!!!!!!!!!!!!!!!!!!!!!");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Controller.start(this);

        requestPermission();
        initComponent();
        initStatusWidgets();
        initControlWidgets();

    }

    private void initComponent() {
        mVolume = new Volume();
        mUnckeckToast = new ElapsedToast(R.string.set_uncheck);
        mStopToast = new ElapsedToast(R.string.set_stop);
        mBackButtonExiter = new BackButtonExiter();
        mNotification = new ResidentNotification(this.getClass());
        mNotification.start();
    }

    private void initStatusWidgets() {

        mStatusDecibelTextView = (TextView)findViewById(R.id.status_decibel_textview);
        mStatusDecibelSeekBar = (SeekBar)findViewById(R.id.status_decibel_range_seekbar);
        mStatusVolumeTextView = (TextView)findViewById(R.id.status_volume_textview);
        mStatusVolumeSeekBar = (SeekBar)findViewById(R.id.status_volume_range_seekbar);

        mStatusDecibelSeekBar.setMax(DecibelMeter.MAX_DECIBEL);
        mStatusDecibelSeekBar.setOnTouchListener(new SeekBar.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;    // ReadOnly
            }
        });
        mStatusVolumeSeekBar.setMax(mVolume.getMax());
        mStatusVolumeSeekBar.setProgress(mVolume.getCurrent());
        mStatusVolumeSeekBar.setOnTouchListener(new SeekBar.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;    // ReadOnly
            }
        });

        // 기기의 MUSIC 볼륨이 변경될때마다 콜백
        mVolume.registerListener(new Volume.Listener() {
            @Override
            public void onChange(int volume) {
                mStatusVolumeTextView.setText(String.valueOf(volume));
                mStatusVolumeSeekBar.setProgress(volume);
            }
        });

    }

    private void initControlWidgets() {

        mControlUseNow = (CheckBox)findViewById(R.id.now_checkbox);
        mControlUseNow.setChecked(Config.getUseNow());

        mControlDecibelTextView = (TextView)findViewById(R.id.control_decibel_textview);
        mControlDecibelSeekBar = (SeekBar)findViewById(R.id.control_decibel_range_seekbar);
        mControlVolumeTextView = (TextView)findViewById(R.id.control_volume_textview);
        mControlVolumeSeekBar = (SeekBar)findViewById(R.id.control_volume_range_seekbar);

        mControlDecibelSeekBar.setMax(DecibelMeter.MAX_DECIBEL);
        setControlDecibelWidget(Config.getDecibel(), true);

        mControlDecibelSeekBar.setOnTouchListener(new SeekBar.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isStartedAutoVolume()) {
                    mStopToast.show(v.getContext());
                    return true;    // ReadOnly
                } else  if (mControlUseNow.isChecked()) {
                    mUnckeckToast.show(v.getContext());
                    return true;    // ReadOnly
                }
                return false;
            }
        });
        mControlDecibelSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setControlDecibelWidget(progress, false);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        mControlVolumeSeekBar.setOnTouchListener(new SeekBar.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isStartedAutoVolume()) {
                    mStopToast.show(v.getContext());
                    return true;    // ReadOnly
                }
                return false;
            }
        });

        mControlVolumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setControlVolumeWidget(progress, false);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        mControlVolumeSeekBar.setMax(mVolume.getMax());
        setControlVolumeWidget(Config.getVolume(), true);

        mStartButton = (ImageButton)findViewById(R.id.start_button);
        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Controller.switchAutoVolume();
            }
        });
    }

    public void startAutoVolume() {
        mControlUseNow.setEnabled(false);
        mStartButton.setImageResource(R.drawable.img_stop);
        mNotification.changeStarted();

        int volume = mControlVolumeSeekBar.getProgress();
        int decibel = mControlDecibelSeekBar.getProgress();
        int amplitude = DecibelMeter.toAmplitude(decibel);

        // 자동 볼륨 시작시 마다 설정 저장
        Config.setDecibel(decibel);
        Config.setVolume(volume);
        Config.setUseNow(mControlUseNow.isChecked());

        AutoVolumeService service = getServiceController().getService();
        service.startControlVolume(volume, amplitude);
    }

    public void stopAutoVolume() {
        mControlUseNow.setEnabled(true);
        mStartButton.setImageResource(R.drawable.img_start);
        mNotification.changeStopped();

        AutoVolumeService service = getServiceController().getService();
        service.stopControlVolume();
    }


    private void setControlDecibelWidget(int decibel, boolean withProgress) {
        mControlDecibelTextView.setText(String.format(getResources().getString(R.string.decibel_simple_status), decibel));
        if (withProgress) {
            mControlDecibelSeekBar.setProgress(decibel);
        }
    }

    private void setControlVolumeWidget(int volume, boolean withProgress) {
        mControlVolumeTextView.setText(String.valueOf(volume));
        if (withProgress) {
            mControlVolumeSeekBar.setProgress(volume);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();

        getServiceController().registCallback(callback_);
    }

    @Override
    protected void onStop() {
        super.onStop();

        getServiceController().unregistCallback(callback_);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy !!!!!!!!!!!!!!!!!!!!!!!");
        super.onDestroy();
        mVolume.unregisterListener();
    }


    /**
     * 뒤로가기 두번 클릭시 종료
     */
    @Override
    public void onBackPressed() {
        mBackButtonExiter.exit(this);
    }

    /**
     * Controller.exit() 사용시 호출됨
     */
    public void exit() {
        mNotification.cancel();

        if (Build.VERSION.SDK_INT >= 16) {
            finishAffinity();
        } else {
            ActivityCompat.finishAffinity(this);
        }
    }

    public void onExitButton(View v) {
        Controller.exit();
    }

    /**
     * 서비스의 자동 볼륨 조절 기능이 동작중인지 확인. (축약하려고 만든 메소드임)
     * @return 자동 볼륨 조절 기능 동작 여부
     */
    private boolean isStartedAutoVolume() {
        return getServiceController().isStartedAutoVolume();
    }

    /**
     * 커스텀 액션바 등록
     * @param menu 메뉴
     * @return 성공 여부
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar, menu);
        return true;
    }

    /**
     * 액션바의 버튼 클릭 시 콜백
     * @param item 아이템
     * @return 성공 여부
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.actionbar_info) {

            // 액션바 Info 버튼 클릭 시 버전 정보 Dialog
            String msg = "";
            msg += "Version Code : " + ProjectInfo.getVersionCode(this);
            msg += "\n";
            msg += "Version Name : " + ProjectInfo.getVersionName(this);

            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle(" ");
            dialog.setMessage(msg);
            dialog.setIcon(R.drawable.dvol_icon_noti);
            dialog.setPositiveButton("CLOSE", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            //dialog.setCancelable(false); // (뒤로 버튼 클릭시 | Dialog 바깐 클릭시) 창 닫히지 않게 함
            dialog.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
