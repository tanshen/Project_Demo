package hk.edu.cuhk.ie.iems5722.a2_1155066083;

import android.support.v7.app.AppCompatActivity;
import android.app.Activity;
import android.os.Bundle;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

/**
 * Created by tanshen on 2016/4/23.
 */
public class GobangActivity extends AppCompatActivity {
    GobangView gobangView = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏标题栏
       // requestWindowFeature(Window.FEATURE_NO_TITLE);

        // 全屏显示
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // 获取屏幕宽高
        Display display = getWindowManager().getDefaultDisplay();
        // 现实GobangView
        GobangView.init(this, display.getWidth(), display.getHeight());
        gobangView = GobangView.getInstance();
        setContentView(gobangView);
//        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }
}
