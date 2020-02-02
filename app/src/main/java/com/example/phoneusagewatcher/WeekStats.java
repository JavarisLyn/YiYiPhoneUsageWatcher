package com.example.phoneusagewatcher;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;


import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import AAChartCoreLib.AAChartConfiger.AAChartModel;
import AAChartCoreLib.AAChartConfiger.AAChartView;
import AAChartCoreLib.AAChartConfiger.AASeriesElement;
import AAChartCoreLib.AAChartEnum.AAChartType;
import AAChartCoreLib.AAOptionsModel.AAPie;
import getappusageinfo.PackageInfo;
import getappusageinfo.UseTimeDataManager;


import static com.example.phoneusagewatcher.MainActivity.WeekPackageInfos;
import static getappusageinfo.DateTransUtils.getPastDate;
import static getappusageinfo.DateTransUtils.milliseconds2hms;

public class WeekStats extends AppCompatActivity {

    //是否使用特殊的标题栏背景颜色，android5.0以上可以设置状态栏背景色，如果不使用则使用透明色值
    protected boolean useThemestatusBarColor = false;
    //是否使用状态栏文字和图标为暗色，如果状态栏采用了白色系，则需要使状态栏和图标为暗色，android6.0以上可以设置
    protected boolean useStatusBarColor = true;

    private Toolbar myToolbar;

    private  UseTimeDataManager mUseTimeDataManager;
    public   static double[] weekUsage = new double[7];
    public static double weekAverage=0;
    //private LineChart mWeekLineChart;
    private AAChartView weekLineChart;
    private TextView weeekAveragestats;
   // private List<LineChartData> dataList = new ArrayList<>();
    //扇形图
    AAChartModel piegraph;
    private AAChartView aaChartView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_week_stats);

        setStatusBar();

        myToolbar = findViewById(R.id.week_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //initpopupWindow();
        myToolbar.setNavigationIcon(R.drawable.ic_keyboard_backspace_black_24dp);
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WeekStats.this,MainActivity.class));
            }
        });
        //扇形图
        aaChartView = findViewById(R.id.week_piegraph);

        initial_graph1();
        initial_graph2();
    }
    //得到七日数据
    public void getStats1()
    {
        mUseTimeDataManager = UseTimeDataManager.getInstance(WeekStats.this);
        for(int i=0;i<7;i++)
        {
            mUseTimeDataManager.refreshData(i,0);
            final List<PackageInfo> packageInfos = mUseTimeDataManager.getmPackageInfoListOrderByTime();
            int sumtime=0;
            for (int j = 0; j < packageInfos.size(); j++) {
                sumtime+=packageInfos.get(j).getmUsedTime();
            }
            double hour = ((sumtime*1.0) / (1000.0 * 60.0*60.0));
            hour = (double) Math.round(hour * 10) / 10;
            //hour=(double)(Math.round(hour*10)/10);
            weekUsage[i]=hour;
            weekAverage+=weekUsage[i];
            Log.d("sumtime", ""+sumtime);
            Log.d("hour", ""+weekUsage[i]);
            Log.d("statsk", ""+milliseconds2hms(sumtime));
        }
        Log.d("weekAverage", "weekaverage"+weekAverage);
        weekAverage=weekAverage/7.0;
        weekAverage=(double) Math.round(weekAverage * 10) / 10;
    }



    public void paintgraph1()
    {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        weekLineChart= findViewById(R.id.week_graph);
        AAChartModel aaChartModel = new AAChartModel()
                .chartType(AAChartType.Area)
                .title("七日使用统计")
                .subtitle("屏幕使用")
                .backgroundColor("#ffffff")
                .categories(new String[]{""+getPastDate(6),""+getPastDate(5),""+getPastDate(4),""+getPastDate(3),""+getPastDate(2),""+getPastDate(1),""+getPastDate(0)})
                .dataLabelsEnabled(false)
                .yAxisGridLineWidth(0f)
                .series(new AASeriesElement[]{
                        new AASeriesElement()
                                .name("屏幕使用时长")
                                .data(new Object[]{weekUsage[0],weekUsage[1],weekUsage[2],weekUsage[3],weekUsage[4],weekUsage[5],weekUsage[6]}),
                });
        weekLineChart.aa_drawChartWithChartModel(aaChartModel);

        weeekAveragestats=findViewById(R.id.averagetime);
        weeekAveragestats.setText("七日平均:"+weekAverage+"小时");
//        mWeekLineChart = (LineChart) findViewById(R.id.graph_image);
//        for (int i = 6; i >=0; i--) {
//            Calendar calendar = Calendar.getInstance();
//            int day = calendar.get(Calendar.DAY_OF_MONTH);
//            LineChartData data = new LineChartData();
//            data.setItem(day-i+"");
//            data.setPoint(weekUsage[i]);
//            dataList.add(data);
//        }
//        mWeekLineChart.setData(dataList);

    }

    public void initial_graph1()
    {
        //getStats1();
        //再mainactivity处计算数据，降低页面打开延迟
        paintgraph1();
    }

    public void initial_graph2()
    {
//        mUseTimeDataManager = UseTimeDataManager.getInstance(WeekStats.this);
//        mUseTimeDataManager.refreshData(7,1);
//        final List<PackageInfo> packageInfos = mUseTimeDataManager.getmPackageInfoListOrderByTime();
        piegraph = new AAChartModel().chartType(AAChartType.Pie)
                .backgroundColor("#ffffff")
                .title("屏幕时间占比")
                .subtitle("top 5")
                .dataLabelsEnabled(true)//是否直接显示扇形图数据
                .yAxisTitle("℃")
                .series(new AAPie[] {
                                new AAPie()
                                        .name("Language market shares")
                                        .innerSize("20%")
                                        .data(new Object[][] {
                                        {WeekPackageInfos.get(0).getmAppName()  ,(int)WeekPackageInfos.get(0).getmUsedTime()},
                                        {WeekPackageInfos.get(1).getmAppName()  ,(int)WeekPackageInfos.get(1).getmUsedTime()},
                                        {WeekPackageInfos.get(2).getmAppName()  ,(int)WeekPackageInfos.get(2).getmUsedTime()},
                                        {WeekPackageInfos.get(3).getmAppName()  ,(int)WeekPackageInfos.get(3).getmUsedTime()},
                                        {WeekPackageInfos.get(4).getmAppName()  ,(int)WeekPackageInfos.get(4).getmUsedTime()},
                                })
                                ,
                        }
                );

        aaChartView.aa_drawChartWithChartModel(piegraph);
    }

    //顶部状态栏----------
    public void setStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//5.0及以上
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            //根据上面设置是否对状态栏单独设置颜色
            if (useThemestatusBarColor) {
                getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));//设置状态栏背景色
            } else {
                getWindow().setStatusBarColor(Color.TRANSPARENT);//透明
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {//4.4到5.0
            WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
            localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
        } else {
            Toast.makeText(this, "低于4.4的android系统版本不存在沉浸式状态栏", Toast.LENGTH_SHORT).show();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && useStatusBarColor) {//android6.0以后可以对状态栏文字颜色和图标进行修改
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }


}


