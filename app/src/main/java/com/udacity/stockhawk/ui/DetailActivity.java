package com.udacity.stockhawk.ui;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DetailActivity extends AppCompatActivity{

    @BindView(R.id.chart)
    LineChart chart;

    @BindView(R.id.company_name)
    TextView symbol;

    @BindView(R.id.price)
    TextView price;

    @BindView(R.id.spinner)
    Spinner spinner;

    String[] history;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);

        history = getHistory();

        price.setContentDescription(getResources().getString(R.string.price,price.getText()));
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sort_type, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                //change the chart according to the sort type
                setGraph(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                price.setText(getResources().getString(R.string.format_price,String.valueOf(e.getY())));
            }

            @Override
            public void onNothingSelected() {
            }
        });

        if (getSupportActionBar()!=null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setGraph(int position){

        final ArrayList<String> quarters= new ArrayList<>();

        if (history!=null) {

            String[] date_value;
            List<Entry> entries = new ArrayList<>();
            for (int i = 0; i < history.length; i++) {

                date_value = history[i].split(", ");

                Double h = Double.parseDouble(date_value[1]);
                entries.add(new Entry(i, h.intValue()));
                String dateString = new SimpleDateFormat("MMM d, ''yy", Locale.ENGLISH).format(new Date(Long.parseLong(date_value[0])));

                quarters.add(dateString);

//                Log.e("history",dateString + "vvv");

            }

            IAxisValueFormatter formatter = new IAxisValueFormatter() {

                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    return quarters.get((int) value);
                }
            };


            XAxis xAxis = chart.getXAxis();
            xAxis.setTextColor(Color.WHITE);
            xAxis.setDrawGridLines(false);
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setAvoidFirstLastClipping(false);

            YAxis yAxis = chart.getAxisLeft();
            yAxis.setTextSize(8f);
            yAxis.setValueFormatter(new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    return "$"+value;
                }
            });
            String label;
            int duration = 1500;
            switch (position){

                case 0:
                    position=2;
                    duration = 0;
                    label = getResources().getString(R.string.one_week_label);
                    break;
                case 1:
                    position=5;
                    duration = 800;
                    label = getResources().getString(R.string.one_month_label);
                    break;
                case 2:
                    position=12;
                    label = getResources().getString(R.string.three_month_label);
                    break;
                case 3:
                    position=24;
                    label = getResources().getString(R.string.six_month_label);
                    break;
                case 4:
                    position=48;
                    label = getResources().getString(R.string.one_year_label);
                    break;
                default:
                    label = getResources().getString(R.string.one_week_label);
                    position=2;

            }

            LineDataSet dataSet = new LineDataSet(entries, label); // add entries to dataset
            dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            dataSet.setDrawFilled(true);

            dataSet.setFillColor(Color.WHITE);
            LineData lineData = new LineData(dataSet);
            lineData.setValueTextColor(Color.WHITE);

            //format axis
            yAxis.setTextColor(Color.WHITE);
            yAxis.setDrawGridLines(false);

            YAxis yAxis2 = chart.getAxisRight();
            yAxis2.setEnabled(false);


            xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
            xAxis.setValueFormatter(formatter);

            xAxis.setAxisMaximum(position-1);
            xAxis.setLabelCount(4);
            xAxis.setTextSize(8f);

            //modify the chart
            chart.setDrawGridBackground(false);
            chart.setPinchZoom(true);
            //alternate background color
            chart.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            chart.setData(lineData);
            chart.animateX(duration, Easing.EasingOption.EaseInSine);
            //customize legend
            Legend legend = chart.getLegend();
            legend.setForm(Legend.LegendForm.LINE);
            legend.setTextColor(Color.WHITE);

            chart.invalidate();
        }

    }

    public String[] getHistory(){

        Cursor cursor = getContentResolver().query(getIntent().getData(),null,null,null,null);

        if (cursor!=null){
            cursor.moveToFirst();


            symbol.setText(cursor.getString(Contract.Quote.POSITION_SYMBOL));
            //set the content description
            symbol.setContentDescription(getResources().getString(R.string.symbol_format,symbol.getText()));

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
