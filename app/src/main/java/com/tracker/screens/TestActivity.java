package com.tracker.screens;

import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tracker.R;

public class TestActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener {

    private boolean isHideToolbarView = false;

    private TextView toolbartext;
    private LinearLayout header_text_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);

        toolbartext = (TextView) findViewById(R.id.toolbartext);
        header_text_layout = (LinearLayout) findViewById(R.id.header_text_layout);
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int offset) {

        int maxScroll = appBarLayout.getTotalScrollRange();
        float percentage = (float) Math.abs(offset) / (float) maxScroll;

        if (percentage == 1f && isHideToolbarView) {
            toolbartext.setVisibility(View.VISIBLE);
            isHideToolbarView = !isHideToolbarView;

        } else if (percentage < 1f && !isHideToolbarView) {
            toolbartext.setVisibility(View.GONE);
            isHideToolbarView = !isHideToolbarView;
        }
    }
}
