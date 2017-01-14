package com.udacity.stockhawk.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        LineChart chart = (LineChart)findViewById(R.id.chart);
        TextView symbol = (TextView)findViewById(R.id.company_name);
        symbol.setText(getIntent().getStringExtra(MainActivity.SYMBOL_EXTRA));
        final ArrayList<String> quarters= new ArrayList<>();

        String[] history = getData(getIntent().getStringExtra(MainActivity.SYMBOL_EXTRA));
        if (history!=null){
            String[] date_value;
            List<Entry> entries = new ArrayList<Entry>();
            for (int i = 0; i < history.length; i++) {

                date_value = history[i].split(", ");

//                Log.e("history",dateString + "vvv");

                Double h = Double.parseDouble(date_value[1]);
                entries.add(new Entry(i, h.intValue()));
                String dateString = new SimpleDateFormat("dd.MM.yy", Locale.ENGLISH).format(new Date(Long.parseLong(date_value[0])));

                quarters.add(dateString);

            }

            IAxisValueFormatter formatter = new IAxisValueFormatter() {

                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    return quarters.get((int)value);
                }
                // we don't draw numbers, so no decimal digits needed
//                @Override
//                public int getDecimalDigits() {  return 0; }
            };


            XAxis xAxis = chart.getXAxis();
            xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
            xAxis.setValueFormatter(formatter);

            LineDataSet dataSet = new LineDataSet(entries, "Label"); // add entries to dataset
            dataSet.setColor(R.color.colorPrimary);
            dataSet.setValueTextColor(R.color.colorAccent);


            LineData lineData = new LineData(dataSet);
            chart.setData(lineData);
            chart.invalidate();

        }


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    public String[] getData(String symbol){

        Cursor cursor = getContentResolver().query(Contract.Quote.makeUriForStock(symbol),null,null,null,null);

        TextView price = (TextView)findViewById(R.id.price);

        if (cursor!=null){
            cursor.moveToFirst();

            setTitle(cursor.getString(Contract.Quote.POSITION_COMPANY_NAME));
            DecimalFormat dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);

            String[] history = cursor.getString(Contract.Quote.POSITION_HISTORY).split("\n");
            price.setText(dollarFormat.format(cursor.getFloat(Contract.Quote.POSITION_PRICE)));
            cursor.close();
            return history;
        }


        return null;
    }

}
