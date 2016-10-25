package com.gmobi.poponews.cases.setting;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gmobi.poponews.BuildConfig;
import com.gmobi.poponews.R;
import com.gmobi.poponews.service.IConfigService;
import com.gmobi.poponews.util.AnalysisUtil;
import com.gmobi.poponews.util.TimeUtil;
import com.momock.app.App;
import com.momock.util.SystemHelper;

public class AboutFragment extends Fragment {
    private static long lastTime = 0L;
    private static int clickCount = 0;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        AnalysisUtil.recordMeAbout();

        View root = inflater.inflate(R.layout.fragment_about, container, false);


        TextView tvVer = (TextView)root.findViewById(R.id.abt_ver);
        if(null != tvVer){
            String ver = SystemHelper.getAppVersion(getActivity());
            tvVer.setText(getResources().getString(R.string.splash_ver, ver)+"("+ BuildConfig.GROUP+")");
        }


        ImageView ivLogo = (ImageView)root.findViewById(R.id.abt_logo);
        if(null != ivLogo)
        {
            ivLogo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(lastTime != 0L){
                        long diffTime = System.currentTimeMillis() - lastTime;
                        if(diffTime < 1000){
                            clickCount ++;
                        }else{
                            clickCount = 0;
                            lastTime = 0L;
                        }

                        if(clickCount >= 3)
                        {
                            clickCount = 0;
                            lastTime = 0L;

                            Toast.makeText(AboutFragment.this.getActivity(),
                                    "Build at" + TimeUtil.getInstance().getYYMMDDDate(BuildConfig.BUILD_DATE) + "\n"
                                            + "Last offline download at " + TimeUtil.getInstance().getTimeFormatStr(App.get().getService(IConfigService.class).getOfflineDownloadTime()),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        lastTime = System.currentTimeMillis();
                        clickCount ++;
                    }
                }
            });
        }
        return root;
    }


}
