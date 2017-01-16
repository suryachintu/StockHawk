package com.udacity.stockhawk.widget;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Binder;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.ui.MainActivity;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by Surya on 16-01-2017.
 */

public class StockWidgetRemoteViewService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {

            private Cursor data = null;

            @Override
            public void onCreate() {
                //Nothing to do

                Log.e("xxx","oncreate");
            }

            @Override
            public void onDataSetChanged() {

                if (data != null) {
                    data.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();
                data = getContentResolver().query(Contract.Quote.URI,
                        Contract.Quote.QUOTE_COLUMNS,
                        null,
                        null,
                        Contract.Quote.COLUMN_SYMBOL);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data!=null){
                    data.close();
                    data=null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount() ;
            }

            @Override
            public RemoteViews getViewAt(int position) {

                if (position == AdapterView.INVALID_POSITION
                        || data == null || !data.moveToPosition(position)){
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_list_item);

                final DecimalFormat dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
                final DecimalFormat dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
                dollarFormatWithPlus.setPositivePrefix("+$");
                final DecimalFormat percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
                percentageFormat.setMaximumFractionDigits(2);
                percentageFormat.setMinimumFractionDigits(2);
                percentageFormat.setPositivePrefix("+");

                String company_name = data.getString(Contract.Quote.POSITION_COMPANY_NAME);
                String symbol = data.getString(Contract.Quote.POSITION_SYMBOL);
                String price = dollarFormat.format(data.getFloat(Contract.Quote.POSITION_PRICE));
                float rawAbsoluteChange = data.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
                float percentageChange = data.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);

                int background = R.drawable.percent_change_pill_red;;
                if (rawAbsoluteChange > 0) {
                    background = R.drawable.percent_change_pill_green;
                } else {
                    background = R.drawable.percent_change_pill_red;
                }
                String change = dollarFormatWithPlus.format(rawAbsoluteChange);
                String percentage = percentageFormat.format(percentageChange / 100);

                String changeValue;
                if (PrefUtils.getDisplayMode(StockWidgetRemoteViewService.this)
                        .equals(StockWidgetRemoteViewService.this.getString(R.string.pref_display_mode_absolute_key))) {
                    changeValue = change;
                } else {
                    changeValue = percentage;
                }
                views.setTextViewText(R.id.widget_symbol, symbol);
                views.setTextViewText(R.id.widget_price, price);
                views.setTextViewText(R.id.widget_change, changeValue);
                final Intent fillInIntent = new Intent();
                Uri stockUri = Contract.Quote.makeUriForStock(symbol);
                fillInIntent.setData(stockUri);
                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);

                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(),R.layout.widget_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(Contract.Quote.POSITION_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
