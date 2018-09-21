package edu.tjrac.swant.unicorn;

import android.util.Log;

import com.yckj.baselib.common.base.BaseApplication;

import cat.ereza.customactivityoncrash.CustomActivityOnCrash;
import edu.tjrac.swant.filesystem.CarryPathDialogFragment;
import edu.tjrac.swant.unicorn.bean.User;
import edu.tjrac.swant.unicorn.view.MainActivity;

/**
 * Created by wpc on 2018/1/13 0013.
 */

public class App extends BaseApplication {

    public static User loged;

    @Override
    public void onCreate() {
        super.onCreate();
        //fix someproblems;

        //Install CustomActivityOnCrash
        if(BuildConfig.DEBUG){

        }else {
            CustomActivityOnCrash.install(this);
            CustomActivityOnCrash.setLaunchErrorActivityWhenInBackground(false);
            CustomActivityOnCrash.setShowErrorDetails(true);
            CustomActivityOnCrash.setDefaultErrorActivityDrawable(R.mipmap.ufo_ed);
            CustomActivityOnCrash.setEventListener(new MyEventListener());

            CustomActivityOnCrash.setEnableAppRestart(true);//true/false 重启/关闭
            CustomActivityOnCrash.setRestartActivityClass(MainActivity.class);
        }
        applicationContext=this;
        instance=this;
//        Realm.init(this);

//        LeakCanary.install(this);
//        LocationUtils.getInstance(getApplicationContext()).showLocation();

//        JPushInterface.setDebugMode(true);
//        JPushInterface.init(this);
//        JPushInterface.setAlias(this, "18814837150", new TagAliasCallback() {
//            @Override
//            public void gotResult(int i, String s, Set<String> set) {
//                L.i("setAlias:","success");
//            }
//        });

        CarryPathDialogFragment.initCarrySetting();

    }

    private static class MyEventListener implements CustomActivityOnCrash.EventListener {
        @Override
        public void onLaunchErrorActivity() {
            Log.i("bqt", "onLaunchErrorActivity");
        }
        @Override
        public void onRestartAppFromErrorActivity() {
            Log.i("bqt", "onRestartAppFromErrorActivity");
        }
        @Override
        public void onCloseAppFromErrorActivity() {
            Log.i("bqt", "onCloseAppFromErrorActivity");
        }
    }

}
