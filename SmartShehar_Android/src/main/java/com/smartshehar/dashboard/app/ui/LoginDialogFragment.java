package com.smartshehar.dashboard.app.ui;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import com.smartshehar.dashboard.app.Constants_dp;
import com.smartshehar.dashboard.app.FragmentTaskCompleteListener;
import com.smartshehar.dashboard.app.R;

public class LoginDialogFragment extends DialogFragment {

    private ImageView imageViewClose;
    private WebView webViewLogin;
    private FragmentTaskCompleteListener taskCompleteListener;
    private String url;

    public LoginDialogFragment() {
        // Required empty public constructor
    }

    public static LoginDialogFragment getInstance(String url) {
        LoginDialogFragment dialogFragment = new LoginDialogFragment();
        dialogFragment.setUrl(url);
        return dialogFragment;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try{
            this.taskCompleteListener = (FragmentTaskCompleteListener) activity;
        }catch (final ClassCastException e) {
            Log.e("error",e+"");
            throw new ClassCastException(activity.toString() + " must implement OnCompleteListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login_dialog, container, false);
        imageViewClose = (ImageView) view.findViewById(R.id.btnClose);
        imageViewClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selfClose();
            }
        });
        webViewLogin = (WebView) view.findViewById(R.id.webViewTwitter);
        webViewLogin.setWebViewClient(new LoginWebViewClient());
        webViewLogin.loadUrl(url);
        return view;
    }

    public void selfClose(){
        Fragment prev = getActivity().getFragmentManager().findFragmentByTag("appified_tag_priyabrat");
        if (prev != null) {
            DialogFragment df = (DialogFragment) prev;
            df.dismiss();
        }
    }

    class LoginWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if(url.contains(Constants_dp.KEY_CALLBACK_URL)){
                Uri uri = Uri.parse(url);
                String verifier = uri.getQueryParameter("oauth_verifier");
                taskCompleteListener.onTaskComplete(String.valueOf(verifier));
                selfClose();
            }
            return super.shouldOverrideUrlLoading(view, url);
        }
    }
}
