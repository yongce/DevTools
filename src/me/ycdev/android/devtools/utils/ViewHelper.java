package me.ycdev.android.devtools.utils;

import android.content.Context;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ViewHelper {
    public static void addTextView(ViewGroup holder, String itemValue) {
        TextView itemView = new TextView(holder.getContext());
        itemView.setLayoutParams(new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        itemView.setText(itemValue);
        holder.addView(itemView);
    }

    public static void addLineView(ViewGroup holder, int color) {
        Context context = holder.getContext();

        ImageView lineView = new ImageView(context);
        lineView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 1));
        lineView.setBackgroundColor(color);

        LinearLayout marginView = new LinearLayout(context);
        marginView.setPadding(0, 6, 0, 6);
        marginView.setLayoutParams(new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        marginView.addView(lineView);

        holder.addView(marginView);
    }

    public static void addTextView(ViewGroup holder, String itemName, String itemValue) {
        addTextView(holder, itemName + ": " + itemValue);
    }

    public static void addTextView(ViewGroup holder, String itemName, boolean itemValue) {
        addTextView(holder, itemName, String.valueOf(itemValue));
    }

    public static void addTextView(ViewGroup holder, String itemName, int itemValue) {
        addTextView(holder, itemName, String.valueOf(itemValue));
    }

    public static void addTextView(ViewGroup holder, String itemName, double itemValue) {
        addTextView(holder, itemName, String.valueOf(itemValue));
    }

    public static void addTimeTextView(ViewGroup holder, String itemName, long macroSeconds) {
        int seconds = (int) (macroSeconds / (1000 * 1000));
        String timeStr = FormatHelper.formatElapsedTime(holder.getContext(), seconds);
        addTextView(holder, itemName, timeStr);
    }

    public static void addBytesTextView(ViewGroup holder, String itemName, long bytes) {
        String bytesStr = FormatHelper.formatBytes(holder.getContext(), bytes);
        addTextView(holder, itemName, bytesStr);
    }

}
