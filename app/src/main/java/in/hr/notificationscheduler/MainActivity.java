package in.hr.notificationscheduler;

import androidx.appcompat.app.AppCompatActivity;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private JobScheduler mScheduler;
    private static final  int JOB_ID =0;
    //Switches for setting job options
    private Switch mDeviceIdleSwitch;
    private Switch mDeviceChargingSwitch;

    // Override deadline seekbar
    private SeekBar mSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDeviceChargingSwitch = findViewById(R.id.chargingSwitch);
        mDeviceIdleSwitch = findViewById(R.id.idleSwitch);
        mSeekBar = findViewById(R.id.seekBar);
        final TextView seekbarProgress = findViewById(R.id.seekBarProgress);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress > 0){
                    seekbarProgress.setText(progress + "s");
                }else{
                    seekbarProgress.setText("Not Set");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public void scheduleJob(View view) {
        RadioGroup networkOptions = findViewById(R.id.networkOptions);
        int selectedNetworkID = networkOptions.getCheckedRadioButtonId();
        int selectedNetworkOption = JobInfo.NETWORK_TYPE_NONE;
        int seekBarInteger = mSeekBar.getProgress();
        boolean seekBarSet = seekBarInteger > 0;

        mScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);

        switch (selectedNetworkID){
            case R.id.noNetwork: selectedNetworkOption =JobInfo.NETWORK_TYPE_NONE;
            break;

            case R.id.anyNetwork: selectedNetworkOption = JobInfo.NETWORK_TYPE_ANY;
            break;

            case R.id.wifiNetwork: selectedNetworkOption = JobInfo.NETWORK_TYPE_UNMETERED;
            break;
        }
        ComponentName serviceName = new ComponentName(getPackageName(),
                NotificationJobService.class.getName());

        JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, serviceName);
        builder.setRequiredNetworkType(selectedNetworkOption)
        .setRequiresCharging(mDeviceChargingSwitch.isChecked())
                .setRequiresDeviceIdle(mDeviceIdleSwitch.isChecked());

        Boolean constraintSet = (selectedNetworkOption != JobInfo.NETWORK_TYPE_NONE ||
                mDeviceChargingSwitch.isChecked() || mDeviceIdleSwitch.isChecked() || seekBarSet);

        if (seekBarSet){
            builder.setOverrideDeadline(seekBarInteger *1000);
        }

        if (constraintSet) {
            JobInfo myJobInfo = builder.build();
            mScheduler.schedule(myJobInfo);

            Toast.makeText(this, "Job Scheduled, Job will run when" +
                            "the constraints are met.", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "Please set at least one constraint",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void cancelJob(View view) {
        if (mScheduler != null){
            mScheduler.cancelAll();;
            mScheduler= null;
            Toast.makeText(this, "Jobs Cancelled", Toast.LENGTH_SHORT).show();
        }
    }
}