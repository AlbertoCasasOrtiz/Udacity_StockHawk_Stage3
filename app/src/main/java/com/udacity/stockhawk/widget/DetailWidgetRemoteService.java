package com.udacity.stockhawk.widget;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;


public class DetailWidgetRemoteService extends RemoteViewsService {

    private final DecimalFormat dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);

    private static final String[] FORECAST_COLUMNS = {
            Contract.Quote.COLUMN_SYMBOL,
            Contract.Quote.COLUMN_PRICE,
            Contract.Quote.COLUMN_PERCENTAGE_CHANGE,
            Contract.Quote.COLUMN_ABSOLUTE_CHANGE,
            Contract.Quote.COLUMN_STOCK_NAME,
            Contract.Quote.COLUMN_HISTORY,
            Contract.Quote.TABLE_NAME + "." + Contract.Quote._ID
    };

    // these indices must match the projection
    private static final int INDEX_SYMBOL = 0;
    private static final int INDEX_PRICE = 1;
    private static final int INDEX_PERCENTAGE_CHANGE = 2;
    private static final int INDEX_ABSOLUTE_CHANGE = 3;
    private static final int INDEX_STOCK_NAME = 4;
    private static final int INDEX_HISTORY = 5;
    private static final int INDEX_ID = 6;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
                // Nothing to do
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

                Uri uri = Contract.Quote.URI;
                data = getContentResolver().query(uri,
                        FORECAST_COLUMNS,
                        null,
                        null,
                        null);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_detail_list_item);

                //Extract data
                String symbol = data.getString(INDEX_SYMBOL);
                float percentageChangeFloat = data.getFloat(INDEX_PERCENTAGE_CHANGE);
                String percentageChange = percentageChangeFloat+"%";
                String price = dollarFormat.format(data.getFloat(INDEX_PRICE));
                String name = data.getString(INDEX_STOCK_NAME);

                views.setTextViewText(R.id.symbol_widget, symbol);
                views.setTextViewText(R.id.price_widget, price);
                views.setTextViewText(R.id.change_widget, percentageChange);
                views.setTextViewText(R.id.name_widget, name);

                final Intent fillInIntent = new Intent();

                Uri uri = Contract.Quote.URI;

                final long identityToken = Binder.clearCallingIdentity();

                data = getContentResolver().query(uri,
                        FORECAST_COLUMNS,
                        null,
                        null,
                        null);
                Binder.restoreCallingIdentity(identityToken);

                if(data != null && data.getCount() > 0) {
                    data.moveToPosition(position);
                    fillInIntent.putExtra(getResources().getString(R.string.key_history), data.getString(INDEX_HISTORY));
                    fillInIntent.putExtra(getResources().getString(R.string.key_symbol), data.getString(INDEX_SYMBOL));
                    fillInIntent.putExtra(getResources().getString(R.string.key_price), data.getFloat(INDEX_PRICE)+"$");
                    fillInIntent.putExtra(getResources().getString(R.string.key_change_percentage), data.getFloat(INDEX_PERCENTAGE_CHANGE)+"%");
                    fillInIntent.putExtra(getResources().getString(R.string.key_change_absolute), data.getFloat(INDEX_ABSOLUTE_CHANGE)+"");
                    fillInIntent.putExtra(getResources().getString(R.string.key_stock_name), data.getString(INDEX_STOCK_NAME));

                }
                fillInIntent.setData(uri);
                views.setOnClickFillInIntent(R.id.widget_detail_list_item, fillInIntent);

                return views;
            }

            /*@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            private void setRemoteContentDescription(RemoteViews views, String description) {
                views.setContentDescription(R.id.widget_icon, description);
            }*/

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_detail_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(INDEX_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
