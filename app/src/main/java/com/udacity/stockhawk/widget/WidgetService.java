package com.udacity.stockhawk.widget;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.RemoteViews;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.ui.MainActivity;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by alber on 21/05/2017.
 */

public class WidgetService extends IntentService{

    private final DecimalFormat dollarFormat;

    private static final String[] FORECAST_COLUMNS = {
            Contract.Quote.COLUMN_SYMBOL,
            Contract.Quote.COLUMN_PRICE,
            Contract.Quote.COLUMN_PERCENTAGE_CHANGE,
            Contract.Quote.COLUMN_STOCK_NAME
    };

    // these indices must match the projection
    private static final int INDEX_SYMBOL = 0;
    private static final int INDEX_PRICE = 1;
    private static final int INDEX_PERCENTAGE_CHANGE = 2;
    private static final int INDEX_STOCK_NAME = 3;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public WidgetService(String name) {
        super(name);
        dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
    }

    public WidgetService() {
        super("WidgetService");
        dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                Widget.class));

        Uri uri = Contract.Quote.URI;
        Cursor data = getContentResolver().query(uri, FORECAST_COLUMNS, null, null, FORECAST_COLUMNS[INDEX_PERCENTAGE_CHANGE] + " DESC");
        if (data == null) {
            return;
        }
        if (!data.moveToFirst()) {
            data.close();
            return;
        }

        //Extract data.
        String symbol = data.getString(INDEX_SYMBOL);
        String percentageChange = data.getFloat(INDEX_PERCENTAGE_CHANGE)+"%";
        String price = dollarFormat.format(data.getFloat(INDEX_PRICE));
        String name = data.getString(INDEX_STOCK_NAME);

        // Perform this loop procedure for each Today widget
        for (int appWidgetId : appWidgetIds) {
            int layoutId;

            int widgetWidth = getWidgetWidth(appWidgetManager, appWidgetId);
            int defaultWidth = getResources().getDimensionPixelSize(R.dimen.widget_today_default_width);
            int largeWidth = getResources().getDimensionPixelSize(R.dimen.widget_today_large_width);

            if (widgetWidth >= largeWidth) {
                layoutId = R.layout.widget_large;
            } else if (widgetWidth >= defaultWidth) {
                layoutId = R.layout.widget;
            } else {
                layoutId = R.layout.widget_small;
            }



            RemoteViews views = new RemoteViews(getPackageName(), layoutId);

            // Add the data to the RemoteViews
            views.setTextViewText(R.id.symbol_widget, symbol);
            views.setTextViewText(R.id.price_widget, price);
            views.setTextViewText(R.id.change_widget, percentageChange);
            views.setTextViewText(R.id.name_widget, name);


            // Create an Intent to launch MainActivity
            Intent launchIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    private int getWidgetWidth(AppWidgetManager appWidgetManager, int appWidgetId) {
        // Prior to Jelly Bean, widgets were always their default size
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return getResources().getDimensionPixelSize(R.dimen.widget_today_default_width);
        }
        // For Jelly Bean and higher devices, widgets can be resized - the current size can be
        // retrieved from the newly added App Widget Options
        return getWidgetWidthFromOptions(appWidgetManager, appWidgetId);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private int getWidgetWidthFromOptions(AppWidgetManager appWidgetManager, int appWidgetId) {
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
        if (options.containsKey(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)) {
            int minWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
            // The width returned is in dp, but we'll convert it to pixels to match the other widths
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, minWidthDp,
                    displayMetrics);
        }
        return  getResources().getDimensionPixelSize(R.dimen.widget_today_default_width);
    }
}
