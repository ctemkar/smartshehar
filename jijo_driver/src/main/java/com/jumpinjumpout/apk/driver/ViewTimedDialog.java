package com.jumpinjumpout.apk.driver;


import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by ctemkar on 21/06/2015.
 */

public class ViewTimedDialog extends timed_dialog

{
    private Context context;
    private View rootView;
    private DialogListener dialogListener;// listener to simulate click events
    private Dialog dialog;

    private TextView messageTextView;
    private Button positiveBttn;
    private Button negativeBttn;

    private Handler handler;// handler will be use as timer
    private Runnable runnable;

    private int defaultTime;

    /**
     * @param context
     * @param title
     * @param message
     * @param positiveBttnText
     * @param negativeBttnText
     * @param defaultTime
     */
    public ViewTimedDialog(Context context, String title, String message, String positiveBttnText, String negativeBttnText, int defaultTime) {
        this.context = context;
        this.defaultTime = defaultTime;
        setTitle(title);
        setMessage(message);
        setPositiveBttnText(positiveBttnText);
        setNegativeBttnText(negativeBttnText);
        setTime(defaultTime);
    }

    private void buildUi() {
        if (rootView != null) {
            messageTextView = (TextView) rootView.findViewById(R.id.my_dialog_layout_message_text_view);
            positiveBttn = (Button) rootView.findViewById(R.id.my_dialog_layout_positive_bttn);
            negativeBttn = (Button) rootView.findViewById(R.id.my_dialog_layout_negative_bttn);

            messageTextView.setText(getMessage());
            positiveBttn.setText(getPositiveBttnText());
            negativeBttn.setText(getNegativeBttnText());

            positiveBttn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (dialogListener != null) {
                        if (handler != null)
                            handler.removeCallbacks(runnable);//remove any queued post
                        setTime(defaultTime);//reset the default time
                        dialogListener.onPositiveClick();
                    }
                }
            });

            negativeBttn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (dialogListener != null) {
                        if (handler != null)
                            handler.removeCallbacks(runnable);//remove the previous post
                        setTime(defaultTime);//reset the default time
                        dialogListener.onNegativeClick();
                    }
                }
            });

            startHandler();
        }
    }

    public void setOnDialogListener(DialogListener listener) {
        dialogListener = listener;
    }

    public interface DialogListener {
        public abstract void onPositiveClick();

        public abstract void onNegativeClick();
    }

    /**
     * build and show dialog
     */
    public void show() {
        rootView = View.inflate(context, R.layout.timed_dialog, null);
        dialog = new Dialog(context);
        handler = new Handler();
        buildUi();
        dialog.setTitle(getTitle());// set title for dialog
        dialog.setContentView(rootView);// set the content view of the dialog
        dialog.setCancelable(false);// set the dialog to be non-cancelable
        dialog.show();
    }

    public void dismissDialog() {
        if (dialog != null) dialog.dismiss();
    }

    private void startHandler() {
        runnable = new Runnable() {

            @Override
            public void run() {
                int time = getTime();
                setTime(time - 1);
                if (time <= 0)// if time is less than or equal to zero then stop the handler
                {
                    handler.removeCallbacks(this);//remove any queued post
                    setTime(defaultTime);// reset the default time
                    if (dialogListener != null)
                        dialogListener.onPositiveClick();// simulate positive button click when no action
                } else {
                    buildUi();// rebuild the ui of the dialog
                }

            }
        };
        if (handler != null) {
            handler.postDelayed(runnable, 1000);// send post after 1 second = 1000 ms
        }
    }
}