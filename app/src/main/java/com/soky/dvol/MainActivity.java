package com.soky.dvol;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

import static com.soky.dvol.control.Controller.getServiceController;

public class MainActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();

    private final int PERM_RECORD_AUDIO_REQUEST_CODE = 100;

    private Volume volume_;
    private ElapsedToast uncheck_toast_;
    private ElapsedToast stop_toast_;
    private BackButtonExiter back_button_exiter_;
    private ResidentNotification noti_;

    private TextView status_decibel_textview_;
    private SeekBar status_decibel_range_seekbar_;
    private TextView status_volume_textview_;
    private SeekBar status_volume_range_seekbar_;

    private CheckBox control_use_now_;
    private TextView control_decibel_textview_;
    private SeekBar control_decibel_range_seekbar_;
    private TextView control_volume_textview_;
    private SeekBar control_volume_range_seekbar_;
    private ImageButton start_button_;



    /**
     * 서비스로 부터 오는 콜백
     */
    private AutoVolumeService.ServiceCallback callback_ = new AutoVolumeService.ServiceCallback() {
        @Override
        public void onResult(int decibel, int amplitude, int volume) {

            Resources res = getResources();
            String str = String.format(res.getString(R.string.decibel_status), decibel, amplitude);

            status_decibel_textview_.setText(str);
            status_decibel_range_seekbar_.setProgress(decibel);

            if (control_use_now_.isChecked()) {
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
        int has_permission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        if (has_permission != PackageManager.PERMISSION_GRANTED) {
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
        volume_ = new Volume();
        uncheck_toast_ = new ElapsedToast(this, R.string.set_uncheck);
        stop_toast_ = new ElapsedToast(this, R.string.set_stop);
        back_button_exiter_ = new BackButtonExiter();
        noti_ = new ResidentNotification(this.getClass());
        noti_.start();
    }

    private void initStatusWidgets() {

        status_decibel_textview_ = (TextView)findViewById(R.id.status_decibel_textview);
        status_decibel_range_seekbar_ = (SeekBar)findViewById(R.id.status_decibel_range_seekbar);
        status_volume_textview_ = (TextView)findViewById(R.id.status_volume_textview);
        status_volume_range_seekbar_ = (SeekBar)findViewById(R.id.status_volume_range_seekbar);

        status_decibel_range_seekbar_.setMax(DecibelMeter.MAX_DECIBEL);
        status_decibel_range_seekbar_.setOnTouchListener(new SeekBar.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;    // ReadOnly
            }
        });
        status_volume_range_seekbar_.setMax(volume_.getMax());
        status_volume_range_seekbar_.setProgress(volume_.getCurrent());
        status_volume_range_seekbar_.setOnTouchListener(new SeekBar.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;    // ReadOnly
            }
        });

        volume_.registerListener(new Volume.Listener() {
            @Override
            public void onChange(int volume) {
                status_volume_textview_.setText(String.valueOf(volume));
                status_volume_range_seekbar_.setProgress(volume);
            }
        });

    }

    private void initControlWidgets() {

        control_use_now_ = (CheckBox)findViewById(R.id.now_checkbox);
        control_use_now_.setChecked(Config.getUseNow());

        control_decibel_textview_ = (TextView)findViewById(R.id.control_decibel_textview);
        control_decibel_range_seekbar_ = (SeekBar)findViewById(R.id.control_decibel_range_seekbar);
        control_volume_textview_ = (TextView)findViewById(R.id.control_volume_textview);
        control_volume_range_seekbar_ = (SeekBar)findViewById(R.id.control_volume_range_seekbar);

        control_decibel_range_seekbar_.setMax(DecibelMeter.MAX_DECIBEL);
        setControlDecibelWidget(Config.getDecibel(), true);

        control_decibel_range_seekbar_.setOnTouchListener(new SeekBar.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isStartedAutoVolume()) {
                    stop_toast_.show();
                    return true;    // ReadOnly
                } else  if (control_use_now_.isChecked()) {
                    uncheck_toast_.show();
                    return true;    // ReadOnly
                }
                return false;
            }
        });
        control_decibel_range_seekbar_.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setControlDecibelWidget(progress, false);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        control_volume_range_seekbar_.setOnTouchListener(new SeekBar.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isStartedAutoVolume()) {
                    stop_toast_.show();
                    return true;    // ReadOnly
                }
                return false;
            }
        });

        control_volume_range_seekbar_.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setControlVolumeWidget(progress, false);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        control_volume_range_seekbar_.setMax(volume_.getMax());
        setControlVolumeWidget(Config.getVolume(), true);

        start_button_ = (ImageButton)findViewById(R.id.start_button);
        start_button_.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Controller.switchAutoVolume();
            }
        });
    }

    public void startAutoVolume() {
        control_use_now_.setEnabled(false);
        start_button_.setImageResource(R.drawable.img_stop);
        noti_.changeStarted();

        int volume = control_volume_range_seekbar_.getProgress();
        int decibel = control_decibel_range_seekbar_.getProgress();
        int amplitude = DecibelMeter.toAmplitude(decibel);

        // 자동 볼륨 시작시 마다 설정 저장
        Config.setDecibel(decibel);
        Config.setVolume(volume);
        Config.setUseNow(control_use_now_.isChecked());

        AutoVolumeService service = getServiceController().getService();
        service.startControlVolume(volume, amplitude);
    }

    public void stopAutoVolume() {
        control_use_now_.setEnabled(true);
        start_button_.setImageResource(R.drawable.img_start);
        noti_.changeStopped();

        AutoVolumeService service = getServiceController().getService();
        service.stopControlVolume();
    }


    private void setControlDecibelWidget(int decibel, boolean prog) {
        control_decibel_textview_.setText(String.format(getResources().getString(R.string.decibel_simple_status), decibel));
        if (prog) {
            control_decibel_range_seekbar_.setProgress(decibel);
        }
    }

    private void setControlVolumeWidget(int volume, boolean prog) {
        control_volume_textview_.setText(String.valueOf(volume));
        if (prog) {
            control_volume_range_seekbar_.setProgress(volume);
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
        volume_.unregisterListener();
    }


    /**
     * 뒤로가기 두번 클릭시 종료
     */
    @Override
    public void onBackPressed() {
        back_button_exiter_.exit(this);
    }

    /**
     * Controller.exit() 사용시 호출됨
     */
    public void exit() {
        noti_.cancel();

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
     * 서비스 자체가 동작하는지와는 다름
     * @return
     */
    private boolean isStartedAutoVolume() {
        return getServiceController().isStartedAutoVolume();
    }
}
