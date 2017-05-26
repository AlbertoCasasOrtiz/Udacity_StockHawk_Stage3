package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.androidplot.xy.CatmullRomInterpolator;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.udacity.stockhawk.R;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;

public class GraphActivity extends AppCompatActivity {

    private XYPlot plot;
    private String history;
    private String symbol;
    private String price;
    private String changePercentage;
    private String changeAbsolute;
    private String stockName;


    private TextView tvStockName;
    private TextView tvStockSymbol;
    private TextView tvStockPrice;
    private TextView tvStockChangePercentage;
    private TextView tvStockChangeAbsolute;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        ActionBar actionBar = getSupportActionBar();

        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent intentThatStartedThisActivity = getIntent();

        this.tvStockName = (TextView) findViewById(R.id.tv_stock_name);
        this.tvStockSymbol = (TextView) findViewById(R.id.tv_stock_symbol);
        this.tvStockPrice = (TextView) findViewById(R.id.tv_price);
        this.tvStockChangePercentage = (TextView) findViewById(R.id.tv_change_percentage);
        this.tvStockChangeAbsolute = (TextView) findViewById(R.id.tv_change_absolute);

        if(intentThatStartedThisActivity.hasExtra("extra_history")){
            this.history = intentThatStartedThisActivity.getStringExtra("extra_history");
        }
        if(intentThatStartedThisActivity.hasExtra("extra_symbol")){
            this.symbol = intentThatStartedThisActivity.getStringExtra("extra_symbol");
            this.tvStockSymbol.setText(getResources().getString(R.string.stock_symbol) + " " + this.symbol);
        }
        if(intentThatStartedThisActivity.hasExtra("extra_price")){
            this.price = intentThatStartedThisActivity.getStringExtra("extra_price");
            this.tvStockPrice.setText(getResources().getString(R.string.price) + " " + this.price);
        }
        if(intentThatStartedThisActivity.hasExtra("extra_change_percentage")){
            this.changePercentage = intentThatStartedThisActivity.getStringExtra("extra_change_percentage");
            this.tvStockChangePercentage.setText(getResources().getString(R.string.change) + " " + this.changePercentage);
        }
        if(intentThatStartedThisActivity.hasExtra("extra_change_absolute")){
            this.changeAbsolute = intentThatStartedThisActivity.getStringExtra("extra_change_absolute");
            this.tvStockChangeAbsolute.setText(getResources().getString(R.string.change_absolute) + " " + this.changeAbsolute + "$");
        }
        if(intentThatStartedThisActivity.hasExtra("extra_stock_name")){
            this.stockName = intentThatStartedThisActivity.getStringExtra("extra_stock_name");
            this.tvStockName.setText(getResources().getString(R.string.change) + " " + this.stockName);
            setTitle(this.stockName);
        }

        //GRAPH
        plot = (XYPlot) findViewById(R.id.plot);

        final String[] domainLabels = extractMonthsFromHistory(history);
        Number[] series1Numbers = extractValuesFromHistory(history);

        XYSeries series1 = new SimpleXYSeries(
                Arrays.asList(series1Numbers), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, this.symbol);

        LineAndPointFormatter series1Format = new LineAndPointFormatter(ResourcesCompat.getColor(getResources(), R.color.colorAccent, null), null, null, null);

        series1Format.setInterpolationParams(new CatmullRomInterpolator.Params(10, CatmullRomInterpolator.Type.Centripetal));

        plot.addSeries(series1, series1Format);

        plot.setDomainStepValue(12);
        plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).setFormat(new Format() {
            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                int i = Math.round(((Number) obj).floatValue());
                return toAppendTo.append(domainLabels[i]);
            }
            @Override
            public Object parseObject(String source, ParsePosition pos) {
                return null;
            }
        });

        plot.setTitle(this.symbol);
        plot.setDomainLabel("");
        plot.setRangeLabel("");
        plot.setRangeLabel("USD");
        plot.getLegend().setVisible(false);

        plot.getGraph().getBackgroundPaint().setColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimaryLight, null));
        plot.getGraph().getGridBackgroundPaint().setColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimaryLight, null));
    }

    public Number[] extractValuesFromHistory(String history){
        String[] splitted= history.split("\n");
        Number[] numbers = new Number[splitted.length];

        for(int i = 0; i < splitted.length; i++){
            numbers[i] = Double.parseDouble(splitted[i].split(", ")[1]);
        }

        Collections.reverse(Arrays.asList(numbers));

        return numbers;
    }

    public String[] extractMonthsFromHistory(String history){
        String[] splitted= history.split("\n");
        String[] months = new String[splitted.length];

        long milis = Long.parseLong(splitted[0].split(", ")[0]);

        GregorianCalendar c = new GregorianCalendar();
        c.setTimeInMillis(milis);
        int month = c.get(Calendar.MONTH);
        int year = c.get(Calendar.YEAR);

        for(int i = 0; i < splitted.length; i++){
            months[i] = (mod(month, 12) + 1) + "-" + year;
            month--;
            if(mod(month, 12) == 11)
                year--;
        }

        Collections.reverse(Arrays.asList(months));


        return months;
    }

    private int mod(int a, int b) {
        int c = a % b;
        return (c < 0) ? c + b : c;
    }
}
